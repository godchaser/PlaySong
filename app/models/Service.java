package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

import models.helpers.SongPrint;
import play.data.format.Formats;
import play.data.validation.Constraints.Required;
import play.db.ebean.*;

@Entity
public class Service extends Model {

	@Id
	public Long id;

	@Required
	public String userEmail;
	@Required
	public String userName;

	@OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
	@OrderColumn(name = "INDEX")
	public List<ServiceSong> songs = new ArrayList<>();

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

	public List<ServiceSong> getSongs() {
		return songs;
	}

	public void setSongs(List<ServiceSong> songs) {
		this.songs = songs;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Formats.DateTime(pattern = "dd-MM-yyyy_hhmm")
	public Date dateCreated = new Date();

	public static Finder<Long, Service> find = new Finder<Long, Service>(Long.class, Service.class);

	public static void delete(Long id) {
		find.byId(id).delete();
	}
}