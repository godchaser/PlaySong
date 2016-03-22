package models.helpers;

import chord.tools.ChordLineTransposer;
import chord.tools.LineTypeChecker;
import models.Song;
import models.SongLyrics;
import play.Logger;

/**
 * Created by samuel on 4/7/15.
 */
public class SongPrint implements PdfPrintable {
    Song song;
    String lyricsID;
    String key;
    boolean excludeChords;

    public SongPrint(Song s, String lid, String key, boolean excludeChords) {
        setSong(s);
        setLyricsID(lid);
        setKey(key);
        setExcludeChords(excludeChords);
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public String getLyricsID() {
        return lyricsID;
    }

    public void setLyricsID(String lyricsID) {
        this.lyricsID = lyricsID;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        String songName = getSong().getSongName();
        return songName;
    }

    public String getContent() {
        SongLyrics songLyricsObject = SongLyrics.get(getLyricsID());
        String songLyrics = songLyricsObject.getsongLyrics();
        // SONG TRANSPOSE FUNCTION
        String origKey = songLyricsObject.getSongKey();
        String newKey = getKey();
        Logger.trace("Orig key: " + origKey + " New key: " + newKey);
        if (!origKey.equals(newKey)) {
            songLyrics = ChordLineTransposer.transposeLyrics(origKey, newKey, songLyrics);
        }
        if (excludeChords) {
            Logger.trace("Removing chords");
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
