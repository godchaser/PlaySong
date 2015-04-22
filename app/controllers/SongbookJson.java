package controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by samuel on 4/22/15.
 */
public class SongbookJson {
    @JsonProperty("songsJson")
    List<SongJson> songsJson;
    @JsonProperty("fonts")
    Font fonts;

    public List<SongJson> getSongsJson() {
        return songsJson;
    }
    public void setSongsJson(List<SongJson> songsJson) {
        this.songsJson = songsJson;
    }

    public Font getFonts() {
        return fonts;
    }

    public void setFonts(Font fonts) {
        this.fonts = fonts;
    }

    //"{"songsJson":[{"id":"259","lyricsID":"259"},{"id":"142","lyricsID":"142"}],"fonts":{"titleFont":"test","lyricsFont":"test2"}}"
}
