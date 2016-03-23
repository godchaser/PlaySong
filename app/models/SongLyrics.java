package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import chord.tools.LineTypeChecker;
import chord.tools.SongSanitizer;
import com.avaje.ebean.Model;

/**
 * Created by samuel on 3/31/15.
 */
@Entity
public class SongLyrics extends Model {

    @Id
    public String id;

    @ManyToOne
    public Song song;

    @Column(columnDefinition = "TEXT")
    public String songLyrics;

    public String songKey;

    public static SongLyrics get(String id) {
        return find.where().eq("id", id).findUnique();
        // TODO: try this after compilation
        // return find.byId(id);
    }

    public static Finder<Long, SongLyrics> find = new Finder<>(SongLyrics.class);

    public static List<SongLyrics> all() {
        return find.all();
    }

    public void updateSongLyrics() {
        updateSongKeys();
        sanitizeLyrics();
        update();
        // Automatically update song modification journal
        Date date = new Date();
        getSong().setDateModified(date);
        getSong().update();
    }

    public static void deleteSongLyricsForSong(Song song) {
        List<SongLyrics> songLyrics = song.getSongLyrics();
        for (SongLyrics songLyric : songLyrics) {
            songLyric.delete();
        }
    }

    public void updateSongKeys() {
        String songKey = LineTypeChecker.getSongKey(songLyrics);
        setSongKey(songKey);
    }

    public void sanitizeLyrics() {
        String newSongLyrics = SongSanitizer.sanitizeSong(songLyrics);
        setsongLyrics(newSongLyrics);
    }

    public String getsongLyrics() {
        return songLyrics;
    }

    public void setsongLyrics(String lyrics) {
        songLyrics = lyrics;
    }

    public String getsongLyricsId() {
        return id;
    }

    public String getSongKey() {
        return songKey;
    }

    public void setSongKey(String songKey) {
        this.songKey = songKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }
    
    
    /*
    // used only for removal of empty lyrics by Collection remove all
    @Override
    public boolean equals(Object o) {
        if ((o instanceof SongLyrics) && (((SongLyrics) o).getsongLyrics().equals(getsongLyrics()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getsongLyrics().hashCode();
    }
*/
}