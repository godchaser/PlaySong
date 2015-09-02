package models;

import javax.persistence.*;

import play.data.validation.Constraints.Required;
import play.db.ebean.*;

@Entity
public class UserAccount extends Model {

	@Id
	@Required
	public String email;
	@Required
	public String name;
	@Required
	public String password;

	public UserAccount(String email, String name, String password) {
		this.email = email;
		this.name = name;
		this.password = password;
	}

	public static Finder<String, UserAccount> find = new Finder<String, UserAccount>(String.class, UserAccount.class);

	public static UserAccount authenticate(String email, String password) {
		return find.where().eq("email", email).eq("password", password).findUnique();
	}

	public static String getNameFromEmail(String email) {
		return find.where().eq("email", email).findUnique().name;
	}
}