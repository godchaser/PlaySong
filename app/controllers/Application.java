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
import models.helpers.SongPrint;
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

import com.avaje.ebean.Expr;
import com.avaje.ebean.Page;

import java.util.AbstractMap.SimpleEntry;

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
		Html welcome = new Html("");
		UserAccount user = null;
		if (request().cookies().get("PLAY_SESSION") != null) {
			Logger.debug("Found PLAY_SESSION cookie");
			String cookieVal = request().cookies().get("PLAY_SESSION").value();
			String userId = cookieVal.substring(cookieVal.indexOf("email=") + 6).replace("%40", "@");
			Logger.debug("PLAY_SESSION User ID: " + userId);
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
			String fileName = uploadedFile.getFilename();
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
			Logger.debug("Found PLAY_SESSION cookie");
			String cookieVal = request().cookies().get("PLAY_SESSION").value();
			String userId = cookieVal.substring(cookieVal.indexOf("email=") + 6).replace("%40", "@");
			Logger.debug("PLAY_SESSION User ID: " + userId);
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
			Logger.debug("Found PLAY_SESSION cookie");
			String cookieVal = request().cookies().get("PLAY_SESSION").value();
			String userId = cookieVal.substring(cookieVal.indexOf("email=") + 6).replace("%40", "@");
			Logger.debug("PLAY_SESSION User ID: " + userId);
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
		Html welcome = new Html("");
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
			Logger.debug("Found PLAY_SESSION cookie");
			String cookieVal = request().cookies().get("PLAY_SESSION").value();
			String userId = cookieVal.substring(cookieVal.indexOf("email=") + 6).replace("%40", "@");
			Logger.debug("PLAY_SESSION User ID: " + userId);
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
		return ok();
	}

	@Security.Authenticated(Secured.class)
	public static Result newsong() {
		Form<Song> filledForm = songForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.error.render());
		} else {
			System.out.println("updateOrCreateSong ");
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

		Integer iTotalRecords = Song.find.findRowCount();
		String filter = params.get("sSearch")[0];
		Integer pageSize = Integer.valueOf(params.get("iDisplayLength")[0]);
		Integer page = Integer.valueOf(params.get("iDisplayStart")[0]) / pageSize;

		/**
		 * Get sorting order and column
		 */
		String sortBy = "songName";
		String order = params.get("sSortDir_0")[0];

		switch (Integer.valueOf(params.get("iSortCol_0")[0])) {
		case 0:
			sortBy = "songName";
			break;
		case 1:
			sortBy = "songOriginalTitle";
			break;
		case 2:
			sortBy = "songAuthor";
			break;
		}

		/**
		 * Get page to show from database It is important to set setFetchAhead
		 * to false, since it doesn't benefit a stateless application at all.
		 */
		Page<Song> songPage = Song.find
				.where(Expr.or(Expr.ilike("songName", "%" + filter + "%"),
						Expr.or(Expr.ilike("songAuthor", "%" + filter + "%"),
								Expr.icontains("songLyrics.songLyrics", "%" + filter + "%"))))
				.orderBy(sortBy + " " + order).findPagingList(pageSize).setFetchAhead(false).getPage(page);
		Integer iTotalDisplayRecords = songPage.getTotalRowCount();

		/**
		 * Construct the JSON to return
		 */
		ObjectNode result = Json.newObject();

		result.put("sEcho", Integer.valueOf(params.get("sEcho")[0]));
		result.put("iTotalRecords", iTotalRecords);
		result.put("iTotalDisplayRecords", iTotalDisplayRecords);

		ArrayNode an = result.putArray("aaData");

		for (Song s : songPage.getList()) {
			ObjectNode songJson = SongToJson.convert(s);
			an.add(songJson);
		}
		return ok(result);
	}

	public static Result songsuggestions() {
		/**
		 * Get needed params
		 */
		// TODO: check for null pointers
		// dont send whole song object, only songname and id
		Map<String, String[]> params = request().queryString();
		String filter = params.get("q")[0];
		// Logger.info("Filter param" + filter);
		/**
		 * Get sorting order and column
		 */
		String sortBy = "songName";
		List<Song> songs = Song.find.where(Expr.or(Expr.ilike("songName", "%" + filter + "%"),
				Expr.or(Expr.ilike("songAuthor", "%" + filter + "%"),
						Expr.icontains("songLyrics.songLyrics", "%" + filter + "%"))))
				.findList();

		List<SimpleEntry> songSuggestionsList = new ArrayList<>();
		for (Song song : songs) {
			songSuggestionsList.add(new SimpleEntry(song.id, song.songName));
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
		Logger.debug("Authenticate redirect url: " + redirecturl);
		if (loginForm.hasErrors()) {
			if (redirecturl != null) {
				flash().put("url", redirecturl);
			}
			return badRequest(login.render(loginForm));
		} else {
			session().clear();
			session("email", loginForm.get().email);
			redirecturl = loginForm.get().redirecturl;
			Logger.debug("Authenticate form redirect url: " + redirecturl);
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
		 * // Sanitizing all lyrics for (SongLyrics sl : SongLyrics.all()) {
		 * //removing all tabs String sanitizedLyrics =
		 * sl.getsongLyrics().replaceAll("\\t", "    ");
		 * sl.setsongLyrics(sanitizedLyrics); sl.save(); }
		 */
		// Sanitizing all songs
		for (Song s : Song.all()) {
			Song.updateOrCreateSong(s);
		}

		return ok();
	}

}
