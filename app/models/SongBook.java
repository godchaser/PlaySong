package models;

import javax.persistence.*;

import com.avaje.ebean.Model;
import com.avaje.ebean.Model.Finder;

@Entity
public class SongBook extends Model  {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long id;

	public String songBookName;
	
	@ManyToOne
	public UserAccount user;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public String getSongBookName() {
        return songBookName;
    }

    public void setSongBookName(String songBookName) {
        this.songBookName = songBookName;
    }

    public boolean isPrivateSongbook() {
        return privateSongbook;
    }

    public void setPrivateSongbook(boolean privateSongbook) {
        this.privateSongbook = privateSongbook;
    }

    public boolean privateSongbook;
    
    public static Finder<Long, SongBook> find = new Finder<>(SongBook.class);
    
    public static SongBook get(Long id) {
         return find.byId(id);
    }
	
}