package models;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;

@Entity
public class UserAccount extends Model {

	@Id
	@Required
	public String email;
	@Required
	public String name;
	@Required
	public String password;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	public List<SongBook> songbooks = new ArrayList<SongBook>();

    public UserAccount(String email, String name, String password) {
		this.email = email;
		this.name = name;
		this.password = password;
	}

	public static Finder<String, UserAccount> find = new Finder<>(UserAccount.class);

	public static UserAccount authenticate(String email, String password) {
		return find.where().eq("email", email).eq("password", password).findUnique();
	}

	public static String getNameFromEmail(String email) {
		return find.where().eq("email", email).findUnique().name;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	   public List<SongBook> getSongbooks() {
	        return songbooks;
	    }

	    public void setSongbooks(List<SongBook> songbooks) {
	        this.songbooks = songbooks;
	    }
	
	
}