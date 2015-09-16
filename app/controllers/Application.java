package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import controllers.chords.ChordLineTransposer;
import controllers.songbook.XLSHelper;
import models.Song;
import models.SongLyrics;
import models.UserAccount;
import models.helpers.ArrayHelper;
import models.helpers.SongPrint;
import models.helpers.SongTableData;
import models.helpers.XMLSongsParser;
import models.json.JsonSongbook;
import play.Logger;
import play.Routes;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Security;
import views.html.*;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.Collator;
import java.util.*;

import com.avaje.ebean.SqlRow;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import play.twirl.api.Html;

import static play.data.Form.form;

public class Application extends Controller {

	static Form<Song> songForm = form(Song.class);
	static Form<UserAccount> userForm = form(UserAccount.class);
	public final static Locale HR_LOCALE = new Locale("HR");
	public final static Collator HR_COLLATOR = Collator.getInstance(HR_LOCALE);

	public static class Login {

		public String email;
		public String password;
		public String redirecturl;

		public String validate() {
			if (UserAccount.authenticate(email, password) == null) {
				return "Invalid user or password";
			}
			return null;
		}
	}

	public static Result index() {
		return redirect(routes.Application.table());
	}

	@Security.Authenticated(Secured.class)
	public static Result admin() {
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
		return ok(admin.render(user, userForm, UserAccount.find.all(), message));
	}

	@Security.Authenticated(Secured.class)
	public static Result upload() {
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
			XLSHelper.importAndUpdateSongs3();

			return redirect(routes.Application.admin());
		} else {
			String message = "File not uploaded - missing  file";
			Logger.trace(message);
			return redirect(routes.Application.admin());
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result addUser() {
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
			return badRequest(admin.render(user, userForm, UserAccount.find.all(), message));
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
	public static Result getUser(String email) {
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
	public static Result deleteUser(String email) {
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
	public static Result updateUser(String email) {
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
	public static Result getXLS() {
		XLSHelper.dumpSongs((Song.find.all()));
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

	public static Result table() {
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
		return ok(table.render(Song.getNumberOfSongsInDatabase(), user));
	}

	@Security.Authenticated(Secured.class)
	public static Result songeditor(Long id) {
		UserAccount user = null;
		if (request().username() != null) {
			user = UserAccount.find.byId(request().username());
		}
		if (user == null) {
			user = new UserAccount("Guest", "", "");
		}
		return ok(songeditor.render(id, songForm, user));
	}

	@Security.Authenticated(Secured.class)
	public static Result newsongeditor() {
		Long id = -1L;
		return redirect(routes.Application.songeditor(id));
	}

	public static Result songview(Long id) {
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
		return ok(songviewer.render(id, user));
	}

	public static Result songbook() {
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
		return ok(songbook.render(sortedSongs, user));
	}

	@Security.Authenticated(Secured.class)
	public static Result getsongs() {
		return ok(Json.toJson(Song.all()));
	}

	@Security.Authenticated(Secured.class)
	public static Result getsongdata() {
		List<Song> songs = Song.all();
		ArrayList<ObjectNode> songsJson = new ArrayList<>();

		for (Song s : songs) {
			ObjectNode songJson = SongToJson.convert(s);
			songsJson.add(songJson);
		}
		return ok(Json.toJson(songsJson));
	}

	@Security.Authenticated(Secured.class)
	public static Result updateFromOnlineSpreadsheet() {
		JsonNode data = request().body().asJson();
		Logger.debug("Online spreadsheet data: " + data.asText());
		return ok();
	}

	public static Result getsongjson(Long id) {
		Song s = Song.get(id);
		ObjectNode songJson = SongToJson.convert(s);
		return ok(Json.toJson(songJson));
	}

	public static Result getsonglyricsjson(Long id) {
		SongLyrics lyricsObject = SongLyrics.find.byId(id);
		String lyrics = lyricsObject.getsongLyrics();
		ObjectNode lyricsResult = Json.newObject();
		lyricsResult.put("songLyrics", lyrics);
		return ok(lyricsResult);
	}

	@Security.Authenticated(Secured.class)
	public static Result deletesong(Long id) {
		Song.delete(id);
		return redirect(routes.Application.table());
	}

	@Security.Authenticated(Secured.class)
	public static Result newsong() {
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
	public static Result emptyDb() {
		Ebean.createSqlUpdate("delete from song_lyrics").execute();
		Ebean.createSqlUpdate("delete from song").execute();
		return ok();
	}

	@Security.Authenticated(Secured.class)
	public static Result init() {
		try {
			// SongImporter.restoreFromSQLDump();
			SongImporter.importFromDb();
			XLSHelper.importAndUpdateSongs();
		} catch (Exception e) {
			Logger.error("Exception occured during init" + e.getStackTrace());
			e.printStackTrace();
			System.out.print(e.getStackTrace());
			System.out.print(e.getMessage());
		}
		return redirect(routes.Application.index());
	}

	public static Result inituser() {
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

	public static Result songs() {
		return ok(songs.render(Song.all()));
	}

	@Security.Authenticated(Secured.class)
	public static Result yamlbackup() {
		System.out.println("yamlbackup!");
		SongImporter.songToYaml();
		return redirect(routes.Application.index());
	}

	@Security.Authenticated(Secured.class)
	public static Result yamlrestore() {
		System.out.println("yamlrestore!");
		SongImporter.yamlToSong();
		return redirect(routes.Application.index());
	}

	@Security.Authenticated(Secured.class)
	public static Result sqlinit() {
		System.out.println("SQL INIT!");
		Ebean.delete(Song.all());
		SongImporter.restoreFromSQLDump();
		return redirect(routes.Application.table());
	}

	@Security.Authenticated(Secured.class)
	public static Result xmlupdate() {
		XMLSongsParser.updateFromXML();
		return ok();
	}

	@Security.Authenticated(Secured.class)
	public static Result updateFromXLS() {
		XLSHelper.importAndUpdateSongs();
		return ok();
	}

	public static Result getsongsdatatable() {
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
					ObjectNode songJson = SongToJson.convert(ts.getSong_name(), ts.getSong_link(),
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

	public static Result songsuggestions() {
		/**
		 * Get needed params
		 */

		Map<String, String[]> params = request().queryString();
		String filter = params.get("q")[0].toLowerCase();

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
		List<SimpleEntry<Long, String>> songSuggestionsList = new ArrayList<>();
		for (Long id : ids) {
			songSuggestionsList.add(new SimpleEntry<Long, String>(id, Song.get(id).getSongName()));
		}

		return ok(Json.toJson(songSuggestionsList));
	}

	public static Result downloadAndDeleteFile() {
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
			tmpFile = new File("resources/" + hashValue + ".docx");
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

	public static Result generateSongbook() {
		JsonNode jsonNode = request().body().asJson();
		ArrayList<SongPrint> songsForPrint = new ArrayList<>();
		DocumentWriter docWriter = null;
		Logger.trace("Songbook generator json string: " + jsonNode);
		ObjectMapper mapper = new ObjectMapper();
		String format = "word";

		try {
			JsonSongbook jsonSongbook = mapper.treeToValue(jsonNode, JsonSongbook.class);
			format = jsonSongbook.getFormat();
			List<models.json.Song> songsJson = jsonSongbook.getSongs();
			for (models.json.Song songJson : songsJson) {
				songsForPrint.add(new SongPrint(Song.get(Long.parseLong(songJson.getSong().getId())),
						Long.parseLong(songJson.getSong().getLyricsID()), songJson.getSong().getKey()));
			}
			if ("word".equals(format)) {
				docWriter = new DocumentWriter();
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

		Random rand = new Random();
		int hash = rand.nextInt(100);

		try {
			if ("word".equals(format)) {
				docWriter.newSongbookWordDoc(Integer.toString(hash), songsForPrint);
			} else if ("pdf".equals(format)) {
				// String in = "resources/" + Integer.toString(hash) + ".docx";
				// String out = "resources/" + Integer.toString(hash) + ".pdf";
				// PdfConverter.convert(in, out);
				String outputPdfPath = "resources/pdf/" + Integer.toString(hash) + ".pdf";
				try {
					Logger.debug("Writing PDF: " + outputPdfPath);
					PdfGenerator.writeSongs(outputPdfPath, songsForPrint);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ok(Integer.toString(hash));
	}

	public static Result login() {
		String redirecturl = flash().get("url");
		Logger.debug("Login flash redirect url: " + redirecturl);
		if (redirecturl != null) {
			flash().put("url", redirecturl);
		}
		return ok(login.render(form(Login.class)));
	}

	public static Result authenticate() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		String redirecturl = request().body().asFormUrlEncoded().get("redirecturl")[0];
		Logger.debug("Authenticate forwarded redirect url: " + redirecturl);
		if (loginForm.hasErrors()) {
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
			Logger.debug("Redirecting to: " + redirecturl);
			return redirect(redirecturl);
		}
	}

	public static Result logout() {
		if (request().cookies().get("PLAY_SESSION") != null) {
			String cookieVal = request().cookies().get("PLAY_SESSION").value();
			String userId = cookieVal.substring(cookieVal.indexOf("email=") + 6).replace("%40", "@");
			Logger.debug("User logged out: " + userId);
		}
		session().clear();
		flash("success", "You've been logged out");
		return redirect(routes.Application.index());
	}

	public static Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(Routes.javascriptRouter("jsRoutes", controllers.routes.javascript.Application.songview(),
				controllers.routes.javascript.Application.login(),
				controllers.routes.javascript.Application.deletesong(),
				controllers.routes.javascript.Application.getsongjson(),
				controllers.routes.javascript.Application.songeditor(),
				controllers.routes.javascript.Application.songsuggestions(),
				controllers.routes.javascript.Application.getsonglyricsjson(),
				controllers.routes.javascript.Application.upload(), controllers.routes.javascript.Application.addUser(),
				controllers.routes.javascript.Application.getUser(),
				controllers.routes.javascript.Application.deleteUser(),
				controllers.routes.javascript.Application.updateUser()));
	}

	public static Result test() {
		System.out.println("TEST!");
		ChordLineTransposer.test();
		return ok();
	}

	@Security.Authenticated(Secured.class)
	public static Result sanitizesongs() {
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
