package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import models.helpers.AndroidLyricsHtmlBuilder;

import org.apache.commons.lang3.builder.ToStringBuilder;

import chord.tools.LineTypeChecker;
import chord.tools.SongSanitizer;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Created by samuel on 3/31/15.
 */
@Entity
public class SongLyrics extends Model {

    @Id
    public String id;

    @ManyToOne
    @JsonBackReference
    public Song song;

    @Column(columnDefinition = "TEXT")
    public String songLyrics;

    public String songKey;

    @Transient
    public String songLyricsAndroidHtml;

    @Transient
    public String songLyricsAndroidChordsHtml;

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
        setSongLyrics(newSongLyrics);
    }

    public String getSongLyrics() {
        return songLyrics;
    }

    public void setSongLyrics(String lyrics) {
        songLyrics = lyrics;
    }

    public String getSongLyricsId() {
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    // used for android html generation
    public String getSongLyricsAndroidHtml() {
        return AndroidLyricsHtmlBuilder.buildHtmlFromSongLyrics(LineTypeChecker.removeChordLines(songLyrics));
    }

    public void setSongLyricsAndroidHtml(String songLyricsAndroidHtml) {
        this.songLyricsAndroidHtml = songLyricsAndroidHtml;
    }

    public String getSongLyricsAndroidChordsHtml() {
        return AndroidLyricsHtmlBuilder.buildHtmlFromSongLyrics(songLyrics);

    }

    public void setSongLyricsAndroidChordsHtml(String songLyricsAndroidChordsHtml) {
        this.songLyricsAndroidChordsHtml = songLyricsAndroidChordsHtml;
    }

    /*
     * // used only for removal of empty lyrics by Collection remove all
     * 
     * @Override public boolean equals(Object o) { if ((o instanceof SongLyrics) && (((SongLyrics) o).getsongLyrics().equals(getsongLyrics()))) { return true; } else { return false; } }
     * 
     * @Override public int hashCode() { return getsongLyrics().hashCode(); }
     */

}