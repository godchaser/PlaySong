package controllers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.SongBook;
import models.UserAccount;
import models.helpers.HtmlBuilder;
import models.helpers.SongTableData;
import models.helpers.SongToJsonConverter;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import database.SqlQueries;

public class Datatable extends Controller {

    public Result getsongsdatatable(String songBookId) {
        UserAccount user = getUserFromCookie();

        String songBookIdFilter = SongBook.DEFAULT_SONGBOOK_ID;
        // Logger.debug("Looking for songbook by ID: " + songBookId);

        boolean isDefaultSongBookId = true;
        isDefaultSongBookId = (songBookId.equals(SongBook.DEFAULT_SONGBOOK_ID)) ? true : false;

        // check if user is owner of this songbook or songbook is public (not private)
        boolean isPublicSongBook = true;
        if (SongBook.get(songBookId) != null) {
            isPublicSongBook = !SongBook.get(songBookId).getPrivateSongbook();
        }
        if (user.containsSongbook(songBookId) || isPublicSongBook) {
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
                            + "where u2.song_book_id like '" + songBookIdFilter.toString()+"' "
                            + SqlQueries.sqlPrivateSongFalse
                            + "order by " + sortBy + " " + order;
                // @formatter:on
            } else {
                //@formatter:off
                sqlQuery =  SqlQueries.sqlSelectSong
                            + SqlQueries.sqlFromSong
                            + SqlQueries.sqlJoin
                            + "where u2.song_book_id like '" + songBookIdFilter.toString() + "' " 
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
                            + "AND (u2.song_book_id like '" + songBookIdFilter.toString() +"' "+ SqlQueries.sqlPrivateSongFalse + ")) "
                            + "UNION ALL "
                            
                            + "(" 
                            + SqlQueries.sqlSelectSong
                            + SqlQueries.sqlFromSong
                            + SqlQueries.sqlJoin  
                            + "where lower(t0.song_name) like :songnameinlinefilter "
                            + "AND (u2.song_book_id like '" + songBookIdFilter.toString() +"' "+ SqlQueries.sqlPrivateSongFalse + ")) "
                            + "UNION ALL "
                            
                            + "(" 
                            + SqlQueries.sqlSelectSong
                            + SqlQueries.sqlFromSong
                            + SqlQueries.sqlJoin     
                            + "where lower(u1.song_lyrics) like :songlyricsfilter "
                            + "AND (u2.song_book_id like '" + songBookIdFilter.toString() +"' "+ SqlQueries.sqlPrivateSongFalse + ")) "
                            + "UNION ALL "
                            
                            + "(" 
                            + SqlQueries.sqlSelectSong
                            + SqlQueries.sqlFromSong
                            + SqlQueries.sqlJoin  
                            + "where lower(t0.song_author) like :songauthorfilter "
                            + "AND (u2.song_book_id like '" + songBookIdFilter.toString() +"' "+ SqlQueries.sqlPrivateSongFalse +")"
                            + ")";
                
            } else {
                sqlQuery = "(" 
                        + SqlQueries.sqlSelectSong
                        + SqlQueries.sqlFromSong
                        + SqlQueries.sqlJoin               
                        + "where lower(t0.song_name) like :songnamefilter "
                        + "AND (u2.song_book_id like '" + songBookIdFilter.toString()+"')) "
                        + "UNION ALL "
                        
                        + "(" 
                        + SqlQueries.sqlSelectSong
                        + SqlQueries.sqlFromSong
                        + SqlQueries.sqlJoin   
                        + "where lower(t0.song_name) like :songnameinlinefilter "
                        + "AND (u2.song_book_id like '" + songBookIdFilter.toString() + "')) "
                        + "UNION ALL "
                        
                        + "(" 
                        + SqlQueries.sqlSelectSong
                        + SqlQueries.sqlFromSong
                        + SqlQueries.sqlJoin  
                        + "where lower(u1.song_lyrics) like :songlyricsfilter "
                        + "AND (u2.song_book_id like '" + songBookIdFilter.toString() + "')) "
                        + "UNION ALL "
                        
                        + "(" 
                        + SqlQueries.sqlSelectSong
                        + SqlQueries.sqlFromSong
                        + SqlQueries.sqlJoin    
                        + "where lower(t0.song_author) like :songauthorfilter "
                        + "AND (u2.song_book_id like '" + songBookIdFilter.toString() + "'))";
            }
            queryResult = Ebean.createSqlQuery(sqlQuery)
                    .setParameter("songnamefilter", filter + "%")
                    .setParameter("songnameinlinefilter", "%" + filter + "%")
                    .setParameter("songlyricsfilter", "%" + filter + "%")
                    .setParameter("songauthorfilter", "%" + filter + "%")
                    .findList();
            // @formatter:on
        }

        Map<String, SongTableData> songTableDataMap = new LinkedHashMap<String, SongTableData>();

        for (SqlRow res : queryResult) {
            String songId = res.getString("id");
            String lyricsId = res.getString("l_id").toString();

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

        Map<String, SongTableData> smallMap = new LinkedHashMap<String, SongTableData>();

        for (Entry<String, SongTableData> item : songTableDataMap.entrySet()) {
            counter++;
            if (counter >= pageStart || iTotalDisplayRecords <= counter) {
                smallMap.put(item.getKey(), item.getValue());
                pageFilledCounter++;
            }
            if (pageFilledCounter == pageLenght || iTotalDisplayRecords <= counter) {
                // process small map
                for (Entry<String, SongTableData> inneritem : smallMap.entrySet()) {
                    String songId = inneritem.getKey();
                    SongTableData ts = inneritem.getValue();
                    ObjectNode songJson = SongToJsonConverter.convert(HtmlBuilder.buildHtmlSongButtonLinks(ts.getLyrics_id(), ts.getSong_name()), HtmlBuilder.buildHtmlVideoButtonLink(ts.getSong_link()), ts.getSong_original_title(), ts.getSong_author(), songId, ts.getSong_importer(),
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
