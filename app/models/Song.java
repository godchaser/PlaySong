package models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.*;

import controllers.chords.LineTypeChecker;
import play.data.format.Formats;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import static java.util.Collections.*;

/**
 * Created by samuel on 19.02.15..
 */
@Entity
public class Song extends Model implements Comparator<Song>{

    @Id
    public Long id;

    @Required
    public String songName;

    public String songOriginalTitle;

    public String songAuthor;

    public String songLink;

    public String songImporter;

    public String songLastModifiedBy;

    public int songBookId;

    @Formats.DateTime(pattern="dd/MM/yyyy hh:mm")
    public Date dateCreated = new Date();

    @Formats.DateTime(pattern="dd/MM/yyyy hh:mm")
    public Date dateModified = new Date();

    @OneToMany(mappedBy="song",cascade= CascadeType.ALL)
    public List<SongLyrics> songLyrics  = new ArrayList<>();

    public static List<Song> all() {
        return find.all();
    }

    public static int getNumberOfSongsInDatabase() {
        return find.all().size();
    }

    public static Song get(Long id){
        return find.byId(id);
    }

    public static void updateOrCreateSong(Song song) {
        //delete empty lyrics
        List removedList = new ArrayList();
        for (int i=0;i<song.songLyrics.size();i++){
             if (song.songLyrics.get(i).getsongLyrics().length()<2){
                removedList.add(song.songLyrics.get(i));
            }
        }
        song.songLyrics.removeAll(removedList);
        for (SongLyrics songLyrics : song.songLyrics){
            String songKey = LineTypeChecker.getSongKey(songLyrics.getsongLyrics());
            songLyrics.setSongKey(songKey);
        }
        if (song.id != null && song.id>0){
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
            Date date = new Date();
            song.setDateModified(date);
            song.update();
        } else {
            song.id = null;
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
            Date date = new Date();
            song.setDateCreated(date);
            song.save();
        }
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static boolean contains(Long id) {
        return find.ref(id).contains(id);
    }


    public static Finder<Long, Song> find = new Finder(Long.class, Song.class);

    @Override
    public int compare(Song song1, Song song2) {
        return song1.songName.compareTo(song2.songName);
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

    public int getSongBookId() {
        return songBookId;
    }

    public void setSongBookId(int songBookId) {
        this.songBookId = songBookId;
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
}