package controllers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Playlist;
import models.Song;
import models.SongBook;
import models.SongLyrics;
import models.helpers.SongToJsonConverter;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

public class Rest extends Controller {
    private DynamicForm dynamicForm;
    
    @Inject
    public Rest(FormFactory formFactory) {
        this.dynamicForm = formFactory.form();
    }

    // @Security.Authenticated(Secured.class)
    public Result getsongdata() {
        List<Song> songs = Song.all();
        ArrayList<ObjectNode> songsJson = new ArrayList<>();

        for (Song s : songs) {
            ObjectNode songJson = SongToJsonConverter.convert(s);
            songsJson.add(songJson);
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
        List<Playlist> playlists = Playlist.all();
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
        String lyrics = lyricsObject.getSongLyrics();
        ObjectNode lyricsResult = Json.newObject();
        lyricsResult.put("songLyrics", lyrics);
        return ok(Json.toJson(lyricsResult));
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
        return ok();
    }

}
