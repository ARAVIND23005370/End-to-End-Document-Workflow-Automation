package com.e2edocs.service.extraction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class DocxTextExtractor implements DocumentTextExtractor {

    private static final Logger logger = LoggerFactory.getLogger(DocxTextExtractor.class);

    @Override
    public boolean supports(String mimeType, String fileExtension) {
        if (mimeType != null && (mimeType.contains("wordprocessingml") || mimeType.contains("msword"))) return true;
        if (fileExtension != null && (fileExtension.equalsIgnoreCase(".docx") || fileExtension.equalsIgnoreCase(".doc"))) return true;
        return false;
    }

    @Override
    public DocumentExtractionResult extract(InputStream stream, String filename, String mimeType) {
        StringBuilder fullText = new StringBuilder();
        int paragraphCount = 0;
        int tableCount = 0;

        try {
            byte[] docxBytes = stream.readAllBytes();
            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(docxBytes))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String name = entry.getName();
                    if ("word/document.xml".equalsIgnoreCase(name)
                            || name.startsWith("word/header")
                            || name.startsWith("word/footer")) {

                        byte[] entryBytes = zis.readAllBytes();
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                        dbf.setNamespaceAware(true);
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.parse(new ByteArrayInputStream(entryBytes));

                        NodeList paragraphs = doc.getElementsByTagNameNS("*", "p");
                        paragraphCount += paragraphs.getLength();

                        NodeList tables = doc.getElementsByTagNameNS("*", "tbl");
                        tableCount += tables.getLength();

                        for (int i = 0; i < paragraphs.getLength(); i++) {
                            Element p = (Element) paragraphs.item(i);
                            NodeList texts = p.getElementsByTagNameNS("*", "t");
                            StringBuilder pText = new StringBuilder();
                            for (int j = 0; j < texts.getLength(); j++) {
                                pText.append(texts.item(j).getTextContent());
                            }
                            if (!pText.isEmpty()) {
                                fullText.append(pText).append("\n");
                            }
                        }
                    }
                    zis.closeEntry();
                }
            }

            String text = fullText.toString().trim();
            DocumentExtractionResult result = new DocumentExtractionResult(text, "OpenXML DOCX Extractor", false, 1);
            result.getMetadata().put("docx.paragraphCount", paragraphCount);
            result.getMetadata().put("docx.tableCount", tableCount);
            return result;
        } catch (Exception e) {
            logger.error("Error extracting text from DOCX {}: {}", filename, e.getMessage());
            return new DocumentExtractionResult(
                    "[DOCX Extraction Error: " + e.getMessage() + "]",
                    "OpenXML DOCX (Failed)",
                    false,
                    0
            );
        }
    }

    @Override
    public int getPriorityOrder() {
        return 20;
    }
}
