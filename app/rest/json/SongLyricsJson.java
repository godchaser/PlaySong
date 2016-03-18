
package rest.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SongLyricsJson {

    @SerializedName("songLyricsId")
    @Expose
    private Long songLyricsId;
    @SerializedName("masterId")
    @Expose
    private Long masterId;
    @SerializedName("songLyrics")
    @Expose
    private String songLyrics;
    @SerializedName("songKey")
    @Expose
    private String songKey;
    @SerializedName("songId")
    @Expose
    private Long songId;

    /**
     * 
     * @return
     *     The songLyricsId
     */
    public Long getSongLyricsId() {
        return songLyricsId;
    }

    /**
     * 
     * @param songLyricsId
     *     The songLyricsId
     */
    public void setSongLyricsId(Long songLyricsId) {
        this.songLyricsId = songLyricsId;
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

	public Long getMasterId() {
		return masterId;
	}

	public void setMasterId(Long masterId) {
		this.masterId = masterId;
	}

}
