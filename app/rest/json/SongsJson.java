
package rest.json;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SongsJson {

    @SerializedName("songName")
    @Expose
    private String songName;
    @SerializedName("songLink")
    @Expose
    private String songLink;
    @SerializedName("songOriginalTitle")
    @Expose
    private String songOriginalTitle;
    @SerializedName("songAuthor")
    @Expose
    private String songAuthor;
    @SerializedName("songId")
    @Expose
    private Long songId;
    @SerializedName("masterId")
    @Expose
    private Long masterId;
    @SerializedName("songImporter")
    @Expose
    private String songImporter;
    @SerializedName("dateCreated")
    @Expose
    private Long dateCreated;
    @SerializedName("dateModified")
    @Expose
    private Long dateModified;
    @SerializedName("privateSong")
    @Expose
    private Boolean privateSong;
    @SerializedName("songBooks")
    @Expose
    private List<SongBookJson> songBooks = new ArrayList<SongBookJson>();
    @SerializedName("songLyricsIDs")
    @Expose
    private List<Integer> songLyricsIDs = new ArrayList<Integer>();

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
     *     The songLink
     */
    public String getSongLink() {
        return songLink;
    }

    /**
     * 
     * @param songLink
     *     The songLink
     */
    public void setSongLink(String songLink) {
        this.songLink = songLink;
    }

    /**
     * 
     * @return
     *     The songOriginalTitle
     */
    public String getSongOriginalTitle() {
        return songOriginalTitle;
    }

    /**
     * 
     * @param songOriginalTitle
     *     The songOriginalTitle
     */
    public void setSongOriginalTitle(String songOriginalTitle) {
        this.songOriginalTitle = songOriginalTitle;
    }

    /**
     * 
     * @return
     *     The songAuthor
     */
    public String getSongAuthor() {
        return songAuthor;
    }

    /**
     * 
     * @param songAuthor
     *     The songAuthor
     */
    public void setSongAuthor(String songAuthor) {
        this.songAuthor = songAuthor;
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
     *     The songImporter
     */
    public String getSongImporter() {
        return songImporter;
    }

    /**
     * 
     * @param songImporter
     *     The songImporter
     */
    public void setSongImporter(String songImporter) {
        this.songImporter = songImporter;
    }

    /**
     * 
     * @return
     *     The dateCreated
     */
    public Long getDateCreated() {
        return dateCreated;
    }

    /**
     * 
     * @param dateCreated
     *     The dateCreated
     */
    public void setDateCreated(Long dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * 
     * @return
     *     The dateModified
     */
    public Long getDateModified() {
        return dateModified;
    }

    /**
     * 
     * @param dateModified
     *     The dateModified
     */
    public void setDateModified(Long dateModified) {
        this.dateModified = dateModified;
    }

    /**
     * 
     * @return
     *     The privateSong
     */
    public Boolean getPrivateSong() {
        return privateSong;
    }

    /**
     * 
     * @param privateSong
     *     The privateSong
     */
    public void setPrivateSong(Boolean privateSong) {
        this.privateSong = privateSong;
    }

    /**
     * 
     * @return
     *     The songBooks
     */
    public List<SongBookJson> getSongBooks() {
        return songBooks;
    }

    /**
     * 
     * @param songBooks
     *     The songBooks
     */
    public void setSongBooks(List<SongBookJson> songBooks) {
        this.songBooks = songBooks;
    }

    /**
     * 
     * @return
     *     The songLyricsIDs
     */
    public List<Integer> getSongLyricsIDs() {
        return songLyricsIDs;
    }

    /**
     * 
     * @param songLyricsIDs
     *     The songLyricsIDs
     */
    public void setSongLyricsIDs(List<Integer> songLyricsIDs) {
        this.songLyricsIDs = songLyricsIDs;
    }

	public Long getMasterId() {
		return masterId;
	}

	public void setMasterId(Long masterId) {
		this.masterId = masterId;
	}

}
