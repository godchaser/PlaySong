package models;

import java.util.*;

import javax.persistence.*;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import static java.util.Collections.*;

/**
 * Created by samuel on 19.02.15..
 */
@Entity
public class Song extends Model implements Comparator<Song>{

    @Id
    public Long id;

    @Required
    public String songName;

    public String songOriginalTitle;

    public String songAuthor;

    public String songLink;

    public String songImporter;

    @OneToMany(mappedBy="song",cascade= CascadeType.ALL)
    public List<SongLyrics> songLyrics  = new ArrayList<>();

    public static List<Song> all() {
        return find.all();
    }

    public static Song get(Long id){
        return find.byId(id);
    }

    public static void updateOrCreateSong(Song song) {
        //delete empty lyrics
        List removedList = new ArrayList();
        for (int i=0;i<song.songLyrics.size();i++){
            if (song.songLyrics.get(i).getsongLyrics().length()<2){
                removedList.add(song.songLyrics.get(i));
            }
        }
        song.songLyrics.removeAll(removedList);
        if (song.id>0){
            song.update();
        } else {
            song.id = null;
            song.save();
        }
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static boolean contains(Long id) {
        return find.ref(id).contains(id);
    }


    public static Finder<Long, Song> find = new Finder(Long.class, Song.class);

    @Override
    public int compare(Song song1, Song song2) {
        return song1.songName.compareTo(song2.songName);
    }
}