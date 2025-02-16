package net.furizon.backend.infrastructure.documents.service;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Entities;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
public class DocumentsService {

    public String convertHtmlToXhtml(String html) {
        // Parse the HTML using Jsoup
        org.jsoup.nodes.Document doc = Jsoup.parse(html);

        // Set the output to use XHTML format
        doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

        // Return the XHTML string
        return doc.html();
    }

    public Byte[] convertHtmlToPdf(String html) throws IOException {
        try (
                Document document = new Document(PageSize.ID_1);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
        ) {
            String xhtml = convertHtmlToXhtml(html);
            document.open();
            ITextRenderer renderer = new ITextRenderer();
            SharedContext sharedContext = renderer.getSharedContext();
            sharedContext.setPrint(true);
            sharedContext.setInteractive(false);
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            renderer.createPDF(byteArrayOutputStream);
            return ArrayUtils.toObject(byteArrayOutputStream.toByteArray());
        }
    }
}
