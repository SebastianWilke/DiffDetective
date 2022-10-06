package org.variantsync.diffdetective.internal;

import org.prop4j.NodeReader;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.Format;
import org.variantsync.diffdetective.diff.difftree.serialize.GraphvizExporter;
import org.variantsync.diffdetective.diff.difftree.serialize.TikzExporter;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.util.fide.FormulaUtils;
import org.variantsync.functjonal.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextDiffToTikz {
    public static String[] UNICODE_PROP_SYMBOLS = new String[]{"¬", "∧", "∨", "⇒", "⇔"};

    /**
     * Format used for the test export.
     */
    private final static Format format =
            new Format(
                    TextDiffToTikz::tikzNodeLabel,
                    // There is a bug in the exporter currently that accidentally switches direction so as a workaround we revert it here.
                    new DefaultEdgeLabelFormat(EdgeLabelFormat.Direction.ParentToChild)
            );

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Expected a path to a diff file or directory as first argument.");
            return;
        }

        final GraphvizExporter.LayoutAlgorithm layout;
        if (args.length < 2) {
            layout = GraphvizExporter.LayoutAlgorithm.DOT;
        } else {
            layout = GraphvizExporter.LayoutAlgorithm.valueOf(args[1].toUpperCase());
        }

        final Path fileToConvert = Path.of(args[0]);
        if (!Files.exists(fileToConvert)) {
            Logger.error("Path {} does not exist!", fileToConvert);
            return;
        }

        if (Files.isDirectory(fileToConvert)) {
            Logger.info("Processing directory " + fileToConvert);
            for (Path file : FileUtils.listAllFilesRecursively(fileToConvert)) {
                if (FileUtils.hasExtension(file, ".diff")) {
                    textDiff2Tikz(file, layout);
                }
            }
        } else {
            textDiff2Tikz(fileToConvert, layout);
        }
    }

    public static void textDiff2Tikz(Path fileToConvert, GraphvizExporter.LayoutAlgorithm layout) throws IOException {
        Logger.info("Converting file " + fileToConvert);
        Logger.info("Using layout " + layout.getExecutableName());
        final Path targetFile = fileToConvert.resolveSibling(fileToConvert.getFileName() + ".tikz");

        DiffTree.fromFile(fileToConvert, true, true).unwrap()
                .bind(diff -> exportAsTikz(diff, layout))
                .bind(tikz -> Result
                        .Try(() -> IO.write(targetFile, tikz))
                        .mapFail(e -> new DiffError(e.getMessage())))
                .peek(
                        u -> Logger.info("Wrote file " + targetFile),
                        Logger::error
                );
    }

    public static Result<String, DiffError> exportAsTikz(final DiffTree diffTree, GraphvizExporter.LayoutAlgorithm layout) {
        // Export the test case
        var tikzOutput = new ByteArrayOutputStream();

        try {
            new TikzExporter(format).exportDiffTree(diffTree, layout, tikzOutput);
        } catch (IOException e) {
            return Result.Failure(new DiffError(e.getMessage()));
        }

        return Result.Success(tikzOutput.toString());
    }

    public static String tikzNodeLabel(DiffNode node) {
        if (node.isRoot()) {
            return "r";
        } else {
            if (node.isIf() || node.isElif()) {
                return node.getDirectFeatureMapping().toString(UNICODE_PROP_SYMBOLS);
            } else {
                return node.getLabel().split(StringUtils.LINEBREAK)[0].trim();
            }
//                    .map(String::trim)
//                    .collect(Collectors.joining("<br>"));// substringBefore(node.getLabel(), StringUtils.LINEBREAK_REGEX);
        }
    }

    public static String substringBefore(String str, Pattern end) {
        Matcher m = end.matcher(str);
        if (m.find()) {
            return m.group(0);
        } else {
            return str;
        }
    }
}
