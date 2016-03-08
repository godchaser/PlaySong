package models;

import java.util.*;

import javax.persistence.*;
import javax.persistence.Transient;

import models.helpers.SongSuggestion;
import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;

/**
 * Created by samuel on 19.02.15..
 */
@Entity
public class Song extends Model implements Comparator<Song> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long id;

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
    public Long songBookId;
    @Transient
    public boolean isPrivateSongBook;

    public static List<Song> all() {
        return find.all();
    }

    public static int getNumberOfSongsInDatabase() {
        return find.all().size();
    }

    public static Song get(Long id) {
        return find.byId(id);
    }

    public static void updateOrCreateSong(Song song, String userEmail) {

        // delete empty lyrics
        List<SongLyrics> removedList = new ArrayList<SongLyrics>();
        for (int i = 0; i < song.songLyrics.size(); i++) {
            if (song.songLyrics.get(i).getsongLyrics().length() < 2) {
                removedList.add(song.songLyrics.get(i));
            }
        }
        song.songLyrics.removeAll(removedList);
        
        // preparing songbook
        SongBook activeSongbook = null;
            
        // first handle if songbooks is empty - create default songbook
        if (song.getSongBookName().isEmpty()) {
            Logger.debug("Using default songbook while song does not have any songbooks");
            activeSongbook = SongBook.getDefaultSongbook(UserAccount.getByEmail(userEmail));
            song.setSongBook(activeSongbook, userEmail);
        }
        else {
            Logger.debug("Updating or creating new songbook");
            activeSongbook = SongBook.updateOrCreate(song.getSongBookId(), song.getSongBookName(), userEmail, song.getPrivateSongBook());
            song.setSongBook(activeSongbook, userEmail);
        }
        
        // song must have lyrics
        if (song.songLyrics.size() > 0) {
            for (SongLyrics songLyrics : song.songLyrics) {
                songLyrics.updateSongKeys();
                songLyrics.sanitizeLyrics();
            }
            if (song.id != null && song.id > 0) {
                // DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                Date date = new Date();
                song.setDateModified(date);
                song.update();
            } else {
                song.id = null;
                // DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                Date date = new Date();
                song.setDateCreated(date);
                song.save();
            }
            Logger.debug("Song updated by user: " + song.songLastModifiedBy);
            Logger.debug("Song updated on: " + song.getDateModified().toString());
            Logger.debug("Adding song to songbook");
            SongBook.addSong(song, activeSongbook);
        } else {
            Logger.debug("Will not save song without lyrics");
        }
    }

    public void setSongBook(SongBook activeSongbook, String userEmail) {
        // Add songbook to song - skip if song already contains songbook
        if (!getSongbooks().contains(SongBook.getByNameAndEmail(activeSongbook.getSongBookName(), userEmail))) {
            getSongbooks().add(activeSongbook);
        }
    }

    public static void delete(Long id) {
        Song thisSong = find.ref(id);

        // first delete all associatons toward this song
        for (SongBook songbook : thisSong.getSongbooks()){
            Logger.debug("Delete song songbook: "+ songbook.getSongBookName());
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

        List<Song> songsModifiedInLastMonth = Song.find.where().between("date_modified", dateBeforeAMonth, dateNow).orderBy("date_modified desc").findList();

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

        List<Song> songsCreatedInLastMonth = Song.find.where().between("date_created", dateBeforeAMonth, dateNow).orderBy("date_created desc").findList();

        List<SongSuggestion> songCreatedList = new ArrayList<>();
        for (Song song : songsCreatedInLastMonth) {
            songCreatedList.add(new SongSuggestion(song.getId(), song.getSongName(), song.getDateModified()));
        }
        return songCreatedList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Long getSongBookId() {
        return songBookId;
    }

    public void setSongBookId(Long songBookId) {
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