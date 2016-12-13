
package rest.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SongsJson {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("tmpId")
    @Expose
    private String tmpId;
    @SerializedName("syncId")
    @Expose
    private String syncId;
    @SerializedName("songName")
    @Expose
    private String songName;
    @SerializedName("songOriginalTitle")
    @Expose
    private String songOriginalTitle;
    @SerializedName("songAuthor")
    @Expose
    private String songAuthor;
    @SerializedName("songLink")
    @Expose
    private String songLink;
    @SerializedName("songImporter")
    @Expose
    private String songImporter;
    @SerializedName("songLastModifiedBy")
    @Expose
    private String songLastModifiedBy;
    @SerializedName("privateSong")
    @Expose
    private Boolean privateSong;
    @SerializedName("songbooks")
    @Expose
    private List<SongBookJson> songbooks = new ArrayList<SongBookJson>();
    @SerializedName("dateCreated")
    @Expose
    private Long dateCreated;
    @SerializedName("dateModified")
    @Expose
    private Long dateModified;
    @SerializedName("songLyrics")
    @Expose
    private List<SongLyricsJson> songLyrics = new ArrayList<SongLyricsJson>();
    @SerializedName("isPrivateSongBook")
    @Expose
    private Boolean isPrivateSongBook;
    @SerializedName("privateSongBook")
    @Expose
    private Boolean privateSongBook;

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
     *     The tmpId
     */
    public String getTmpId() {
        return tmpId;
    }

    /**
     * 
     * @param tmpId
     *     The tmpId
     */
    public void setTmpId(String tmpId) {
        this.tmpId = tmpId;
    }

    /**
     * 
     * @return
     *     The syncId
     */
    public String getSyncId() {
        return syncId;
    }

    /**
     * 
     * @param syncId
     *     The syncId
     */
    public void setSyncId(String syncId) {
        this.syncId = syncId;
    }

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
     *     The songLastModifiedBy
     */
    public String getSongLastModifiedBy() {
        return songLastModifiedBy;
    }

    /**
     * 
     * @param songLastModifiedBy
     *     The songLastModifiedBy
     */
    public void setSongLastModifiedBy(String songLastModifiedBy) {
        this.songLastModifiedBy = songLastModifiedBy;
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
     *     The songbooks
     */
    public List<SongBookJson> getSongbooks() {
        return songbooks;
    }

    /**
     * 
     * @param songbooks
     *     The songbooks
     */
    public void setSongbooks(List<SongBookJson> songbooks) {
        this.songbooks = songbooks;
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
     *     The songLyrics
     */
    public List<SongLyricsJson> getSongLyrics() {
        return songLyrics;
    }

    /**
     * 
     * @param songLyrics
     *     The songLyrics
     */
    public void setSongLyrics(List<SongLyricsJson> songLyrics) {
        this.songLyrics = songLyrics;
    }

    /**
     * 
     * @return
     *     The isPrivateSongBook
     */
    public Boolean getIsPrivateSongBook() {
        return isPrivateSongBook;
    }

    /**
     * 
     * @param isPrivateSongBook
     *     The isPrivateSongBook
     */
    public void setIsPrivateSongBook(Boolean isPrivateSongBook) {
        this.isPrivateSongBook = isPrivateSongBook;
    }

    /**
     * 
     * @return
     *     The privateSongBook
     */
    public Boolean getPrivateSongBook() {
        return privateSongBook;
    }

    /**
     * 
     * @param privateSongBook
     *     The privateSongBook
     */
    public void setPrivateSongBook(Boolean privateSongBook) {
        this.privateSongBook = privateSongBook;
    }


}
