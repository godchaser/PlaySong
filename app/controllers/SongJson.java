package controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by samuel on 4/22/15.
 */
public class SongJson {
    @JsonProperty("song")
    public List<SongPrintTuple> song = new ArrayList<>();

    public List<SongPrintTuple> getSongs() {
        return song;
    }
    public void setSongs(List<SongPrintTuple> songs) {
        this.song = songs;
    }

}