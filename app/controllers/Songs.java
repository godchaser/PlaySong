package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import models.Song;
import models.SongBook;
import models.UserAccount;
import models.helpers.ArrayHelper;
import models.helpers.PdfPrintable;
import models.helpers.SongPrint;
import play.Configuration;
import play.Logger;
import play.cache.CacheApi;
import play.data.Form;
import play.data.FormFactory;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import chord.tools.LineTypeChecker;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.node.ObjectNode;

import database.SqlQueries;
import document.tools.PdfGenerator;
import mail.NoticationMailerConfig;
import views.html.songs;;

public class Songs extends Controller {

    boolean cachingFeature;
    @Inject
    CacheApi cache;

    boolean notificationMailerFeature = false;

    private Form<Song> songForm;

    @Inject
    public Songs(FormFactory formFactory, Configuration configuration) {
        this.songForm = formFactory.form(Song.class);
        cachingFeature = configuration.underlying().getBoolean("playsong.songtable.caching.enabled");
        notificationMailerFeature = configuration.underlying().getBoolean("playsong.notification-mailer.enabled");
        if (notificationMailerFeature) {
            NoticationMailerConfig.setNotification_mailer_username(configuration.underlying().getString(("playsong.notification-mailer.username")));
            NoticationMailerConfig.setNotification_mailer_password(configuration.underlying().getString(("playsong.notification-mailer.password")));
            NoticationMailerConfig.setNotification_mailer_smtp(configuration.underlying().getString(("playsong.notification-mailer.smtp")));
            NoticationMailerConfig.setNotification_mailer_port(configuration.underlying().getString(("playsong.notification-mailer.port")));
            NoticationMailerConfig.setNotification_mailer_recipient(configuration.underlying().getString(("playsong.notification-mailer.recipient")));
        }
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
        clearSongTableCache();
        clearSongJsonCache();
        return redirect(controllers.routes.Application.table());
    }

    // Helper method to execute transaction
    @Transactional
    private void deleteSong(String id) {
        Song.deleteById(id);
    }

    @Security.Authenticated(Secured.class)
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
            String songId = updateOrCreateSong(updatedSong, user);
            Logger.debug("Removing stale songbook references, if they exist");
            SongBook.staleSongbookCleanup(user.getEmail());
            clearSongTableCache();
            clearSongJsonCache();
            return redirect(controllers.routes.Application.table());
        }
    }

    // Helper method to execute transaction
    @Transactional
    private String updateOrCreateSong(Song updatedSong, UserAccount user) {
        return Song.updateOrCreateSong(updatedSong, user.getEmail());
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

    public Result printSong(String id) {
        boolean excludeChords = false;
        boolean useColumns = true;
        boolean excludePageOfContent = true;

        Map<String, String[]> params = request().queryString();
        excludeChords = Boolean.parseBoolean(params.get("excludeChords")[0].toLowerCase());
        Song song = Song.get(id);

        if (song == null) {
            return notFound("<h1>Song not found</h1>").as("text/html");
        }

        if (excludeChords) {
            String lyricsWithoutChords = song.getSongLyrics().get(0).getSongLyrics();
            lyricsWithoutChords = LineTypeChecker.removeChordLines(lyricsWithoutChords);

        }

        ArrayList<PdfPrintable> songPrintList = new ArrayList<PdfPrintable>();
        songPrintList.add(new SongPrint(Song.get(song.getId()), song.getSongLyrics().get(0).getId(), song.getSongLyrics().get(0).getSongKey(), excludeChords));

        String outputPdfPath = "resources/pdf/" + song.getId() + ".pdf";
        try {
            Logger.debug("Writing PDF: " + outputPdfPath);
            PdfGenerator.writeListContent(outputPdfPath, songPrintList, useColumns, excludePageOfContent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        File tmpFile = new File(outputPdfPath);
        response().setHeader(CONTENT_TYPE, "application/pdf");

        Logger.debug("File: " + tmpFile.getAbsolutePath());
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(tmpFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        response().setHeader("Content-disposition", "attachment;filename=" + tmpFile.getName());

        return ok(fin);
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
