package models;

import javax.persistence.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

import models.helpers.PdfPrintable;
import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;
import com.avaje.ebean.Model.Finder;

@Entity
public class PlaylistSong extends Model implements PdfPrintable, Comparable<PlaylistSong> {

    public String id;
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    public Long insertSequence;

    @Required
    public String songName;

    @Required
    public String songId;

    @Required
    public String lyricsId;

    @Required
    public String songKey;

    @ManyToOne
    public Playlist playlist;

    @Required
    @Column(columnDefinition = "TEXT")
    public String songLyrics;

    public String getSongName() {
        return songName;
    }
    
    public static Finder<Long, PlaylistSong> find = new Finder<>(PlaylistSong.class);
    
    
    public static PlaylistSong get(String id) {
        return find.where().eq("id", id).findUnique();
        // TODO: try this after compilation
        // return find.byId(id);
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getLyricsId() {
        return lyricsId;
    }

    public void setLyricsId(String lyricsId) {
        this.lyricsId = lyricsId;
    }

    public String getSongKey() {
        return songKey;
    }

    public void setSongKey(String songKey) {
        this.songKey = songKey;
    }

    public String getSongLyrics() {
        return songLyrics;
    }

    public void setSongLyrics(String songLyrics) {
        this.songLyrics = songLyrics;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist service) {
        this.playlist = service;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        String songName = getSongName();
        return songName;
    }

    public String getContent() {
        String songLyrics = getSongLyrics();
        return songLyrics;
    }
    
    public Long getInsertSequence() {
        return insertSequence;
    }

    public void setInsertSequence(Long insertSequence) {
        this.insertSequence = insertSequence;
    }

    @Override
    public int compareTo(PlaylistSong otherSong) {
        return this.insertSequence.compareTo(otherSong.insertSequence);
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}