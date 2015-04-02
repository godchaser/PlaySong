package controllers;

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

    public static ObjectNode convert (Song s) {
    ObjectNode songObject = Json.newObject();

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode songLyricsIDs= mapper.createObjectNode();
    ArrayNode songLyricsIDsArray = songLyricsIDs.putArray("songLyricsIDs");

    for (SongLyrics lyrics : s.songLyrics){
        songLyricsIDsArray.add(lyrics.getsongLyricsId());
    }

    songObject.put("songName", s.songName);
    songObject.put("songOriginalTitle", s.songOriginalTitle);
    songObject.put("songAuthor", s.songAuthor);
    songObject.put("songLink", s.songLink);
    songObject.put("songId", s.id);
    songObject.put("songImporter", s.songImporter);
    songObject.putArray("songLyricsIDs").addAll(songLyricsIDsArray);

    return songObject;
    }
}
