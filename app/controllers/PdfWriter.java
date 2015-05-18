package controllers;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class PdfWriter {

    public static void convert (){
        // 1) Load DOCX into XWPFDocument
        InputStream in;
        try {
            /*
            in = new FileInputStream(new File("resources//260.docx"));
            XWPFDocument document = new XWPFDocument(in);
            XWPFDocument document = new XWPFDocument(is);
            // 2) Prepare Pdf options
            PdfOptions options = PdfOptions.create();
            // 3) Convert XWPFDocument to Pdf
            OutputStream out = new FileOutputStream(new File("resources//260.pdf"));
            PdfConverter.getInstance().convert(document, out, options);
            */
            long start = System.currentTimeMillis();

            // 1) Load DOCX into XWPFDocument
            InputStream is = new FileInputStream(new File(
                    "resources/976.docx"));
            XWPFDocument document = new XWPFDocument(is);

            // 2) Prepare Pdf options
            PdfOptions options = PdfOptions.create();

            // 3) Convert XWPFDocument to Pdf
            OutputStream out = new FileOutputStream(new File(
                    "resources/export.pdf"));
            PdfConverter.getInstance().convert(document, out, options);

            System.err.println("Generate resources/260.pdf with "
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
        PdfWriter.convert();
    }
}