package models;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.spec.DHGenParameterSpec;
import javax.persistence.*;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;

import database.DatabaseHelper;
import play.Logger;
import play.db.ebean.Transactional;

@Entity
public class SongBook extends Model {

    public static final Long DEFAULT_SONGBOOK_ID = 1l;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long id;

    public Long masterId;

    public String songBookName;

    public boolean privateSongbook = false;

    private static DatabaseHelper dh = DatabaseHelper.getInstance();

    @ManyToMany
    public List<Song> songs = new ArrayList<>();

    @ManyToMany
    public List<UserAccount> users = new ArrayList<>();

    public static SongBook getDefaultSongbook(UserAccount user) {
        SongBook defaultSongbook = SongBook.getByMasterId(DEFAULT_SONGBOOK_ID);
        if (defaultSongbook == null) {
            defaultSongbook = new SongBook();
            defaultSongbook.setMasterId(DEFAULT_SONGBOOK_ID);
            defaultSongbook.setSongBookName("default");
            defaultSongbook.setPrivateSongbook(false);
            defaultSongbook.save();
            setUser(user, defaultSongbook);
        }
        return defaultSongbook;
    }

    public static SongBook updateOrCreate(Long id, Long masterId, String songbookName, String userEmail, boolean isPrivateSongBook) {
        SongBook foundSongbook = null;

        // first search by master id
        if (masterId != null) {
            Logger.debug("Received songbook by master ID: " + id);
            foundSongbook = getByMasterId(masterId);
        }

        if (id != null && masterId == null) {
            foundSongbook = get(id);
        } else {
            Logger.debug("Songbook ID is null");
        }
        Logger.debug("Received songbook by ID: " + id);

        // check if id and songbook name match - then only update existing
        // song
        if (foundSongbook != null && foundSongbook.getSongBookName().equals(songbookName)) {
            // do nothing
            Logger.debug("Found song by same ID and Name");
        } else {
            // try finding if I have songbook already by that name
            Logger.debug("Sent songbook ID does not match songbook Name: " + id + "->" + songbookName);
            Logger.debug("Looking for existing songbook by this Name only: " + songbookName);
            foundSongbook = getByNameAndEmail(songbookName, userEmail);
        }
        // now update private flag if needed
        if (foundSongbook != null) {
            Logger.debug("Now updating songbook by ID: " + foundSongbook.getId());
            if (foundSongbook.getPrivateSongbook() != isPrivateSongBook) {
                foundSongbook.setPrivateSongbook(isPrivateSongBook);
                foundSongbook.update();
            }
        }
        if (foundSongbook == null) {
            Logger.debug("Not found songbook, creating new");
            foundSongbook = new SongBook();
            // try reusing songbook id
            if (id != null) {
                Logger.debug("Trying to reuse songbook id: " + id);
                // foundSongbook.setId(id);

            } else {
                Logger.debug("New songbook id will be created");
                // id = null;
                foundSongbook.id = null;
            }

            // get next available id if it is null
            if (masterId == null) {
                masterId = dh.getNextSongBookMasterId();
            }
            foundSongbook.setMasterId(masterId);
            foundSongbook.setSongBookName(songbookName);
            foundSongbook.setPrivateSongbook(isPrivateSongBook);
            foundSongbook.save();
        }

        // associate user to this songbook
        setUser(UserAccount.getByEmail(userEmail), foundSongbook);
        return foundSongbook;
    }

    @Transactional
    public static void staleSongbookCleanup(String email) {
        Logger.debug("Starting Songbook cleanup action");
        for (SongBook songbookEntry : getSongbooksOwnedByUser(email)) {
            Logger.debug("Found user songbook: " + email + "->" + songbookEntry.getSongBookName());
            Logger.debug("Checking if she songbook is empty");
            // I should ignore default songbooks
            if (songbookEntry.getSongs().isEmpty() && songbookEntry.getId() != SongBook.DEFAULT_SONGBOOK_ID) {
                Logger.debug("Deleting stale Songbook: " + songbookEntry.getSongBookName());
                UserAccount ua = UserAccount.getByEmail(email);
                // I have to remove all many to many relationship first, before
                // deleting the sb object
                ua.removeSongbook(songbookEntry);
                songbookEntry.removeUser(ua);
                songbookEntry.delete();
            }
        }
        Logger.debug("Finished Songbook cleanup action");
    }

    private static void setUser(UserAccount user, SongBook songbook) {
        user.addSongbook(songbook);
        // Add user to owners of songbook - skip if user is already owning this
        // songbook
        if (!songbook.getUsers().contains(UserAccount.getByEmail(user.getEmail()))) {
            songbook.getUsers().add(user);
            Logger.debug("Adding user to songbook: " + user.getEmail());
            songbook.update();
        }
    }

    private void removeUser(UserAccount user) {
        Logger.debug("Removing user from songbook");
        getUsers().remove(user);
        update();
    }

    public static Finder<Long, SongBook> find = new Finder<>(SongBook.class);

    public static SongBook get(Long id) {
        return find.byId(id);
    }

    public static SongBook getByMasterId(Long masterId) {
        return find.where().eq("master_id", masterId).findUnique();
    }

    private static List<SongBook> getSongbooksOwnedByUser(String email) {
        return UserAccount.getByEmail(email).getSongbooks();
    }

    public static List<SongBook> getAllPublicSongbooks() {
        return find.where().eq("private_songbook", false).findList();
    }

    // INTERESTING: I have to fetch users first and then query it's members
    private static List<SongBook> getSongbooksOwnedByUserExample(String email, String songBookName) {
        return find.fetch("users").where().and(Expr.eq("users.email", email), Expr.eq("song_book_name", songBookName)).findList();
    }

    public static SongBook getByNameAndEmail(String songBookName, String email) {
        Logger.debug("Looking if user owns songbook:" + email + "->" + songBookName);
        List<SongBook> foundSongBooks = getSongbooksOwnedByUser(email);
        SongBook matchedSongbook = null;
        // return null if no songbooks
        if (foundSongBooks == null || foundSongBooks.isEmpty()) {
            return null;
        } else {
            // iterate through all owned songbooks, ignore default one and return the matching one
            for (SongBook sb : foundSongBooks) {
                if (sb.getMasterId().equals(SongBook.DEFAULT_SONGBOOK_ID)) {
                    continue;
                } else {
                    if (sb.getSongBookName().equals(songBookName)) {
                        matchedSongbook = sb;
                        break;
                    }
                }
            }
        }
        return matchedSongbook;
    }

    public static List<SongBook> all() {
        return find.all();
    }

    public static void addSong(Song song, SongBook activeSongbook) {
        // Add song to songbook if it is not already added
        if (!activeSongbook.getSongs().contains(song)) {
            Logger.debug("Adding song to songbook:" + song.getSongName() + "->" + activeSongbook.getSongBookName());
            activeSongbook.getSongs().add(song);
            activeSongbook.update();
        }
    }

    /*
     * public static void deleteIfNoMoreSongs(Long id) { if (id != DEFAULT_SONGBOOK_ID) { List<Song> songsUsingSongbook = Song.find.where().eq("song_book_id", id).findList(); if
     * (songsUsingSongbook == null || songsUsingSongbook.isEmpty()) { find.byId(id).delete(); } } }
     */

    @Override
    public boolean equals(Object o) {
        // of songbook has Id, try matching with Id, if not then try match with master Id, otherwise not equal
        if (o instanceof SongBook) {
            if (((SongBook) o).getId() != null && ((SongBook) o).getId().equals(getId())) {
                return true;
            } else if (((SongBook) o).getMasterId() != null && ((SongBook) o).getMasterId().equals(getMasterId())) {
                return true;
            } else {
                return false;
            }
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

    public Long getMasterId() {
        return masterId;
    }

    public void setMasterId(Long masterId) {
        this.masterId = masterId;
    }

}