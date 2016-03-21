package controllers;
import java.util.concurrent.CompletionStage;

import play.Logger;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Result;

public class VerboseAction extends play.mvc.Action.Simple {

    @Override
    public CompletionStage<Result> call(Context ctx) {
        return delegate.call(ctx);
    }
}