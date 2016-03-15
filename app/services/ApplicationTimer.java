package services;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import javax.inject.*;

import akka.actor.*;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;


/**
 * This class demonstrates how to run code when the
 * application starts and stops. It starts a timer when the
 * application starts. When the application stops it prints out how
 * long the application was running for.
 *
 * This class is registered for Guice dependency injection in the
 * {@link Module} class. We want the class to start when the application
 * starts, so it is registered as an "eager singleton". See the code
 * in the {@link Module} class to see how this happens.
 *
 * This class needs to run code when the server stops. It uses the
 * application's {@link ApplicationLifecycle} to register a stop hook.
 */
@Singleton
public class ApplicationTimer {

    private final Clock clock;
    private final ApplicationLifecycle appLifecycle;
    private final Instant start;
    private final ActorSystem system;
    

    @Inject
    public ApplicationTimer(Clock clock, ApplicationLifecycle appLifecycle, ActorSystem system) {
        this.clock = clock;
        this.appLifecycle = appLifecycle;
        
        this.system = system;
        // This code is called when the application starts.
        start = clock.instant();
        Logger.info("ApplicationTimer: Starting application at " + start);
        Logger.info("Starting cleanup actor");
        
        ActorRef cleanupActor = this.system.actorOf(CleanupActor.props(42));
        this.system.scheduler().schedule(Duration.create(0, TimeUnit.MILLISECONDS), // Initial
                Duration.create(30, TimeUnit.SECONDS), // Frequency 15 seconds
                cleanupActor, "cleaning resource files", system.dispatcher(), null);
        
        // When the application starts, register a stop hook with the
        // ApplicationLifecyle object. The code inside the stop hook will
        // be run when the application stops.
        appLifecycle.addStopHook(() -> {
            Instant stop = clock.instant();
            Long runningTime = stop.getEpochSecond() - start.getEpochSecond();
            Logger.info("ApplicationTimer demo: Stopping application at " + clock.instant() + " after " + runningTime + "s.");
            return CompletableFuture.completedFuture(null);
        });
    }

}
