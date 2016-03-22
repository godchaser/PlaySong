package models.helpers;

import java.util.Date;

import play.data.format.Formats;

public class SongSuggestion {
	private String id;
	private String songName;
	
	@Formats.DateTime(pattern = "dd/MM/yyyy hh:mm")
	private Date dateModified;

	public SongSuggestion(String id, String songName, Date dateModified) {
		this.id = id;
		this.songName = songName;
		this.dateModified = dateModified;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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
