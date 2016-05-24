package models.helpers;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import models.Song;
import play.Environment;
import play.Logger;

/**
 * Created by samuel on 4/1/15.
 */
public class JsonToSongConverter {
    @Inject Environment environment;
  
    public void run() {
        ObjectMapper mapper = new ObjectMapper();

        try {

            File songsFile = environment.getFile("resources/songs/songs.json");

            // Convert JSON string from file to Object
            //Song song = mapper.readValue(songsFile, Song.class);
            
            //MyClass[] myObjects = mapper.readValue(json, MyClass[].class);
            Song[] songs = (mapper.readValue(songsFile, Song[].class));
            
            System.out.println(songs);

            //Pretty print
            String prettySongs = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(songs);
            System.out.println(prettySongs);
            
            
        } catch (JsonGenerationException e) {
            Logger.error("Unable to generate song object from json");
            e.printStackTrace();
        } catch (JsonMappingException e) {
            Logger.error("Unable to map song object from json");
            e.printStackTrace();
        } catch (IOException e) {
            Logger.error("Problem with json file");
            e.printStackTrace();
        }
    }

}
