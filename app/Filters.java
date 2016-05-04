import javax.inject.Inject;

import play.filters.gzip.GzipFilter;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

public class Filters implements HttpFilters {

    @Inject
    GzipFilter gzipFilter;

    public EssentialFilter[] filters() {
        return new EssentialFilter[] { gzipFilter.asJava() };
    }
}