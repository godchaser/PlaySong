package models.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import controllers.chords.LineTypeChecker;
import models.Song;
import models.SongLyrics;

import org.eclipse.persistence.jaxb.JAXBContextFactory;

public class XMLSongsParser {

	public static void convertToSong(SongsXML songs) {
		System.out.println("Checking if unmarshalled XML songs is null");
		if (songs.getSongs() != null) {
			System.out
					.println("Now converting unmarshalled XML songs to Song object: ");
			for (SongXML songXML : songs.getSongs()) {
				Song song = new Song();
				song.setId(Long.parseLong(songXML.getId()));
				song.setSongName(songXML.getSongName());
				song.setSongOriginalTitle(songXML.getSongOriginalTitle());
				song.setSongAuthor(songXML.getSongAuthor());
				song.setSongAuthor(songXML.getSongAuthor());
				song.setSongLink(songXML.getSongLink());
				List<SongLyrics> songLyrics = new ArrayList<SongLyrics>();
				for (SongLyricsXML lyricsXML : songXML.getLyrics()) {
					SongLyrics lyrics = new SongLyrics();
					lyrics.setId(Long.parseLong(lyricsXML.getId()));

					String[] lines = lyricsXML.getSongLyrics().split("\n");
					String sanitizedLyrics = null;
					// Check if first line is empty, and then delete it
					if (lines[0].length() < 2) {
						System.out.println("Sanitizing leading blank line");
						StringBuilder sb = new StringBuilder();
						boolean skippedFirstLine = false;
						for (String line : lines) {
							if (skippedFirstLine) {
								sb.append(line + "\n");
							} else {
								skippedFirstLine = true;
							}
						}
						sanitizedLyrics = sb.toString();
					} else {
						sanitizedLyrics = lyricsXML.getSongLyrics();
					}

					lyrics.setsongLyrics(sanitizedLyrics);
					String songKey = LineTypeChecker.getSongKey(lyricsXML
							.getSongLyrics());
					lyrics.setSongKey(songKey);
					songLyrics.add(lyrics);
				}
				song.setSongLyrics(songLyrics);
				Song.updateOrCreateSong(song);
			}
		} else {
			System.out
					.println("Skipping conversions - unmarshalled songs are null");
		}
	}

	public static SongsXML readXMLFile(String songXMLPath) {
		JAXBContext jc;
		SongsXML songs = null;
		System.out.println("Reading XML File: " + songXMLPath);
		try {
			jc = JAXBContextFactory.createContext(
					new Class[] { SongsXML.class }, null);

			Unmarshaller unmarshaller = jc.createUnmarshaller();
			songs = (SongsXML) unmarshaller.unmarshal(new File(songXMLPath));

			/*
			 * Marshaller marshaller = jc.createMarshaller();
			 * marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			 * marshaller.marshal(songs, System.out);
			 */

			if (songs.getSongs() != null) {
				System.out.println("Printing unmarshalled song names");
				for (SongXML s : songs.getSongs()) {
					System.out.println(s.getSongName());
				}
			} else {
				System.out.println("Cannot find songs in XML file");
			}

		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return songs;
	}

	public static void updateFromXML() {
		SongsXML songs = readXMLFile("resources/songs.html");
		convertToSong(songs);
	}

	public static void main(String[] args) throws Exception {
		SongsXML songs = readXMLFile("resources/songs.html");
	}

}