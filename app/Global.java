import play.Application;
import play.GlobalSettings;
import play.libs.Akka;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;

import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;

import scala.concurrent.duration.Duration;

/**
 * Created by samuel on 4/8/15.
 */

public class Global extends GlobalSettings {
	public <T extends EssentialFilter> Class<T>[] filters() {
		return new Class[] { GzipFilter.class };
	}

	@Override
	public void onStart(Application app) {
		ActorRef cleanupActor = Akka.system().actorOf(CleanupActor.props(42), "demo");
		Akka.system().scheduler().schedule(Duration.create(0, TimeUnit.MILLISECONDS), // Initial
				Duration.create(30, TimeUnit.SECONDS), // Frequency 15 seconds
				cleanupActor, "cleaning resource files", Akka.system().dispatcher(), null);
	}
}
