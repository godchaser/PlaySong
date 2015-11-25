package models;

import java.util.Date;

import play.data.format.Formats;

public class SongSuggestion {
	private Long id;
	private String songName;
	
	@Formats.DateTime(pattern = "dd/MM/yyyy hh:mm")
	private Date dateModified;

	public SongSuggestion(Long id, String songName, Date dateModified) {
		this.id = id;
		this.songName = songName;
		this.dateModified = dateModified;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}
	
	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}
	
	public Date getDateModified() {
		return dateModified;
	}
}
