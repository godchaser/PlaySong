package models.helpers;

import models.Song;

/**
 * Created by samuel on 4/7/15.
 */
public class SongPrint {
    Song song;
    Long lyricsID;

    public SongPrint(Song s, Long lid){
        setSong(s);
        setLyricsID(lid);
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public Long getLyricsID() {
        return lyricsID;
    }

    public void setLyricsID(Long lyricsID) {
        this.lyricsID = lyricsID;
    }
}
