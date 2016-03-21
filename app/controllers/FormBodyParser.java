package controllers;
import akka.util.ByteString;
import play.api.http.HttpConfiguration;
import play.core.parsers.FormUrlEncodedParser;
import play.http.HttpErrorHandler;
import play.libs.F;
import play.libs.streams.Accumulator;
import play.mvc.BodyParser;
import play.mvc.BodyParsers;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Map;

/**
 * Created by mohsen on 3/21/16.
 */
public class FormBodyParser extends BodyParser.BufferingBodyParser<Map<String, String[]>> {
    private final HttpErrorHandler errorHandler;

    public FormBodyParser(long maxLength, HttpErrorHandler errorHandler) {
        super(maxLength, errorHandler, "Error parsing form");
        this.errorHandler = errorHandler;
    }

    @Inject
    public FormBodyParser(HttpConfiguration httpConfiguration, HttpErrorHandler errorHandler) {
        super(httpConfiguration, errorHandler, "Error parsing form");
        this.errorHandler = errorHandler;
    }

    @Override
    public Accumulator<ByteString, F.Either<Result, Map<String, String[]>>> apply(Http.RequestHeader request) {
        return BodyParsers.validateContentType(errorHandler, request, "Expected application/x-www-form-urlencoded",
                ct -> ct.equalsIgnoreCase("application/x-www-form-urlencoded"), super::apply);
    }

    @Override
    protected Map<String, String[]> parse(Http.RequestHeader request, ByteString bytes) throws Exception {
        String charset = request.charset().orElse("UTF-8");
        return FormUrlEncodedParser.parseAsJavaArrayValues(bytes.decodeString(charset), charset);
    }
}