package database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public void writeJsonSongsToDb2(List<SongsJson> songsJson, String userEmail) {
        int currentSongNumber = 0;
        int numberOfSongs = songsJson.size();
        long startTime = System.currentTimeMillis();
       
        try {
            for (SongsJson song : songsJson) {
                currentSongNumber++;
                Logger.trace("Saving  song: " + currentSongNumber + "/" + numberOfSongs);
                boolean shouldUpdateSong = false;
                Song foundSong = Song.get(song.getId());
                if (foundSong != null) {
                    if (foundSong.getDateModified().getTime() < song.getDateModified()) {
                        // UPDATE SONG
                        shouldUpdateSong = true;
                    } else {
                      Logger.trace("Song in db already up to date: " + song.getId() + " : " +
                                song.getSongName());
                        continue;
                    }
                } else {
                    // NEW SONG SCENARIO
                }
                // TODO: CHECK FOR NULLS FIRST
                Song songdb = new Song();
                songdb.setId(song.getId());
                songdb.setSongName(song.getSongName());
                songdb.setSongOriginalTitle(song.getSongOriginalTitle());
                songdb.setSongLink(song.getSongLink());
                songdb.setSongImporter(song.getSongImporter());
                songdb.setSongAuthor(song.getSongAuthor());
                //songdb.setDateCreated(song.getDateCreated());
                //songdb.setDateModified(song.getDateModified());

                SongBook existingSongBook = SongBook.get(song.getSongbooks().get
                        (0).getId());
                // if songbook already exists add new song to songbook - later I should check if
                // maybe the private flag is changed?
                //TODO: private flag update check!
                if (existingSongBook != null) {
                    // songbook found
                    //songdb.associateSongDb(existingSongBook);
                    songdb.setSongBook(existingSongBook, userEmail);
                } else {
                    // new songbook
                    Logger.trace("Creating new songbook: " + song.getSongbooks().get(0)
                            .getSongBookName());
                    existingSongBook = new SongBook();
                    existingSongBook.setId(song.getSongbooks().get(0).getId());
                    existingSongBook.setSongBookName(song.getSongbooks().get(0).getSongBookName());
                    existingSongBook.setPrivateSongbook(song.getSongbooks().get(0)
                            .getPrivateSongbook());
                    existingSongBook.save();
                }

               // songdb.associateSongDb(existingSongBook);

                if (shouldUpdateSong) {
                    songdb.update();
                } else {
                    songdb.save();
                }

                for (SongLyricsJson songlyrics : song.getSongLyrics()) {
                    // TODO: CHECK FOR NULLS FIRST
                    SongLyrics songlyricsdb = new SongLyrics();
                    songlyricsdb.setId(songlyrics.getSongLyricsId());
                    songlyricsdb.setSongKey(songlyrics.getSongKey());
                    songlyricsdb.setSongLyrics(songlyrics.getSongLyrics());
                    songlyricsdb.setSongLyrics(songlyrics
                            .getSongLyricsAndroidChordsHtml());
                //    songlyricsdb.setSongLyricsHtml(songlyrics.getSongLyricsAndroidHtml());
                    songlyricsdb.setSong(songdb);
                   // songlyricsdb.associateSongDb(songdb);
                    if (shouldUpdateSong) {
                        songlyricsdb.update();
                    } else {
                        songlyricsdb.save();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.trace("Error during song database import: " + e.getMessage());
        } finally {
            Logger.trace("Closing db transaction");
        }
        long difference = System.currentTimeMillis() - startTime;
    }

    public void writeJsonSongLyricsToDb(List<SongLyricsJson> songLyricsJson, List<String> updatedSongs) {
        // TODO: IMPLEMENT OPTIONAL DELETION OF MISSING REMOTE SONGS
        for (SongLyricsJson songlyrics : songLyricsJson) {
            SongLyrics songlyricsdb = new SongLyrics();
            Logger.trace("PlaySongDatabase : Checking if song lyrics is already in db: " + songlyrics.getSongLyricsId() + " : " + songlyrics.getId());

            boolean shouldUpdateSong = false;
            /*
             * boolean songUpdated = updatedSongs.contains(songlyrics.getSongId()); // UPDATE SONG LYRICS IF IT IS PRESENT AND SONG WAS UPDATED ALREADY if (foundSongLyrics != null &&
             * songUpdated) { Logger.trace("PlaySongDatabase : Updating songlyrics (id) to db = " + songlyrics.getSongLyricsId()); shouldUpdateSong = true; } else if (!songUpdated) {
             * Logger.trace("PlaySongDatabase : Song Lyrics up to date: " + songlyrics.getSongLyricsId()); continue; }
             */

            // SONG IS MODIFIED BUT DID NOT FIND ASSOCIATED LYRICS
            Logger.trace("PlaySongDatabase : Adding new songlyrics (id) to db = " + songlyrics.getSongLyricsId());

            //Song song = Song.getByTmpId(songlyrics.getSongId());
            Song song = Song.get(songlyrics.getSongLyricsId());
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
                Logger.trace("PlaySongDatabase : Could not find song for songlyrics!!! Song ID: " + songlyrics.getId());
            }
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
