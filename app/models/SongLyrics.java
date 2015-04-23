package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;
/**
 * Created by samuel on 3/31/15.
 */
@Entity
public class SongLyrics extends Model {

    @Id
    public Long id;

    @ManyToOne
    public Song song;

    @Column(columnDefinition = "TEXT")
    public String songLyrics;

    public String songKey;

    public String getsongLyrics() {
        return songLyrics;
    }

    public void setsongLyrics(String lyrics) {
        songLyrics = lyrics;
    }


    public Long getsongLyricsId() {
        return id;
    }

    public String getSongKey() {
        return songKey;
    }

    public void setSongKey(String songKey) {
        this.songKey = songKey;
    }

    public static SongLyrics get(Long id){
        return find.byId(id);
    }


    public static Finder<Long, SongLyrics> find = new Finder(Long.class, SongLyrics.class);
}