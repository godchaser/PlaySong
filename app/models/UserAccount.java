package models;

import javax.persistence.*;
import play.db.ebean.*;

@Entity
public class UserAccount extends Model {

    @Id
    public String email;
    public String name;
    public String password;

    public UserAccount(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
    }

    public static Finder<String, UserAccount> find = new Finder<String, UserAccount>(
            String.class, UserAccount.class
    );

    public static UserAccount authenticate(String email, String password) {
        return find.where().eq("email", email)
                .eq("password", password).findUnique();
    }
}