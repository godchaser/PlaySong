package models.helpers;

import java.util.ArrayList;

/**
 * Created by samuel on 4/7/15.
 */
public class SongTableData {
	private String song_name;
	private String song_original_title;
	private String song_author;
	private String song_link;
	private String song_importer;
	private ArrayList<String> lyrics_id;

	public SongTableData() {
		lyrics_id = new ArrayList<String>();
	}

	public String getSong_name() {
		return song_name;
	}

	public void setSong_name(String song_name) {
		this.song_name = song_name;
	}

	public String getSong_original_title() {
		return song_original_title;
	}

	public void setSong_original_title(String song_original_title) {
		this.song_original_title = song_original_title;
	}

	public String getSong_author() {
		return song_author;
	}

	public void setSong_author(String song_author) {
		this.song_author = song_author;
	}

	public String getSong_link() {
		return song_link;
	}

	public void setSong_link(String song_link) {
		this.song_link = song_link;
	}

	public String getSong_importer() {
		return song_importer;
	}

	public void setSong_importer(String song_importer) {
		this.song_importer = song_importer;
	}

	public ArrayList<String> getLyrics_id() {
		return lyrics_id;
	}

	public void setLyrics_id(ArrayList<String> lyrics_id) {
		this.lyrics_id = lyrics_id;
	}

}
