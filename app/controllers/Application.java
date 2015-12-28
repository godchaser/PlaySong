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

import chord.tools.ChordLineTransposer;
import document.tools.DocxGenerator;
import document.tools.PdfGenerator;
import document.tools.XlsHelper;
import document.tools.XmlSongsParser;
import helpers.ArrayHelper;
import models.Service;
import models.ServiceSong;
import models.Song;
import models.SongLyrics;
import models.UserAccount;
import models.helpers.PdfPrintable;
import models.helpers.SongPrint;
import models.helpers.SongTableData;
import models.helpers.SongToJsonConverter;
import models.json.JsonSongbook;
import play.Logger;
import play.Routes;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Security;
import songimporters.SongImporter;
import views.html.admin;
import views.html.login;
import views.html.services;
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

	@Security.Authenticated(Secured.class)
	public Result admin() {
		UserAccount user = null;
		if (request().cookies().get("PLAY_SESSION") != null) {
			// Logger.debug("Found PLAY_SESSION cookie");
			String uuid = null;
			String cookieVal = request().cookies().get("PLAY_SESSION").value();
			String userId = cookieVal.substring(cookieVal.indexOf("email=") + 6).replace("%40", "@");
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
		String message = "Welcome admin";
		Logger.trace(message);
		return ok(admin.render(user, userForm, UserAccount.find.all(), message, Song.getSongModifiedList(),
				Song.getSongCreatedList()));
	}

	@Security.Authenticated(Secured.class)
	public Result upload() {
		UserAccount user = null;
		if (request().username() != null) {
			user = UserAccount.find.byId(request().username());
		}
		if (user == null) {
			user = new UserAccount("Guest", "", "");
		}

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
				// TODO Auto-generated catch block
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
	public Result addUser() {
		Form<UserAccount> filledForm = userForm.bindFromRequest();
		UserAccount user = null;
		if (request().username() != null) {
			user = UserAccount.find.byId(request().username());
		}
		if (user == null) {
			user = new UserAccount("Guest", "", "");
		}

		if (filledForm.hasErrors()) {
			String message = "Invalid user form";
			Logger.trace(message);
			return badRequest(admin.render(user, userForm, UserAccount.find.all(), message, Song.getSongModifiedList(),
					Song.getSongCreatedList()));
		}

		UserAccount newUser = filledForm.get();
		String message = null;
		if (UserAccount.find.byId(newUser.email) != null) {
			message = "User with this email already exists: " + newUser.email;
		} else {
			message = "Adding new user: " + newUser.email;
			newUser.save();
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

	public Result table() {
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

		return ok(table.render(Song.getNumberOfSongsInDatabase(), Song.getSongModifiedList(), Song.getSongCreatedList(),
				user));
	}

	@Security.Authenticated(Secured.class)
	public Result songeditor(Long id) {
		UserAccount user = null;
		if (request().username() != null) {
			user = UserAccount.find.byId(request().username());
		}
		if (user == null) {
			user = new UserAccount("Guest", "", "");
		}
		return ok(songeditor.render(id, songForm, user, Song.getSongModifiedList(), Song.getSongCreatedList()));
	}

	@Security.Authenticated(Secured.class)
	public Result newsongeditor() {
		Long id = -1L;
		return redirect(routes.Application.songeditor(id));
	}

	public Result songview(Long id) {
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
		return ok(songviewer.render(id, user, Song.getSongModifiedList(), Song.getSongCreatedList()));
	}

	public Result songbook() {
		HR_COLLATOR.setStrength(Collator.PRIMARY);
		List<Song> sortedSongs = Song.all();
		Collections.sort(sortedSongs, new Comparator<Song>() {
			@Override
			public int compare(Song s1, Song s2) {
				return HR_COLLATOR.compare(s1.songName, s2.songName);
			}
		});
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
		return ok(songbook.render(sortedSongs, user, Song.getSongModifiedList(), Song.getSongCreatedList()));
	}

	public Result services() {
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

		// Manual sorting because of JPA OrderBy bidirectional relationship bug
		List<Service> serviceList = Service.find.all();
		for (Service service : serviceList) {
			Collections.sort(service.getSongs());
		}

		return ok(services.render(serviceList, user, Song.getSongModifiedList(), Song.getSongCreatedList()));
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

	//@Security.Authenticated(Secured.class)
	public Result getsongdata() {
		List<Song> songs = Song.all();
		ArrayList<ObjectNode> songsJson = new ArrayList<>();

		for (Song s : songs) {
			ObjectNode songJson = SongToJsonConverter.convert(s);
			songsJson.add(songJson);
		}
		return ok(Json.toJson(songsJson));
	}
	
	//@Security.Authenticated(Secured.class)
	public Result getsonglyricsdata() {
		List<SongLyrics> songlyrics = SongLyrics.all();

		ArrayList<ObjectNode> songlyricsJson = new ArrayList<>();

		for (SongLyrics sl : songlyrics) {
			ObjectNode songLyricsJson = SongToJsonConverter.convertLyrics(sl);
			songlyricsJson.add(songLyricsJson);
		}
		return ok(Json.toJson(songlyricsJson));
	}

	@Security.Authenticated(Secured.class)
	public Result updateFromOnlineSpreadsheet() {
		JsonNode data = request().body().asJson();
		Logger.debug("Online spreadsheet data: " + data.asText());
		return ok();
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
		Song.delete(id);
		return redirect(routes.Application.table());
	}

	@Security.Authenticated(Secured.class)
	public Result newsong() {
		Form<Song> filledForm = songForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.error.render());
		} else {
			String cookieVal = request().cookies().get("PLAY_SESSION").value();
			String userEmail = cookieVal.substring(cookieVal.indexOf("email=") + 6).replace("%40", "@");

			String uuid = null;
			if (request().cookies().get("PLAYSONG-UUID") != null) {
				uuid = request().cookies().get("PLAYSONG-UUID").value();
			} else {
				uuid = UUID.randomUUID().toString();
				response().setCookie("PLAYSONG-UUID", uuid);
			}

			Logger.debug(uuid + ": User ID: " + userEmail);

			String userName = UserAccount.getNameFromEmail(userEmail);
			Song updatedSong = filledForm.get();
			updatedSong.setSongLastModifiedBy(userName);
			Logger.debug("Update or create song");
			Song.updateOrCreateSong(filledForm.get());
			return redirect(routes.Application.table());
		}
	}

	@Security.Authenticated(Secured.class)
	public Result emptyDb() {
		Ebean.createSqlUpdate("delete from song_lyrics").execute();
		Ebean.createSqlUpdate("delete from song").execute();
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

	public Result inituser() {
		try {
			UserAccount test = new UserAccount("test@test.com", "test", "test");
			test.save();
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

	@Security.Authenticated(Secured.class)
	public Result yamlbackup() {
		System.out.println("yamlbackup!");
		SongImporter.songToYaml();
		return redirect(routes.Application.index());
	}

	@Security.Authenticated(Secured.class)
	public Result yamlrestore() {
		System.out.println("yamlrestore!");
		SongImporter.yamlToSong();
		return redirect(routes.Application.index());
	}

	@Security.Authenticated(Secured.class)
	public Result sqlinit() {
		System.out.println("SQL INIT!");
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

	public Result getsongsdatatable() {
		/**
		 * Get needed params
		 */
		Map<String, String[]> params = request().queryString();

		int iTotalRecords = Song.find.findRowCount();
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

		if (filter.isEmpty()) {
			queryResult = Ebean.createSqlQuery(
					"select t0.id,  t0.song_name, t0.song_original_title, t0.song_author, t0.song_link, t0.song_importer, t0.song_last_modified_by, t0.song_book_id, t0.date_created, t0.date_modified, u1.id as l_id from song t0 join song_lyrics u1 on u1.song_id = t0.id order by "
							+ sortBy + " " + order)
					.findList();
		} else {
			queryResult = Ebean
					.createSqlQuery(
							"(select t0.id,  t0.song_name, t0.song_original_title, t0.song_author, t0.song_link, t0.song_importer, t0.song_last_modified_by, t0.song_book_id, t0.date_created, t0.date_modified, u1.id as l_id from song t0 join song_lyrics u1 on u1.song_id = t0.id  where lower(t0.song_name) like :songnamefilter) UNION ALL"
									+ " (select t0.id,  t0.song_name, t0.song_original_title, t0.song_author, t0.song_link, t0.song_importer, t0.song_last_modified_by, t0.song_book_id, t0.date_created, t0.date_modified, u1.id as l_id from song t0 join song_lyrics u1 on u1.song_id = t0.id  where lower(t0.song_name) like :songnameinlinefilter) UNION ALL"
									+ " (select t0.id,  t0.song_name, t0.song_original_title, t0.song_author, t0.song_link, t0.song_importer, t0.song_last_modified_by, t0.song_book_id, t0.date_created, t0.date_modified, u1.id as l_id from song t0 join song_lyrics u1 on u1.song_id = t0.id  where lower(u1.song_lyrics) like :songlyricsfilter) UNION ALL"
									+ " (select t0.id,  t0.song_name, t0.song_original_title, t0.song_author, t0.song_link, t0.song_importer, t0.song_last_modified_by, t0.song_book_id, t0.date_created, t0.date_modified, u1.id as l_id from song t0 join song_lyrics u1 on u1.song_id = t0.id  where lower(t0.song_author) like :songauthorfilter)")
					.setParameter("songnamefilter", filter + "%")
					.setParameter("songnameinlinefilter", "%" + filter + "%")
					.setParameter("songlyricsfilter", "%" + filter + "%")
					.setParameter("songauthorfilter", "%" + filter + "%").findList();
		}

		Map<Long, SongTableData> songTableDataMap = new LinkedHashMap<Long, SongTableData>();

		for (SqlRow res : queryResult) {
			// System.out.println(res);

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
					ObjectNode songJson = SongToJsonConverter.convert(ts.getSong_name(), ts.getSong_link(),
							ts.getSong_original_title(), ts.getSong_author(), songId, ts.getSong_importer(),
							ts.getLyrics_id());
					an.add(songJson);
				}
				break;
			}
		}

		result.put("sEcho", Integer.valueOf(params.get("sEcho")[0]));
		result.put("iTotalRecords", iTotalRecords);
		result.put("iTotalDisplayRecords", iTotalDisplayRecords);

		return ok(result);
	}

	public Result songsuggestions() {
		/**
		 * Get needed params
		 */

		Map<String, String[]> params = request().queryString();
		String filter = params.get("q")[0].toLowerCase();
		// Logger.trace("Quick search song suggestion filter: " + filter);
		List<SqlRow> result = Ebean
				.createSqlQuery("(SELECT t0.id" + " FROM song t0"
						+ " WHERE lower(t0.song_name) like :songnamefilter) UNION ALL" + "(SELECT t0.id"
						+ " FROM song t0" + " WHERE lower(t0.song_name) like :songnameinlinefilter) UNION ALL"
						+ " (SELECT t0.id" + " FROM song t0" + " JOIN song_lyrics u1 on u1.song_id = t0.id"
						+ " WHERE lower(u1.song_lyrics) like :songlyricsfilter) UNION ALL" + "(SELECT t0.id"
						+ " FROM song t0" + " WHERE lower(t0.song_author) like :songauthorfilter)")
				.setParameter("songnamefilter", filter + "%").setParameter("songnameinlinefilter", "%" + filter + "%")
				.setParameter("songlyricsfilter", "%" + filter + "%")
				.setParameter("songauthorfilter", "%" + filter + "%").findList();
		ArrayList<Long> ids = new ArrayList<>();
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
			response().setHeader(CONTENT_TYPE,
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document");
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

		JsonNode jsonNode = request().body().asJson();
		List<SongPrint> songsForPrint = new ArrayList<>();
		DocxGenerator docWriter = null;
		Logger.trace("Songbook generator json string: " + jsonNode);
		ObjectMapper mapper = new ObjectMapper();
		String format = "word";

		boolean publishService = false;
		String songBookName = null;

		try {
			JsonSongbook jsonSongbook = mapper.treeToValue(jsonNode, JsonSongbook.class);
			format = jsonSongbook.getFormat();
			Map<String, Object> additionalProperties = jsonSongbook.getAdditionalProperties();
			if (additionalProperties != null) {
				if (additionalProperties.get("publishService") != null) {
					publishService = Boolean.parseBoolean(additionalProperties.get("publishService").toString());
				}
				if (additionalProperties.get("songBookName") != null) {
					songBookName = additionalProperties.get("songBookName").toString();
				}
			}
			List<models.json.Song> songsJson = jsonSongbook.getSongs();
			for (models.json.Song songJson : songsJson) {
				songsForPrint.add(new SongPrint(Song.get(Long.parseLong(songJson.getSong().getId())),
						Long.parseLong(songJson.getSong().getLyricsID()), songJson.getSong().getKey()));
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

		// Random rand = new Random();
		// int hash = rand.nextInt(100);

		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_hhmmss");
		Date date = new Date();
		String hash = (dateFormat.format(date));

		try {
			if ("word".equals(format)) {
				docWriter.newSongbookWordDoc(hash, songsForPrint);
			} else if ("pdf".equals(format)) {
				String outputPdfPath = "resources/pdf/" + hash + ".pdf";
				try {
					Logger.debug("Writing PDF: " + outputPdfPath);
					PdfGenerator.writeListContent(outputPdfPath, songsForPrint);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			Logger.error("Failed to generate document "+format);
			e.printStackTrace();
		}

		// this should maybe made async somehow, because it is not crucial for
		// this response
		if (!("Guest".equals(user.name)) && publishService) {
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
				Logger.debug("Publishing service: " + service.getDateCreated());
			} catch (Exception e) {
				Logger.error("Failed to publish service");
				e.printStackTrace();
			}
		}
		return ok(hash);
	}

	public Result generateService(Long id) {

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

		Service service = Service.find.byId(id);

		ArrayList<PdfPrintable> songPrintList = new ArrayList<PdfPrintable>();

		// Manual sorting because of JPA OrderBy bidirectional relationship bug
		Collections.sort(service.getSongs());

		for (ServiceSong serviceSong : service.getSongs()) {
			songPrintList.add(serviceSong);
		}
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy_hhmm");
		String date = (df.format(service.getDateCreated()));

		String normalizedFileName = service.serviceName.replaceAll("\\W+", "");

		String outputPdfPath = "resources/pdf/" + normalizedFileName + "_" + date + ".pdf";
		try {
			Logger.debug("Writing PDF: " + outputPdfPath);
			PdfGenerator.writeListContent(outputPdfPath, songPrintList);
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

	public Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(Routes.javascriptRouter("jsRoutes", controllers.routes.javascript.Application.songview(),
				controllers.routes.javascript.Application.login(),
				controllers.routes.javascript.Application.deletesong(),
				controllers.routes.javascript.Application.getsongjson(),
				controllers.routes.javascript.Application.songeditor(),
				controllers.routes.javascript.Application.songsuggestions(),
				controllers.routes.javascript.Application.getsonglyricsjson(),
				controllers.routes.javascript.Application.updatesonglyricsjson(),
				controllers.routes.javascript.Application.services(),
				controllers.routes.javascript.Application.generateService(),
				controllers.routes.javascript.Application.deleteservice(),
				controllers.routes.javascript.Application.upload(), controllers.routes.javascript.Application.addUser(),
				controllers.routes.javascript.Application.getUser(),
				controllers.routes.javascript.Application.deleteUser(),
				controllers.routes.javascript.Application.updateUser()));
	}

	public Result test() {
		System.out.println("TEST!");
		ChordLineTransposer.test();
		return ok();
	}

	@Security.Authenticated(Secured.class)
	public Result sanitizesongs() {
		System.out.println("sanitizesongs!");
		/*
		 * for (SongLyrics sl : SongLyrics.all()) { StringBuilder sb = new
		 * StringBuilder(); // removing all tabs String sanitizedLyrics String[]
		 * lines = sl.getsongLyrics().split("\n"); for (String line : lines) {
		 * sb.append(StringUtils.stripEnd(line," ")); sb.append("\n"); } //
		 * String sanitizedLyrics = sl.getsongLyrics().trim();
		 * sl.setsongLyrics(sb.toString()); sl.save(); }
		 */

		// Logger.debug("Found PLAY_SESSION cookie");
		String cookieVal = request().cookies().get("PLAY_SESSION").value();
		String userId = cookieVal.substring(cookieVal.indexOf("email=") + 6).replace("%40", "@");
		UserAccount ua = null;
		if (userId != null) {
			ua = UserAccount.find.byId(userId);
		}

		// Sanitizing all songs
		for (Song s : Song.all()) {
			s.setSongLastModifiedBy(ua.name.toString());
			Song.updateOrCreateSong(s);
		}

		return redirect(routes.Application.index());
	}

}
