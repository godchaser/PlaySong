
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;

import play.Logger;

import play.test.*;
import play.libs.F.*;

import static play.test.Helpers.*;

public class IntegrationTest {
	//private WebDriver driver;
	private HtmlUnitDriver driver;
	private int playPort = 9000;
	private String baseUrl = "http://localhost:"+playPort;
	private TestServer testServer;

	/*
	@Before
	public void setUpFirefox() throws Exception {
		driver = new FirefoxDriver();
		//driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
	}
	*/
	
	@Before
	public void setUpTestServer() throws Exception{
		Logger.warn("Starting Test Server");
    	testServer = Helpers.testServer(playPort, fakeApplication());
    	testServer.start();
	}
	
	@Before
	public void setUpHeadless() throws Exception {
		//driver = new FirefoxDriver();
		//driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);

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

		driver.setJavascriptEnabled(true);
	}
	
	@Test
	public void quickSearchTest() throws Exception {
		Logger.warn("Running Quick Search Test");
		driver.get(baseUrl);
		String queryString = "Nitko";
		List<String> expectedSuggestions = Arrays.asList("Nitko kao ti", "Moj Isus, moj Gospod", "Odlučio sam",
				"Dajemo ti hvalu", "Ovo su proročki dani", "Prebivat ću", "Predivan si", "Naš Bog je velik",
				"Zagrli me ti", "Alabare", "To je Bog koga slavimo mi", "Tko će te osuditi", "Želim ti reći",
				"Veličanstven");
		String expectedTitle = "PlaySong";
		Logger.info("Testing title: " + expectedTitle);
		Logger.info("Testing query string: " + queryString);
		Logger.info("Testing expected suggestions: " + expectedSuggestions);

		String actualTitle = driver.getTitle();
		Assert.assertTrue(expectedTitle.equals(actualTitle));
		// Enter the query string
		WebElement query = driver.findElement(By.name("q"));
		query.sendKeys(queryString);

		// Sleep until the div we want is visible or 2 seconds is over
		long end = System.currentTimeMillis() + 2000;
		while (System.currentTimeMillis() < end) {
			WebElement resultsDiv = driver.findElement(By.className("tt-menu"));

			// If results have been returned, the results are displayed in a drop down.
			if (resultsDiv.isDisplayed()) {
				break;
			}
		}

		// And now list the suggestions
		List<WebElement> allSuggestions = driver.findElements(By.cssSelector(".tt-suggestion.tt-selectable"));
		List<String> receivedSuggestions = new ArrayList<String>();
		for (WebElement suggestion : allSuggestions) {
			receivedSuggestions.add(suggestion.getText());
		}
		Logger.info("These are received suggestions:" + receivedSuggestions);
		Assert.assertTrue(receivedSuggestions.containsAll(expectedSuggestions));
	}
	
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
    public void test() {
        running(testServer(playPort, fakeApplication()), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
            	driver.get(baseUrl);
            	String actualTitle = driver.getTitle();
        		Logger.info("Current Title: " + actualTitle);
            }
        });
    }

	@After
	public void tearDownDriver() throws Exception {
		driver.quit();
	}
	
	@After
	public void tearDownServer() throws Exception {
		testServer.stop();
	}

}