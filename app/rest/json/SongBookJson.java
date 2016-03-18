package rest.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SongBookJson {

    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("masterId")
    @Expose
    private Long masterId;
    @SerializedName("songBookName")
    @Expose
    private String songBookName;
    @SerializedName("privateSongbook")
    @Expose
    private boolean privateSongbook;
    @SerializedName("songbookOwner")
    @Expose
    private String songbookOwner;
    @SerializedName("songIDs")
    @Expose
    private List<Long> songIDs = new ArrayList<Long>();
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getSongBookName() {
        return songBookName;
    }
    public void setSongBookName(String songBookName) {
        this.songBookName = songBookName;
    }
    public boolean getPrivateSongbook() {
        return privateSongbook;
    }
    public void setPrivateSongbook(boolean privateSongbook) {
        this.privateSongbook = privateSongbook;
    }
    public String getSongbookOwner() {
        return songbookOwner;
    }
    public void setSongbookOwner(String songbookOwner) {
        this.songbookOwner = songbookOwner;
    }
    public List<Long> getSongIDs() {
        return songIDs;
    }
    public void setSongIDs(List<Long> songIDs) {
        this.songIDs = songIDs;
    }
	public Long getMasterId() {
		return masterId;
	}
	public void setMasterId(Long masterId) {
		this.masterId = masterId;
	}

}
