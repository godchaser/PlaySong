package document.tools;

/**
 * Created by samuel on 4/6/15.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

import models.SongLyrics;
import models.helpers.SongPrint;

import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;

import com.google.inject.Inject;

import play.Environment;
import play.Logger;
import chord.tools.ChordLineTransposer;
import chord.tools.LineTypeChecker;

public class DocxGenerator {

    private String songTitleFont = "Arial";
    private int songTitleSize = 18;
    private String songLyricsFont = "Courier New";
    private int songLyricsFontSize = 14;
    private String title = "Songbook";
    private String subtitle = "2015";

    @Inject Environment environment;
    
    public void writeSong(XWPFDocument document, String songTitle, String songLyrics, int songTotalNumber, boolean isLast) {
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
        // breaking song by new lines so we can write multiple runs in paragraph
        // and add break after each reun
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
            if (!isLast) {
                System.out.println("adding break");
                tmpRun.addBreak(BreakType.PAGE);
                // tmpParagraph.setPageBreak(true);
            } else {
                System.out.println("not adding break");
            }
        }
    }

    public File newSongbookWordDoc(String filename, List<SongPrint> songPrintObjects) throws Exception {

        File templateFile = environment.getFile("conf/template.dotx");

        InputStream is = new FileInputStream(templateFile);
        System.out.println("######## is: " + is);
        File outputFile = new File("resources/docx/" + filename + ".docx");
        XWPFDocument template = new XWPFDocument(is);

        final XWPFDocument document = new XWPFDocument();
        // copy styles from template to new doc
        XWPFStyles newStyles = document.createStyles();
        newStyles.setStyles(template.getStyle());
        template.close();

        CTBody body = document.getDocument().getBody();
        if (!body.isSetSectPr()) {
            body.addNewSectPr();
        }
        CTSectPr section = body.getSectPr();

        if (!section.isSetPgSz()) {
            section.addNewPgSz();
        }
        CTPageSz pageSize = section.getPgSz();

        pageSize.setOrient(STPageOrientation.LANDSCAPE);
        // word page sizes:
        // http://stackoverflow.com/questions/20188953/how-to-set-page-orientation-for-word-document
        pageSize.setH(BigInteger.valueOf(11900));
        pageSize.setW(BigInteger.valueOf(16900));

        boolean singleSong = (songPrintObjects.size() == 1) ? true : false;
        int songTotalNumber = (singleSong) ? -1 : songPrintObjects.size();
        Logger.trace("Fonts - title:" + songTitleFont + " lyrics: " + songLyricsFont);
        Logger.trace("Sizes - title:" + songTitleSize + " lyrics: " + songLyricsFontSize);
        Logger.trace("Number of songs (-1 if single page): " + songTotalNumber);
        boolean isLast = false;
        for (int i = 0; i < songPrintObjects.size(); i++) {
            SongPrint s = songPrintObjects.get(i);

            // if this is single page or last page then we don't need page break
            if (singleSong || (i == songPrintObjects.size() - 1)) {
                songTotalNumber = -1;
            } else {
                songTotalNumber = i;
            }
            Logger.trace("Now exporting songId: " + songPrintObjects.get(i).getSong().id);

            String origKey = SongLyrics.get(s.getLyricsID()).songKey;
            String newKey = songPrintObjects.get(i).getKey();
            Logger.trace("Orig key: " + origKey + " New key: " + newKey);
            String songLyrics = SongLyrics.get(s.getLyricsID()).songLyrics;
            if (!origKey.equals(newKey)) {
                songLyrics = chordTranspose(origKey, newKey, songLyrics);
            }
            isLast = (i == (songPrintObjects.size() - 1)) ? true : false;
            System.out.println("isLast value: " + isLast);
            writeSong(document, s.getSong().songName, songLyrics, songTotalNumber, isLast);
        }

        FileOutputStream fos = null;
        try {
            Logger.trace("Now writing to exported file: " + outputFile);
            // fos = new FileOutputStream(new File(outputFile));
            fos = new FileOutputStream(outputFile);
            document.write(fos);         
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } finally {
            fos.close();
            is.close();
        }

        return outputFile;
    }

    private String chordTranspose(String origKey, String newKey, String songText) {
        String[] songLines = songText.split("[\r\n]+");
        StringBuilder transposedSong = new StringBuilder();
        for (String songLine : songLines) {
            Logger.trace("Checking song lines: " + songLine);
            String updatedSongLine = songLine;
            if (LineTypeChecker.isChordLine(songLine)) {
                // Logger.trace("Transposing by ammount: " + transposeAmmount);
                ChordLineTransposer clt = new ChordLineTransposer(songLine);
                updatedSongLine = clt.transpose2(origKey, newKey);
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
        this.songLyricsFont = "Consolas";
        // this.songLyricsFont = songLyricsFont;
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
        // DocxGenerator doc = new DocxGenerator();
        // ArrayList<String> song = ParserHelpers.readFile("test_data\\" +
        // "inputTestSong");
        // TODO: this should be unit testable somehow?!
        // doc.newSongbookWordDoc("testOutputSong", "Test song", song);
    }
}
