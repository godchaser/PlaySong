package models.helpers;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.SongLyrics;
import play.libs.Json;
import models.Service;
import models.ServiceSong;
import models.Song;

/**
 * Created by samuel on 4/1/15.
 */
public class SongToJsonConverter {

	public static ObjectNode convert(Song s) {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode songLyricsIDs = mapper.createObjectNode();

		ArrayNode songLyricsIDsArray = songLyricsIDs.putArray("songLyricsIDs");

		for (SongLyrics lyrics : s.songLyrics) {
			songLyricsIDsArray.add(lyrics.getsongLyricsId());
		}

		ObjectNode songObject = convert(s.songName, s.songLink, s.songOriginalTitle, s.songAuthor, s.id, s.songImporter,
				s.dateCreated.getTime(), s.dateModified.getTime(), songLyricsIDsArray);

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
			Long id, String songImporter, Long dateCreated, Long dateModified, ArrayNode songLyricsIDsArray) {

		ObjectNode songObject = Json.newObject();

		songObject.put("songName", songName);
		songObject.put("songLink", songLink);
		songObject.put("songOriginalTitle", songOriginalTitle);
		songObject.put("songAuthor", songAuthor);
		songObject.put("songId", id);
		songObject.put("songImporter", songImporter);
		songObject.put("songImporter", songImporter);
		songObject.put("dateCreated", dateCreated);
		songObject.put("dateModified", dateModified);
		songObject.putArray("songLyricsIDs").addAll(songLyricsIDsArray);

		return songObject;
	}

	public static ObjectNode convertLyrics(SongLyrics s) {

		ObjectNode songLyricsObject = Json.newObject();

		songLyricsObject.put("songLyricsId", s.getId());
		songLyricsObject.put("songLyrics", s.getsongLyrics());
		songLyricsObject.put("songKey", s.getSongKey());
		songLyricsObject.put("songId", s.getSong().getId());

		return songLyricsObject;
	}

public static ObjectNode convert(Service s) {
		
		ArrayList <ObjectNode> serviceSongs = new ArrayList<>();
		for (ServiceSong ss : s.getSongs()){
			ObjectNode serviceObject = Json.newObject();
			serviceObject.put("songName", ss.getSongName());
			serviceObject.put("songId", ss.getSongId());
			serviceObject.put("lyricsId", ss.getLyricsId());
			serviceObject.put("songKey", ss.getSongKey());
			serviceObject.put("songLyrics", ss.getSongLyrics());
			serviceSongs.add(serviceObject);
		}
		
		ObjectNode serviceObject = Json.newObject();
		serviceObject.put("id", s.getId());
		serviceObject.put("dateCreated", s.getDateCreated().getTime());
		serviceObject.put("userEmail", s.getUserEmail());
		serviceObject.put("userName", s.getUserName());
		serviceObject.put("songBookName", s.getServiceName());
		serviceObject.putArray("serviceSongs").addAll(serviceSongs);

		return serviceObject;
	}
	
	public static List<ObjectNode> convert (List<Service> serviceList){
		//ObjectNode servicesObject = Json.newObject();
		ArrayList <ObjectNode> servicesArray = new ArrayList<>();
		for (Service s : serviceList){
			servicesArray.add(convert(s));
		}
		//servicesObject.putArray("services").addAll(servicesArray);
		//return servicesObject;
		return servicesArray;
	}

}
