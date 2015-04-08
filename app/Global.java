import play.Application;
import play.GlobalSettings;
import play.libs.Akka;
import scala.concurrent.duration.FiniteDuration;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by samuel on 4/8/15.
 */

public class Global extends GlobalSettings {
    @Override
    public void onStart(Application app) {
        Long delayInSeconds;

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 16);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date plannedStart = c.getTime();
        Date now = new Date();
        Date nextRun;
        if(now.after(plannedStart)) {
            c.add(Calendar.DAY_OF_WEEK, 1);
            nextRun = c.getTime();
        } else {
            nextRun = c.getTime();
        }
        delayInSeconds = (nextRun.getTime() - now.getTime()) / 1000; //To convert milliseconds to seconds.

        FiniteDuration delay = FiniteDuration.create(delayInSeconds, TimeUnit.SECONDS);
        FiniteDuration frequency = FiniteDuration.create(1, TimeUnit.DAYS);
        Runnable showTime = new Runnable() {
            @Override
            public void run() {
                System.out.println("Time is now: " + new Date());
            }
        };

        Akka.system().scheduler().schedule(delay, frequency, showTime, Akka.system().dispatcher());
    }

}
