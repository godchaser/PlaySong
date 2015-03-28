package controllers;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;


import java.io.File;

import models.Song;

/**
 * Created by samuel on 23.02.15..
 */
public class SongImporter  {

    private static String opensongDirectory = "test//test_data//opensong_data//";
    private static String dbPath = "//home//samuel//git//opensongbook//WebContent//WEB-INF//resources//opensongbook.sql";
    public static void exportFolder () throws IOException,
            ClassNotFoundException, ParserConfigurationException, SAXException {
        File path = new File(opensongDirectory);
        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                System.out.println("Starting file: " + files[i].getCanonicalPath());
                importSong(files[i].getCanonicalPath());
            }
        }
    }

    public static void importSong(String xmlFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(xmlFile);
        NodeList nodes = dom.getChildNodes();
        Element eElement = (Element) nodes.item(0).getChildNodes();
        Song song = new Song();
        String name = eElement.getElementsByTagName("title").item(0).getTextContent();
        String author= eElement.getElementsByTagName("author").item(0).getTextContent();
        //TODO: this is temp solution to clean all .
        //String cleanedSong = eElement.getElementsByTagName("lyrics").item(0).getTextContent().replace(".", "");
        String lyrics= eElement.getElementsByTagName("lyrics").item(0).getTextContent();
        saveSong(name,author,lyrics);
    }

    public static void importFromDb ()
            throws ClassNotFoundException {
        Class.forName("org.hsqldb.jdbcDriver");
        Connection connection = null;
        try {
            Properties credentials= new Properties();
            credentials.setProperty("user", "");
            credentials.setProperty("password", "");
            connection = DriverManager.getConnection("jdbc:hsqldb:file:" + dbPath, credentials);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            ResultSet rs = statement.executeQuery("select * from opensongbook");
            while (rs.next()) {
                String name = rs.getString(2);
                String author = rs.getString(4);
                String lyrics = rs.getString(3);
                saveSong(name,author,lyrics);
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

    public static void saveSong (String name, String author, String lyrics){
        Song song = new Song();
        song.songName = name;
        song.songAuthor= author;
        //TODO: this is temp solution to clean all .
        song.songLyrics= lyrics;
        Song.create(song);
    }
}
