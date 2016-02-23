package models;

import java.util.List;

import javax.persistence.*;

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

    @ManyToOne
    public UserAccount user;

    public boolean privateSongbook;

    public static SongBook getDefaultSongbook() {
        SongBook defaultSongbook = SongBook.get(1l);
        if (defaultSongbook == null) {
            defaultSongbook = new SongBook();
            defaultSongbook.setId(DEFAULT_SONGBOOK_ID);
            defaultSongbook.setSongBookName("default");
            defaultSongbook.setPrivateSongbook(false);
            defaultSongbook.save();
        }
        return defaultSongbook;
    }

    public static SongBook updateOrCreate(Long id, String songbookName, String userEmail) {
        SongBook foundSongbook = null;
        if (id != null) {
            foundSongbook = get(id);
            // check if id and songbook name match - then only update existing song
            if (foundSongbook.getSongBookName().equals(songbookName)) {
                // do nothing
            } else {
                // try finding if I have songbook already by that name
                foundSongbook = getByNameAndEmail(songbookName, userEmail);
                // I should create new songbook
                // foundSongbook = null;
                // delete stale songbooks
                // deleteIfNoMoreSongs(id);
            }
        }
        if (foundSongbook == null) {
            foundSongbook = new SongBook();
            foundSongbook.setSongBookName(songbookName);
            foundSongbook.setPrivateSongbook(false);
            // if email is set then find user by it
            if (!"".equals(userEmail)) {
                foundSongbook.setUser(UserAccount.getByEmail(userEmail));
            }
            foundSongbook.save();
        }
        return foundSongbook;
    }

    public static Finder<Long, SongBook> find = new Finder<>(SongBook.class);

    public static SongBook get(Long id) {
        return find.byId(id);
    }

    public static SongBook getByNameAndEmail(String name, String email) {
        return find.where().and(Expr.eq("user_email", email), Expr.eq("song_book_name", name)).findUnique();
    }

    public static List<SongBook> all() {
        return find.all();
    }

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

    public static void deleteIfNoMoreSongs(Long id) {
        if (id != DEFAULT_SONGBOOK_ID) {
            List <Song> songsUsingSongbook = Song.find.where().eq("song_book_id", id).findList();
            if (songsUsingSongbook == null || songsUsingSongbook.isEmpty()) {
                find.byId(id).delete();
            }
        }
    }

}