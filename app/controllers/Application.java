package controllers;

import static play.data.Form.form;

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
import models.Service;
import models.ServiceSong;
import models.Song;
import models.SongBook;
import models.SongLyrics;
import models.UserAccount;
import models.helpers.ArrayHelper;
import models.helpers.PdfPrintable;
import models.helpers.SongPrint;
import models.helpers.SongTableData;
import models.helpers.SongToJsonConverter;
import models.json.JsonSongbook;
import play.Logger;
import play.Routes;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Security;
import rest.PlaySongRestService;
import songimporters.SongImporter;
import views.html.admin;
import views.html.login;
import views.html.playlists;
import views.html.songbook;
import views.html.songeditor;
import views.html.songs;
import views.html.songviewer;
import views.html.table;

public class Application extends Controller {

    static Form<Song> songForm = form(Song.class);
    static Form<UserAccount> userForm = form(UserAccount.class);
    static Form<Login> loginForm = Form.form(Login.class);

    public final static Locale HR_LOCALE = new Locale("HR");
    public final static Collator HR_COLLATOR = Collator.getInstance(HR_LOCALE);

    public Result index() {
        return redirect(routes.Application.table());
    }

    // VIEWS
    @Security.Authenticated(Secured.class)
    public Result admin() {
        UserAccount user = getUserFromCookie();
        String message = "Welcome admin";
        Logger.trace(message);
        return ok(admin.render(user, userForm, UserAccount.find.all(), message, Song.getSongModifiedList(), Song.getSongCreatedList()));
    }

    public Result table() {
        UserAccount user = getUserFromCookie();

        List<SongBook> songbooks = user.getSongbooks();
        songbooks.addAll(SongBook.getAllPublicSongbooks());
        // remove duplicates
        Set<SongBook> songbooksWithoutDuplicates = new LinkedHashSet<>(songbooks);
        return ok(table.render(Song.getNumberOfSongsInDatabase(), Song.getSongModifiedList(), Song.getSongCreatedList(), user, new ArrayList<>(songbooksWithoutDuplicates)));
    }

    @Security.Authenticated(Secured.class)
    public Result songeditor(Long id) {
        UserAccount user = getUserFromCookie();
        return ok(songeditor.render(id, songForm, user, Song.getSongModifiedList(), Song.getSongCreatedList(), user.getSongbooks()));
    }

    @Security.Authenticated(Secured.class)
    public Result newsongeditor() {
        Long id = -1L;
        return redirect(routes.Application.songeditor(id));
    }

    public Result songview(Long id) {
        UserAccount user = getUserFromCookie();
        return ok(songviewer.render(id, user, Song.getSongModifiedList(), Song.getSongCreatedList()));
    }

    public Result songbook(Long id) {
        UserAccount user = getUserFromCookie();

        // switch songbooks according to id - but should also check credentials first to account the owner
        List<SongBook> songbooks = user.getSongbooks();
        songbooks.addAll(SongBook.getAllPublicSongbooks());
        // remove duplicates
        Set<SongBook> songbooksWithoutDuplicates = new LinkedHashSet<>(songbooks);
        SongBook filteredSongbook = SongBook.get(SongBook.DEFAULT_SONGBOOK_ID);
        for (SongBook songbook : songbooksWithoutDuplicates) {
            Logger.debug("Checking songbook: " + songbook.getId() + " with matched Id: " + id);
            if (songbook.getId().equals(id)) {
                Logger.debug("Found songbook match by ID: " + id);
                filteredSongbook = songbook;
                break;
            }
        }

        HR_COLLATOR.setStrength(Collator.PRIMARY);
        List<Song> sortedSongs = filteredSongbook.getSongs();
        Collections.sort(sortedSongs, new Comparator<Song>() {
            @Override
            public int compare(Song s1, Song s2) {
                return HR_COLLATOR.compare(s1.songName, s2.songName);
            }
        });

        return ok(songbook.render(sortedSongs, new ArrayList<SongBook>(songbooksWithoutDuplicates), id, user, Song.getSongModifiedList(), Song.getSongCreatedList()));
    }

    public Result services() {
        UserAccount user = getUserFromCookie();

        List<Service> serviceList = Service.find.all();
        // sort by date created
        Collections.sort(serviceList, new Comparator<Service>() {
            public int compare(Service o1, Service o2) {
                if (o1.getDateCreated() == null || o2.getDateCreated() == null)
                    return 0;
                return o1.getDateCreated().compareTo(o2.getDateCreated());
            }
        });

        // Manual sorting because of JPA OrderBy bidirectional relationship bug
        for (Service service : serviceList) {
            Collections.sort(service.getSongs());
        }

        return ok(playlists.render(serviceList, user, Song.getSongModifiedList(), Song.getSongCreatedList()));
    }

    @Security.Authenticated(Secured.class)
    public Result deleteservice(Long id) {
        Service.delete(id);
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public Result getsongs() {
        return ok(Json.toJson(Song.all()));
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

    public Result getfavoritessongsdata() {
        List<Service> services = Service.all();
        return ok(Json.toJson(SongToJsonConverter.convert(services)));
    }

    public Result getsongbooksdata() {
        return ok(Json.toJson(SongBook.all()));
    }

    public Result getsongjson(Long id) {
        Song s = Song.get(id);
        ObjectNode songJson = SongToJsonConverter.convert(s);
        return ok(Json.toJson(songJson));
    }

    public Result getsonglyricsjson(Long id) {
        SongLyrics lyricsObject = SongLyrics.find.byId(id);
        String lyrics = lyricsObject.getsongLyrics();
        ObjectNode lyricsResult = Json.newObject();
        lyricsResult.put("songLyrics", lyrics);
        return ok(lyricsResult);
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public Result updatesonglyricsjson(Long id) {
        SongLyrics lyricsObject = SongLyrics.find.byId(id);
        DynamicForm df = play.data.Form.form().bindFromRequest();
        String songLyrics = df.get("songLyrics");
        lyricsObject.setsongLyrics(songLyrics);
        lyricsObject.updateSongLyrics();
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public Result deletesong(Long id) {
        UserAccount user = getUserFromCookie();

        deleteSong(id);
        SongBook.staleSongbookCleanup(user.getEmail());
        return redirect(routes.Application.table());
    }

    // Helper method to execute transaction
    @Transactional
    private void deleteSong(Long id) {
        Song.delete(id);
    }

    @Security.Authenticated(Secured.class)
    public Result updateorcreatesong() {
        Form<Song> filledForm = songForm.bindFromRequest();
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
            return redirect(routes.Application.table());
        }
    }

    // Helper method to execute transaction
    @Transactional
    private void updateOrCreateSong(Song updatedSong, UserAccount user) {
        Song.updateOrCreateSong(updatedSong, user.getEmail());
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public Result emptyDb() {
        Ebean.createSqlUpdate("delete from song_lyrics").execute();
        Ebean.createSqlUpdate("delete from song_book_song").execute();
        Ebean.createSqlUpdate("delete from song_book_user_account").execute();
        Ebean.createSqlUpdate("delete from user_account_song_book").execute();
        Ebean.createSqlUpdate("delete from song_book").execute();
        Ebean.createSqlUpdate("delete from song").execute();
        Ebean.createSqlUpdate("delete from service_song").execute();
        Ebean.createSqlUpdate("delete from service").execute();
        Ebean.createSqlUpdate("delete from song_book").execute();
        return redirect(routes.Application.table());
    }

    @Transactional
    public Result inituser() {
        try {
            Ebean.createSqlUpdate("delete from user_account").execute();
            UserAccount test = new UserAccount("test@test.com", "test", "test");
            test.save();
            test.setDefaultSongbook();
            test.update();
        } catch (Exception e) {
            Logger.error("Exception occured during init" + e.getStackTrace());
            e.printStackTrace();
            System.out.print(e.getStackTrace());
            System.out.print(e.getMessage());
        }
        return redirect(routes.Application.index());
    }

    public Result songs() {
        return ok(songs.render(Song.all()));
    }

    public Result getsongsdatatable(Long songBookId) {
        UserAccount user = getUserFromCookie();

        Long songBookIdFilter = SongBook.DEFAULT_SONGBOOK_ID;
        Logger.debug("Looking for songbook by ID: " + songBookId);

        boolean isDefaultSongBookId = true;
        isDefaultSongBookId = (songBookId.equals(SongBook.DEFAULT_SONGBOOK_ID)) ? true : false;

        // check if user is owner of this songbook or songbook is public (not private)
        boolean isPublicSong = true;
        if (SongBook.get(songBookId) != null) {
            isPublicSong = SongBook.get(songBookId).getPrivateSongbook();
        }
        if (user.containsSongbook(songBookId) || isPublicSong) {
            songBookIdFilter = songBookId;
        }

        Map<String, String[]> params = request().queryString();

        String filter = params.get("sSearch")[0].toLowerCase();

        /**
         * Get sorting order and column
         */
        String sortBy = "song_name";
        String order = "asc";

        if (params.get("sSortDir_0")[0].equals("desc")) {
            order = "desc";
        }

        switch (Integer.valueOf(params.get("iSortCol_0")[0])) {
        case 0:
            sortBy = "song_name";
            break;
        case 1:
            sortBy = "song_original_title";
            break;
        case 2:
            sortBy = "song_author";
            break;
        }

        List<SqlRow> queryResult;

        String sqlQuery = null;

        // searching without full text search filter
        if (filter.isEmpty()) {
            if (isDefaultSongBookId) {
                // default songbook - query default songbook and all public songs (private false)
                //@formatter:off
                sqlQuery =  SqlQueries.sqlSelectSong
                            + SqlQueries.sqlFromSong
                            + SqlQueries.sqlJoin
                            + "where u2.song_book_id = " + songBookIdFilter.toString() 
                            + SqlQueries.sqlPrivateSongFalse
                            + "order by " + sortBy + " " + order;
                // @formatter:on
            } else {
                //@formatter:off
                sqlQuery =  SqlQueries.sqlSelectSong
                            + SqlQueries.sqlFromSong
                            + SqlQueries.sqlJoin
                            + "where u2.song_book_id = " + songBookIdFilter.toString() + " " 
                            + "order by " + sortBy + " " + order;
                // @formatter:on
            }
            queryResult = Ebean.createSqlQuery(sqlQuery).findList();
        }
        // this is scenario with string filter
        else {
            if (isDefaultSongBookId) {
                // @formatter:off
                sqlQuery = "(" 
                            + SqlQueries.sqlSelectSong
                            + SqlQueries.sqlFromSong
                            + SqlQueries.sqlJoin          
                            + "where lower(t0.song_name) like :songnamefilter "
                            + "AND (u2.song_book_id = " + songBookIdFilter.toString() + SqlQueries.sqlPrivateSongFalse + ")) "
                            + "UNION ALL "
                            
                            + "(" 
                            + SqlQueries.sqlSelectSong
                            + SqlQueries.sqlFromSong
                            + SqlQueries.sqlJoin  
                            + "where lower(t0.song_name) like :songnameinlinefilter "
                            + "AND (u2.song_book_id = " + songBookIdFilter.toString() + SqlQueries.sqlPrivateSongFalse + ")) "
                            + "UNION ALL "
                            
                            + "(" 
                            + SqlQueries.sqlSelectSong
                            + SqlQueries.sqlFromSong
                            + SqlQueries.sqlJoin     
                            + "where lower(u1.song_lyrics) like :songlyricsfilter "
                            + "AND (u2.song_book_id = " + songBookIdFilter.toString() + SqlQueries.sqlPrivateSongFalse + ")) "
                            + "UNION ALL "
                            
                            + "(" 
                            + SqlQueries.sqlSelectSong
                            + SqlQueries.sqlFromSong
                            + SqlQueries.sqlJoin  
                            + "where lower(t0.song_author) like :songauthorfilter "
                            + "AND (u2.song_book_id = " + songBookIdFilter.toString() + SqlQueries.sqlPrivateSongFalse +")"
                            + ")";
                
            } else {
                sqlQuery = "(" 
                        + SqlQueries.sqlSelectSong
                        + SqlQueries.sqlFromSong
                        + SqlQueries.sqlJoin               
                        + "where lower(t0.song_name) like :songnamefilter "
                        + "AND (u2.song_book_id = " + songBookIdFilter.toString()+")) "
                        + "UNION ALL "
                        
                        + "(" 
                        + SqlQueries.sqlSelectSong
                        + SqlQueries.sqlFromSong
                        + SqlQueries.sqlJoin   
                        + "where lower(t0.song_name) like :songnameinlinefilter "
                        + "AND (u2.song_book_id = " + songBookIdFilter.toString() + ")) "
                        + "UNION ALL "
                        
                        + "(" 
                        + SqlQueries.sqlSelectSong
                        + SqlQueries.sqlFromSong
                        + SqlQueries.sqlJoin  
                        + "where lower(u1.song_lyrics) like :songlyricsfilter "
                        + "AND (u2.song_book_id = " + songBookIdFilter.toString() + ")) "
                        + "UNION ALL "
                        
                        + "(" 
                        + SqlQueries.sqlSelectSong
                        + SqlQueries.sqlFromSong
                        + SqlQueries.sqlJoin    
                        + "where lower(t0.song_author) like :songauthorfilter "
                        + "AND (u2.song_book_id = " + songBookIdFilter.toString() + "))";
            }
            queryResult = Ebean.createSqlQuery(sqlQuery)
                    .setParameter("songnamefilter", filter + "%")
                    .setParameter("songnameinlinefilter", "%" + filter + "%")
                    .setParameter("songlyricsfilter", "%" + filter + "%")
                    .setParameter("songauthorfilter", "%" + filter + "%")
                    .findList();
            // @formatter:on
        }

        Map<Long, SongTableData> songTableDataMap = new LinkedHashMap<Long, SongTableData>();

        for (SqlRow res : queryResult) {
            Long songId = res.getLong("id");
            String lyricsId = res.getLong("l_id").toString();

            // search through existing songs
            if (songTableDataMap.containsKey(songId)) {
                SongTableData tableData = songTableDataMap.get(songId);
                // update additional lyrics
                if (!(tableData.getLyrics_id().contains(lyricsId))) {
                    tableData.getLyrics_id().add(lyricsId);
                }
            } else {
                // create new SongTableData
                SongTableData ts = new SongTableData();
                ts.setSong_name(res.getString("song_name"));
                ts.setSong_original_title(res.getString("song_original_title"));
                ts.setSong_author(res.getString("song_author"));
                ts.setSong_link(res.getString("song_link"));
                ts.setSong_importer(res.getString("song_importer"));
                ts.getLyrics_id().add(lyricsId);
                songTableDataMap.put(songId, ts);
            }
        }

        /**
         * Construct the JSON to return
         */
        ObjectNode result = Json.newObject();

        ArrayNode an = result.putArray("aaData");

        // usually fixed to 10 entries
        int pageLenght = Integer.valueOf(params.get("iDisplayLength")[0]);
        // starts from 0 then 10 ...
        int pageStart = Integer.valueOf(params.get("iDisplayStart")[0]);

        int iTotalDisplayRecords = songTableDataMap.size();

        int pageFilledCounter = 0;
        int counter = 0;

        Map<Long, SongTableData> smallMap = new LinkedHashMap<Long, SongTableData>();

        for (Entry<Long, SongTableData> item : songTableDataMap.entrySet()) {
            counter++;
            if (counter >= pageStart || iTotalDisplayRecords <= counter) {
                smallMap.put(item.getKey(), item.getValue());
                pageFilledCounter++;
            }
            if (pageFilledCounter == pageLenght || iTotalDisplayRecords <= counter) {
                // process small map
                for (Entry<Long, SongTableData> inneritem : smallMap.entrySet()) {
                    Long songId = inneritem.getKey();
                    SongTableData ts = inneritem.getValue();
                    ObjectNode songJson = SongToJsonConverter.convert(ts.getSong_name(), ts.getSong_link(), ts.getSong_original_title(), ts.getSong_author(), songId, ts.getSong_importer(),
                            ts.getLyrics_id());
                    an.add(songJson);
                }
                break;
            }
        }

        int iTotalRecords = queryResult.size();

        result.put("sEcho", Integer.valueOf(params.get("sEcho")[0]));
        result.put("iTotalRecords", iTotalRecords);
        result.put("iTotalDisplayRecords", iTotalDisplayRecords);

        return ok(result);
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
        ArrayList<Long> ids = new ArrayList<>();
        //TODO: Get song name through this sql query
        for (SqlRow res : result) {
            ids.add(res.getLong("id"));
        }
        ids = ArrayHelper.removeDuplicates(ids);
        List<ObjectNode> songSuggestions = new ArrayList<ObjectNode>();

        for (Long id : ids) {
            ObjectNode songSuggestion = Json.newObject();
            songSuggestion.put("key", id);
            songSuggestion.put("value", Song.get(id).getSongName());
            songSuggestions.add(songSuggestion);
        }

        return ok(Json.toJson(songSuggestions));
    }

    public Result downloadAndDeleteFile() {
        final Set<Map.Entry<String, String[]>> entries = request().queryString().entrySet();
        String hashValue = null;
        String formatValue = null;
        File tmpFile = null;
        for (Map.Entry<String, String[]> entry : entries) {
            final String key = entry.getKey();
            String value = Arrays.toString(entry.getValue());
            Logger.debug(key + " " + value);
            if ("format".equals(key.toString())) {
                formatValue = value.substring(1, value.length() - 1);
            } else if ("hash".equals(key.toString())) {
                hashValue = value.substring(1, value.length() - 1);
            }
        }
        Logger.debug("Format: " + formatValue + " Hash: " + hashValue);
        switch (formatValue) {
        case "pdf":
            tmpFile = new File("resources/pdf/" + hashValue + ".pdf");
            response().setHeader(CONTENT_TYPE, "application/pdf");
            break;
        case "word":
            tmpFile = new File("resources/docx/" + hashValue + ".docx");
            response().setHeader(CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            break;
        }
        Logger.debug("File: " + tmpFile.getAbsolutePath());
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(tmpFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        response().setHeader("Content-disposition", "attachment;filename=" + tmpFile.getName());
        // response().setHeader(CONTENT_TYPE, "application/zip");
        // response().setHeader(CONTENT_LENGTH, tmpFile.length() + "");
        // tmpFile.delete();

        return ok(fin);
    }

    public Result generateSongbook() {

        UserAccount user = getUserFromCookie();

        JsonNode jsonNode = request().body().asJson();
        List<SongPrint> songsForPrint = new ArrayList<>();
        DocxGenerator docWriter = null;
        Logger.trace("Songbook generator json string: " + jsonNode);
        ObjectMapper mapper = new ObjectMapper();
        String format = "word";

        boolean publishPlaylist = false;
        String songBookName = null;
        boolean excludeChords = false;
        boolean useColumns = false;

        try {
            JsonSongbook jsonSongbook = mapper.treeToValue(jsonNode, JsonSongbook.class);
            format = jsonSongbook.getFormat();
            Map<String, Object> additionalProperties = jsonSongbook.getAdditionalProperties();
            if (additionalProperties != null) {
                if (additionalProperties.get("publishPlaylist") != null) {
                    publishPlaylist = Boolean.parseBoolean(additionalProperties.get("publishPlaylist").toString());
                }
                if (additionalProperties.get("songBookName") != null) {
                    songBookName = additionalProperties.get("songBookName").toString();
                }
                if (additionalProperties.get("excludeChords") != null) {
                    excludeChords = Boolean.parseBoolean(additionalProperties.get("excludeChords").toString());
                }
                if (additionalProperties.get("useColumns") != null) {
                    useColumns = Boolean.parseBoolean(additionalProperties.get("useColumns").toString());
                }
            }
            List<models.json.Song> songsJson = jsonSongbook.getSongs();
            for (models.json.Song songJson : songsJson) {
                songsForPrint.add(
                        new SongPrint(Song.get(Long.parseLong(songJson.getSong().getId())), Long.parseLong(songJson.getSong().getLyricsID()), songJson.getSong().getKey(), excludeChords));
            }
            if ("word".equals(format)) {
                docWriter = new DocxGenerator();
                try {
                    docWriter.setSongLyricsFont(jsonSongbook.getFonts().getLyricsFont());
                    docWriter.setSongTitleFont(jsonSongbook.getFonts().getTitleFont());
                } catch (NullPointerException e) {
                } finally {
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_hhmmss");
        Date date = new Date();

        // use songbook name as file hash if available
        String hash = songBookName + "_" + dateFormat.format(date);
        if (songBookName == null || songBookName.isEmpty()) {
            hash = "Songbook_" + (dateFormat.format(date));
        }

        try {
            if ("word".equals(format)) {
                docWriter.newSongbookWordDoc(hash, songsForPrint);
            } else if ("pdf".equals(format)) {
                String outputPdfPath = "resources/pdf/" + hash + ".pdf";
                try {
                    Logger.debug("Writing PDF: " + outputPdfPath);
                    PdfGenerator.writeListContent(outputPdfPath, songsForPrint, useColumns);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Logger.error("Failed to generate document " + format);
            e.printStackTrace();
        }

        // TODO: this should maybe made async somehow, because it is not crucial for this response
        if (!("Guest".equals(user.name)) && publishPlaylist) {
            try {
                Service service = new Service();
                ArrayList<ServiceSong> serviceSongList = new ArrayList<ServiceSong>();
                for (SongPrint sp : songsForPrint) {
                    ServiceSong servicesong = new ServiceSong();
                    servicesong.setSongName(sp.getSong().getSongName());
                    servicesong.setSongId(sp.getSong().getId());
                    servicesong.setLyricsId(sp.getLyricsID());
                    servicesong.setSongKey(sp.getKey());
                    servicesong.setSongLyrics(SongLyrics.find.byId((sp.getLyricsID())).songLyrics);
                    serviceSongList.add(servicesong);
                }
                service.setSongs(serviceSongList);
                service.setDateCreated(new Date());
                service.setUserEmail(user.email);
                service.setUserName(user.name);
                if (songBookName != null) {
                    service.setServiceName(songBookName);
                }
                service.save();
                Logger.debug("Publishing playlist: " + service.getDateCreated());
            } catch (Exception e) {
                Logger.error("Failed to publish playlist");
                e.printStackTrace();
            }
        }
        return ok(hash);
    }

    public Result generateService(String id) {
        UserAccount user = getUserFromCookie();

        boolean useColumns = true;
        boolean excludeChords = false;
        Long service_id = null;
        boolean defaultServiceOptions = true;

        // Skip this if id is shorter than 3 digits while it is default case
        if (id.length() > 3) {
            switch (id.substring(id.length() - 3)) {
            case "_x0":
                excludeChords = true;
                defaultServiceOptions = false;
                break;
            case "_0c":
                useColumns = false;
                defaultServiceOptions = false;
                break;
            case "_xc":
                excludeChords = true;
                useColumns = false;
                defaultServiceOptions = false;
                break;
            default:
            }
        }
        if (!defaultServiceOptions) {
            service_id = Long.parseLong(id.substring(0, id.length() - 3));
        } else {
            service_id = Long.parseLong(id);
        }

        Service service = Service.find.byId(service_id);

        ArrayList<PdfPrintable> songPrintList = new ArrayList<PdfPrintable>();

        // Manual sorting because of JPA OrderBy bidirectional relationship bug
        Collections.sort(service.getSongs());

        for (ServiceSong serviceSong : service.getSongs()) {
            if (excludeChords) {
                String lyricsWithoutChords = serviceSong.getContent();
                lyricsWithoutChords = LineTypeChecker.removeChordLines(lyricsWithoutChords);
                serviceSong.setSongLyrics(lyricsWithoutChords);
            }
            songPrintList.add(serviceSong);
        }
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy_hhmm");
        String date = (df.format(service.getDateCreated()));

        String normalizedFileName = service.serviceName.replaceAll("\\W+", "");

        String outputPdfPath = "resources/pdf/" + normalizedFileName + "_" + date + ".pdf";
        try {
            Logger.debug("Writing PDF: " + outputPdfPath);
            PdfGenerator.writeListContent(outputPdfPath, songPrintList, useColumns);
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

    public Result login() {
        String redirecturl = flash().get("url");
        Logger.debug("Login flash redirect url: " + redirecturl);
        if (redirecturl != null) {
            flash().put("url", redirecturl);
        }
        return ok(login.render(loginForm));
    }

    public Result authenticate() {
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        String redirecturl = request().body().asFormUrlEncoded().get("redirecturl")[0];
        String email = request().body().asFormUrlEncoded().get("email")[0];
        if (!redirecturl.isEmpty() || "null".equals(redirecturl)) {
            Logger.debug("Authenticate forwarded redirect url: " + redirecturl);
        }
        if (loginForm.hasErrors()) {
            Logger.debug("Failed login for: " + email);
            if (redirecturl != null) {
                flash().put("url", redirecturl);
            }
            return badRequest(login.render(loginForm));
        } else {
            session().clear();
            session("email", loginForm.get().email);
            redirecturl = loginForm.get().redirecturl;
            if (redirecturl.isEmpty()) {
                redirecturl = "/";
            }
            Logger.debug("Successfull login for: " + email);
            Logger.debug("Redirecting to: " + redirecturl);
            return redirect(redirecturl);
        }
    }

    public Result logout() {
        if (request().cookies().get("PLAY_SESSION") != null) {
            String cookieVal = request().cookies().get("PLAY_SESSION").value();
            String userId = cookieVal.substring(cookieVal.indexOf("email=") + 6).replace("%40", "@");
            Logger.debug("User logged out: " + userId);
        }
        session().clear();
        flash("success", "You've been logged out");
        return redirect(routes.Application.index());
    }

    public Result test() {
        Logger.debug("TEST!");
        return redirect(routes.Application.table());
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public Result syncDb() {
        UserAccount user = getUserFromCookie();

        PlaySongRestService psrs = new PlaySongRestService();
        psrs.downloadSongsData(user.getEmail());
        psrs.downloadFavoritesSongsData();
        return redirect(routes.Application.table());
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public Result sanitizesongs() {
        System.out.println("sanitizesongs!");

        UserAccount ua = getUserFromCookie();

        // Sanitizing all songs
        for (Song s : Song.all()) {
            s.setSongLastModifiedBy(ua.getName().toString());
            Song.updateOrCreateSong(s, ua.getEmail());
        }

        return redirect(routes.Application.index());
    }

    public UserAccount getUserFromCookie() {
        UserAccount user = null;
        if (request().cookies().get("PLAY_SESSION") != null) {
            // Logger.debug("Found PLAY_SESSION cookie");
            String cookieVal = request().cookies().get("PLAY_SESSION").value();
            String userId = cookieVal.substring(cookieVal.indexOf("email=") + 6).replace("%40", "@");
            String uuid = null;
            if (request().cookies().get("PLAYSONG-UUID") != null) {
                uuid = request().cookies().get("PLAYSONG-UUID").value();
            } else {
                uuid = UUID.randomUUID().toString();
                response().setCookie("PLAYSONG-UUID", uuid);
            }

            Logger.debug(uuid + ": User ID: " + userId);
            if (userId != null) {
                user = UserAccount.find.byId(userId);
            }
        }
        if (user == null) {
            Logger.debug("Using guest session");
            user = new UserAccount("Guest", "", "");
        }
        return user;
    }

    public Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(Routes.javascriptRouter("jsRoutes", controllers.routes.javascript.Application.songview(), controllers.routes.javascript.Application.login(),
                controllers.routes.javascript.Application.deletesong(), controllers.routes.javascript.Application.getsongjson(), controllers.routes.javascript.Application.songeditor(),
                controllers.routes.javascript.Application.songsuggestions(), controllers.routes.javascript.Application.getsonglyricsjson(),
                controllers.routes.javascript.Application.updatesonglyricsjson(), controllers.routes.javascript.Application.services(),
                controllers.routes.javascript.Application.generateService(), controllers.routes.javascript.Application.deleteservice(), controllers.routes.javascript.Application.upload(),
                controllers.routes.javascript.Application.addUser(), controllers.routes.javascript.Application.getUser(), controllers.routes.javascript.Application.deleteUser(),
                controllers.routes.javascript.Application.updateUser()));
    }

    //
    // USER ACTIONS
    @Security.Authenticated(Secured.class)
    public Result addUser() {
        Form<UserAccount> filledForm = userForm.bindFromRequest();
        UserAccount user = getUserFromCookie();

        if (filledForm.hasErrors()) {
            String message = "Invalid user form";
            Logger.trace(message);
            return badRequest(admin.render(user, userForm, UserAccount.find.all(), message, Song.getSongModifiedList(), Song.getSongCreatedList()));
        }

        UserAccount newUser = filledForm.get();
        String message = null;
        if (UserAccount.find.byId(newUser.email) != null) {
            message = "User with this email already exists: " + newUser.email;
        } else {
            message = "Adding new user: " + newUser.email;
            newUser.save();
            newUser.setDefaultSongbook();
            newUser.update();
        }
        Logger.trace(message);
        return redirect(routes.Application.admin());
    }

    @Security.Authenticated(Secured.class)
    public Result getUser(String email) {
        UserAccount foundUser = null;
        if (email != null) {
            foundUser = UserAccount.find.byId(email);
        }
        if (foundUser == null) {
            String message = "Cannot find user for get: " + email;
            Logger.trace(message);
            return badRequest();
        }
        String message = "Getting user: " + email;
        Logger.trace(message);
        return ok(Json.toJson(foundUser));
    }

    @Security.Authenticated(Secured.class)
    public Result deleteUser(String email) {
        UserAccount foundUser = null;
        if (email != null) {
            foundUser = UserAccount.find.byId(email);
        }
        if (foundUser == null) {
            String message = "Cannot find user for deletion: " + email;
            Logger.trace(message);
            return badRequest();
        }
        String message = "Deleting user: " + email;
        foundUser.delete();
        Logger.trace(message);
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public Result updateUser(String email) {
        Form<UserAccount> filledForm = userForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            String message = "Invalid user update form";
            Logger.trace(message);
            return badRequest();
        }
        UserAccount updateUser = filledForm.get();
        String message = "Updating  user: " + updateUser.email;
        Logger.trace(message);
        updateUser.update();
        return ok();
    }

    // Some not used Actions
    @Security.Authenticated(Secured.class)
    public Result upload() {
        Logger.trace("Upload file form");
        MultipartFormData body = request().body().asMultipartFormData();
        FilePart uploadedFile = body.getFile("uploadedfile");
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

            return redirect(routes.Application.admin());
        } else {
            String message = "File not uploaded - missing  file";
            Logger.trace(message);
            return redirect(routes.Application.admin());
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
        return redirect(routes.Application.index());
    }

    @Security.Authenticated(Secured.class)
    public Result yamlrestore() {
        SongImporter.yamlToSong();
        return redirect(routes.Application.index());
    }

    @Security.Authenticated(Secured.class)
    public Result sqlinit() {
        Ebean.delete(Song.all());
        SongImporter.restoreFromSQLDump();
        return redirect(routes.Application.table());
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
        return redirect(routes.Application.index());
    }

}
