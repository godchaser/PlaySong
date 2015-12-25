package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
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

	public static SongLyrics get(Long id) {
		return find.byId(id);
	}

	public static Finder<Long, SongLyrics> find = new Finder<Long, SongLyrics>(
			Long.class, SongLyrics.class);

	public static List<SongLyrics> all() {
		return find.all();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Song getSong() {
		return song;
	}

	public void setSong(Song song) {
		this.song = song;
	}
	
	public void updateSongLyrics(){
		updateSongKeys();
		sanitizeLyrics();
		update();
		// Automatically update song modification journal
		Date date = new Date();
		getSong().setDateModified(date);
		getSong().update();
	}
	
	public void updateSongKeys(){
		String songKey = LineTypeChecker.getSongKey(songLyrics);
		setSongKey(songKey);
	}
	
	public void sanitizeLyrics(){
		String newSongLyrics = SongSanitizer.sanitizeSong(songLyrics);
		setsongLyrics(newSongLyrics);
	}
}