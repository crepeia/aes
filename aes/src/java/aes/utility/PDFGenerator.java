package aes.utility;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PDFGenerator {

    public ByteArrayOutputStream generatePDF(String html) {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        try {
            Document document = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, pdf);
            document.open();
            XMLWorkerHelper.getInstance().parseXHtml(pdfWriter, document, new ByteArrayInputStream(html.getBytes("UTF-8")),StandardCharsets.UTF_8);
            document.close();
            pdf.close();
        } catch (DocumentException ex) {
            Logger.getLogger(PDFGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PDFGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pdf;
    }
}
