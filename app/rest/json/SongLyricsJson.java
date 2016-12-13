
package rest.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SongLyricsJson {


    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("songLyrics")
    @Expose
    private String songLyrics;
    @SerializedName("songKey")
    @Expose
    private String songKey;
    @SerializedName("songLyricsId")
    @Expose
    private String songLyricsId;
    @SerializedName("songLyricsAndroidHtml")
    @Expose
    private String songLyricsAndroidHtml;
    @SerializedName("songLyricsAndroidChordsHtml")
    @Expose
    private String songLyricsAndroidChordsHtml;

    /**
     * 
     * @return
     *     The id
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The songLyrics
     */
    public String getSongLyrics() {
        return songLyrics;
    }

    /**
     * 
     * @param songLyrics
     *     The songLyrics
     */
    public void setSongLyrics(String songLyrics) {
        this.songLyrics = songLyrics;
    }

    /**
     * 
     * @return
     *     The songKey
     */
    public String getSongKey() {
        return songKey;
    }

    /**
     * 
     * @param songKey
     *     The songKey
     */
    public void setSongKey(String songKey) {
        this.songKey = songKey;
    }

    /**
     * 
     * @return
     *     The songLyricsId
     */
    public String getSongLyricsId() {
        return songLyricsId;
    }

    /**
     * 
     * @param songLyricsId
     *     The songLyricsId
     */
    public void setSongLyricsId(String songLyricsId) {
        this.songLyricsId = songLyricsId;
    }

    public String getSongLyricsAndroidHtml() {
        return songLyricsAndroidHtml;
    }

    public void setSongLyricsAndroidHtml(String songLyricsAndroidHtml) {
        this.songLyricsAndroidHtml = songLyricsAndroidHtml;
    }

    public String getSongLyricsAndroidChordsHtml() {
        return songLyricsAndroidChordsHtml;
    }

    public void setSongLyricsAndroidChordsHtml(String songLyricsAndroidChordsHtml) {
        this.songLyricsAndroidChordsHtml = songLyricsAndroidChordsHtml;
    }

}
