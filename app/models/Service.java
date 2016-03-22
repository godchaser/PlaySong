package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import play.data.format.Formats;
import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;

@Entity
public class Service extends Model {

	@Id
	public String id;
	
	@Required
	public String userEmail;
	@Required
	public String userName;

	@OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
	public List<ServiceSong> songs = new ArrayList<>();
	
	public String serviceName;

	public static Service get(String id) {
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
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Formats.DateTime(pattern = "dd-MM-yyyy_hhmm")
	public Date dateCreated = new Date();

	public static Finder<Long, Service> find = new Finder<>(Service.class);
    
    public static List <Service> all (){
		return find.all();
	}
	public static void deleteById(String id) {
		get(id).delete();
	}

}