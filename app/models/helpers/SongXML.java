package models.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.eclipse.persistence.oxm.annotations.XmlPath;
/**
 * Created by samuel on 4/7/15.
 */
//@XmlRootElement(name="div")
//@XmlType(propOrder={"firstName", "lastName", "address", "phoneNumbers"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SongXML {
	
	    @XmlPath("p[@name='id']/text()")
	    private String id;
	    
	    @XmlPath("p[@name='songName']/text()")
	    private String songName;
	 
	    @XmlPath("p[@name='songOriginalTitle']/text()")
	    private String songOriginalTitle;
	    
	    @XmlPath("p[@name='songAuthor']/text()")
	    private String songAuthor;
	    
	    @XmlPath("p[@name='songLink']/text()")
	    private String songLink;
	    
	    @XmlPath("p[@name='songImporter']/text()")
	    private String songImporter;
	    
	    @XmlPath("p[@name='songLastModifiedBy']/text()")
	    private String songLastModifiedBy;
	    
	    @XmlPath("p[@name='dateCreated']/text()")
	    private String dateCreated;   

	    @XmlPath("p[@name='dateModified']/text()")
	    private String dateModified;
	    
	    @XmlPath("p[@name='songBookId']/text()")
	    private String songBookId;
	    
	    @XmlPath("span[@name='lyrics']")
	    private List<SongLyricsXML> lyrics;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
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

		public String getDateCreated() {
			return dateCreated;
		}

		public void setDateCreated(String dateCreated) {
			this.dateCreated = dateCreated;
		}

		public String getDateModified() {
			return dateModified;
		}

		public void setDateModified(String dateModified) {
			this.dateModified = dateModified;
		}

		public List<SongLyricsXML> getLyrics() {
			return lyrics;
		}

		public void setLyrics(List<SongLyricsXML> lyrics) {
			this.lyrics = lyrics;
		}
}
