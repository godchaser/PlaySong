package database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import models.Playlist;
import models.PlaylistSong;
import models.Song;
import models.SongBook;
import models.SongLyrics;
import models.helpers.IdHelper;
import play.Logger;
import play.db.ebean.Transactional;
import rest.json.PlaylistJson;
import rest.json.ServiceSongJson;
import rest.json.SongBookJson;
import rest.json.SongLyricsJson;
import rest.json.SongsJson;

public class DatabaseHelper {

    // Singleton
    private static DatabaseHelper instance = null;

    protected DatabaseHelper() {
    }

    public static DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    @Transactional
    public List<String> writeJsonSongsToDb(List<SongsJson> songsJson, String userEmail) {
        List<String> updatedSongs = new ArrayList<>();
        for (SongsJson song : songsJson) {
            Song songdb = new Song();
            Song foundSong = null;
            // don't set local ids, but only master ids
            // songdb.setId(song.getSongId());
            if (song.getMasterId() != null) {
                songdb.setId(IdHelper.getNextAvailableSongId(song.getSongName()));
                foundSong = Song.getByTmpId(song.getMasterId());
            }
            boolean shouldUpdateSong = false;
            Logger.trace("PlaySongDatabase : Checking if songs is already in db: " + song.getSongId() + " : " + song.getSongName());

            if (foundSong != null) {
                if (foundSong.getDateModified().getTime() < song.getDateModified()) {
                    // UPDATE SONG
                    shouldUpdateSong = true;
                } else {
                    Logger.trace("PlaySongDatabase : Song in db already up to date: " + song.getSongId() + " : " + song.getSongName());
                    continue;
                }
            } else {
                // THIS IS NEW SONG SCENARIO
                Logger.trace("PlaySongDatabase : Adding new song to db = " + song.getSongId() + " : " + song.getSongName());
            }

            // songdb.setId(song.getSongId());
            songdb.setSongName(song.getSongName());
            songdb.setSongOriginalTitle(song.getSongOriginalTitle());
            songdb.setSongLink(song.getSongLink());
            songdb.setSongImporter(song.getSongImporter());
            songdb.setSongAuthor(song.getSongAuthor());
            songdb.setSongLink(song.getSongLink());
            songdb.setDateCreated(new Date(song.getDateCreated()));
            songdb.setDateModified(new Date(song.getDateModified()));
            songdb.setPrivateSong(song.getPrivateSong());

            // temp id used during song migration
            songdb.setTmpId(song.getMasterId());

            // song
            if (shouldUpdateSong) {
                songdb.update();
            } else {
                songdb.setSyncId(IdHelper.getNextAvailableSyncId());
                songdb.save();
            }

            // now updating songbook
            if (!song.getSongBooks().isEmpty()) {
                Logger.trace("PlaySongDatabase : updating songbook");
                // TODO: later implement multiple songbooks
                // TODO: workaround to support currently writing only to default songbook
                // songdb.setSongBookName(song.getSongBooks().get(0).getSongBookName());
                // songdb.setSongBookId(song.getSongBooks().get(0).getId());
                songdb.setSongBookName(SongBook.DEFAULT_SONGBOOK_NAME);
                songdb.setSongBookId(SongBook.DEFAULT_SONGBOOK_ID);
                songdb.setPrivateSongBook(song.getSongBooks().get(0).getPrivateSongbook());
            }

            // I have to create dummy song lyrics
            // songdb.setSongLyrics(new ArrayList <SongLyrics>());

            Song.updateOrCreateSong(songdb, userEmail);

            updatedSongs.add(song.getSongId());
        }
        return updatedSongs;
    }

    public void writeJsonSongLyricsToDb(List<SongLyricsJson> songLyricsJson, List<String> updatedSongs) {
        // TODO: IMPLEMENT OPTIONAL DELETION OF MISSING REMOTE SONGS
        for (SongLyricsJson songlyrics : songLyricsJson) {
            SongLyrics songlyricsdb = new SongLyrics();
            SongLyrics foundSongLyrics = null;

            foundSongLyrics = SongLyrics.get(songlyrics.getSongLyricsId());

            Logger.trace("PlaySongDatabase : Checking if song lyrics is already in db: " + songlyrics.getSongLyricsId() + " : " + songlyrics.getSongId());

            boolean shouldUpdateSong = false;
            /*
             * boolean songUpdated = updatedSongs.contains(songlyrics.getSongId()); // UPDATE SONG LYRICS IF IT IS PRESENT AND SONG WAS UPDATED ALREADY if (foundSongLyrics != null &&
             * songUpdated) { Logger.trace("PlaySongDatabase : Updating songlyrics (id) to db = " + songlyrics.getSongLyricsId()); shouldUpdateSong = true; } else if (!songUpdated) {
             * Logger.trace("PlaySongDatabase : Song Lyrics up to date: " + songlyrics.getSongLyricsId()); continue; }
             */

            // SONG IS MODIFIED BUT DID NOT FIND ASSOCIATED LYRICS
            Logger.trace("PlaySongDatabase : Adding new songlyrics (id) to db = " + songlyrics.getSongLyricsId());

            Song song = Song.getByTmpId(songlyrics.getSongId());
            // DELETE PREVIOUS LOCAL SONG LYRICS IF REMOTE SONG HAS ONLY 1 SONG
            // LYRICS - THIS I SFOR SAVE
            if (!shouldUpdateSong && song != null && song.getSongLyrics().size() < 2) {
                Logger.trace("PlaySongDatabase : Deleting stale songlyrics!");
                SongLyrics.deleteSongLyricsForSong(song);
            }

            // fill up data
            songlyricsdb.setSongKey(songlyrics.getSongKey());
            songlyricsdb.setSongLyrics(songlyrics.getSongLyrics());

            // i won't save lyrics that are not associated with song
            if (song != null) {
                songlyricsdb.setSong(song);
                songlyricsdb.setId(IdHelper.getNextAvailableSongLyricsId(song.songName));
                if (shouldUpdateSong) {
                    songlyricsdb.update();
                } else {
                    songlyricsdb.save();
                }
            } else {
                Logger.trace("PlaySongDatabase : Could not find song for songlyrics!!! Song ID: " + songlyrics.getSongId());
            }
        }
    }

    public void writeJsonSongbooksToDb(List<SongBookJson> SongbookJson) {
        for (SongBookJson songbook : SongbookJson) {
            Logger.trace("PlaySongDatabase : Trying to writes json songbook to db: " + songbook.getSongBookName());
            SongBook.updateOrCreate(songbook.getId(), songbook.getSongBookName(), songbook.getSongbookOwner(), songbook.getPrivateSongbook());
        }
    }

    public List<String> writeJsonFavoritesSongsToDb(List<PlaylistJson> playlistsJson) {
        List<String> updatedPlaylists = new ArrayList<>();
        Logger.trace("PlaySongDatabase : Trying to writes json playlists to db");
        for (PlaylistJson playlist : playlistsJson) {
            Playlist playlistDb = new Playlist();
            Playlist foundPlaylist = null;
            // don't set local ids, but only master ids
            // favoriteDb.setId(favorite.getId());
            if (playlist.getId() != null) {
                playlistDb.setId(playlist.getId());
                foundPlaylist = Playlist.get(playlist.getId());
            } else {
                // TODO: remove this temp workaround - because I currently don't have master id in db
                playlistDb.setId(playlist.getId());
            }

            // first Checking if service already imported
            Logger.trace("PlaySongDatabase : Checking if playlist is already in db: " + playlist.getSongBookName());
            if (foundPlaylist != null) {
                if (foundPlaylist.getDateCreated().getTime() < playlist.getDateCreated().longValue()) {
                    // UPDATE SERVICE
                } else {
                    Logger.trace("PlaySongDatabase : Service in db already up to date");
                    continue;
                }
            } else {
                // THIS IS NEW SONG SCENARIO
                Logger.trace("PlaySongDatabase : Adding new service to db = " + playlist.getSongBookName());
            }

            Logger.trace("PlaySongDatabase : Processing json service");

            // Now filling up with data
            playlistDb.setDateCreated(new Date(playlist.getDateCreated()));
            playlistDb.setPlayListName(playlist.getSongBookName());
            playlistDb.setUserEmail(playlist.getUserEmail());
            playlistDb.setUserName(playlist.getUserName());
            playlistDb.save();
            Logger.trace("PlaySongDatabase : Saving service");
            for (ServiceSongJson serviceSongJson : playlist.getServiceSongJsons()) {
                Logger.trace("PlaySongDatabase : Processing " + "");
                PlaylistSong playlistSong = new PlaylistSong();
                Playlist playlistMatch = null;

                // don't set local ids, but only master ids
                // favoriteDb.setId(favorite.getId());
                if (playlistSong.getId() != null) {
                    playlistDb.setId(playlistSong.getId());
                    playlistMatch = Playlist.get(playlist.getId());
                } else {
                    // TODO: remove this temp workaround - because I currently don't have master id in db
                    playlistMatch = Playlist.get(playlist.getId());
                    playlistSong.setId(playlist.getId());
                }

                playlistSong.setLyricsId(serviceSongJson.getLyricsId());
                playlistSong.setSongId(serviceSongJson.getSongId());
                playlistSong.setSongName(serviceSongJson.getSongName());
                playlistSong.setSongKey(serviceSongJson.getSongKey());
                playlistSong.setSongLyrics(serviceSongJson.getSongLyrics());
                // TODO: fix this search by master id
                if (playlistMatch != null) {
                    playlistSong.setPlaylist(playlistMatch);
                }
                playlistSong.save();
                Logger.trace("PlaySongDatabase : Saving playlist song");
            }
            updatedPlaylists.add(playlist.getId());
        }
        return updatedPlaylists;
    }

}
