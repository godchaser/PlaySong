
package rest.json;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlaylistJson {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("dateCreated")
    @Expose
    private Long dateCreated;
    @SerializedName("userEmail")
    @Expose
    private String userEmail;
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("songBookName")
    @Expose
    private String songBookName;
    @SerializedName("serviceSongs")
    @Expose
    private List<ServiceSongJson> serviceSongJsons = new ArrayList<ServiceSongJson>();

    /**
     * 
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id
     *            The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * @return The dateCreated
     */
    public Long getDateCreated() {
        return dateCreated;
    }

    /**
     * 
     * @param dateCreated
     *            The dateCreated
     */
    public void setDateCreated(Long dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * 
     * @return The userEmail
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * 
     * @param userEmail
     *            The userEmail
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * 
     * @return The userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 
     * @param userName
     *            The userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 
     * @return The songBookName
     */
    public String getSongBookName() {
        return songBookName;
    }

    /**
     * 
     * @param songBookName
     *            The songBookName
     */
    public void setSongBookName(String songBookName) {
        this.songBookName = songBookName;
    }

    /**
     * 
     * @return The serviceSongJsons
     */
    public List<ServiceSongJson> getServiceSongJsons() {
        return serviceSongJsons;
    }

    /**
     * 
     * @param serviceSongJsons
     *            The serviceSongJsons
     */
    public void setServiceSongJsons(List<ServiceSongJson> serviceSongJsons) {
        this.serviceSongJsons = serviceSongJsons;
    }

}
