package rest.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SongBookJson {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("songBookName")
    @Expose
    private String songBookName;
    @SerializedName("privateSongbook")
    @Expose
    private Boolean privateSongbook;

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
     *     The privateSongbook
     */
    public Boolean getPrivateSongbook() {
        return privateSongbook;
    }

    /**
     * 
     * @param privateSongbook
     *     The privateSongbook
     */
    public void setPrivateSongbook(Boolean privateSongbook) {
        this.privateSongbook = privateSongbook;
    }


}
