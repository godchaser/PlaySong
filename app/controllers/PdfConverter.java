package controllers;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import com.lowagie.text.FontFactory;

public class PdfConverter {

    public static void convert (String inputFile, String outputFile){
        try {
            
            long start = System.currentTimeMillis();
            System.out.println("Starting on: " + start);

            // 1) Load DOCX into XWPFDocument
            InputStream is = new FileInputStream(new File(inputFile));
            XWPFDocument document = new XWPFDocument(is);

            // 2) Prepare Pdf options
            PdfOptions options = PdfOptions.create();
            String fontPath = "resources/fonts/Courier_New.ttf";
            String fontAlias = "Courier New";
            FontFactory.register(fontPath, fontAlias);
            
            fontPath = "resources/fonts/LiberationMono-Regular.ttf";
            fontAlias = "Liberation Mono";
            FontFactory.register(fontPath, fontAlias);
            
            fontPath = "resources/fonts/DroidSansMono.ttf";
            fontAlias = "Droid Mono";
            FontFactory.register(fontPath, fontAlias);
            
            fontPath = "resources/fonts/Consolas.ttf";
            fontAlias = "Consolas";
            FontFactory.register(fontPath, fontAlias);
            
            fontPath = "resources/fonts/Times_New_Roman.ttf";
            fontAlias = "Times New Roman";
            FontFactory.register(fontPath, fontAlias);

            // 3) Convert XWPFDocument to Pdf
            OutputStream out = new FileOutputStream(new File(outputFile));
            org.apache.poi.xwpf.converter.pdf.PdfConverter.getInstance().convert(document, out, options);

            System.err.println("Generate with " + outputFile+
            		+ (System.currentTimeMillis() - start) + "ms");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        PdfConverter.convert("resources/fonts/test.docx", "resources/test.pdf");
        PdfConverter.convert("resources/fonts/test.docx", "resources/test2.pdf");
        PdfConverter.convert("resources/fonts/test.docx", "resources/test3.pdf");
    }
}