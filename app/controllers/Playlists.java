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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import chord.tools.LineTypeChecker;
import document.tools.DocxGenerator;
import document.tools.PdfGenerator;
import models.Service;
import models.ServiceSong;
import models.Song;
import models.SongLyrics;
import models.UserAccount;
import models.helpers.PdfPrintable;
import models.helpers.SongPrint;
import models.json.JsonSongbook;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

public class Playlists extends Controller {

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
                songsForPrint.add(new SongPrint(Song.get(songJson.getSong().getId()), songJson.getSong().getLyricsID(), songJson.getSong().getKey(), excludeChords));
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
                    servicesong.setSongLyrics(SongLyrics.get((sp.getLyricsID())).songLyrics);
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
        String service_id = null;
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
            service_id = id.substring(0, id.length() - 3);
        } else {
            service_id = id;
        }

        Service service = Service.get(service_id);

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
    public Result deleteservice(String id) {
        Service.deleteById(id);
        return ok();
    }

}
