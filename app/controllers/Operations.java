package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import models.Song;
import models.UserAccount;
import models.helpers.JsonToSongConverter;
import models.helpers.URLParamEncoder;
import play.Logger;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Security;
import rest.PlaySongRestService;
import songimporters.SongImporter;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

import document.tools.DocxGenerator;
import document.tools.XlsHelper;
import document.tools.XmlSongsParser;

public class Operations extends Controller {
    
    @Inject JsonToSongConverter jsonToSongConverter;
    
    @Security.Authenticated(Secured.class)
    @Transactional
    public Result syncDb() {
        UserAccount user = getUserFromCookie();

        PlaySongRestService psrs = new PlaySongRestService();
        psrs.downloadSongsData(user.getEmail());
        psrs.downloadFavoritesSongsData();
        return redirect(controllers.routes.Application.table());
    }
    
    @Transactional
    @Security.Authenticated(Secured.class)
    public Result emptyDb() {
        Logger.warn("Deleting all song data!");
        // TODO: move this to SqlQueries
        Ebean.createSqlUpdate("delete from song_lyrics").execute();
        Ebean.createSqlUpdate("delete from song_book_song").execute();
        Ebean.createSqlUpdate("delete from song_book_user_account").execute();
        Ebean.createSqlUpdate("delete from user_account_song_book").execute();
        Ebean.createSqlUpdate("delete from song_book").execute();
        Ebean.createSqlUpdate("delete from song").execute();
        Ebean.createSqlUpdate("delete from playlist_song").execute();
        Ebean.createSqlUpdate("delete from playlist").execute();
        Ebean.createSqlUpdate("delete from song_book").execute();
        return redirect(controllers.routes.Application.table());
    }

    @Transactional
    public Result inituser() {
        Logger.debug("Initializing default user!");
        UserAccount.initDefaultUser();
        return redirect(controllers.routes.Application.index());
    }
    

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result sanitizesongs() {
        Logger.debug("Sanitizing songs!");
        
        UserAccount ua = getUserFromCookie();

        // Sanitizing all songs
        for (Song s : Song.all()) {
            s.setSongLastModifiedBy(ua.getName().toString());
            Song.updateOrCreateSong(s, ua.getEmail());
        }

        return redirect(controllers.routes.Application.index());
    }
    
    public Result test() {
        jsonToSongConverter.run();
        return ok();
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

    // Some not used Actions
    @Security.Authenticated(Secured.class)
    public Result upload() {
        Logger.trace("Upload file form");

        MultipartFormData<File> body = request().body().asMultipartFormData();
        FilePart<File> uploadedFile = body.getFile("uploadedfile");
        if (uploadedFile != null) {
            String contentType = uploadedFile.getContentType();
            File file = uploadedFile.getFile();
            String message = "File successfully uploaded: " + file.getAbsolutePath() + " " + contentType;
            Logger.trace(message);
            File target = new File("resources/upload/songs.xlsx");

            try {
                Files.copy(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.trace("Updating songs");
            XlsHelper.importAndUpdateSongs3();

            return redirect(controllers.routes.Application.admin());
        } else {
            String message = "File not uploaded - missing  file";
            Logger.trace(message);
            return redirect(controllers.routes.Application.admin());
        }
    }

    @Security.Authenticated(Secured.class)
    public Result getXLS() {
        XlsHelper.dumpSongs((Song.find.all()));
        String message = "Getting xls songs";
        Logger.trace(message);
        File tmpFile = new File("resources/xlsx/songs.xlsx");
        response().setHeader(CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

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

    @Security.Authenticated(Secured.class)
    public Result updateFromOnlineSpreadsheet() {
        JsonNode data = request().body().asJson();
        Logger.debug("Online spreadsheet data: " + data.asText());
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public Result yamlbackup() {
        SongImporter.songToYaml();
        return redirect(controllers.routes.Application.index());
    }

    @Security.Authenticated(Secured.class)
    public Result yamlrestore() {
        SongImporter.yamlToSong();
        return redirect(controllers.routes.Application.index());
    }

    @Security.Authenticated(Secured.class)
    public Result sqlinit() {
        Ebean.delete(Song.all());
        SongImporter.restoreFromSQLDump();
        return redirect(controllers.routes.Application.table());
    }

    @Security.Authenticated(Secured.class)
    public Result xmlupdate() {
        XmlSongsParser.updateFromXML();
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public Result updateFromXLS() {
        XlsHelper.importAndUpdateSongs();
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public Result init() {
        try {
            // SongImporter.restoreFromSQLDump();
            SongImporter.importFromDb();
            XlsHelper.importAndUpdateSongs();
        } catch (Exception e) {
            Logger.error("Exception occured during init" + e.getStackTrace());
            e.printStackTrace();
            System.out.print(e.getStackTrace());
            System.out.print(e.getMessage());
        }
        return redirect(controllers.routes.Application.index());
    }
}
