
package rest.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SongLyricsJson {

    @SerializedName("songLyricsId")
    @Expose
    private String songLyricsId;
    @SerializedName("songLyrics")
    @Expose
    private String songLyrics;
    @SerializedName("songKey")
    @Expose
    private String songKey;
    @SerializedName("songId")
    @Expose
    private String songId;

    /**
     * 
     * @return The songLyricsId
     */
    public String getSongLyricsId() {
        return songLyricsId;
    }

    /**
     * 
     * @param songLyricsId
     *            The songLyricsId
     */
    public void setSongLyricsId(String songLyricsId) {
        this.songLyricsId = songLyricsId;
    }

    /**
     * 
     * @return The songLyrics
     */
    public String getSongLyrics() {
        return songLyrics;
    }

    /**
     * 
     * @param songLyrics
     *            The songLyrics
     */
    public void setSongLyrics(String songLyrics) {
        this.songLyrics = songLyrics;
    }

    /**
     * 
     * @return The songKey
     */
    public String getSongKey() {
        return songKey;
    }

    /**
     * 
     * @param songKey
     *            The songKey
     */
    public void setSongKey(String songKey) {
        this.songKey = songKey;
    }

    /**
     * 
     * @return The songId
     */
    public String getSongId() {
        return songId;
    }

    /**
     * 
     * @param songId
     *            The songId
     */
    public void setSongId(String songId) {
        this.songId = songId;
    }

}
