package controllers;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.SongLyrics;
import play.libs.Json;

import models.Song;

/**
 * Created by samuel on 4/1/15.
 */
public class SongToJson {

	public static ObjectNode convert(Song s) {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode songLyricsIDs = mapper.createObjectNode();

		ArrayNode songLyricsIDsArray = songLyricsIDs.putArray("songLyricsIDs");

		for (SongLyrics lyrics : s.songLyrics) {
			songLyricsIDsArray.add(lyrics.getsongLyricsId());
		}

		ObjectNode songObject = convert(s.songName, s.songLink, s.songOriginalTitle, s.songAuthor, s.id, s.songImporter,
				songLyricsIDsArray);

		return songObject;
	}
	
	public static ObjectNode convert(String songName, String songLink, String songOriginalTitle, String songAuthor,
			Long id, String songImporter, ArrayList<String> songLyricsIDsArrayList) {
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode songLyricsIDs = mapper.createObjectNode();

		ArrayNode songLyricsIDsArray = songLyricsIDs.putArray("songLyricsIDs");

		for (String lyricsId : songLyricsIDsArrayList) {
			songLyricsIDsArray.add(lyricsId);
		}
		ObjectNode songObject = Json.newObject();

		songObject.put("songName", songName);
		songObject.put("songLink", songLink);
		songObject.put("songOriginalTitle", songOriginalTitle);
		songObject.put("songAuthor", songAuthor);
		songObject.put("songId", id);
		songObject.put("songImporter", songImporter);
		songObject.putArray("songLyricsIDs").addAll(songLyricsIDsArray);

		return songObject;
	}

	public static ObjectNode convert(String songName, String songLink, String songOriginalTitle, String songAuthor,
			Long id, String songImporter, ArrayNode songLyricsIDsArray) {

		ObjectNode songObject = Json.newObject();

		songObject.put("songName", songName);
		songObject.put("songLink", songLink);
		songObject.put("songOriginalTitle", songOriginalTitle);
		songObject.put("songAuthor", songAuthor);
		songObject.put("songId", id);
		songObject.put("songImporter", songImporter);
		songObject.putArray("songLyricsIDs").addAll(songLyricsIDsArray);

		return songObject;
	}

}
