package models.helpers;

import controllers.chords.ChordLineTransposer;
import models.Song;
import models.SongLyrics;
import play.Logger;

/**
 * Created by samuel on 4/7/15.
 */
public class SongPrint implements PdfPrintable {
	Song song;
	Long lyricsID;
	String key;

	public SongPrint(Song s, Long lid, String key) {
		setSong(s);
		setLyricsID(lid);
		setKey(key);
	}

	public Song getSong() {
		return song;
	}

	public void setSong(Song song) {
		this.song = song;
	}

	public Long getLyricsID() {
		return lyricsID;
	}

	public void setLyricsID(Long lyricsID) {
		this.lyricsID = lyricsID;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getTitle() {
		String songName = getSong().getSongName();
		return songName;
	}

	public String getContent() {
		SongLyrics songLyricsObject = SongLyrics.find.byId(getLyricsID().longValue());
		String songLyrics = songLyricsObject.getsongLyrics();	
		// SONG TRANSPOSE FUNCTION
		String origKey = songLyricsObject.getSongKey();
		String newKey = getKey();
		Logger.trace("Orig key: " + origKey + " New key: " + newKey);
		if (!origKey.equals(newKey)) {
			songLyrics = ChordLineTransposer.transposeLyrics(origKey, newKey, songLyrics);
		}

		return songLyrics;
	}

}
