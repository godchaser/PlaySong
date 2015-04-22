package controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by samuel on 4/22/15.
 */
public class Font {
        @JsonProperty("titleFont")
        public String titleFont;
        @JsonProperty("lyricsFont")
        public String lyricsFont;
        public String getTitleFont() {
            return titleFont;
        }

        public void setTitleFont(String titleFont) {
            this.titleFont = titleFont;
        }

        public String getLyricsFont() {
            return lyricsFont;
        }

        public void setLyricsFont(String lyricsFont) {
            this.lyricsFont = lyricsFont;
        }
}
