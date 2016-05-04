package models.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * Created by samuel on 4/7/15.
 */
@XmlRootElement(name="html")
@XmlAccessorType(XmlAccessType.FIELD)
public class SongsXML {

	@XmlPath("body/div/div[@name='song']")
	private List<SongXML> songs;

	public List<SongXML> getSongs() {
		return songs;
	}

	public void setSongs(List<SongXML> songs) {
		this.songs = songs;
	}
}
