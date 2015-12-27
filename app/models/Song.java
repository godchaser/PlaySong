package models;

import java.util.*;

import javax.persistence.*;

import models.helpers.SongSuggestion;
import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;

/**
 * Created by samuel on 19.02.15..
 */
@Entity
public class Song extends Model implements Comparator<Song> {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long id;

	@Required
	public String songName;

	public String songOriginalTitle;

	public String songAuthor;

	public String songLink;

	public String songImporter;

	public String songLastModifiedBy;

	public int songBookId;

	@Column(updatable = false)
	@Formats.DateTime(pattern = "dd/MM/yyyy hh:mm")
	public Date dateCreated = new Date();

	@Formats.DateTime(pattern = "dd/MM/yyyy hh:mm")
	public Date dateModified = new Date();

	@OneToMany(mappedBy = "song", cascade = CascadeType.ALL)
	public List<SongLyrics> songLyrics = new ArrayList<>();

	public static List<Song> all() {
		return find.all();
	}

	public static int getNumberOfSongsInDatabase() {
		return find.all().size();
	}

	public static Song get(Long id) {
		return find.byId(id);
	}

	public static void updateOrCreateSong(Song song) {
		// delete empty lyrics
		List<SongLyrics> removedList = new ArrayList<SongLyrics>();
		for (int i = 0; i < song.songLyrics.size(); i++) {
			if (song.songLyrics.get(i).getsongLyrics().length() < 2) {
				removedList.add(song.songLyrics.get(i));
			}
		}
		song.songLyrics.removeAll(removedList);

		// song must have lyrics
		if (song.songLyrics.size() > 0) {
			for (SongLyrics songLyrics : song.songLyrics) {
				songLyrics.updateSongKeys();
				songLyrics.sanitizeLyrics();
			}
			if (song.id != null && song.id > 0) {
				// DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
				Date date = new Date();
				song.setDateModified(date);
				song.update();
			} else {
				song.id = null;
				// DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
				Date date = new Date();
				song.setDateCreated(date);
				song.save();
			}
			Logger.debug("Song updated by user: " + song.songLastModifiedBy);
			Logger.debug("Song updated on: " + song.getDateModified().toString());
		} else {
			Logger.debug("Will not save song without lyrics");
		}
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static Finder<Long, Song> find = new Finder<>(Song.class);

	@Override
	public int compare(Song song1, Song song2) {
		return song1.songName.compareTo(song2.songName);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<SongLyrics> getSongLyrics() {
		return songLyrics;
	}

	public void setSongLyrics(List<SongLyrics> songLyrics) {
		this.songLyrics = songLyrics;
	}

	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}

	public String getSongOriginalTitle() {
		return songOriginalTitle;
	}

	public void setSongOriginalTitle(String songOriginalTitle) {
		this.songOriginalTitle = songOriginalTitle;
	}

	public String getSongAuthor() {
		return songAuthor;
	}

	public void setSongAuthor(String songAuthor) {
		this.songAuthor = songAuthor;
	}

	public String getSongLink() {
		return songLink;
	}

	public void setSongLink(String songLink) {
		this.songLink = songLink;
	}

	public String getSongImporter() {
		return songImporter;
	}

	public void setSongImporter(String songImporter) {
		this.songImporter = songImporter;
	}

	public String getSongLastModifiedBy() {
		return songLastModifiedBy;
	}

	public void setSongLastModifiedBy(String songLastModifiedBy) {
		this.songLastModifiedBy = songLastModifiedBy;
	}

	public int getSongBookId() {
		return songBookId;
	}

	public void setSongBookId(int songBookId) {
		this.songBookId = songBookId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateModified() {
		return dateModified;
	}

	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}

	public static List<SongSuggestion> getSongModifiedList() {

		int minusMonth = 1;

		Calendar calNow = Calendar.getInstance();
		// adding -1 month
		calNow.add(Calendar.MONTH, -minusMonth);
		Date dateBeforeAMonth = calNow.getTime();

		Date dateNow = Calendar.getInstance().getTime();

		List<Song> songsModifiedInLastMonth = Song.find.where().between("date_modified", dateBeforeAMonth, dateNow)
				.orderBy("date_modified desc").findList();

		List<SongSuggestion> songModifiedList = new ArrayList<>();
		for (Song song : songsModifiedInLastMonth) {
			songModifiedList.add(new SongSuggestion(song.getId(), song.getSongName(), song.getDateModified()));
		}
		return songModifiedList;
	}

	public static List<SongSuggestion> getSongCreatedList() {

		int minusMonth = 1;

		Calendar calNow = Calendar.getInstance();
		// adding -1 month
		calNow.add(Calendar.MONTH, -minusMonth);
		Date dateBeforeAMonth = calNow.getTime();

		Date dateNow = Calendar.getInstance().getTime();

		List<Song> songsCreatedInLastMonth = Song.find.where().between("date_created", dateBeforeAMonth, dateNow)
				.orderBy("date_created desc").findList();

		List<SongSuggestion> songCreatedList = new ArrayList<>();
		for (Song song : songsCreatedInLastMonth) {
			songCreatedList.add(new SongSuggestion(song.getId(), song.getSongName(), song.getDateModified()));
		}
		return songCreatedList;
	}

}