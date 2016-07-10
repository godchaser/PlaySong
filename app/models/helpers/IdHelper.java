package models.helpers;

import java.util.UUID;

import models.Playlist;
import models.PlaylistSong;
import models.Song;
import models.SongLyrics;
public class IdHelper {
    public static String getRandomId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    public static String getNextAvailableSyncId(){
        String syncId = getRandomId();
        // search for next available syncid
        while (Song.getBySyncId(syncId) != null){
            syncId = getRandomId();
        }    
        return syncId;
    }

    public static String getIdFromName(String name) { 
        name = name.replace(" ", "-").replace(",", "-").replace("'", "").toLowerCase();
        return (new URLParamEncoder(name)).encode();
    }

    public static String getNextAvailableSongId(String songName) {
    	String songNameId = getIdFromName(songName);
        // check if song with same name already exists
        if (Song.get(songNameId) == null) {
            return songNameId;
        } else {
            for (int i = 2; i < 100; i++) {
                // iterate through song-name-1, song-name-2 and use the first free one
                String searchForSong = songNameId + "-" + i;
                if (Song.get(searchForSong) == null) {
                    return searchForSong;
                }
            }
        }
        return getIdFromName(songName);
    }

    public static String getNextAvailableSongLyricsId(String songName) {
    	String songNameId = getIdFromName(songName);
        // check if song with same name already exists
        if (SongLyrics.get(songNameId) == null) {
            return songNameId;
        } else {
            for (int i = 2; i < 100; i++) {
                // iterate through song-name-1, song-name-2 and use the first free one
                String searchForSong = songNameId + "-" + i;
                if (SongLyrics.get(searchForSong) == null) {
                    return searchForSong;
                }
            }
        }
        return getIdFromName(songName);
    }

    public static String getNextAvailablePlayListId(String playListName) {
    	String playListNameId = getIdFromName(playListName);
        // check if song with same name already exists
        if (Playlist.get(playListNameId) == null) {
            return playListNameId;
        } else {
            for (int i = 2; i < 100; i++) {
                // iterate through playlist-name-1, playlist-name-2 and use the first free one
                String searchForPlaylist = playListNameId + "-" + i;
                if (Playlist.get(searchForPlaylist) == null) {
                    return searchForPlaylist;
                }
            }
        }
        return getIdFromName(playListName);
    }
    
    public static String getNextAvailablePlayListSongId(String songName) {
    	String songNameId = getIdFromName(songName);
        // check if song with same name already exists
        if (PlaylistSong.get(songNameId) == null) {
            return songNameId;
        } else {
            for (int i = 2; i < 100; i++) {
                // iterate through song-name-1, song-name-2 and use the first free one
                String searchForServiceSong = songNameId + "-" + i;
                if (PlaylistSong.get(searchForServiceSong) == null) {
                    return searchForServiceSong;
                }
            }
        }
        return getIdFromName(songName);
    }
}
