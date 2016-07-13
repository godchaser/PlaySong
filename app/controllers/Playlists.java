package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Playlist;
import models.PlaylistSong;
import models.Song;
import models.SongLyrics;
import models.UserAccount;
import models.helpers.IdHelper;
import models.helpers.PdfPrintable;
import models.helpers.SongPrint;
import models.helpers.URLParamEncoder;
import models.json.JsonPlaylist;
import play.Configuration;
import play.Logger;
import play.cache.CacheApi;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import chord.tools.ChordLineTransposer;
import chord.tools.LineTypeChecker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import document.tools.DocxGenerator;
import document.tools.PdfGenerator;

public class Playlists extends Controller {

    boolean cachingFeature;
    @Inject
    CacheApi cache;

    @Inject
    DocxGenerator docxGenerator;

    @Inject
    public Playlists(Configuration configuration) {
        cachingFeature = configuration.underlying().getBoolean("playsong.songtable.caching.enabled");
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

    public Result generatePlaylist() {

        UserAccount user = getUserFromCookie();

        JsonNode jsonNode = request().body().asJson();
        List<SongPrint> songsForPrint = new ArrayList<>();

        Logger.trace("Playlist generator json string: " + jsonNode);
        ObjectMapper mapper = new ObjectMapper();
        String format = "word";

        boolean publishPlaylist = false;
        String playListName = null;
        boolean excludeChords = false;
        boolean excludePageOfContent = false;
        boolean useColumns = false;

        try {
            JsonPlaylist jsonPlaylist = mapper.treeToValue(jsonNode, JsonPlaylist.class);
            format = jsonPlaylist.getFormat();
            Map<String, Object> additionalProperties = jsonPlaylist.getAdditionalProperties();
            if (additionalProperties != null) {
                if (additionalProperties.get("publishPlaylist") != null) {
                    publishPlaylist = Boolean.parseBoolean(additionalProperties.get("publishPlaylist").toString());
                }
                if (additionalProperties.get("playListName") != null) {
                    playListName = additionalProperties.get("playListName").toString();
                }
                if (additionalProperties.get("excludeChords") != null) {
                    excludeChords = Boolean.parseBoolean(additionalProperties.get("excludeChords").toString());
                }
                if (additionalProperties.get("useColumns") != null) {
                    useColumns = Boolean.parseBoolean(additionalProperties.get("useColumns").toString());
                }
            }
            List<models.json.Song> songsJson = jsonPlaylist.getSongs();
            for (models.json.Song songJson : songsJson) {
                songsForPrint.add(new SongPrint(Song.get(songJson.getSong().getId()), songJson.getSong().getLyricsID(), songJson.getSong().getKey(), excludeChords));
            }
            if ("word".equals(format)) {
                try {
                    docxGenerator.setSongLyricsFont(jsonPlaylist.getFonts().getLyricsFont());
                    docxGenerator.setSongTitleFont(jsonPlaylist.getFonts().getTitleFont());
                } catch (NullPointerException e) {
                } finally {
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_hhmmss");
        Date date = new Date();

        if (playListName == null || playListName.length() < 1) {
            playListName = "Playlist";
        }

        // use playlist name as file hash if available
        String playlistHash = (new URLParamEncoder(playListName)).encode() + "_" + dateFormat.format(date);
        if (playListName == null || playListName.isEmpty()) {
            playlistHash = "Playlist_" + (dateFormat.format(date));
        }

        try {
            if ("word".equals(format)) {
                docxGenerator.newSongbookWordDoc(playlistHash, songsForPrint);
            } else if ("pdf".equals(format)) {
                String outputPdfPath = "resources/pdf/" + playlistHash + ".pdf";
                try {
                    Logger.debug("Writing PDF: " + outputPdfPath);
                    PdfGenerator.writeListContent(outputPdfPath, songsForPrint, useColumns, excludePageOfContent);
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
                Playlist playlist = new Playlist();
                ArrayList<PlaylistSong> playlistList = new ArrayList<PlaylistSong>();
                for (SongPrint sp : songsForPrint) {
                    PlaylistSong playlistsong = new PlaylistSong();
                    playlistsong.setId(IdHelper.getNextAvailablePlayListSongId(sp.getSong().getSongName()));
                    playlistsong.setSongName(sp.getSong().getSongName());
                    playlistsong.setSongId(sp.getSong().getId());
                    playlistsong.setLyricsId(sp.getLyricsID());
                    playlistsong.setSongKey(sp.getKey());
                    playlistsong.setSongLyrics(SongLyrics.get((sp.getLyricsID())).songLyrics);
                    playlistList.add(playlistsong);
                }
                playlist.setId(playlistHash);
                playlist.setSongs(playlistList);
                playlist.setDateCreated(new Date());
                playlist.setUserEmail(user.email);
                playlist.setUserName(user.name);
                if (playListName != null) {
                    playlist.setPlayListName(playListName);
                }
                playlist.save();
                Logger.debug("Publishing playlist: " + playlist.getDateCreated());
                clearPlaylistsJsonCache();
            } catch (Exception e) {
                Logger.error("Failed to publish playlist");
                e.printStackTrace();
            }
        }
        return ok(playlistHash);
    }

    public Result downloadPlaylist(String id) {
        boolean useColumns = true;
        boolean excludeChords = false;
        boolean excludePageOfContent = false;

        Map<String, String[]> params = request().queryString();
        useColumns = Boolean.parseBoolean(params.get("useColumns")[0].toLowerCase());
        excludeChords = Boolean.parseBoolean(params.get("excludeChords")[0].toLowerCase());

        Playlist playlist = Playlist.get(id);

        ArrayList<PdfPrintable> songPrintList = new ArrayList<PdfPrintable>();

        // TODO: BUG - now I cannot sort song by id which was int before
        // Manual sorting because of JPA OrderBy bidirectional relationship bug
        Collections.sort(playlist.getSongs());

        for (PlaylistSong playListSong : playlist.getSongs()) {
            String lyrics = playListSong.getContent();
            if (excludeChords) {
                playListSong.setSongLyrics(LineTypeChecker.removeChordLines(lyrics));
            } else {
                // transpose song if necessary
                String origKey = LineTypeChecker.getSongKey(lyrics);
                String newKey = playListSong.getSongKey();
                if (!origKey.equals(newKey)) {
                    lyrics = ChordLineTransposer.transposeLyrics(origKey, newKey, lyrics);
                    playListSong.setSongLyrics(lyrics);
                }
            }
            songPrintList.add(playListSong);
        }
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy_hhmm");
        String date = (df.format(playlist.getDateCreated()));

        String normalizedFileName = playlist.playListName.replaceAll("\\W+", "");

        String outputPdfPath = "resources/pdf/" + normalizedFileName + "_" + date + ".pdf";
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

    @Security.Authenticated(Secured.class)
    public Result deletePlayList(String id) {
        Playlist.deleteById(id);
        clearPlaylistsJsonCache();
        return ok();
    }

    private void clearPlaylistsJsonCache() {
        if (cachingFeature) {
            Logger.debug("Clearing playlists json data cache");
            cache.set(Rest.PLAYLISTS_JSON_CACHE_NAME, null, 0);
        }
    }

}
