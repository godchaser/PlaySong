package models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import models.helpers.IdHelper;
import models.helpers.SongSuggestion;

import org.apache.commons.lang3.builder.ToStringBuilder;

import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints.Required;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Created by samuel on 19.02.15..
 */
@Entity
public class Song extends Model implements Comparator<Song> {

    @Id
    public String id;

    // used during db migration
    public String tmpId;
    
    public String syncId;

    @Required
    public String songName;

    public String songOriginalTitle;

    public String songAuthor;

    public String songLink;

    public String songImporter;

    public String songLastModifiedBy;

    public boolean privateSong = false;

    @ManyToMany(mappedBy = "songs")
    @JsonManagedReference
    public List<SongBook> songbooks = new ArrayList<SongBook>();

    @Column(updatable = false)
    @Formats.DateTime(pattern = "dd/MM/yyyy hh:mm")
    public Date dateCreated = new Date();

    @Formats.DateTime(pattern = "dd/MM/yyyy hh:mm")
    public Date dateModified = new Date();

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL)
    @JsonManagedReference
    public List<SongLyrics> songLyrics = new ArrayList<>();

    // this fields are only for form validation
    @Transient
    @JsonIgnore
    public String songBookName;
    @Transient
    @JsonIgnore
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
    
    public static Song getBySyncId(String syncId) {
        return find.where().eq("sync_id", syncId).findUnique();
    }

    public static Song getBySongName(String songName) {
        return find.where().ilike("song_name", songName).findUnique();
    }

    public static Song getByTmpId(String tmpId) {
        return find.where().ilike("tmp_id", tmpId).findUnique();
    }

    public static void updateOrCreateSong(Song song, String userEmail) {

        // TODO: Fix bug when song has empty lyrics
        Logger.debug("Received song with name: " + song.getSongName());
        // LYRICS HANDLING
        boolean songHasSongLyrics = (song.getSongLyrics() != null && (song.getSongLyrics().size() > 0) && !(song.getSongLyrics().get(0).getSongLyrics().isEmpty())) ? true : false;

        if (songHasSongLyrics) {
            Logger.debug("Song contains lyrics");

            // delete empty lyrics
            List<Integer> removedIdxList = new ArrayList<Integer>();
            int i = 0;

            for (SongLyrics singleSongLyrics : song.songLyrics) {
                // Logger.debug("Song for check content: " + singleSongLyrics.getsongLyrics());
                if (singleSongLyrics.getSongLyrics().length() < 2) {
                    removedIdxList.add(i);
                    // Logger.debug("Removed song content: " + singleSongLyrics.getsongLyrics());
                    // Logger.debug("Index for removal: " + i);
                }
                i++;
            }

            // remove from list backwards so the list won't shrink automatically
            // and changed indexes
            for (int idx = removedIdxList.size() - 1; idx >= 0; idx--) {
                // check if current iteration index is in removal list
                // Logger.debug("check iteration: " + idx);
                // 3 2 1 0
                // if this index is in list, then remove it from song lyrics
                if (removedIdxList.get(idx) != null) {
                    song.songLyrics.remove(idx + 1);
                    // Logger.debug("Removing empty lyrics from lyrics by index: " + (idx + 1));
                }
            }

            // sanitize song lyrics
            for (SongLyrics singleSongLyrics : song.songLyrics) {
                singleSongLyrics.updateSongKeys();
                singleSongLyrics.sanitizeLyrics();
                // create new song lyrics id if not present
                if (singleSongLyrics.getId() == null || singleSongLyrics.getId().isEmpty()) {
                    singleSongLyrics.setId(IdHelper.getNextAvailableSongLyricsId(song.songName));
                    Logger.debug("Creating new song lyrics with Id: " + singleSongLyrics.getId());
                } else {
                    Logger.debug("Trying to reuse song lyrics Id: " + singleSongLyrics.getId());
                }
            }
        }
        /*
         * // checking again for if song now does not have any lyrics songHasSongLyrics = (song.getSongLyrics() != null && (song.getSongLyrics().size() > 0)) ? true : false; if
         * (!songHasSongLyrics) { Logger.debug( "Song does not have lyrics - creating empty lyrics"); SongLyrics emptySongLyrics = new SongLyrics();
         * emptySongLyrics.setId(IdHelper.getRandomId()); emptySongLyrics.setsongLyrics(""); emptySongLyrics.setSong(song); emptySongLyrics.save(); song.getSongLyrics().add(emptySongLyrics);
         * }
         */

        // SONG HANDLING
        boolean createNewSong = false;
        Song foundExistingSong = null;

        if (song.id == null) {
            // create new song
            createNewSong = true;
        } else if (song.id != null) {
            // looking for song - if not found create new
            foundExistingSong = Song.get(song.id);
            if (foundExistingSong == null) {
                createNewSong = true;
            }
        }

        // SONGBOOK HANDLING
        // preparing songbook
        SongBook activeSongbook = null;
        // first handle if songbooks is empty - create default songbook
        if (song.getSongBookName() == null || song.getSongBookName().isEmpty() || "default".equals(song.getSongBookName())) {
            // don't set songbook if song already exists while I can reuse its existing default songbook association
            if (createNewSong) {
                Logger.debug("Using default songbook while song does not have any songbooks");
                activeSongbook = SongBook.getDefaultSongbook(UserAccount.getByEmail(userEmail));
                song.setSongBook(activeSongbook, userEmail);
                // TODO: remove this temporary workaround for song migration
            } else if (song.getTmpId() != null) {
                Logger.debug("Tmp workaround: Using default songbook while song does not have any songbooks");
                activeSongbook = SongBook.getDefaultSongbook(UserAccount.getByEmail(userEmail));
                song.setSongBook(activeSongbook, userEmail);
            }
        } else {
            Logger.debug("Updating or creating new songbook");
            activeSongbook = SongBook.updateOrCreate(song.getSongBookId(), song.getSongBookName(), userEmail, song.getPrivateSongBook());
            song.setSongBook(activeSongbook, userEmail);
        }

        Date date = new Date();
        // create new song
        if (createNewSong) {
            // check if song with same name already exists
            song.setId(IdHelper.getNextAvailableSongId(song.songName));
            Logger.debug("Saving song - by ID: " + song.id);
            song.setSyncId(IdHelper.getNextAvailableSyncId());
            //Logger.debug("Saving song - by ID: " + song.id);
            song.setDateCreated(date);
            song.setDateModified(date);
            song.save();
            Logger.debug("Saving song: " + song.toString());
        }
        // update existing song
        else {
            Logger.debug("Updating song - by ID: " + song.id);
            song.setDateModified(date);
            song.update();
        }
        Logger.debug("Song last modified by user: " + song.songLastModifiedBy);
        Logger.debug("Song updated on: " + song.getDateModified().toString());
        Logger.debug("Adding song to songbook");
        SongBook.addSong(song, activeSongbook);
    }

    public void setSongBook(SongBook activeSongbook, String userEmail) {
        // Add songbook to song - skip if song already contains songbook
        // Logger.debug("############# " +
        // SongBook.getByNameAndEmail(activeSongbook.getSongBookName(),
        // userEmail).getSongBookName());
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getTmpId() {
        return tmpId;
    }

    public void setTmpId(String tmpId) {
        this.tmpId = tmpId;
    }

    public String getSyncId() {
        return syncId;
    }

    public void setSyncId(String syncId) {
        this.syncId = syncId;
    }

    

}