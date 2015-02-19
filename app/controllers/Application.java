package controllers;

import models.Task;
import models.Song;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.songs;
import views.html.song;

public class Application extends Controller {

    // this is for validation constraints
    static Form<Task> taskForm = Form.form(Task.class);
    /*
    public static Result index() {
        return redirect(routes.Application.tasks());
    }
    */
    public static Result tasks() {
        //Logger.debug("Here I am trying to get all tasks" + Task.all());
        return ok(index.render(Task.all(), taskForm));
    }

    public static Result newTask() {
        Form<Task> filledForm = taskForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(
                    views.html.index.render(Task.all(), filledForm)
            );
        } else {
            Task.create(filledForm.get());
            return redirect(routes.Application.tasks());
        }
    }

    public static Result deleteTask(Long id) {
        Task.delete(id);
        return redirect(routes.Application.tasks());
    }


    // SONGS IMPLEMENTATION

    public static Result index() {
        return redirect(routes.Application.songs());
    }

    static Form<Song> songForm = Form.form(Song.class);

    public static Result songs() {
        return ok(songs.render(Song.all(), songForm));
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

}
