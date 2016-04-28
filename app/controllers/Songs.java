package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.node.ObjectNode;

import database.SqlQueries;
import models.Song;
import models.SongBook;
import models.UserAccount;
import models.helpers.ArrayHelper;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import views.html.songs;

public class Songs extends Controller {

    private Form<Song> songForm;
    private DynamicForm dynamicForm;

    @Inject
    public Songs(FormFactory formFactory) {
        this.songForm = formFactory.form(Song.class);
        this.dynamicForm = formFactory.form();
    }

    @Security.Authenticated(Secured.class)
    public Result getsongs() {
        return ok(Json.toJson(Song.all()));
    }

    @Security.Authenticated(Secured.class)
    public Result deletesong(String id) {
        Logger.debug("Deleting song by id: " + id);
        UserAccount user = getUserFromCookie();
        deleteSong(id);
        SongBook.staleSongbookCleanup(user.getEmail());
        return redirect(controllers.routes.Application.table());
    }

    // Helper method to execute transaction
    @Transactional
    private void deleteSong(String id) {
        Song.deleteById(id);
    }

    // TODO: remove this body parser workaround when Bug gets fixed https://github.com/playframework/playframework/issues/5919
    @Security.Authenticated(Secured.class)
    @With(VerboseAction.class)
    public Result updateorcreatesong() {
        Form<Song> filledForm = songForm.bindFromRequest();
        // filledForm.
        if (filledForm.hasErrors()) {
            return badRequest(views.html.error.render());
        } else {
            UserAccount user = getUserFromCookie();
            String userName = UserAccount.getNameFromEmail(user.getEmail());
            Song updatedSong = filledForm.get();
            updatedSong.setSongLastModifiedBy(userName);

            Logger.debug("Update or create song");
            updateOrCreateSong(updatedSong, user);

            Logger.debug("Removing stale songbook references, if they exist");
            SongBook.staleSongbookCleanup(user.getEmail());
            return redirect(controllers.routes.Application.table());
        }
    }

    // Helper method to execute transaction
    @Transactional
    private void updateOrCreateSong(Song updatedSong, UserAccount user) {
        Song.updateOrCreateSong(updatedSong, user.getEmail());
    }

    public Result songs() {
        return ok(songs.render(Song.all()));
    }

    public Result songsuggestions() {
        Map<String, String[]> params = request().queryString();
        String filter = params.get("q")[0].toLowerCase();
        // Logger.trace("Quick search song suggestion filter: " + filter);
        //@formatter:off
        List<SqlRow> result = Ebean
                .createSqlQuery(""
                        + "("
                        + SqlQueries.sqlSelectSongId
                        + SqlQueries.sqlFromSong
                        + " WHERE lower(t0.song_name) like :songnamefilter) UNION ALL" 
                        + "("
                        + SqlQueries.sqlSelectSongId
                        + SqlQueries.sqlFromSong
                        + " WHERE lower(t0.song_name) like :songnameinlinefilter) "
                        + "UNION ALL" 
                        + "("
                        + SqlQueries.sqlSelectSongId
                        + SqlQueries.sqlFromSong
                        + " JOIN song_lyrics u1 on u1.song_id = t0.id"
                        + " WHERE lower(u1.song_lyrics) like :songlyricsfilter) "
                        + "UNION ALL" 
                        + "("
                        + SqlQueries.sqlSelectSongId
                        + SqlQueries.sqlFromSong
                        + " WHERE lower(t0.song_author) like :songauthorfilter)")
                .setParameter("songnamefilter", filter + "%")
                .setParameter("songnameinlinefilter", "%" + filter + "%")
                .setParameter("songlyricsfilter", "%" + filter + "%")
                .setParameter("songauthorfilter", "%" + filter + "%")
                .findList();
        //@formatter:on
        ArrayList<String> ids = new ArrayList<>();
        // TODO: Get song name through this sql query
        for (SqlRow res : result) {
            ids.add(res.getString("id"));
        }
        ids = ArrayHelper.removeDuplicatesFromStrings(ids);
        List<ObjectNode> songSuggestions = new ArrayList<ObjectNode>();

        for (String id : ids) {
            ObjectNode songSuggestion = Json.newObject();
            songSuggestion.put("key", id);
            songSuggestion.put("value", Song.get(id).getSongName());
            songSuggestions.add(songSuggestion);
        }

        return ok(Json.toJson(songSuggestions));
    }

    public UserAccount getUserFromCookie() {
        UserAccount user = null;
        if (request().cookies().get("PLAY_SESSION") != null) {
            String cookieVal = request().cookies().get("PLAY_SESSION").value();
            String userId = cookieVal.substring(cookieVal.indexOf("email=") + 6).replace("%40", "@");

            if (userId != null) {
                user = UserAccount.find.byId(userId);
            }
        }
        if (user == null) {
            // Logger.debug("Using guest session");
            user = new UserAccount("Guest", "", "");
        }
        return user;
    }

}
