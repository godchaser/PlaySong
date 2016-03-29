package models.helpers;

import java.util.ArrayList;
import java.util.List;

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
import models.Playlist;
import models.PlaylistSong;
import models.Song;
import models.SongBook;

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

        ObjectNode songObject = convert(s.songName, s.songLink, s.songOriginalTitle, s.songAuthor, s.id, s.songImporter, s.dateCreated.getTime(), s.dateModified.getTime(),
                s.getPrivateSong(), songLyricsIDsArray, s.getSongbooks());

        return songObject;
    }

    public static ObjectNode convert(String songName, String songLink, String songOriginalTitle, String songAuthor, String id, String songImporter,
            ArrayList<String> songLyricsIDsArrayList) {

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

    public static ObjectNode convert(String songName, String songLink, String songOriginalTitle, String songAuthor, String id, String songImporter, Long dateCreated, Long dateModified,
            boolean privateSong, ArrayNode songLyricsIDsArray, List<SongBook> songbooks) {

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
        songObject.put("privateSong", privateSong);

        // ObjectNode songbooksObject = Json.newObject();
        ArrayNode songbookArray = Json.newArray();
        for (SongBook songbook : songbooks) {
            ObjectNode songbookObject = Json.newObject();
            songbookObject.put("songBookName", songbook.getSongBookName());
            songbookObject.put("id", songbook.getId());
            songbookObject.put("privateSongbook", songbook.getPrivateSongbook());
            songbookArray.add(songbookObject);
        }
        songObject.putArray("songBooks").addAll(songbookArray);
        // songObject.set("songBooks", songbooksObject);

        songObject.putArray("songLyricsIDs").addAll(songLyricsIDsArray);

        return songObject;
    }

    public static ObjectNode convertLyrics(SongLyrics s) {

        ObjectNode songLyricsObject = Json.newObject();

        songLyricsObject.put("songLyricsId", s.getId());
        songLyricsObject.put("songLyrics", s.getsongLyrics());
        songLyricsObject.put("songKey", s.getSongKey());

        return songLyricsObject;
    }

    public static ObjectNode convert(Playlist s) {

        ArrayList<ObjectNode> serviceSongs = new ArrayList<>();
        for (PlaylistSong ss : s.getSongs()) {
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
        serviceObject.put("songBookName", s.getPlayListName());
        serviceObject.putArray("serviceSongs").addAll(serviceSongs);

        return serviceObject;
    }

    public static List<ObjectNode> convert(List<Playlist> serviceList) {
        ArrayList<ObjectNode> servicesArray = new ArrayList<>();
        for (Playlist s : serviceList) {
            servicesArray.add(convert(s));
        }

        return servicesArray;
    }

}
