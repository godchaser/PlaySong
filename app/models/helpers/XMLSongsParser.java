package models.helpers;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class XMLSongsParser {

	public static void main(String[] args) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(SongsXML.class);

		Unmarshaller unmarshaller = jc.createUnmarshaller();
		SongsXML songs = (SongsXML) unmarshaller.unmarshal(new File(
				"resources/songs.html"));

		
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(songs, System.out);
		
		/*
		for (SongXML s : songs.getSongs()){
			System.out.println(s.getSongName());
		}
		*/
	}

}