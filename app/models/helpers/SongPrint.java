package models.helpers;

import models.Song;
import models.SongLyrics;
import chord.tools.ChordLineTransposer;
import chord.tools.LineTypeChecker;

/**
 * Created by samuel on 4/7/15.
 */
public class SongPrint implements PdfPrintable {
    Song song;
    String lyricsId;
    String lyricsKey;
    boolean excludeChords;

    public SongPrint(Song song, String lyricsId, String lyricsKey, boolean excludeChords) {
        this.song = song;
        this.lyricsId = lyricsId;
        this.lyricsKey = lyricsKey;
        this.excludeChords = excludeChords;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public String getLyricsID() {
        return lyricsId;
    }

    public void setLyricsID(String lyricsID) {
        this.lyricsId = lyricsID;
    }

    public String getKey() {
        return lyricsKey;
    }

    public void setKey(String key) {
        this.lyricsKey = key;
    }

    public String getTitle() {
        String songName = getSong().getSongName();
        return songName;
    }

    public String getContent() {
        SongLyrics songLyricsObject = SongLyrics.get(getLyricsID());
        String songLyrics = songLyricsObject.getSongLyrics();
        // SONG TRANSPOSE FUNCTION
        String origKey = songLyricsObject.getSongKey();
        String newKey = getKey();
        //Logger.trace("Orig key: " + origKey + " New key: " + newKey);
        if (!origKey.equals(newKey)) {
            songLyrics = ChordLineTransposer.transposeLyrics(origKey, newKey, songLyrics);
        }
        if (excludeChords) {
            //Logger.trace("Removing chords");
            songLyrics = LineTypeChecker.removeChordLines(songLyrics);
        }

        return songLyrics;
    }

    public boolean isExcludeChords() {
        return excludeChords;
    }

    public void setExcludeChords(boolean excludeChords) {
        this.excludeChords = excludeChords;
    }

}
