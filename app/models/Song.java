package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

/**
 * Created by samuel on 19.02.15..
 */
@Entity
public class Song extends Model {

    @Id
    public Long id;

    @Required
    public String songName;

    public String songAuthor;

    @Column(columnDefinition = "TEXT")
    public String songLyrics;

    public static List<Song> all() {
        return find.all();
    }

    public static Song getSong(Long id){
        return find.byId(id);
    }

    public static String getsongName(Long id) {
        return find.ref(id).songName;
    }

    public static String getsongAuthor(Long id) {
        return find.ref(id).songAuthor;
    }

    public static String getsongLyrics(Long id) {
        return find.ref(id).songLyrics;
    }

    public static void create(Song song) {
        song.save();
    }
    /*
    public static List<Song> searchLyrics(){
        // More complex song query
        /*
        List<Song> songs= find.where()
                .ilike("name", "%coco%")
                .orderBy("dueDate asc")
                .findPagingList(25)
                .getPage(1);

        return songs;
    }
    */
    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static Finder<Long, Song> find = new Finder(Long.class, Song.class);
}
