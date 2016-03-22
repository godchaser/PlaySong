package models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.avaje.ebean.Model;

import database.DatabaseHelper;
import models.helpers.IdHelper;
import models.helpers.SongSuggestion;
import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints.Required;

/**
 * Created by samuel on 19.02.15..
 */
@Entity
public class Song extends Model implements Comparator<Song> {

    @Id
    public String id;

    @Required
    public String songName;

    public String songOriginalTitle;

    public String songAuthor;

    public String songLink;

    public String songImporter;

    public String songLastModifiedBy;

    public boolean privateSong = false;

    @ManyToMany(mappedBy = "songs")
    public List<SongBook> songbooks = new ArrayList<SongBook>();

    @Column(updatable = false)
    @Formats.DateTime(pattern = "dd/MM/yyyy hh:mm")
    public Date dateCreated = new Date();

    @Formats.DateTime(pattern = "dd/MM/yyyy hh:mm")
    public Date dateModified = new Date();

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL)
    public List<SongLyrics> songLyrics = new ArrayList<>();

    // this is for form validation
    @Transient
    public String songBookName;
    @Transient
    public String songBookId;

    @Transient
    public boolean isPrivateSongBook;

    public static List<Song> all() {
        return find.all();
    }

    public static int getNumberOfSongsInDatabase() {
        return find.all().size();
    }

    public static Song get(String id) {
        return find.where().eq("id", id).findUnique();
        // TODO: try this after compilation
        // return find.byId(id);
    }

    public static Song getByMasterId(Long masterId) {
        return find.where().eq("master_id", masterId).findUnique();
    }

    public static void updateOrCreateSong(Song song, String userEmail) {

        // LYRICS HANDLING
        boolean songHasSongLyrics = (song.getSongLyrics() != null && (song.getSongLyrics().size() > 0)) ? true : false;

        if (songHasSongLyrics) {
            // delete empty lyrics
            List<SongLyrics> removedList = new ArrayList<SongLyrics>();
            for (int i = 0; i < song.songLyrics.size(); i++) {
                if (song.songLyrics.get(i).getsongLyrics().length() < 2) {
                    removedList.add(song.songLyrics.get(i));
                }
            }
            song.songLyrics.removeAll(removedList);

            // sanitize song lyrics
            for (SongLyrics songLyrics : song.songLyrics) {
                songLyrics.updateSongKeys();
                songLyrics.sanitizeLyrics();
                //create song lyrics id if not present
                if (songLyrics.getId() == null) {
                    songLyrics.setId(IdHelper.getRandomId());
                }
            }
        } else {
            Logger.debug("Song does not have lyrics");
        }

        // SONGBOOK HANDLING
        // preparing songbook
        SongBook activeSongbook = null;
        // first handle if songbooks is empty - create default songbook
        if (song.getSongBookName().isEmpty() || "default".equals(song.getSongBookName())) {
            Logger.debug("Using default songbook while song does not have any songbooks");
            activeSongbook = SongBook.getDefaultSongbook(UserAccount.getByEmail(userEmail));
            song.setSongBook(activeSongbook, userEmail);
        } else {
            Logger.debug("Updating or creating new songbook");
            activeSongbook = SongBook.updateOrCreate(song.getSongBookId(), song.getSongBookName(), userEmail, song.getPrivateSongBook());
            song.setSongBook(activeSongbook, userEmail);
        }

        // SONG HANDLING
        boolean createNewSong = false;

        if (song.id == null) {
            // create new song
            createNewSong = true;
        } else if (song.id != null) {
            // looking for song - if not found create new
            Song foundSong = Song.get(song.id);
            if (foundSong == null) {
                createNewSong = true;
            }
        }

        Date date = new Date();
        // create new song
        if (createNewSong) {
            song.setId(IdHelper.getRandomId());
            Logger.debug("Saving song - by ID: " + song.id);
            song.setDateCreated(date);
            song.setDateModified(date);
            song.save();
        }
        // update existing song
        else {
            Logger.debug("Updating song - by ID: " + song.id);
            song.setDateModified(date);
            song.update();
        }
        Logger.debug("Song updated by user: " + song.songLastModifiedBy);
        Logger.debug("Song updated on: " + song.getDateModified().toString());
        Logger.debug("Adding song to songbook");
        SongBook.addSong(song, activeSongbook);
    }

    public void setSongBook(SongBook activeSongbook, String userEmail) {
        // Add songbook to song - skip if song already contains songbook
        // Logger.debug("############# " + SongBook.getByNameAndEmail(activeSongbook.getSongBookName(), userEmail).getSongBookName());
        // 1. check if song has any songbooks
        if (getSongbooks() != null && !getSongbooks().isEmpty()) {
            // 2. check if song contains has any songbooks
            if (!getSongbooks().contains(SongBook.getByNameAndEmail(activeSongbook.getSongBookName(), userEmail))) {
                getSongbooks().add(activeSongbook);
            } else {
                // songbook is already added
            }
        } else {
            // add new songbook to song
            List<SongBook> songbooks = new ArrayList<SongBook>();
            songbooks.add(activeSongbook);
            setSongbooks(songbooks);
        }
    }

    public static void deleteById(String id) {
        Song thisSong = Song.get(id);

        // first delete all associatons toward this song
        for (SongBook songbook : thisSong.getSongbooks()) {
            Logger.debug("Delete song songbook: " + songbook.getSongBookName());
            songbook.getSongs().remove(thisSong);
            songbook.update();
        }

        thisSong.delete();
    }

    public static Finder<Long, Song> find = new Finder<>(Song.class);

    @Override
    public int compare(Song song1, Song song2) {
        return song1.songName.compareTo(song2.songName);
    }

    public static List<SongSuggestion> getSongModifiedList() {

        int minusMonth = 1;

        Calendar calNow = Calendar.getInstance();
        // adding -1 month
        calNow.add(Calendar.MONTH, -minusMonth);
        Date dateBeforeAMonth = calNow.getTime();

        Date dateNow = Calendar.getInstance().getTime();

        List<Song> songsModifiedInLastMonth = Song.find.where().between("date_modified", dateBeforeAMonth, dateNow).orderBy("date_modified desc").setMaxRows(10).findList();

        List<SongSuggestion> songModifiedList = new ArrayList<>();
        for (Song song : songsModifiedInLastMonth) {
            songModifiedList.add(new SongSuggestion(song.getId(), song.getSongName(), song.getDateModified()));
        }
        return songModifiedList;
    }

    public static List<SongSuggestion> getSongCreatedList() {

        int minusMonth = 1;

        Calendar calNow = Calendar.getInstance();
        // adding -1 month
        calNow.add(Calendar.MONTH, -minusMonth);
        Date dateBeforeAMonth = calNow.getTime();

        Date dateNow = Calendar.getInstance().getTime();

        List<Song> songsCreatedInLastMonth = Song.find.where().between("date_created", dateBeforeAMonth, dateNow).orderBy("date_created desc").setMaxRows(10).findList();

        List<SongSuggestion> songCreatedList = new ArrayList<>();
        for (Song song : songsCreatedInLastMonth) {
            songCreatedList.add(new SongSuggestion(song.getId(), song.getSongName(), song.getDateModified()));
        }
        return songCreatedList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SongLyrics> getSongLyrics() {
        return songLyrics;
    }

    public void setSongLyrics(List<SongLyrics> songLyrics) {
        this.songLyrics = songLyrics;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongOriginalTitle() {
        return songOriginalTitle;
    }

    public void setSongOriginalTitle(String songOriginalTitle) {
        this.songOriginalTitle = songOriginalTitle;
    }

    public String getSongAuthor() {
        return songAuthor;
    }

    public void setSongAuthor(String songAuthor) {
        this.songAuthor = songAuthor;
    }

    public String getSongLink() {
        return songLink;
    }

    public void setSongLink(String songLink) {
        this.songLink = songLink;
    }

    public String getSongImporter() {
        return songImporter;
    }

    public void setSongImporter(String songImporter) {
        this.songImporter = songImporter;
    }

    public String getSongLastModifiedBy() {
        return songLastModifiedBy;
    }

    public void setSongLastModifiedBy(String songLastModifiedBy) {
        this.songLastModifiedBy = songLastModifiedBy;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public List<SongBook> getSongbooks() {
        return songbooks;
    }

    public void setSongbooks(List<SongBook> songbooks) {
        this.songbooks = songbooks;
    }

    public boolean getPrivateSong() {
        return privateSong;
    }

    public void setPrivateSong(boolean privateSong) {
        this.privateSong = privateSong;
    }

    public String getSongBookName() {
        return songBookName;
    }

    public void setSongBookName(String songBookName) {
        this.songBookName = songBookName;
    }

    public String getSongBookId() {
        return songBookId;
    }

    public void setSongBookId(String songBookId) {
        this.songBookId = songBookId;
    }

    public boolean getPrivateSongBook() {
        return isPrivateSongBook;
    }

    public void setPrivateSongBook(boolean isPrivateSongBook) {
        this.isPrivateSongBook = isPrivateSongBook;
    }

    @Override
    public boolean equals(Object o) {
        if ((o instanceof Song) && (((Song) o).getId().equals(getId()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

}