package controllers;

import javax.inject.Inject;

import com.avaje.ebean.Ebean;

import models.Song;
import models.UserAccount;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import views.html.admin;

public class Users extends Controller {

    private Form<UserAccount> userForm;

    @Inject
    public Users(FormFactory formFactory) {
        this.userForm = formFactory.form(UserAccount.class);
    }

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
        return redirect(controllers.routes.Application.admin());
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
