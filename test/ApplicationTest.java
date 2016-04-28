import static org.junit.Assert.assertEquals;
import static play.test.Helpers.fakeApplication;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import models.*;
import play.Logger;
import play.db.Database;
import play.db.Databases;
import play.db.evolutions.Evolutions;
import play.libs.ws.WSClient;
import play.test.Helpers;
import play.test.TestServer;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {

    private static Database database;
    private static TestServer testServer;
    private static int playPort = 8000;
    
    
    @BeforeClass
    public static void setUp() throws Exception {
        // initializing db
        database = Databases.inMemory("playsong", ImmutableMap.of("MODE", "PostgreSQL"), ImmutableMap.of("logStatements", true));
        Evolutions.applyEvolutions(database);

        Map<String, Object> testDb = new HashMap<String, Object>();
        testDb.put("db.default.driver", "org.h2.Driver");
        testDb.put("db.default.url", database.getUrl());
        Logger.warn("Db info: " + testDb);
        
        Logger.warn("Starting Test Server");
        testServer = Helpers.testServer(playPort, fakeApplication(testDb));
        testServer.start();
    }

    @Test
    public void simpleCheck() {
    	int a = 2;
        assertEquals(a, 2);
    }
    
    /**
     * test the UserAccount#updateOrCreate() method when new Song is being created
     */
    
    
    /**
     * test the Song#updateOrCreate() method when new Song is being created
     */
    @Test
    public void songCreation() {
        UserAccount ua = new UserAccount("test@test.com", "test", "test");
        ua.save();
        Logger.debug("User account:" + ua.toString());
        //Song newSong = new Song();
        //newSong.setSongAuthor("test");
        //s.setSongLyrics(songLyrics);
        //Song.updateOrCreateSong(newSong, ua.getEmail());
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        database.shutdown();
    }

    

}
