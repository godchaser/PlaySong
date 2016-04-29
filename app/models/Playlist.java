package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

import play.data.format.Formats;
import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Playlist extends Model {

	@Id
	public String id;
	
	@Required
	public String userEmail;
	@Required
	public String userName;

	@OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL)
	@JsonManagedReference
	public List<PlaylistSong> songs = new ArrayList<>();
	
	public String playListName;

	public static Playlist get(String id) {
        return find.where().eq("id", id).findUnique();
        // TODO: try this after compilation
        // return find.byId(id);
    }
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public List<PlaylistSong> getSongs() {
		return songs;
	}

	public void setSongs(List<PlaylistSong> songs) {
		this.songs = songs;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public String getPlayListName() {
		return playListName;
	}

	public void setPlayListName(String playListName) {
		this.playListName = playListName;
	}

	@Formats.DateTime(pattern = "dd-MM-yyyy_hhmm")
	public Date dateCreated = new Date();

	public static Finder<Long, Playlist> find = new Finder<>(Playlist.class);
    
    public static List <Playlist> all (){
		return find.all();
	}
	public static void deleteById(String id) {
		get(id).delete();
	}
	
	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}