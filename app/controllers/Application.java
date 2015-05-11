package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.songbook.XLSHelper;
import models.Song;
import models.SongLyrics;
import models.helpers.SongPrint;
import models.json.JsonSongbookGenerator;
import play.Logger;
import play.Routes;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Page;

import java.util.AbstractMap.SimpleEntry;

import play.twirl.api.Html;

public class Application extends Controller {

    static Form<Song> songForm = Form.form(Song.class);


    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("jsRoutes",
                        controllers.routes.javascript.Application.songview(),
                        controllers.routes.javascript.Application.deletesong(),
                        controllers.routes.javascript.Application.getsongjson(),
                        controllers.routes.javascript.Application.songeditor(),
                        controllers.routes.javascript.Application.songsuggestions(),
                        controllers.routes.javascript.Application.getsonglyricsjson()
                )
        );
    }

    public static Result index() {
        return redirect(routes.Application.table());
    }

    public static Result admin() {
        Html welcome = new Html("");
        return ok(admin.render("", welcome));
    }

    public static Result songbook() {
        Html welcome = new Html("");
        List <Song> sortedSongs = Song.all();
        Collections.sort(sortedSongs, new Comparator<Song>(){
            @Override
            public int compare(Song s1, Song s2) {
                return s1.songName.compareTo(s2.songName);
            }});
        return ok(songbook.render(sortedSongs));
    }

    public static Result getsongs() {
        return ok(Json.toJson(Song.all()));
    }

    public static Result getsongjson(Long id) {
        Song s = Song.get(id);
        ObjectNode songJson = SongToJson.convert(s);
        return ok(Json.toJson(songJson));
    }

    public static Result getsonglyricsjson (Long id){
        SongLyrics lyricsObject = SongLyrics.find.byId(id);
        String lyrics = lyricsObject.getsongLyrics();
        ObjectNode lyricsResult  =Json.newObject();
        lyricsResult.put("songLyrics", lyrics);
        return ok(lyricsResult);
    }

    public static Result deletesong(Long id) {
        Song.delete(id);
        return ok();
    }

    public static Result newsong() {
        Form<Song> filledForm = songForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(
                    views.html.error.render()
            );
        } else {
            Song.updateOrCreateSong(filledForm.get());
            return redirect(routes.Application.table());
        }
    }

    public static Result init(){
        try {
            SongImporter.importFromDb();
        }
        catch (Exception e){
            Logger.error("Exception occured during init" + e.getStackTrace());
            e.printStackTrace();
            System.out.print(e.getStackTrace());
            System.out.print(e.getMessage());
        }
        return redirect(routes.Application.index());
    }

    public static Result updateFromXLS(){
        XLSHelper.importAndUpdateSongs();
        return redirect(routes.Application.index());
    }

    public static Result getsongsdatatable() {
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
            case 1 : sortBy = "songOriginalTitle"; break;
            case 2 : sortBy = "songAuthor"; break;
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
                                Expr.contains("songLyrics.songLyrics", "%" + filter + "%")
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
            ObjectNode songJson = SongToJson.convert(s);
            an.add(songJson);
        }
        return ok(result);
    }

    public static Result songsuggestions() {
        /**
         * Get needed params
         */
        //TODO: check for null pointers
        // dont send whole song object, only songname and id
        Map<String, String[]> params = request().queryString();
        String filter = params.get("q")[0];
        //Logger.info("Filter param" + filter);
        /**
         * Get sorting order and column
         */
        String sortBy = "songName";
        List<Song> songs = Song.find.where(
                Expr.or(
                        Expr.ilike("songName", "%"+filter+"%"),
                        Expr.or(
                                Expr.ilike("songAuthor", "%"+filter+"%"),
                                Expr.contains("songLyrics.songLyrics", "%" + filter + "%")
                        )
                )
        ).findList();

        List<SimpleEntry> songSuggestionsList = new ArrayList<>();
        for (Song song : songs) {
            songSuggestionsList.add(new SimpleEntry(song.id, song.songName));
        }

        return ok(Json.toJson(songSuggestionsList));
    }


    public static Result downloadAndDeleteFile() {
        final Set<Map.Entry<String,String[]>> entries = request().queryString().entrySet();
        String value = null;
        for (Map.Entry<String,String[]> entry : entries) {
            final String key = entry.getKey();
            value = Arrays.toString(entry.getValue());
            Logger.debug(key + " " + value);
        }

        File tmpFile = new File("resources/"+value.substring(1,value.length()-1)+".docx");

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(tmpFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        response().setHeader("Content-disposition", "attachment;filename=" + tmpFile.getName());
        //response().setHeader(CONTENT_TYPE, "application/zip");
        response().setHeader(CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        //response().setHeader(CONTENT_LENGTH, tmpFile.length() + "");

        tmpFile.delete();

        return ok(fin);
    }

    public static Result generateSongbook() {
        JsonNode jsonNode= request().body().asJson();
        ArrayList<SongPrint> songsForPrint = new ArrayList<>();
        DocumentWriter docWriter = null;
        Logger.trace("Songbook generator json string: " + jsonNode);
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonSongbookGenerator jsonSongbook = mapper.treeToValue(jsonNode, JsonSongbookGenerator.class);
            List<models.json.Song> songsJson = jsonSongbook.getSongs();
            for (models.json.Song songJson : songsJson){
                    songsForPrint.add(new SongPrint(Song.get(Long.parseLong(songJson.getSong().getId())), Long.parseLong(songJson.getSong().getLyricsID()), songJson.getSong().getKey()));
            }
            docWriter = new DocumentWriter();
            try {
                docWriter.setSongLyricsFont(jsonSongbook.getFonts().getLyricsFont());
                docWriter.setSongTitleFont(jsonSongbook.getFonts().getTitleFont());
            } catch(NullPointerException e) {
            } finally {
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Random rand = new Random();
        int hash = rand.nextInt(1000);

        try {
            docWriter.newSongbookWordDoc(Integer.toString(hash), songsForPrint);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ok(Integer.toString(hash));
    }

    public static Result table() {
        return ok(table.render(Song.getNumberOfSongsInDatabase()));
    }

    public static Result songeditor(Long id) {
        return ok(songeditor.render(id, songForm));
    }

    public static Result newsongeditor() {
        Long id = -1L;
        return redirect(routes.Application.songeditor(id));
    }

    public static Result songview(Long id) {
        return ok(songviewer.render(id));
    }

}
