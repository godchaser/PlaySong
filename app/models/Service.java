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
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long id;
	
	public Long masterId;

	@Required
	public String userEmail;
	@Required
	public String userName;

	@OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
	public List<ServiceSong> songs = new ArrayList<>();
	
	public String serviceName;

	public static Service get(Long id) {
        return find.byId(id);
    }
	
	public static Service getByMasterId(Long masterId) {
		return find.where().eq("master_id", masterId).findUnique();
    }
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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
	public static void delete(Long id) {
		find.byId(id).delete();
	}

	public Long getMasterId() {
		return masterId;
	}

	public void setMasterId(Long masterId) {
		this.masterId = masterId;
	}
}