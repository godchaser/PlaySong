package controllers;

import models.Song;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.admin;
import views.html.songs;
import views.html.song;
import views.html.table;
import views.html.songbook;
import views.html.songeditor;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Page;

import play.twirl.api.Html;
public class Application extends Controller {

   // SONGS IMPLEMENTATION


    public static Result index() {
        return redirect(routes.Application.admin());
    }

    public static Result admin() {
        Html welcome = new Html("");
        return ok(admin.render("",welcome));
    }

    public static Result songbook() {
        Html welcome = new Html("");
        return ok(songbook.render());
    }

    static Form<Song> songForm = Form.form(Song.class);

    public static Result songs() {
        return ok(songs.render(Song.all(), songForm));
    }

    public static Result getSongs() {
        return ok(Json.toJson(Song.all()));
    }

    public static Result getSong(Long id) {
        return ok(song.render(Song.getSong(id)));
    }

    public static Result getSongJson(Long id) {
        ObjectNode songJson = Json.newObject();
        Song s = Song.getSong(id);
        songJson.put("songName", s.songName);
        songJson.put("songAuthor", s.songAuthor);
        songJson.put("songLyrics", s.songLyrics);
        return ok(songJson);
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

    public static Result list() {
        /**
         * Get needed params
         */
        Map<String, String[]> params = request().queryString();

        Integer iTotalRecords = Song.find.findRowCount();
        String filter = params.get("sSearch")[0];
        Integer pageSize = Integer.valueOf(params.get("iDisplayLength")[0]);
        Integer page = Integer.valueOf(params.get("iDisplayStart")[0]) / pageSize;

        /**
         * Get sorting order and column
         */
        String sortBy = "songName";
        String order = params.get("sSortDir_0")[0];

        switch(Integer.valueOf(params.get("iSortCol_0")[0])) {
            case 0 : sortBy = "songName"; break;
            case 1 : sortBy = "songAuthor"; break;
        }

        /**
         * Get page to show from database
         * It is important to set setFetchAhead to false, since it doesn't benefit a stateless application at all.
         */
        Page<Song> songPage = Song.find.where(
                Expr.or(
                        Expr.ilike("songName", "%"+filter+"%"),
                        Expr.or(
                                Expr.ilike("songAuthor", "%"+filter+"%"),
                                Expr.contains("songLyrics", "%" + filter + "%")
                        )
                )
        )
                .orderBy(sortBy + " " + order + ", id " + order)
                .findPagingList(pageSize).setFetchAhead(false)
                .getPage(page);

        Integer iTotalDisplayRecords = songPage.getTotalRowCount();

        /**
         * Construct the JSON to return
         */
        ObjectNode result =Json.newObject();

        result.put("sEcho", Integer.valueOf(params.get("sEcho")[0]));
        result.put("iTotalRecords", iTotalRecords);
        result.put("iTotalDisplayRecords", iTotalDisplayRecords);

        ArrayNode an = result.putArray("aaData");

        for(Song s : songPage.getList()) {
            ObjectNode row = Json.newObject();
            row.put("0", s.songName);
            row.put("1", s.songAuthor);
            row.put("2", s.id);
            an.add(row);
        }

        return ok(result);
    }

    public static Result songSuggestions() {
        /**
         * Get needed params
         */
        //TODO: check for null pointers
        // dont send whole song object, only songname and id
        Map<String, String[]> params = request().queryString();
        String filter = params.get("q")[0];
        Logger.info("Filter param" + filter);
        /**
         * Get sorting order and column
         */
        String sortBy = "songName";
        List<Song> songs = Song.find.where(
                Expr.or(
                        Expr.ilike("songName", "%"+filter+"%"),
                        Expr.or(
                                Expr.ilike("songAuthor", "%"+filter+"%"),
                                Expr.contains("songLyrics", "%" + filter + "%")
                        )
                )
        ).findList();
                //.orderBy(sortBy + " " + sortBy + ", id " + sortBy);
                //.findPagingList(pageSize).setFetchAhead(false)
                //.getPage(page);

        return ok(Json.toJson(songs));
    }

    public static Result downloadAndDeleteFile() {

        File tmpFile = new File("/path/to/your/generated.zip");

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(tmpFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        response().setHeader("Content-disposition", "attachment;filename=" + tmpFile.getName());
        response().setHeader(CONTENT_TYPE, "application/zip");
        response().setHeader(CONTENT_LENGTH, tmpFile.length() + "");

        tmpFile.delete();

        return ok(fin);
    }

    public static Result table() {
        return ok(table.render());
    }

    public static Result songeditor(Long id) {
        return ok(songeditor.render(Song.getSong(id), songForm));
    }

}
