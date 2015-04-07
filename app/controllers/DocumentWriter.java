package controllers;

/**
 * Created by samuel on 4/6/15.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import models.SongLyrics;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import play.Logger;


public class DocumentWriter {

    private String songTitleFont = "Arial";
    private int songTitleSize = 18;
    private String songLyricsFont = "Courier New";
    private int songLyricsFontSize = 14;

    public void writeSong(XWPFDocument document, String songTitle, String songLyrics, int songTotalNumber) {
        Logger.trace("Exporting song: " + songTitle);
        XWPFParagraph tmpHeader = document.createParagraph();
        XWPFRun tmpRunHeader = tmpHeader.createRun();
        tmpRunHeader.setFontFamily(songTitleFont);
        tmpRunHeader.getCTR().getRPr().getRFonts().setHAnsi(songTitleFont);
        tmpRunHeader.setText(songTitle);
        tmpRunHeader.setFontSize(songTitleSize);
        tmpHeader.setStyle("Heading1");
        tmpHeader.setBorderBottom(Borders.SINGLE);

        // Create Song body
        XWPFParagraph tmpParagraph = document.createParagraph();

        XWPFRun tmpRun = null;
        // breaking song by new lines so we can write multiple runs in paragraph and add break after each reun
        String[] songLines = songLyrics.split("\\r?\\n");

        for (String songLine : songLines) {
            tmpRun = tmpParagraph.createRun();
            tmpRun.setFontFamily(songLyricsFont);
            tmpRun.getCTR().getRPr().getRFonts().setHAnsi(songLyricsFont);
            tmpRun.setText(songLine);
            tmpRun.setFontSize(songLyricsFontSize);
            tmpRun.addBreak(BreakType.TEXT_WRAPPING);
            tmpParagraph.setStyle("NoSpacing");
        }
        if (songTotalNumber != -1) {
            tmpRun.addBreak(BreakType.PAGE);
        }
    }

    public File newSongbookWordDoc(String filename, List<SongPrint> songPrintObjects) throws Exception {

        final String outputFile = "/tmp/" + "filename" + ".docx";
        File templateFile = new File( "/tmp/resources/template.dotx");
        XWPFDocument template = new XWPFDocument(new FileInputStream(templateFile));

        final XWPFDocument document = new XWPFDocument();
        // copy styles from template to new doc
        XWPFStyles newStyles = document.createStyles();
        newStyles.setStyles(template.getStyle());

        boolean singleSong = (songPrintObjects.size() == 1) ? true : false;
        int songTotalNumber = (singleSong) ? -1 : songPrintObjects.size();
        Logger.trace("Number of songs (-1 if single page): " + songTotalNumber);

        for (int i = 0; i < songPrintObjects.size(); i++) {
            SongPrint s = songPrintObjects.get(i);

            // if this is single page or last page then we don't need page break
            if (singleSong || (i == songPrintObjects.size()-1)) {
                songTotalNumber = -1;
            } else {
                songTotalNumber = i;
            }
            Logger.trace("Now exporting songId: " + songPrintObjects.get(i));
            writeSong(document, s.getSong().songName, SongLyrics.get(s.getLyricsID()).songLyrics, songTotalNumber);
        }

        FileOutputStream fos = null;
        try {
            Logger.trace("Now writing to exported file: " + outputFile);
            fos = new FileOutputStream(new File(outputFile));
            document.write(fos);
            fos.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return new File(outputFile);
    }

    public static void main(String[] args) throws Exception {
        // DocumentWriter doc = new DocumentWriter();
        // ArrayList<String> song = ParserHelpers.readFile("test_data\\" + "inputTestSong");
        // TODO: this should be unit testable somehow?!
        // doc.newSongbookWordDoc("testOutputSong", "Test song", song);
    }
}
