package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import chord.tools.LineTypeChecker;
import database.SqlQueries;
import document.tools.DocxGenerator;
import document.tools.PdfGenerator;
import document.tools.XlsHelper;
import document.tools.XmlSongsParser;
import models.Playlist;
import models.PlaylistSong;
import models.Song;
import models.SongBook;
import models.SongLyrics;
import models.UserAccount;
import models.helpers.ArrayHelper;
import models.helpers.PdfPrintable;
import models.helpers.SongPrint;
import models.helpers.SongTableData;
import models.helpers.SongToJsonConverter;
import models.json.JsonPlaylist;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import play.mvc.BodyParser;
import play.routing.JavaScriptReverseRouter;
import play.data.FormFactory;

import javax.inject.*;
import rest.PlaySongRestService;
import songimporters.SongImporter;
import views.html.admin;
import views.html.login;
import views.html.playlists;
import views.html.playlistmaker;
import views.html.songeditor;
import views.html.songs;
import views.html.songviewer;
import views.html.table;

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
    @BodyParser.Of(FormBodyParser.class)
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
