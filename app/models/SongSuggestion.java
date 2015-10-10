package models;

public class SongSuggestion {
	private Long id;
	private String songName;

	public SongSuggestion(Long id, String songName) {
		this.id = id;
		this.songName = songName;
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
}
