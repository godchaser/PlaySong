package models;

import java.util.Comparator;

import javax.persistence.*;

import models.helpers.PdfPrintable;
import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;

@Entity
public class ServiceSong extends Model implements PdfPrintable, Comparable<ServiceSong> {

	@Id
	@GeneratedValue
	public int id;

	@Required
	public String songName;

	@Required
	public Long songId;

	@Required
	public Long lyricsId;

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

	public Long getSongId() {
		return songId;
	}

	public void setSongId(Long songId) {
		this.songId = songId;
	}

	public Long getLyricsId() {
		return lyricsId;
	}

	public void setLyricsId(Long lyricsId) {
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
		return Integer.compare(this.id, otherSong.id);
	}

}