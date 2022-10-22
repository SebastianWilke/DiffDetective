import org.junit.Assert;
import org.junit.Test;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;
import org.variantsync.diffdetective.diff.difftree.parse.DiffTreeParser;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeSerializeDebugData;
import org.variantsync.diffdetective.diff.difftree.serialize.GraphFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExport;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.DebugDiffNodeFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.functjonal.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestMultiLineMacros {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("multilinemacros");

    public void diffToDiffTree(DiffTreeLineGraphExportOptions exportOptions, Path p) throws IOException, DiffParseException {
        DiffTree tree;
        try (BufferedReader fullDiff = Files.newBufferedReader(p)) {
            tree = DiffTreeParser.createDiffTree(
                    fullDiff,
                    true,
                    false,
                    CPPAnnotationParser.Default);
        }

        final Pair<DiffTreeSerializeDebugData, String> result = LineGraphExport.toLineGraphFormat(tree, exportOptions);
        Assert.assertNotNull(result);
        final DiffTreeSerializeDebugData debugData = result.first();
        Logger.info("Parsed {} nodes of diff type NON.", debugData.numExportedNonNodes);
        Logger.info("Parsed {} nodes of diff type ADD.", debugData.numExportedAddNodes);
        Logger.info("Parsed {} nodes of diff type REM.", debugData.numExportedRemNodes);

        final String lg = "t # 1" +
                StringUtils.LINEBREAK +
                result.second();

        IO.write(resDir.resolve("gen").resolve(p.getFileName() + ".lg"), lg);
    }

    @Test
    public void test() throws IOException, DiffParseException {
        final DiffTreeLineGraphExportOptions exportOptions = new DiffTreeLineGraphExportOptions(
                GraphFormat.DIFFTREE,
                new CommitDiffDiffTreeLabelFormat(),
                new DebugDiffNodeFormat(),
                new DefaultEdgeLabelFormat()
        );

        diffToDiffTree(exportOptions, resDir.resolve("mldiff1.txt"));
        diffToDiffTree(exportOptions, resDir.resolve("diffWithComments.txt"));
    }
}
