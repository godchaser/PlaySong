package controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by samuel on 4/22/15.
 */
public class SongPrintTuple {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("lyricsID")
    private Long lyricsID;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLyricsID() {
        return lyricsID;
    }

    public void setLyricsID(Long lyricsID) {
        this.lyricsID = lyricsID;
    }
}