package models.helpers;

import models.Song;

/**
 * Created by samuel on 4/7/15.
 */
public class SongPrint {
    Song song;
    Long lyricsID;
    String key;

    public SongPrint(Song s, Long lid, String key){
        setSong(s);
        setLyricsID(lid);
        setKey(key);
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
