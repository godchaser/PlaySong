package database;

import rest.json.ServiceJson;
import rest.json.ServiceSongJson;
import rest.json.SongLyricsJson;
import rest.json.SongsJson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Service;
import models.ServiceSong;
import models.Song;
import models.SongLyrics;
import play.Logger;

public class DatabaseHelper {
    
    public void writeJsonSongLyricsToDb(List<SongLyricsJson> songLyricsJson, List<Long> updatedSongs){
        //TODO: IMPLEMENT OPTIONAL DELETION OF MISSING REMOTE SONGS
        for (SongLyricsJson songlyrics : songLyricsJson) {
            Logger.trace("PlaySongDatabase : Checkin if song lyrics is already in db: " + songlyrics.getSongLyricsId() + " : " + songlyrics.getSongId());
            SongLyrics foundSongLyrics = SongLyrics.get(songlyrics.getSongLyricsId());
            boolean shouldUpdateSong = false;
            boolean songUpdated = updatedSongs.contains(songlyrics.getSongId());
            // UPDATE SONG LYRICS IF IT IS PRESENT AND SONG WAS UPDATED ALREADY
            if (foundSongLyrics!=null && songUpdated){
                Logger.trace("PlaySongDatabase : Updating songlyrics (id) to db = " + songlyrics.getSongLyricsId());
                shouldUpdateSong = true;
            } else if (!songUpdated){
                Logger.trace("PlaySongDatabase : Song Lyrics up to date: " + songlyrics.getSongLyricsId());
                continue;
            }
            // SONG IS MODIFIED BUT DID NOT FIND ASSOCIATED LYRICS
            Logger.trace("PlaySongDatabase : Adding new songlyrics (id) to db = " + songlyrics.getSongLyricsId());

            Song song = Song.get(songlyrics.getSongId());
            // DELETE PREVIOUS LOCAL SONG LYRICS IF REMOTE SONG HAS ONLY 1 SONG LYRICS - THIS I SFOR SAVE
            if (!shouldUpdateSong && song != null && song.getSongLyrics().size()<2){
                Logger.trace("PlaySongDatabase : Deleting stale songlyrics!");         
                SongLyrics.deleteSongLyricsForSong(song);
            }

            // TODO: CHECK FOR NULLS FIRST
            SongLyrics songlyricsdb = new SongLyrics();
            songlyricsdb.setId((songlyrics.getSongLyricsId()));
            songlyricsdb.setSongKey(songlyrics.getSongKey());
            songlyricsdb.setsongLyrics(songlyrics.getSongLyrics());
            //songlyricsdb.setSongLyricsHtml(LyricsHtmlBuilder.buildHtmlFromSongLyrics(songlyrics.getSongLyrics()));
            //.setSongLyricsWithoutChordsHtml(LyricsHtmlBuilder.buildHtmlFromSongLyrics(LineTypeChecker.removeChordLines(songlyrics.getSongLyrics())));
            if (song != null) {
                songlyricsdb.setSong(song);
            } else {
                Logger.trace("PlaySongDatabase : Could not find song for songlyrics!!! Song ID: " + songlyrics.getSongId());
            }
            if (shouldUpdateSong){
                songlyricsdb.update();
            } else {
                songlyricsdb.save();
            }
         }
    }

    public List<Long> writeJsonSongsToDb(List<SongsJson> songsJson){
        List<Long> updatedSongs = new ArrayList<>();
        for (SongsJson song : songsJson){
            boolean shouldUpdateSong = false;
            Logger.trace("PlaySongDatabase : Checkin if songs is already in db: " + song.getSongId() + " : " + song.getSongName());
            Song foundSong = Song.get(song.getSongId());
            if (foundSong!=null){
                if (foundSong.getDateModified().getTime()<song.getDateModified()){
                    //UPDATE SONG
                    shouldUpdateSong= true;
                } else {
                    Logger.trace("PlaySongDatabase : Song in db already up to date: " + song.getSongId() + " : " + song.getSongName());
                    continue;
                }
            } else {
                // THIS IS NEW SONG SCENARIO
                Logger.trace("PlaySongDatabase : Adding new song to db = " + song.getSongId() + " : " + song.getSongName());
            }
            // TODO: CHECK FOR NULLS FIRST
            Song songdb = new Song();
            songdb.setId(song.getSongId());
            songdb.setSongName(song.getSongName());
            songdb.setSongOriginalTitle(song.getSongOriginalTitle());
            songdb.setSongLink(song.getSongLink());
            songdb.setSongImporter(song.getSongImporter());
            songdb.setSongAuthor(song.getSongAuthor());
            songdb.setSongLink(song.getSongLink());
            songdb.setDateCreated(new Date(song.getDateCreated()));
            songdb.setDateModified(new Date(song.getDateModified()));
            if (shouldUpdateSong){
                songdb.update();
            } else {
                songdb.save();
            }
            updatedSongs.add(song.getSongId());
        }
        return updatedSongs;
    }

    public List<Long> writeJsonFavoritesSongsToDb(List<ServiceJson> servicesJson){
        List<Long> updatedFavorites = new ArrayList<>();
        Logger.trace("PlaySongDatabase : Trying to writes json favorites to db");
        for (ServiceJson favorite : servicesJson){
            // first checking if service already imported
            Service foundService =  Service.get(favorite.getId());
            Logger.trace("PlaySongDatabase : Checkin if service is already in db: " + favorite.getSongBookName());
            if (foundService!=null){
                if (foundService.getDateCreated().getTime()<favorite.getDateCreated().longValue()){
                    //UPDATE SERVICE
                } else {
                    Logger.trace("PlaySongDatabase : Service in db already up to date");
                    continue;
                }
            } else {
                // THIS IS NEW SONG SCENARIO
                Logger.trace("PlaySongDatabase : Adding new service to db = " + favorite.getSongBookName());
            }

            Logger.trace("PlaySongDatabase : Processing json service");
            Service favoriteDb = new Service();
            favoriteDb.setId(favorite.getId());
            favoriteDb.setDateCreated(new Date(favorite.getDateCreated()));
            favoriteDb.setServiceName(favorite.getSongBookName());
            favoriteDb.setUserEmail(favorite.getUserEmail());
            favoriteDb.setUserName(favorite.getUserName());
            favoriteDb.save();
            Logger.trace("PlaySongDatabase : Saving service");
            for (ServiceSongJson serviceSongJson : favorite.getServiceSongJsons()) {
                Logger.trace("PlaySongDatabase : Processing json service song");
                ServiceSong favoriteSong = new ServiceSong();
                favoriteSong.setLyricsId(serviceSongJson.getLyricsId());
                favoriteSong.setSongId(serviceSongJson.getSongId());
                favoriteSong.setSongName(serviceSongJson.getSongName());
                favoriteSong.setSongKey(serviceSongJson.getSongKey());
                favoriteSong.setSongLyrics(serviceSongJson.getSongLyrics());
                Service favoriteMatch = Service.get(favorite.getId());
                if (favoriteMatch != null) {
                    favoriteSong.setService(favoriteMatch);
                }
                favoriteSong.save();
                Logger.trace("PlaySongDatabase : Saving service song");
            }
            updatedFavorites.add(favorite.getId());
        }
        return updatedFavorites;
    }
}
