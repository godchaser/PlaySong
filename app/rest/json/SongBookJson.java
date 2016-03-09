
package rest.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SongBookJson {

    @SerializedName("songBookName")
    @Expose
    private String songBookName;
    @SerializedName("songBookId")
    @Expose
    private Long songBookId;
    @SerializedName("songBookPrivate")
    @Expose
    private Boolean songBookPrivate;

    /**
     * 
     * @return
     *     The songBookName
     */
    public String getSongBookName() {
        return songBookName;
    }

    /**
     * 
     * @param songBookName
     *     The songBookName
     */
    public void setSongBookName(String songBookName) {
        this.songBookName = songBookName;
    }

    /**
     * 
     * @return
     *     The songBookId
     */
    public Long getSongBookId() {
        return songBookId;
    }

    /**
     * 
     * @param songBookId
     *     The songBookId
     */
    public void setSongBookId(Long songBookId) {
        this.songBookId = songBookId;
    }

    /**
     * 
     * @return
     *     The songBookPrivate
     */
    public Boolean getSongBookPrivate() {
        return songBookPrivate;
    }

    /**
     * 
     * @param songBookPrivate
     *     The songBookPrivate
     */
    public void setSongBookPrivate(Boolean songBookPrivate) {
        this.songBookPrivate = songBookPrivate;
    }

}
