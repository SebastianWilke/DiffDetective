package main;

import diff.difftree.DiffTree;
import diff.difftree.render.DiffTreeRenderer;
import diff.serialize.LineGraphExport;
import mining.Postprocessor;
import org.pmw.tinylog.Logger;
import util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Performs a postprocessing on mined frequent subgraphs in edits to find semantic edit patterns.
 */
public class MiningPostprocessing {
    private static final DiffTreeRenderer DefaultRenderer = DiffTreeRenderer.WithinDiffDetective();
    private static final DiffTreeRenderer.RenderOptions DefaultRenderOptions = new DiffTreeRenderer.RenderOptions(
            LineGraphExport.NodePrintStyle.LabelOnly,
            false,
            DiffTreeRenderer.RenderOptions.DEFAULT.dpi(),
            DiffTreeRenderer.RenderOptions.DEFAULT.nodesize(),
            DiffTreeRenderer.RenderOptions.DEFAULT.edgesize(),
            DiffTreeRenderer.RenderOptions.DEFAULT.arrowsize(),
            DiffTreeRenderer.RenderOptions.DEFAULT.fontsize(),
            true
    );

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected path to directory of mined patterns as first argument and path to output directory as second argument but got only " + args.length + " arguments!");
        }

        final Path inputPath = Path.of(args[0]);
        final Path outputPath = Path.of(args[1]);
        if (!Files.isDirectory(inputPath)) {
            throw new IllegalArgumentException("Expected path to directory of mined patterns as first argument but got a path that is not a directory, namely \"" + inputPath + "\"!");
        }
        if (!Files.isDirectory(outputPath)) {
            throw new IllegalArgumentException("Expected path to output directory as second argument but got a path that is not a directory, namely \"" + outputPath + "\"!");
        }

        postprocessAndInterpretResults(
                parseFrequentSubgraphsIn(inputPath),
                Postprocessor.Default(),
                Logger::info,
                DefaultRenderer,
                DefaultRenderOptions,
                outputPath
        );
    }

    /**
     * Parses all linegraph files in the given directory as patterns (i.e., as DiffGraphs).
     * @param directory A directory containing linegraph files.
     * @return The list of all diffgraphs parsed from linegraph files in the given directory.
     * @throws IOException If the directory could not be accessed ({@link Files.list}).
     */
    public static List<DiffTree> parseFrequentSubgraphsIn(final Path directory) throws IOException {
        return Files.list(directory)
                .filter(FileUtils::isLineGraph)
                .map(MiningPostprocessing::parseDiffTreeFromLineGraph)
                .collect(Collectors.toList());
    }

    private static DiffTree parseDiffTreeFromLineGraph(final Path p) {
        throw new UnsupportedOperationException("Not implemented! Waiting for issue#3!");
    }

    public static void postprocessAndInterpretResults(
            final List<DiffTree> frequentSubgraphs,
            final Postprocessor postprocessor,
            final Consumer<String> printer,
            final DiffTreeRenderer renderer,
            DiffTreeRenderer.RenderOptions renderOptions,
            final Path outputDir)
    {
        final Postprocessor.Result result = postprocessor.postprocess(frequentSubgraphs);
        final List<DiffTree> semanticPatterns = result.processedTrees();

        printer.accept("Of " + frequentSubgraphs.size() + " mined subgraphs "
                + semanticPatterns.size() + " are candidates for semantic patterns.");
        printer.accept("Subgraphs were discarded for the following reasons:");
        for (Map.Entry<String, Integer> nameAndCount : result.filterCounts().entrySet()) {
            printer.accept("    " + nameAndCount.getKey() + ": " + nameAndCount.getValue());
        }
        printer.accept("");

        if (renderer != null) {
            if (renderOptions == null) {
                renderOptions = DiffTreeRenderer.RenderOptions.DEFAULT;
            }

            printer.accept("Exporting and rendering semantic patterns to " + outputDir);
            int patternNo = 0;
            for (final DiffTree semanticPattern : semanticPatterns) {
                renderer.render(semanticPattern, "SemanticPattern " + patternNo, outputDir, renderOptions);
                ++patternNo;
            }
        }
    }
}
