package org.variantsync.diffdetective.diff.difftree.serialize;

import java.io.OutputStream;
import java.io.PrintStream;

import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.LineGraphConstants;
import org.variantsync.functjonal.Functjonal;

/**
 * Exporter that converts a single DiffTree's nodes and edges to linegraph.
 */
public class DiffTreeLineGraphExporter implements Exporter {
    private final Format format;
    private final DiffTreeSerializeDebugData debugData;

    public DiffTreeLineGraphExporter(Format format) {
        this.format = format;
        this.debugData = new DiffTreeSerializeDebugData();
    }

    public DiffTreeLineGraphExporter(DiffTreeLineGraphExportOptions options) {
        this(new Format(options.nodeFormat(), options.edgeFormat()));
    }

    /**
     * Export a line graph of {@code diffTree} into {@code destination}.
     *
     * @param diffTree to be exported
     * @param destination where the result should be written
     */
    @Override
    public void exportDiffTree(DiffTree diffTree, OutputStream destination) {
        var output = new PrintStream(destination);
        format.forEachNode(diffTree, (node) -> {
            switch (node.diffType) {
                case ADD -> ++debugData.numExportedAddNodes;
                case REM -> ++debugData.numExportedRemNodes;
                case NON -> ++debugData.numExportedNonNodes;
            }

            output.println(LineGraphConstants.LG_NODE + " " + node.getID() + " " + format.getNodeFormat().toLabel(node));
        });

        format.forEachUniqueEdge(diffTree, (edges) -> {
            output.print(Functjonal.unwords(LineGraphConstants.LG_EDGE, edges.get(0).from().getID(), edges.get(0).to().getID(), ""));

            for (var edge : edges) {
                output.print(edge.style().lineGraphType());
            }

            for (var edge : edges) {
                output.print(format.getEdgeFormat().labelOf(edge));
            }

            output.println();
        });
    }

    /**
     * Returns debug metadata that was recorded during export.
     */
    public DiffTreeSerializeDebugData getDebugData() {
        return debugData;
    }
}
