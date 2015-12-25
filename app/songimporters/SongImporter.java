package songimporters;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import models.SongLyrics;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import play.libs.Yaml;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebeaninternal.server.core.DefaultSqlUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import models.Song;

/**
 * Created by samuel on 23.02.15..
 */
public class SongImporter {

	private static String opensongDirectory = "test//test_data//opensong_data//";
	private static String dbPath = "test//test_data//opensongbook//opensongbook.sql";
	private static String sqlDumpPaths[] = { "resources/PUBLIC_SONG.sql",
			"resources/PUBLIC_SONG_LYRICS.sql" };

	public static void exportFolder() throws IOException,
			ClassNotFoundException, ParserConfigurationException, SAXException {
		File path = new File(opensongDirectory);
		File[] files = path.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				System.out.println("Starting file: "
						+ files[i].getCanonicalPath());
				importSong(files[i].getCanonicalPath());
			}
		}
	}

	public static void importSong(String xmlFile)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse(xmlFile);
		NodeList nodes = dom.getChildNodes();
		Element eElement = (Element) nodes.item(0).getChildNodes();
		Song song = new Song();
		String name = eElement.getElementsByTagName("title").item(0)
				.getTextContent();
		String author = eElement.getElementsByTagName("author").item(0)
				.getTextContent();
		// TODO: this is temp solution to clean all .
		// String cleanedSong =
		// eElement.getElementsByTagName("lyrics").item(0).getTextContent().replace(".",
		// "");
		String lyrics = eElement.getElementsByTagName("lyrics").item(0)
				.getTextContent();
		saveSong(name, author, lyrics);
	}

	public static void importFromDb() throws ClassNotFoundException {
		Class.forName("org.hsqldb.jdbcDriver");
		Connection connection = null;
		try {
			Properties credentials = new Properties();
			credentials.setProperty("user", "");
			credentials.setProperty("password", "");
			connection = DriverManager.getConnection("jdbc:hsqldb:file:"
					+ dbPath, credentials);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);

			ResultSet rs = statement.executeQuery("select * from opensongbook");
			while (rs.next()) {
				String name = rs.getString(2);
				String author = rs.getString(4);

				String lyrics = rs.getString(3);
				saveSong(name, author, lyrics);
			}

			rs.close();
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e);
			}
		}
	}

	public static void saveSong(String name, String author, String lyrics) {
		Song song = new Song();
        String cleanlyrics = lyrics.replaceAll("\\n\\s*\\n\\s*", "\n\n");
		song.songName = name;
		song.songAuthor = author;
		// TODO: this is temp solution to clean all .
		SongLyrics songLyrics = new SongLyrics();
		songLyrics.setsongLyrics(cleanlyrics);
		song.songLyrics.add(songLyrics);
		Song.updateOrCreateSong(song);
	}

	public static void restoreFromSQLDump() {
		// SqlUpdate downSongs =
		// Ebean.createSqlUpdate("DELETE FROM PUBLIC.SONG WHERE id !=0");
		// downSongs.execute();
		// SqlUpdate downLyrics =
		// Ebean.createSqlUpdate("DELETE FROM PUBLIC.SONG_LYRICS WHERE id != 0");
		// downLyrics.execute();

		FileInputStream fis = null;
		for (String sqlDumpFile : sqlDumpPaths) {
			try {
				fis = new FileInputStream(sqlDumpFile);
				byte[] data = new byte[fis.available()];
				fis.read(data);
				String sql = new String(data);
				Ebean.execute(new DefaultSqlUpdate(sql));
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void songToYaml(){
		List<Song> songs = Song.all();
		ObjectMapper mapper = new ObjectMapper();
		//mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(SongLyrics.get(106L).getsongLyrics()));			
			mapper.writeValue(new File("conf/songs.json"), SongLyrics.get(106L).getsongLyrics());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
        List<Song> songs = Song.all();

        DumperOptions options = new DumperOptions();
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.SINGLE_QUOTED);
        //options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        //options.setLineBreak(DumperOptions.LineBreak.UNIX);
        //options.setWidth(180);
        //options.
        //options.setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);
       // options.set
        Yaml yaml = new Yaml(options);
        FileWriter writer = null;

        try {
            writer = new FileWriter("conf/songs.yml");
            System.out.println("Opening writer");
            System.out.println("Trying to dump yaml writer");
            yaml.dump(songs, writer);
            System.out.println("Trying to dump yaml writer");

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                System.out.println("Closing writer");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        */
	}

    public static void yamlToSong(){
        ArrayList <Song> data = (ArrayList)play.libs.Yaml.load("songs.yml");
        //Ebean.save((Collection)(data));

        for (Song s : data){
            System.out.println(s.songName);
        }
        System.out.println("########11");
        try {
            PrintWriter out = new PrintWriter("1324.txt");
            out.println(SongLyrics.get(1324L).getsongLyrics());
            out.close();
            PrintWriter out2 = new PrintWriter("1323.txt");
            out2.println(SongLyrics.get(1323L).getsongLyrics());
            out2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("########22");
        //System.out.println(SongLyrics.get(290L).getsongLyrics());
        //System.out.println("keys: " + keys);
        //System.out.println("values: " + data.toString());
    }

	public static void ttt() {
		//Map data = (Map) Yaml.load("data/testing-data.yml");
	}
}
