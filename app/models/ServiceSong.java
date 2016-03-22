package models;

import javax.persistence.*;

import models.helpers.PdfPrintable;
import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;

@Entity
public class ServiceSong extends Model implements PdfPrintable, Comparable<ServiceSong> {

    @Id
    public String id;

    @Required
    public String songName;

    @Required
    public String songId;

    @Required
    public String lyricsId;

    @Required
    public String songKey;

    @ManyToOne
    public Service service;

    @Required
    @Column(columnDefinition = "TEXT")
    public String songLyrics;

    public String getSongName() {
        return songName;
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

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getTitle() {
        String songName = getSongName();
        return songName;
    }

    public String getContent() {
        String songLyrics = getSongLyrics();
        return songLyrics;
    }

    @Override
    public int compareTo(ServiceSong otherSong) {
        return this.id.compareTo(otherSong.id);
    }

}