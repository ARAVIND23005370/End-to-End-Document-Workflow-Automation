package com.e2edocs.service.extraction;

import com.e2edocs.service.ocr.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@Component
public class PdfTextExtractor implements DocumentTextExtractor {

    private static final Logger logger = LoggerFactory.getLogger(PdfTextExtractor.class);

    private final OcrService ocrService;

    // Regex patterns for PDF structure and text operators
    private static final Pattern STREAM_PATTERN = Pattern.compile("stream[\\r\\n]+(.*?)[\\r\\n]+endstream", Pattern.DOTALL);
    private static final Pattern BT_ET_PATTERN = Pattern.compile("BT(.*?)ET", Pattern.DOTALL);
    private static final Pattern TJ_LITERAL_PATTERN = Pattern.compile("\\(([^\\)]*)\\)\\s*(?:Tj|\')");
    private static final Pattern TJ_ARRAY_PATTERN = Pattern.compile("\\[([^\\]]*)\\]\\s*TJ");
    private static final Pattern PAREN_STRING_PATTERN = Pattern.compile("\\(([^\\)]*)\\)");
    private static final Pattern PAGE_COUNT_PATTERN = Pattern.compile("/Type\\s*/Page(?:[^s]|$)");

    public PdfTextExtractor(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @Override
    public boolean supports(String mimeType, String fileExtension) {
        if ("application/pdf".equalsIgnoreCase(mimeType)) return true;
        if (fileExtension != null && fileExtension.equalsIgnoreCase(".pdf")) return true;
        return false;
    }

    @Override
    public DocumentExtractionResult extract(InputStream stream, String filename, String mimeType) {
        try {
            byte[] pdfBytes = stream.readAllBytes();
            String rawPdf = new String(pdfBytes, StandardCharsets.ISO_8859_1);

            int pageCount = countPages(rawPdf);
            StringBuilder extractedText = new StringBuilder();

            // Extract all uncompressed and decompress flate-compressed streams
            extractStreamsAndText(pdfBytes, extractedText);

            String text = extractedText.toString().trim();
            boolean ocrUsed = false;
            String extractorName = "PDF Native Stream Extractor";

            // If selectable text is negligible/scanned, attempt OCR fallback
            if (text.length() < 15) {
                if (ocrService.isAvailable()) {
                    logger.info("PDF selectable text is empty, attempting OCR for PDF: {}", filename);
                    String ocrResult = ocrService.extractText(new ByteArrayInputStream(pdfBytes), "pdf");
                    if (ocrResult != null && !ocrResult.isBlank()) {
                        text = ocrResult.trim();
                        ocrUsed = true;
                        extractorName = "PDF Native + Tesseract OCR";
                    }
                } else if (text.isBlank()) {
                    text = "[PDF Document: " + filename + " (" + (pageCount > 0 ? pageCount : 1) + " pages). Selectable text or OCR ready]";
                }
            }

            DocumentExtractionResult result = new DocumentExtractionResult(text, extractorName, ocrUsed, Math.max(1, pageCount));
            result.getMetadata().put("pdf.pageCount", Math.max(1, pageCount));
            return result;
        } catch (Exception e) {
            logger.error("Error extracting text from PDF {}: {}", filename, e.getMessage());
            return new DocumentExtractionResult(
                    "[PDF Extraction Error: " + e.getMessage() + "]",
                    "PDF Extractor (Failed)",
                    false,
                    0
            );
        }
    }

    private int countPages(String rawPdf) {
        Matcher matcher = PAGE_COUNT_PATTERN.matcher(rawPdf);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private void extractStreamsAndText(byte[] pdfBytes, StringBuilder outText) {
        int index = 0;
        byte[] streamMarker = "stream".getBytes(StandardCharsets.US_ASCII);
        byte[] endstreamMarker = "endstream".getBytes(StandardCharsets.US_ASCII);

        while (index < pdfBytes.length) {
            int streamStart = findSequence(pdfBytes, streamMarker, index);
            if (streamStart == -1) break;

            // skip CRLF after "stream"
            int contentStart = streamStart + streamMarker.length;
            if (contentStart < pdfBytes.length && pdfBytes[contentStart] == '\r') contentStart++;
            if (contentStart < pdfBytes.length && pdfBytes[contentStart] == '\n') contentStart++;

            int streamEnd = findSequence(pdfBytes, endstreamMarker, contentStart);
            if (streamEnd == -1) break;

            int length = streamEnd - contentStart;
            if (length > 0) {
                byte[] streamData = new byte[length];
                System.arraycopy(pdfBytes, contentStart, streamData, 0, length);

                // Attempt Inflater decompression (FlateDecode)
                byte[] decompressed = tryDecompress(streamData);
                String streamText = (decompressed != null)
                        ? new String(decompressed, StandardCharsets.ISO_8859_1)
                        : new String(streamData, StandardCharsets.ISO_8859_1);

                parsePdfTextOperators(streamText, outText);
            }

            index = streamEnd + endstreamMarker.length;
        }

        // Also check uncompressed text blocks in outer document
        if (outText.isEmpty()) {
            String raw = new String(pdfBytes, StandardCharsets.ISO_8859_1);
            parsePdfTextOperators(raw, outText);
        }
    }

    private void parsePdfTextOperators(String content, StringBuilder outText) {
        Matcher btMatcher = BT_ET_PATTERN.matcher(content);
        while (btMatcher.find()) {
            String btBlock = btMatcher.group(1);

            // 1. Literal Tj strings: (Hello World) Tj
            Matcher tjMatcher = TJ_LITERAL_PATTERN.matcher(btBlock);
            while (tjMatcher.find()) {
                String str = cleanPdfString(tjMatcher.group(1));
                if (!str.isBlank()) {
                    outText.append(str).append(" ");
                }
            }

            // 2. TJ array strings: [(Hello) -10 (World)] TJ
            Matcher arrayMatcher = TJ_ARRAY_PATTERN.matcher(btBlock);
            while (arrayMatcher.find()) {
                String arrayContent = arrayMatcher.group(1);
                Matcher parenMatcher = PAREN_STRING_PATTERN.matcher(arrayContent);
                while (parenMatcher.find()) {
                    String str = cleanPdfString(parenMatcher.group(1));
                    if (!str.isBlank()) {
                        outText.append(str).append(" ");
                    }
                }
            }
            outText.append("\n");
        }
    }

    private String cleanPdfString(String raw) {
        if (raw == null) return "";
        return raw.replace("\\(", "(")
                .replace("\\)", ")")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\")
                .trim();
    }

    private byte[] tryDecompress(byte[] data) {
        try {
            Inflater inflater = new Inflater(false);
            InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(data), inflater);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = iis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            return null; // Not zlib/flate compressed or corrupted
        }
    }

    private int findSequence(byte[] source, byte[] match, int start) {
        if (start >= source.length || match.length == 0) return -1;
        for (int i = start; i <= source.length - match.length; i++) {
            boolean found = true;
            for (int j = 0; j < match.length; j++) {
                if (source[i + j] != match[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    @Override
    public int getPriorityOrder() {
        return 10;
    }
}
