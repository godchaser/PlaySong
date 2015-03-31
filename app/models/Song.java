package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

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

    public String songOriginalTitle;

    public String songAuthor;

    public String songLink;

    @OneToMany(mappedBy="song",cascade= CascadeType.ALL)
    public Set<SongLyrics> songLyrics  = new HashSet<>();

    public static List<Song> all() {
        return find.all();
    }

    public static Song get(Long id){
        return find.byId(id);
    }

    public static void create(Song song) {
        song.save();
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static Finder<Long, Song> find = new Finder(Long.class, Song.class);
}