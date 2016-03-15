
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.avaje.ebean.Ebean;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.common.collect.ImmutableMap;

import database.SqlQueries;
import play.Logger;
import play.db.Database;
import play.db.Databases;
import play.db.evolutions.*;

import org.junit.*;

import play.test.*;

import org.openqa.selenium.Keys;
import java.util.HashMap;
import java.util.Map;

import static play.test.Helpers.*;

public class IntegrationTest {
    private static WebDriver driver;
    // private HtmlUnitDriver driver;
    private static int timeout = 3;
    private static int playPort = 8000;
    private static String baseUrl = "http://localhost:" + playPort;
    private static TestServer testServer;
    private static WebDriverWait wait;
    private static Database database;

    @BeforeClass
    public static void setUp() throws Exception {
        // initializing db
        database = Databases.inMemory("playsong", ImmutableMap.of("MODE", "PostgreSQL"), ImmutableMap.of("logStatements", true));
        Evolutions.applyEvolutions(database);

        Map<String, Object> testDb = new HashMap<String, Object>();
        testDb.put("db.default.driver", "org.h2.Driver");
        testDb.put("db.default.url", database.getUrl());
        Logger.warn("Db info: " + testDb);

        // initializing browser driver
        
        // uncomment this if you want firefox driver and comment headless setup
        //driver = new FirefoxDriver();
        
        //headless setup
        driver = new HtmlUnitDriver(BrowserVersion.CHROME) {
            @Override
            protected WebClient newWebClient(BrowserVersion version) {
                WebClient webClient = super.newWebClient(version);
                webClient.getOptions().setThrowExceptionOnScriptError(false);
                webClient.setCssErrorHandler(new SilentCssErrorHandler());
                webClient.getOptions().setRedirectEnabled(true);
                webClient.getOptions().setThrowExceptionOnScriptError(false);
                webClient.getOptions().setCssEnabled(true);
                webClient.getOptions().setUseInsecureSSL(true);
                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                webClient.setAjaxController(new NicelyResynchronizingAjaxController());
                webClient.getCookieManager().setCookiesEnabled(true);
                return webClient;
            }
        };

        ((HtmlUnitDriver) driver).setJavascriptEnabled(true);
        //headless setup
        
        wait = new WebDriverWait(driver, timeout);
        // driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);

        // initializing application server
        Logger.warn("Starting Test Server");
        testServer = Helpers.testServer(playPort, fakeApplication(testDb));
        testServer.start();

        // setting up test data
        Logger.warn("Setting up test data");
        Logger.warn("Provisioning user");
        driver.get(baseUrl + controllers.routes.Application.inituser());
        Thread.sleep(1000);
        Logger.warn("Running Login Test");
        driver.get(baseUrl);
        String expectedTitle = "PlaySong Login";
        String expectedRedirectedTitle = "PlaySong";
        String testEmail = "test@test.com";
        String testPassword = "test";
        Logger.info("Clicking on user dropdown box");
        driver.findElement(By.id("dropdown-user-box")).click();
        driver.findElement(By.id("user-login")).click();
        String actualTitle = driver.getTitle();
        Logger.info("Current Title: " + actualTitle);
        Assert.assertTrue(expectedTitle.equals(actualTitle));
        Logger.info("Entering login credentials");
        driver.findElement(By.name("email")).sendKeys(testEmail);
        driver.findElement(By.name("password")).sendKeys(testPassword);
        Logger.info("Submitting form");
        driver.findElement(By.name("password")).submit();
        String redirectedTitle = driver.getTitle();
        Logger.info("Current Title: " + redirectedTitle);
        Assert.assertTrue(expectedRedirectedTitle.equals(redirectedTitle));
        Thread.sleep(1000);
        Logger.warn("Sync with remote db");
        driver.get(baseUrl + controllers.routes.Application.syncDb());
        Thread.sleep(10000);
        driver.get(baseUrl);
        // Fix sequences
        Logger.info("Fixing db sequences");
        Ebean.createSqlUpdate(SqlQueries.sqlH2SeqFix).execute();
    }

    @Test
    public void quickSearchTest() throws Exception {
        Logger.warn("Running Quick Search Test");
        //driver.get(baseUrl);
        String queryString = "Nitko";
        List<String> expectedSuggestions = Arrays.asList("Nitko kao ti", "Moj Isus, moj Gospod", "Odlučio sam", "Dajemo ti hvalu", "Ovo su proročki dani", "Prebivat ću", "Predivan si",
                "Naš Bog je velik", "Zagrli me ti", "Alabare", "To je Bog koga slavimo mi");
        String expectedTitle = "PlaySong";
        Logger.info("Testing title: " + expectedTitle);
        Logger.info("Testing query string: " + queryString);
        Logger.info("Testing expected suggestions: " + expectedSuggestions);

        String actualTitle = driver.getTitle();
        Assert.assertTrue(expectedTitle.equals(actualTitle));
        // Enter the query string
        WebElement query = driver.findElement(By.name("q"));
        query.sendKeys(queryString);

        wait.until(ExpectedConditions.elementToBeClickable(By.className("tt-menu")));

        // And now list the suggestions
        List<WebElement> allSuggestions = driver.findElements(By.cssSelector(".tt-suggestion.tt-selectable"));
        List<String> receivedSuggestions = new ArrayList<String>();
        for (WebElement suggestion : allSuggestions) {
            receivedSuggestions.add(suggestion.getText());
        }
        Logger.info("These are received suggestions:" + receivedSuggestions);
        Assert.assertTrue(receivedSuggestions.containsAll(expectedSuggestions));
    }

    @Ignore
    @Test
    public void loginTest() throws Exception {
        Logger.warn("Running Login Test");
        driver.get(baseUrl);
        String expectedTitle = "PlaySong Login";
        String expectedRedirectedTitle = "PlaySong";
        String testEmail = "test@test.com";
        String testPassword = "test";
        Logger.info("Clicking on user dropdown box");
        driver.findElement(By.id("dropdown-user-box")).click();
        driver.findElement(By.id("user-login")).click();
        String actualTitle = driver.getTitle();
        Logger.info("Current Title: " + actualTitle);
        Assert.assertTrue(expectedTitle.equals(actualTitle));
        Logger.info("Entering login credentials");
        driver.findElement(By.name("email")).sendKeys(testEmail);
        driver.findElement(By.name("password")).sendKeys(testPassword);
        Logger.info("Submitting form");
        driver.findElement(By.name("password")).submit();
        String redirectedTitle = driver.getTitle();
        Logger.info("Current Title: " + redirectedTitle);
        Assert.assertTrue(expectedRedirectedTitle.equals(redirectedTitle));
    }

    @Ignore
    @Test
    public void addNewSongAndThenDeleteIt() throws Exception {
        Logger.warn("Running Add New Song Test");

        String testSongName = "10000 Test Song Name";
        String testSongLyrics = "A1 Test Song Lyrics";
        String testSongLyricsEdited = "A1 Test Song Lyrics Edited";

        // LOGIN PROCEDURE
        driver.get(baseUrl);
        String expectedLoginTitle = "PlaySong Login";
        String expectedRedirectedTitle = "PlaySong";
        String testEmail = "test@test.com";
        String testPassword = "test";
        Logger.info("Clicking on user dropdown box");
        driver.findElement(By.id("dropdown-user-box")).click();
        driver.findElement(By.id("user-login")).click();
        String actualTitle = driver.getTitle();
        Logger.info("Current Title: " + actualTitle);
        Assert.assertTrue(expectedLoginTitle.equals(actualTitle));
        Logger.info("Entering login credentials");
        driver.findElement(By.name("email")).sendKeys(testEmail);
        driver.findElement(By.name("password")).sendKeys(testPassword);
        Logger.info("Submitting form");
        driver.findElement(By.name("password")).submit();
        String redirectedTitle = driver.getTitle();
        Logger.info("Current Title: " + redirectedTitle);
        Assert.assertTrue(expectedRedirectedTitle.equals(redirectedTitle));

        // ADD NEW SONG PROCEDURE
        Logger.info("Waiting for add song button to appear");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("addsongbutton")));
        Logger.info("Clicking add song button");
        driver.findElement(By.id("addsongbutton")).click();
        Logger.info("Waiting for new song form to appear");
        wait.until(ExpectedConditions.elementToBeClickable(By.name("songName")));
        Logger.info("Filling up new song form");
        driver.findElement(By.name("songName")).sendKeys(testSongName);
        driver.findElement(By.name("songOriginalTitle")).sendKeys("A1 Test Song Original Title");
        driver.findElement(By.name("songAuthor")).sendKeys("A1 Test Song Author");
        driver.findElement(By.name("songLink")).sendKeys("https://www.youtube.com/watch?v=EimeyyaKKQk");
        driver.findElement(By.name("songLyrics[0].songLyrics")).sendKeys(testSongLyrics);
        Logger.info("Submitting new song form");
        driver.findElement(By.name("songName")).submit();
        Thread.sleep(1000);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".songRow")));
        Logger.info("Looking for new song");
        // And now list the songs
        List<WebElement> allSongs = driver.findElements(By.cssSelector(".songRow"));
        WebElement foundSong = null;
        String songId = null;
        String songButtonId = null;
        List<String> receivedSongs = new ArrayList<String>();
        for (WebElement song : allSongs) {
            WebElement innerElement = song.findElement(By.cssSelector(".btn.btn-link.lyrics-link"));
            receivedSongs.add(innerElement.getText());
            if (testSongName.equals(innerElement.getText())) {
                songId = song.getAttribute("id");
                foundSong = driver.findElement(By.id(songId));
                Logger.info("Found new song Id: " + songId);
                songButtonId = innerElement.getAttribute("id");
            }
        }

        Assert.assertNotNull(foundSong);

        Logger.info("Found songs after update: " + receivedSongs.toString());
        Assert.assertTrue(receivedSongs.contains(testSongName));

        // CHECK LYRICS VIEW PROCEDURE
        Logger.info("Now clicking on newly added song");
        driver.findElement(By.id(songButtonId)).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("songLyrics")));
        String newSongLyrics = driver.findElement(By.id("songLyrics")).getText();
        Assert.assertEquals(newSongLyrics, testSongLyrics);

        // EDIT PROCEDURE
        // editsongbutton
        Logger.info("Now editing the newly added song");
        driver.findElement(By.id("editsongbutton")).click();
        Logger.info("Waiting for song form to appear");
        Thread.sleep(1000);
        wait.until(ExpectedConditions.elementToBeClickable(By.name("songLyrics[0].songLyrics")));
        WebElement songLyricsElement = driver.findElement(By.name("songLyrics[0].songLyrics"));
        songLyricsElement.sendKeys(Keys.CONTROL + "a");
        songLyricsElement.sendKeys(Keys.DELETE);
        songLyricsElement.sendKeys(testSongLyricsEdited);
        Logger.info("Submitting updated song");
        driver.findElement(By.name("songName")).submit();
        Thread.sleep(1000);
        wait.until(ExpectedConditions.presenceOfElementLocated((By.id(songId))));
        WebElement w = driver.findElement(By.id(songId));

        // TODO: Continue writing test here
        /*
         * System.out.println(w.getText());
         * 
         * w.findElement(By.cssSelector(".btn.btn-link.lyrics-link")).click(); // driver.findElement(By.id(songId)).findElement(By.cssSelector(".btn.btn-link.lyrics-link")).click();
         * 
         * String newSongLyricsAfterUpdate = driver.findElement(By.id("songLyrics")).getText(); Assert.assertEquals(testSongLyricsEdited, newSongLyricsAfterUpdate);
         * 
         * // QUICK UPDATE PROCEDURE // editlyricsbutton
         * 
         * // DELETE SONG PROCEDURE Logger.info("Now deleting it on newly added song"); driver.findElement(By.id("deletesongbutton")).click(); Logger.info("Answering delete dialog"); Alert
         * javascriptprompt = driver.switchTo().alert(); String deleteSongQuestion = "Do you really want to delete song?"; Assert.assertEquals(deleteSongQuestion,
         * javascriptprompt.getText()); javascriptprompt.accept();
         * 
         * Logger.info("Now checking if song is deleted"); wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".songRow"))); // And now list the songs List<WebElement>
         * updatedSongs = driver.findElements(By.cssSelector(".songRow")); receivedSongs.clear(); boolean foundDeletedSong = false; for (WebElement song : updatedSongs) {
         * receivedSongs.add(song.getText()); if (testSongName.equals(song.getText())) { foundDeletedSong = true; } } Assert.assertFalse(foundDeletedSong); Logger.info(
         * "Found songs (after deletion): " + receivedSongs.toString());
         */
    }

    @Ignore
    @Test
    public void publishSongbook() throws Exception {
        Logger.warn("Running publish new songbook test");

        driver.get(baseUrl);
        String expectedLoginTitle = "PlaySong Login";
        String expectedRedirectedTitle = "PlaySong";
        String testEmail = "test@test.com";
        String testPassword = "test";
        Logger.info("Clicking on user dropdown box");
        driver.findElement(By.id("dropdown-user-box")).click();
        driver.findElement(By.id("user-login")).click();
        String actualTitle = driver.getTitle();
        Logger.info("Current Title: " + actualTitle);
        Assert.assertTrue(expectedLoginTitle.equals(actualTitle));
        Logger.info("Entering login credentials");
        driver.findElement(By.name("email")).sendKeys(testEmail);
        driver.findElement(By.name("password")).sendKeys(testPassword);
        Logger.info("Submitting form");
        driver.findElement(By.name("password")).submit();
        String redirectedTitle = driver.getTitle();
        Logger.info("Current Title: " + redirectedTitle);
        Assert.assertTrue(expectedRedirectedTitle.equals(redirectedTitle));
        Logger.info("Waiting for add song button to appear");
        Thread.sleep(1000);
        Logger.info("Clicking add song button");
        driver.findElement(By.id("addsongbutton")).click();
        Logger.info("Waiting for new song form to appear");

        wait.until(ExpectedConditions.elementToBeClickable(By.name("songName")));
        Logger.info("Filling up new song form");
        // driver.findElement(By.name("songName")).sendKeys(testSongName);
        driver.findElement(By.name("songOriginalTitle")).sendKeys("A1 Test Song Original Title");
        driver.findElement(By.name("songAuthor")).sendKeys("A1 Test Song Author");
        driver.findElement(By.name("songLink")).sendKeys("https://www.youtube.com/watch?v=EimeyyaKKQk");
        driver.findElement(By.name("songLyrics[0].songLyrics")).sendKeys("A1 Test Song Lyrics");
        Logger.info("Submitting new song form");
        driver.findElement(By.name("songName")).submit();

        Logger.info("Looking for new song");
        // And now list the songs
        List<WebElement> allSongs = driver.findElements(By.cssSelector(".songRow"));
        List<String> receivedSongs = new ArrayList<String>();
        for (WebElement songs : allSongs) {
            receivedSongs.add(songs.getText());
        }

        Logger.info("Found songs: " + receivedSongs.toString());
        // Assert.assertTrue(receivedSongs.contains(testSongName));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        driver.quit();
        testServer.stop();
        database.shutdown();
    }

}