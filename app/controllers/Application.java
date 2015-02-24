package controllers;

import models.Song;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.songs;
import views.html.song;
import views.html.songtable;

public class Application extends Controller {

   // SONGS IMPLEMENTATION

    public static Result index() {
        return redirect(routes.Application.songs());
    }

    static Form<Song> songForm = Form.form(Song.class);

    public static Result songs() {
        return ok(songs.render(Song.all(), songForm));
    }

    public static Result songTable() {
        return ok(songtable.render(Song.all()));
    }

    public static Result getSongs() {
        return ok(Json.toJson(Song.all()));
    }

    public static Result getSong(Long id) {
        return ok(song.render(Song.getSong(id)));
    }

    public static Result deleteSong(Long id) {
        Song.delete(id);
        return redirect(routes.Application.songs());
    }

    public static Result newSong() {
        Form<Song> filledForm = songForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(
                    views.html.songs.render(Song.all(), filledForm)
            );
        } else {
            Song.create(filledForm.get());
            return redirect(routes.Application.songs());
        }
    }

    public static Result init(){
        try {
            //SongImporter.exportFolder();
            SongImporter.importFromDb();
        }
        catch (Exception e){
            Logger.error("Exception occured during init" + e.getStackTrace());
            e.printStackTrace();
            System.out.print(e.getStackTrace());
            System.out.print(e.getMessage());
        }
        return redirect(routes.Application.songs());
    }

}
