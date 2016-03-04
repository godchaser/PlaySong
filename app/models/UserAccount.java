package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;
import play.Logger;

@Entity
public class UserAccount extends Model {

    @Id
    @Required
    public String email;
    @Required
    public String name;
    @Required
    public String password;

    @ManyToMany
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

    public static UserAccount getByEmail(String userEmail) {
        return find.where().eq("email", userEmail).findUnique();
    }

    public static String getNameFromEmail(String email) {
        return find.where().eq("email", email).findUnique().name;
    }

    public void setDefaultSongbook() {
        getSongbooks().add(SongBook.getDefaultSongbook(UserAccount.getByEmail(email)));
    }

    public void addSongbook(SongBook songbook) {
        // Add songbook to user if he is not owner already
        if (!getSongbooks().contains(songbook)) {
            getSongbooks().add(songbook);
        }
        update();
    }

    public void removeSongbook(SongBook songbook) {
        // Remove songbook user if he is not owner already
        // if (!getSongbooks().contains(songbook)) {
        Logger.debug("Removing songbook from user: " + getEmail());
        getSongbooks().remove(songbook);
        // }

        update();
        Logger.debug("Removed songbook from user");
    }

    public boolean containsSongbook(Long id) {
        SongBook searchedSongbook = new SongBook();
        searchedSongbook.setId(id);
        return getSongbooks().contains(searchedSongbook);
    }

    @Override
    public boolean equals(Object o) {
        if ((o instanceof UserAccount) && (((UserAccount) o).getEmail().equals(getEmail()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getEmail().hashCode();
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