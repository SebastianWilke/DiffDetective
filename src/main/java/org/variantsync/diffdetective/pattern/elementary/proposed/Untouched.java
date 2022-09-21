package org.variantsync.diffdetective.pattern.elementary.proposed;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;

/**
 * Our Untouched pattern from the ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
public class Untouched extends ElementaryPattern {
    Untouched() {
        super("Untouched", DiffType.NON);
    }

    @Override
    protected boolean matchesArtifactNode(DiffNode artifactNode) {
        final Node pcb = artifactNode.getBeforeFeatureMapping();
        final Node pca = artifactNode.getAfterFeatureMapping();
        return SAT.equivalent(pcb, pca) && artifactNode.beforePathEqualsAfterPath();
    }
}
