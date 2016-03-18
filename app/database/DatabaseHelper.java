package database;

import rest.json.ServiceJson;
import rest.json.ServiceSongJson;
import rest.json.SongLyricsJson;
import rest.json.SongBookJson;
import rest.json.SongsJson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Service;
import models.ServiceSong;
import models.Song;
import models.SongBook;
import models.SongLyrics;
import models.UserAccount;
import play.Logger;
import play.db.ebean.Transactional;

public class DatabaseHelper {

    public void writeJsonSongLyricsToDb(List<SongLyricsJson> songLyricsJson, List<Long> updatedSongs) {
        // TODO: IMPLEMENT OPTIONAL DELETION OF MISSING REMOTE SONGS
        for (SongLyricsJson songlyrics : songLyricsJson) {
            SongLyrics songlyricsdb = new SongLyrics();
            SongLyrics foundSongLyrics = null;

            songlyricsdb.setMasterId(songlyrics.getSongLyricsId());
            foundSongLyrics = SongLyrics.getByMasterId(songlyrics.getSongLyricsId());
            
            Logger.trace("PlaySongDatabase : Checking if song lyrics is already in db: " + songlyrics.getSongLyricsId() + " : " + songlyrics.getSongId());

            boolean shouldUpdateSong = false;
            boolean songUpdated = updatedSongs.contains(songlyrics.getSongId());
            // UPDATE SONG LYRICS IF IT IS PRESENT AND SONG WAS UPDATED ALREADY
            if (foundSongLyrics != null && songUpdated) {
                Logger.trace("PlaySongDatabase : Updating songlyrics (id) to db = " + songlyrics.getSongLyricsId());
                shouldUpdateSong = true;
            } else if (!songUpdated) {
                Logger.trace("PlaySongDatabase : Song Lyrics up to date: " + songlyrics.getSongLyricsId());
                continue;
            }
            // SONG IS MODIFIED BUT DID NOT FIND ASSOCIATED LYRICS
            Logger.trace("PlaySongDatabase : Adding new songlyrics (id) to db = " + songlyrics.getSongLyricsId());

            Song song = Song.getByMasterId(songlyrics.getSongId());
            // DELETE PREVIOUS LOCAL SONG LYRICS IF REMOTE SONG HAS ONLY 1 SONG
            // LYRICS - THIS I SFOR SAVE
            if (!shouldUpdateSong && song != null && song.getSongLyrics().size() < 2) {
                Logger.trace("PlaySongDatabase : Deleting stale songlyrics!");
                SongLyrics.deleteSongLyricsForSong(song);
            }

            // fill up data
            songlyricsdb.setSongKey(songlyrics.getSongKey());
            songlyricsdb.setsongLyrics(songlyrics.getSongLyrics());

            if (song != null) {
                songlyricsdb.setSong(song);
            } else {
                Logger.trace("PlaySongDatabase : Could not find song for songlyrics!!! Song ID: " + songlyrics.getSongId());
            }
            if (shouldUpdateSong) {
                songlyricsdb.update();
            } else {
                songlyricsdb.save();
            }
        }
    }

    @Transactional
    public List<Long> writeJsonSongsToDb(List<SongsJson> songsJson, String userEmail) {
        List<Long> updatedSongs = new ArrayList<>();
        for (SongsJson song : songsJson) {
            Song songdb = new Song();
            Song foundSong = null;
            // don't set local ids, but only master ids
            // songdb.setId(song.getSongId());
            if (song.getMasterId() != null) {
                songdb.setMasterId(song.getMasterId());
                foundSong = Song.getByMasterId(song.getMasterId());
            }
            boolean shouldUpdateSong = false;
            Logger.trace("PlaySongDatabase : Checking if songs is already in db: " + song.getMasterId() + " : " + song.getSongName());

            if (foundSong != null) {
                if (foundSong.getDateModified().getTime() < song.getDateModified()) {
                    // UPDATE SONG
                    shouldUpdateSong = true;
                } else {
                    Logger.trace("PlaySongDatabase : Song in db already up to date: " + song.getMasterId() + " : " + song.getSongName());
                    continue;
                }
            } else {
                // THIS IS NEW SONG SCENARIO
                Logger.trace("PlaySongDatabase : Adding new song to db = " + song.getMasterId() + " : " + song.getSongName());
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

            // song
            if (shouldUpdateSong) {
                songdb.update();
            } else {
                songdb.save();
            }

            // now updating songbook
            if (!song.getSongBooks().isEmpty()) {
                Logger.trace("PlaySongDatabase : updating songbook");
                // TODO: later implement multiple songbooks
                songdb.setSongBookName(song.getSongBooks().get(0).getSongBookName());
                songdb.setSongBookmasterId(song.getSongBooks().get(0).getMasterId());
                songdb.setPrivateSongBook(song.getSongBooks().get(0).getPrivateSongbook());
            }

            // I have to create dummy song lyrics
            // songdb.setSongLyrics(new ArrayList <SongLyrics>());

            Song.updateOrCreateSong(songdb, userEmail);

            updatedSongs.add(song.getMasterId());
        }
        return updatedSongs;
    }

    public void writeJsonSongbooksToDb(List<SongBookJson> SongbookJson) {
        for (SongBookJson songbook : SongbookJson) {
            Logger.trace("PlaySongDatabase : Trying to writes json songbook to db: " + songbook.getSongBookName());
            SongBook.updateOrCreate(songbook.getId(), songbook.getMasterId(), songbook.getSongBookName(), songbook.getSongbookOwner(), songbook.getPrivateSongbook());
        }
    }

    public List<Long> writeJsonFavoritesSongsToDb(List<ServiceJson> servicesJson) {
        List<Long> updatedFavorites = new ArrayList<>();
        Logger.trace("PlaySongDatabase : Trying to writes json favorites to db");
        for (ServiceJson favorite : servicesJson) {
            Service favoriteDb = new Service();
            Service foundService = null;
            // don't set local ids, but only master ids
            // favoriteDb.setId(favorite.getId());
            if (favorite.getMasterId() != null) {
                favoriteDb.setMasterId(favorite.getMasterId());
                foundService = Service.getByMasterId(favorite.getMasterId());
            } else {
                // TODO: remove this temp workaround - because I currently don't have master id in db
                favoriteDb.setMasterId(favorite.getId());
            }

            // first Checking if service already imported
            Logger.trace("PlaySongDatabase : Checking if service is already in db: " + favorite.getSongBookName());
            if (foundService != null) {
                if (foundService.getDateCreated().getTime() < favorite.getDateCreated().longValue()) {
                    // UPDATE SERVICE
                } else {
                    Logger.trace("PlaySongDatabase : Service in db already up to date");
                    continue;
                }
            } else {
                // THIS IS NEW SONG SCENARIO
                Logger.trace("PlaySongDatabase : Adding new service to db = " + favorite.getSongBookName());
            }

            Logger.trace("PlaySongDatabase : Processing json service");

            // Now filling up with data
            favoriteDb.setDateCreated(new Date(favorite.getDateCreated()));
            favoriteDb.setServiceName(favorite.getSongBookName());
            favoriteDb.setUserEmail(favorite.getUserEmail());
            favoriteDb.setUserName(favorite.getUserName());
            favoriteDb.save();
            Logger.trace("PlaySongDatabase : Saving service");
            for (ServiceSongJson serviceSongJson : favorite.getServiceSongJsons()) {
                Logger.trace("PlaySongDatabase : Processing " + "");
                ServiceSong favoriteSong = new ServiceSong();
                Service favoriteMatch = null;

                // don't set local ids, but only master ids
                // favoriteDb.setId(favorite.getId());
                if (favoriteSong.getMasterId() != null) {
                    favoriteDb.setMasterId(favoriteSong.getMasterId());
                    favoriteMatch = Service.getByMasterId(favorite.getMasterId());
                } else {
                    // TODO: remove this temp workaround - because I currently don't have master id in db
                    favoriteMatch = Service.getByMasterId(favorite.getId());
                    favoriteSong.setMasterId(favorite.getId());
                }

                favoriteSong.setLyricsId(serviceSongJson.getLyricsId());
                favoriteSong.setSongId(serviceSongJson.getSongId());
                favoriteSong.setSongName(serviceSongJson.getSongName());
                favoriteSong.setSongKey(serviceSongJson.getSongKey());
                favoriteSong.setSongLyrics(serviceSongJson.getSongLyrics());
                // TODO: fix this search by master id
                if (favoriteMatch != null) {
                    favoriteSong.setService(favoriteMatch);
                }
                favoriteSong.save();
                Logger.trace("PlaySongDatabase : Saving service song");
            }
            updatedFavorites.add(favorite.getMasterId());
        }
        return updatedFavorites;
    }
}
