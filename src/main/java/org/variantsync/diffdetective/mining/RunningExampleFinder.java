package org.variantsync.diffdetective.mining;

import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.diff.GitPatch;
import org.variantsync.diffdetective.diff.TextBasedDiff;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;
import org.variantsync.diffdetective.diff.difftree.filter.DiffTreeFilter;
import org.variantsync.diffdetective.diff.difftree.filter.ExplainedFilter;
import org.variantsync.diffdetective.diff.difftree.filter.TaggedPredicate;
import org.variantsync.diffdetective.diff.difftree.parse.DiffNodeParser;
import org.variantsync.diffdetective.diff.difftree.render.DiffTreeRenderer;
import org.variantsync.diffdetective.diff.difftree.transform.ExampleFinder;
import org.variantsync.diffdetective.diff.result.DiffResult;
import org.variantsync.diffdetective.util.Assert;

import java.nio.file.Path;
import java.util.Optional;

public class RunningExampleFinder {
    public static final Path DefaultExamplesDirectory = Path.of("examples");
    public static final int DefaultMaxDiffLineCount = 20;
    public static final ExplainedFilter<DiffTree> DefaultExampleConditions = new ExplainedFilter<>(
            new TaggedPredicate<>("diff length <= " + DefaultMaxDiffLineCount, t -> diffIsNotLongerThan(t, DefaultMaxDiffLineCount)),
            new TaggedPredicate<>("has nesting before the edit", RunningExampleFinder::hasNestingBeforeEdit),
            new TaggedPredicate<>("has additions", t -> t.anyMatch(DiffNode::isAdd)),
            new TaggedPredicate<>("an artifact was edited", t -> t.anyMatch(n -> n.isArtifact() && !n.isNon())),
            DiffTreeFilter.hasAtLeastOneEditToVariability(),
            DiffTreeFilter.moreThanOneArtifactNode(),
            new TaggedPredicate<>("has no annotated macros", t -> !RunningExampleFinder.hasAnnotatedMacros(t)),
            new TaggedPredicate<>("has a complex formula", RunningExampleFinder::hasAtLeastOneComplexFormulaBeforeTheEdit)
    );

    private final DiffNodeParser nodeParser;

    public RunningExampleFinder(final DiffNodeParser nodeParser) {
        this.nodeParser = nodeParser;
    }

    public ExampleFinder The_Diff_Itself_Is_A_Valid_DiffTree_And(
            final ExplainedFilter<DiffTree> treeConditions,
            final Path exportDirectory)
    {
        return new ExampleFinder(
                diffTree -> {
                    final String localDiff = getDiff(diffTree);
                    final DiffResult<DiffTree> parseResult = DiffTree.fromDiff(localDiff, true, true, nodeParser);
                    // Not every local diff can be parsed to a difftree because diffs are unaware of the underlying language (i.e., CPP).
                    // We want only running examples whose diffs describe entire diff trees for easier understanding.
                    return parseResult.unwrap().match(
                            localTree -> {
                                if (treeConditions.test(localTree)) {
                                    Assert.assertTrue(diffTree.getSource() instanceof GitPatch);
                                    final GitPatch diffTreeSource = (GitPatch) diffTree.getSource();
                                    localTree.setSource(diffTreeSource.shallowClone());
                                    return Optional.of(localTree);
                                }
                                return Optional.empty();
                            },
                            error -> Optional.empty()
                    );
                },
                exportDirectory,
                DiffTreeRenderer.WithinDiffDetective()
        );
    }

    private static boolean diffIsNotLongerThan(final DiffTree t, int maxLines) {
        return getNumberOfLinesIn(getDiff(t)) <= maxLines;
    }

    private static boolean hasAnnotatedMacros(final DiffTree diffTree) {
        return diffTree.anyMatch(n -> n.isArtifact() && n.getLabel().trim().startsWith("#"));
    }

    private static boolean hasNestingBeforeEdit(final DiffTree diffTree) {
        return diffTree.anyMatch(n ->
                           !n.isAdd()
                        && n.getBeforeDepth() > 2
                        && !(n.getBeforeParent().isElse() || n.getBeforeParent().isElif())
        );
    }

    private static boolean hasAtLeastOneComplexFormulaBeforeTheEdit(final DiffTree diffTree) {
        // We would like to have a complex formula in the tree (complex := not just a positive literal).
        return diffTree.anyMatch(n -> {
            // and the formula should be visible before the edit
            if (n.isAnnotation() && !n.isAdd()) {
                return isComplexFormula(n.getDirectFeatureMapping());
            }

            return false;
        });
    }

    private static String getDiff(final DiffTree tree) {
        final DiffTreeSource source = tree.getSource();
        Assert.assertTrue(source instanceof TextBasedDiff);
        return ((TextBasedDiff) source).getDiff();
    }

    private static int getNumberOfLinesIn(final String text) {
        return (int)text.trim().lines().count();
    }

    private static boolean isComplexFormula(final Node formula) {
        if (formula instanceof Literal) {
            // if a mapping is a negative literal, we count it as complex
            return !((Literal) formula).positive;
        } else {
            return true;
        }
    }
}
