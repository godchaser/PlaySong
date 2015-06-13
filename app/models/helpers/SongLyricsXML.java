package models.helpers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import org.eclipse.persistence.oxm.annotations.XmlPath;

@XmlAccessorType(XmlAccessType.FIELD)
public class SongLyricsXML {
	/*
	 * @XmlAttribute private String type;
	 * 
	 * @XmlValue private String number;
	 */
	/*
	 * <span name="lyrics"> <p name="songLyrics.id">1</p> <p
	 * name="songKey">D</p> <pre name="songLyrics">
	 */
	@XmlPath("p[@name='songLyrics.id']/text()")
	private String id;
	
	@XmlPath("pre[@name='songLyrics']/text()")
	private String songLyrics;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSongLyrics() {
		return songLyrics;
	}

	public void setSongLyrics(String songLyrics) {
		this.songLyrics = songLyrics;
	}
}
