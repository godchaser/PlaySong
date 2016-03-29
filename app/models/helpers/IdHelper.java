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

    public static String getIdFromName(String name) {
        return name.toLowerCase().replace(' ', '-');
    }

    public static String getNextAvailableSongId(String songName) {
        // check if song with same name already exists
        if (Song.getBySongName(songName) == null) {
            return getIdFromName(songName);
        } else {
            for (int i = 2; i < 100; i++) {
                // iterate through song-name-1, song-name-2 and use the first free one
                String searchForSong = songName + "-" + i;
                if (Song.getBySongName(searchForSong) == null) {
                    return getIdFromName(searchForSong);
                }
            }
        }
        return getIdFromName(songName);
    }

    public static String getNextAvailableSongLyricsId(String songName) {
        // check if song with same name already exists
        if (SongLyrics.get(songName) == null) {
            return getIdFromName(songName);
        } else {
            for (int i = 2; i < 100; i++) {
                // iterate through song-name-1, song-name-2 and use the first free one
                String searchForSong = songName + "-" + i;
                if (SongLyrics.get(searchForSong) == null) {
                    return getIdFromName(searchForSong);
                }
            }
        }
        return getIdFromName(songName);
    }

    public static String getNextAvailablePlayListId(String playListName) {
        // check if song with same name already exists
        if (Playlist.get(playListName) == null) {
            return getIdFromName(playListName);
        } else {
            for (int i = 2; i < 100; i++) {
                // iterate through playlist-name-1, playlist-name-2 and use the first free one
                String searchForPlaylist = playListName + "-" + i;
                if (Playlist.get(searchForPlaylist) == null) {
                    return getIdFromName(searchForPlaylist);
                }
            }
        }
        return getIdFromName(playListName);
    }
    
    public static String getNextAvailablePlayListSongId(String songName) {
        // check if song with same name already exists
        if (PlaylistSong.get(songName) == null) {
            return getIdFromName(songName);
        } else {
            for (int i = 2; i < 100; i++) {
                // iterate through song-name-1, song-name-2 and use the first free one
                String searchForServiceSong = songName + "-" + i;
                if (PlaylistSong.get(searchForServiceSong) == null) {
                    return getIdFromName(searchForServiceSong);
                }
            }
        }
        return getIdFromName(songName);
    }
}
