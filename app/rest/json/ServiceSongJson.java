
package rest.json;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServiceSongJson {

    @SerializedName("songName")
    @Expose
    private String songName;
    @SerializedName("songId")
    @Expose
    private Long songId;
    @SerializedName("lyricsId")
    @Expose
    private Long lyricsId;
    @SerializedName("songKey")
    @Expose
    private String songKey;
    @SerializedName("songLyrics")
    @Expose
    private String songLyrics;

    /**
     * 
     * @return
     *     The songName
     */
    public String getSongName() {
        return songName;
    }

    /**
     * 
     * @param songName
     *     The songName
     */
    public void setSongName(String songName) {
        this.songName = songName;
    }

    /**
     * 
     * @return
     *     The songId
     */
    public Long getSongId() {
        return songId;
    }

    /**
     * 
     * @param songId
     *     The songId
     */
    public void setSongId(Long songId) {
        this.songId = songId;
    }

    /**
     * 
     * @return
     *     The lyricsId
     */
    public Long getLyricsId() {
        return lyricsId;
    }

    /**
     * 
     * @param lyricsId
     *     The lyricsId
     */
    public void setLyricsId(Long lyricsId) {
        this.lyricsId = lyricsId;
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
}
