package controllers;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import models.Playlist;
import models.Song;
import models.SongBook;
import models.UserAccount;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.routing.JavaScriptReverseRouter;
import views.html.admin;
import views.html.login;
import views.html.playlistmaker;
import views.html.playlists;
import views.html.songeditor;
import views.html.songviewer;
import views.html.table;

public class Application extends Controller {
    private Form<Song> songForm;
    private Form<UserAccount> userForm;
    private Form<Login> loginForm;
    private Locale HR_LOCALE;
    private Collator HR_COLLATOR;

    @Inject
    public Application(FormFactory formFactory) {
        this.songForm = formFactory.form(Song.class);
        this.userForm = formFactory.form(UserAccount.class);
        this.loginForm = formFactory.form(Login.class);
        HR_LOCALE = new Locale("HR");
        HR_COLLATOR = Collator.getInstance(HR_LOCALE);
        HR_COLLATOR.setStrength(Collator.PRIMARY);
    }

    public Result index() {
        return redirect(controllers.routes.Application.table());
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
    public Result songeditor(String id) {
        UserAccount user = getUserFromCookie();
        return ok(songeditor.render(id, songForm, user, Song.getSongModifiedList(), Song.getSongCreatedList(), user.getSongbooks()));
    }

    public Result songview(String id) {
        UserAccount user = getUserFromCookie();
        // Logger.debug("song view id: "+ id);
        return ok(songviewer.render(Song.get(id), user, Song.getSongModifiedList(), Song.getSongCreatedList()));
    }

    public Result playlistmaker(String id) {
        UserAccount user = getUserFromCookie();

        // switch songbooks according to id - but should also check credentials first to account the owner
        List<SongBook> songbooks = user.getSongbooks();

        // get default songbook
        SongBook filteredSongbook = SongBook.get(SongBook.DEFAULT_SONGBOOK_ID);

        // if db empty imediately return answer
        boolean databaseIsEmpty = (songbooks.isEmpty() && (filteredSongbook == null)) ? true : false;
        if (databaseIsEmpty) {
            Logger.debug("PlayListMaker: Database is empty");
            return ok(playlistmaker.render(new ArrayList<Song>(), new ArrayList<SongBook>(), id, user, Song.getSongModifiedList(), Song.getSongCreatedList()));
        }

        songbooks.addAll(SongBook.getAllPublicSongbooks());
        // remove duplicates
        Set<SongBook> songbooksWithoutDuplicates = new LinkedHashSet<>(songbooks);

        for (SongBook songbook : songbooksWithoutDuplicates) {
            Logger.debug("Checking songbook: " + songbook.getId() + " with matched Id: " + id);
            if (songbook.getId().equals(id)) {
                Logger.debug("Found songbook match by ID: " + id);
                filteredSongbook = songbook;
                break;
            }
        }

        List<Song> sortedSongs = new ArrayList<Song>();
        if (filteredSongbook != null) {
            sortedSongs = filteredSongbook.getSongs();
            Collections.sort(sortedSongs, new Comparator<Song>() {
                @Override
                public int compare(Song s1, Song s2) {
                    return HR_COLLATOR.compare(s1.songName, s2.songName);
                }
            });
        }

        return ok(playlistmaker.render(sortedSongs, new ArrayList<SongBook>(songbooksWithoutDuplicates), id, user, Song.getSongModifiedList(), Song.getSongCreatedList()));
    }

    public Result playlists() {
        UserAccount user = getUserFromCookie();

        List<Playlist> playlistList = Playlist.find.all();
        // sort by date created
        Collections.sort(playlistList, new Comparator<Playlist>() {
            public int compare(Playlist o1, Playlist o2) {
                if (o1.getDateCreated() == null || o2.getDateCreated() == null)
                    return 0;
                return o1.getDateCreated().compareTo(o2.getDateCreated());
            }
        });

        // Manual sorting because of JPA OrderBy bidirectional relationship bug
        for (Playlist playlist : playlistList) {
            Collections.sort(playlist.getSongs());
        }

        return ok(playlists.render(playlistList, user, Song.getSongModifiedList(), Song.getSongCreatedList()));
    }

    public Result login() {
        String redirecturl = flash().get("url");
        // Logger.debug("Login flash redirect url: " + redirecturl);
        if (redirecturl != null) {
            flash().put("url", redirecturl);
        }
        return ok(login.render(loginForm));
    }

    public Result logout() {
        if (request().cookies().get("PLAY_SESSION") != null) {
            String cookieVal = request().cookies().get("PLAY_SESSION").value();
            String userId = cookieVal.substring(cookieVal.indexOf("email=") + 6).replace("%40", "@");
            Logger.debug("User logged out: " + userId);
        }
        session().clear();
        flash("success", "You've been logged out");
        return redirect(controllers.routes.Application.index());
    }

    public Result authenticate() {
        Form<Login> filledLoginForm = loginForm.bindFromRequest();
        String redirecturl = request().body().asFormUrlEncoded().get("redirecturl")[0];
        String email = request().body().asFormUrlEncoded().get("email")[0];
        if (!redirecturl.isEmpty() || "null".equals(redirecturl)) {
            Logger.debug("Authenticate forwarded redirect url: " + redirecturl);
        }
        if (filledLoginForm.hasErrors()) {
            Logger.debug("Failed login for: " + email);
            if (redirecturl != null) {
                flash().put("url", redirecturl);
            }
            return badRequest(login.render(filledLoginForm));
        } else {
            session().clear();
            session("email", filledLoginForm.get().email);
            redirecturl = filledLoginForm.get().redirecturl;
            if (redirecturl.isEmpty()) {
                redirecturl = "/";
            }
            Logger.debug("Successfull login for: " + email);
            // Logger.debug("Redirecting to: " + redirecturl);
            return redirect(redirecturl);
        }
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

    //@formatter:off
    public Result javascriptRoutes() {
        return ok(JavaScriptReverseRouter.create("jsRoutes", 
                controllers.routes.javascript.Application.songview(), 
                controllers.routes.javascript.Application.login(),
                controllers.routes.javascript.Application.songeditor(),
                controllers.routes.javascript.Application.playlistmaker(), 
                controllers.routes.javascript.Application.playlists(),
                controllers.routes.javascript.Songs.deletesong(), 
                controllers.routes.javascript.Songs.songsuggestions(), 
                controllers.routes.javascript.Playlists.downloadPlaylist(), 
                controllers.routes.javascript.Playlists.generatePlaylist(), 
                controllers.routes.javascript.Playlists.deletePlayList(), 
                controllers.routes.javascript.Users.addUser(), 
                controllers.routes.javascript.Users.getUser(), 
                controllers.routes.javascript.Users.deleteUser(),
                controllers.routes.javascript.Users.updateUser(),
                controllers.routes.javascript.Operations.upload(),
                controllers.routes.javascript.Rest.getsongjson(),
                controllers.routes.javascript.Rest.getsonglyricsjson(), 
                controllers.routes.javascript.Rest.getsonglyricsjson(),
                controllers.routes.javascript.Rest.updatesonglyricsjson()
                ));
    }
   //@formatter:on

}
