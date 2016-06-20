package controllers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import models.Playlist;
import models.Song;
import models.SongBook;
import models.SongLyrics;
import models.helpers.SongToJsonConverter;
import play.Logger;
import play.cache.CacheApi;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.Configuration;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Rest extends Controller {

    public static final String SONGS_JSON_CACHE_NAME = ".songs.json.cache.data";
    public static final String PLAYLISTS_JSON_CACHE_NAME = ".playlists.json.cache.data";

    boolean cachingFeature;
    @Inject
    CacheApi cache;

    private DynamicForm dynamicForm;

    @Inject
    public Rest(FormFactory formFactory, Configuration configuration) {
        this.dynamicForm = formFactory.form();
        cachingFeature = configuration.underlying().getBoolean("playsong.songtable.caching.enabled");
    }

    // @Security.Authenticated(Secured.class)
    public Result getsongdata() {
        List<Song> songs = null;
        ArrayList<ObjectNode> songsJson = null;

        // caching is turned on
        if (cachingFeature) {
            songsJson = cache.get(SONGS_JSON_CACHE_NAME);
        }

        // if song lyrics json still null - while cache is off, or empty - refetch cache
        if (songsJson == null) {
            songs = Song.all();
            songsJson = new ArrayList<>();

            for (Song s : songs) {
                ObjectNode songJson = SongToJsonConverter.convert(s);
                songsJson.add(songJson);
            }

            if (cachingFeature) {
                cache.set(SONGS_JSON_CACHE_NAME, songsJson);
                Logger.debug("Cache not available, creating songs json cache");
            }
        } else {
            Logger.debug("Cache available, using songs json cache");
        }

        return ok(Json.toJson(songsJson));
    }

    // @Security.Authenticated(Secured.class)
    public Result getsonglyricsdata() {
        List<SongLyrics> songlyrics = SongLyrics.all();

        ArrayList<ObjectNode> songlyricsJson = new ArrayList<>();

        for (SongLyrics sl : songlyrics) {
            ObjectNode songLyricsJson = SongToJsonConverter.convertLyrics(sl);
            songlyricsJson.add(songLyricsJson);
        }
        return ok(Json.toJson(songlyricsJson));
    }

    public Result getplaylistdata() {
        List<Playlist> playlists = null;

        // caching is turned on
        if (cachingFeature) {
            playlists = cache.get(PLAYLISTS_JSON_CACHE_NAME);
        }
        
        // if song lyrics json still null - while cache is off, or empty - refetch cache
        if (playlists == null) {
            playlists = Playlist.all();
            if (cachingFeature) {
                cache.set(PLAYLISTS_JSON_CACHE_NAME, playlists);
                Logger.debug("Cache not available, creating playlists json cache");
            }
        } else {
            Logger.debug("Cache available, using playlists json cache");
        }

        return ok(Json.toJson(SongToJsonConverter.convert(playlists)));
    }

    public Result getsongbooksdata() {
        return ok(Json.toJson(SongBook.all()));
    }

    public Result getsongjson(String id) {
        Song s = Song.get(id);
        ObjectNode songJson = SongToJsonConverter.convert(s);
        return ok(Json.toJson(songJson));
    }

    public Result getsonglyricsjson(String id) {
        SongLyrics lyricsObject = SongLyrics.get(id);
        if (lyricsObject != null) {
            String lyrics = lyricsObject.getSongLyrics();
            ObjectNode lyricsResult = Json.newObject();
            lyricsResult.put("songLyrics", lyrics);
            return ok(Json.toJson(lyricsResult));
        }
        return ok(Json.toJson(""));
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public Result updatesonglyricsjson(String id) {
        Logger.debug("Updating lyrics by id: " + id);
        SongLyrics lyricsObject = SongLyrics.get(id);
        DynamicForm df = dynamicForm.bindFromRequest();
        String songLyrics = df.get("songLyrics");
        lyricsObject.setSongLyrics(songLyrics);
        lyricsObject.updateSongLyrics();
        clearSongTableCache();
        clearSongJsonCache();
        return ok();
    }
    
    private void clearSongTableCache() {
        if (cachingFeature) {
            Logger.debug("Clearing songbooks table data cache");
            for (String songbookId : SongBook.getAllSongbookIds()) {
                Logger.debug("Clearing songbook cache: " + songbookId);
                cache.set(songbookId + SongBook.SONGBOOK_TABLE_CACHE_NAME, null, 0);
            }
        }
    }

    private void clearSongJsonCache() {
        if (cachingFeature) {
            Logger.debug("Clearing song json data cache");
            cache.set(Rest.SONGS_JSON_CACHE_NAME, null, 0);
        }
    }
}
