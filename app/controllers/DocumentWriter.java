package controllers;

/**
 * Created by samuel on 4/6/15.
 */
import java.io.*;
import java.util.List;

import controllers.chords.ChordLineTransposer;
import controllers.chords.LineTypeChecker;
import models.SongLyrics;
import models.helpers.SongPrint;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import play.Logger;
import play.Play;


public class DocumentWriter {

    private String songTitleFont = "Arial";
    private int songTitleSize = 18;
    private String songLyricsFont = "Courier New";
    private int songLyricsFontSize = 14;
    private String title = "Songbook";
    private String subtitle = "2015";

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

        InputStream is = Play.application().resourceAsStream("template.dotx");
        File outputFile = new File("resources/" + filename + ".docx");
        XWPFDocument template = new XWPFDocument(is);

        final XWPFDocument document = new XWPFDocument();
        // copy styles from template to new doc
        XWPFStyles newStyles = document.createStyles();
        newStyles.setStyles(template.getStyle());

        boolean singleSong = (songPrintObjects.size() == 1) ? true : false;
        int songTotalNumber = (singleSong) ? -1 : songPrintObjects.size();
        Logger.trace("Fonts - title:" + songTitleFont + " lyrics: " + songLyricsFont);
        Logger.trace("Sizes - title:" + songTitleSize + " lyrics: " + songLyricsFontSize);
        Logger.trace("Number of songs (-1 if single page): " + songTotalNumber);

        for (int i = 0; i < songPrintObjects.size(); i++) {
            SongPrint s = songPrintObjects.get(i);

            // if this is single page or last page then we don't need page break
            if (singleSong || (i == songPrintObjects.size()-1)) {
                songTotalNumber = -1;
            } else {
                songTotalNumber = i;
            }
            Logger.trace("Now exporting songId: " + songPrintObjects.get(i).getSong().id);

            String origLyrics = SongLyrics.get(s.getLyricsID()).songLyrics;
            String origKey = SongLyrics.get(s.getLyricsID()).songKey;
            String newKey = songPrintObjects.get(i).getKey();
            Logger.trace("Orig key: " + origKey + " New key: " + newKey);
            String songLyrics = SongLyrics.get(s.getLyricsID()).songLyrics;
            if (!origKey.equals(newKey)){
                //TODO: this has to be implemented - calculate ammount needed for transposing
                Logger.trace("Transposing! ");
                songLyrics = chordTranspose(2,songLyrics);
            }
            writeSong(document, s.getSong().songName, songLyrics, songTotalNumber);
        }

        FileOutputStream fos = null;
        try {
            Logger.trace("Now writing to exported file: " + outputFile);
            //fos = new FileOutputStream(new File(outputFile));
            fos = new FileOutputStream(outputFile);
            document.write(fos);
            fos.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return outputFile;
    }

    private String chordTranspose(int transposeAmmount, String songText) {
        String[] songLines = songText.split("[\r\n]+");
        StringBuilder transposedSong = new StringBuilder();
        for (String songLine : songLines) {
            Logger.trace("Checking song lines: " + songLine);
            String updatedSongLine = songLine;
            if (LineTypeChecker.isChordLine(songLine)) {
                Logger.trace("Transposing by ammount: " + transposeAmmount);
                ChordLineTransposer clt = new ChordLineTransposer(songLine);
                updatedSongLine = clt.transpose(transposeAmmount, null);
                Logger.trace(updatedSongLine);
            }
            transposedSong.append(updatedSongLine + "\r\n");
        }
        return transposedSong.toString();
    }

    public String getSongTitleFont() {
        return songTitleFont;
    }

    public void setSongTitleFont(String songTitleFont) {
        this.songTitleFont = songTitleFont;
    }

    public int getSongTitleSize() {
        return songTitleSize;
    }

    public void setSongTitleSize(int songTitleSize) {
        this.songTitleSize = songTitleSize;
    }

    public String getSongLyricsFont() {
        return songLyricsFont;
    }

    public void setSongLyricsFont(String songLyricsFont) {
        this.songLyricsFont = songLyricsFont;
    }

    public int getSongLyricsFontSize() {
        return songLyricsFontSize;
    }

    public void setSongLyricsFontSize(int songLyricsFontSize) {
        this.songLyricsFontSize = songLyricsFontSize;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public static void main(String[] args) throws Exception {
        // DocumentWriter doc = new DocumentWriter();
        // ArrayList<String> song = ParserHelpers.readFile("test_data\\" + "inputTestSong");
        // TODO: this should be unit testable somehow?!
        // doc.newSongbookWordDoc("testOutputSong", "Test song", song);
    }
}
