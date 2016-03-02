package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.UsersDocument;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Model;
import com.avaje.ebean.Model.Finder;

@Entity
public class SongBook extends Model {

    public static final Long DEFAULT_SONGBOOK_ID = 1l;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long id;

    public String songBookName;
    
    public boolean privateSongbook = false;
    
    @ManyToMany
    public List<Song> songs = new ArrayList<>();

    @ManyToMany
    public List<UserAccount> users = new ArrayList<>();

    public static SongBook getDefaultSongbook(UserAccount user) {
        SongBook defaultSongbook = SongBook.get(1l);
        if (defaultSongbook == null) {
            defaultSongbook = new SongBook();
            defaultSongbook.setId(DEFAULT_SONGBOOK_ID);
            defaultSongbook.setSongBookName("default");
            defaultSongbook.setPrivateSongbook(false);
            defaultSongbook.setUser(user);
            defaultSongbook.save();
        }
        return defaultSongbook;
    }

    public static SongBook updateOrCreate(Long id, String songbookName, String userEmail, boolean isPrivateSongBook) {
        SongBook foundSongbook = null;
        if (id != null) {
            foundSongbook = get(id);
            // check if id and songbook name match - then only update existing song
            if (foundSongbook.getSongBookName().equals(songbookName)) {
                // do nothing
            } else {
                // try finding if I have songbook already by that name
                foundSongbook = getByNameAndEmail(songbookName, userEmail);
            }
            // now update private flag if needed
            if (foundSongbook!=null){
                if (foundSongbook.getPrivateSongbook() != isPrivateSongBook){
                    foundSongbook.setPrivateSongbook(isPrivateSongBook);
                    foundSongbook.update();
                }
            }
            
        }
        if (foundSongbook == null) {
            foundSongbook = new SongBook();
            foundSongbook.setSongBookName(songbookName);
            foundSongbook.setPrivateSongbook(isPrivateSongBook);
            // if email is set then find user by it
            if (!"".equals(userEmail)) {
                foundSongbook.setUser(UserAccount.getByEmail(userEmail));
            }
            foundSongbook.save();
        }
        return foundSongbook;
    }

    private void setUser(UserAccount user) {
        // Add user to owners of songbook - skip if user is already owning this songbook
        if (! getUsers().contains(UserAccount.getByEmail(user.getEmail()))){
            getUsers().add(user);
        }        
    }

    public static Finder<Long, SongBook> find = new Finder<>(SongBook.class);

    public static SongBook get(Long id) {
        return find.byId(id);
    }

    // INTERESTING: I have to fetch users first and then query it's members
    public static SongBook getByNameAndEmail(String name, String email) {
        return find.fetch("users").where().and(Expr.eq("users.email", email), Expr.eq("song_book_name", name)).findUnique();
    }

    public static List<SongBook> all() {
        return find.all();
    }

    public static void deleteIfNoMoreSongs(Long id) {
        if (id != DEFAULT_SONGBOOK_ID) {
            List <Song> songsUsingSongbook = Song.find.where().eq("song_book_id", id).findList();
            if (songsUsingSongbook == null || songsUsingSongbook.isEmpty()) {
                find.byId(id).delete();
            }
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if ((o instanceof SongBook) && (((SongBook) o).getId().equals(getId()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
    
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
    
    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
    
    public List<UserAccount> getUsers() {
        return users;
    }

    public void setUsers(List<UserAccount> users) {
        this.users = users;
    }


}