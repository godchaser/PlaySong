package rest;

//import com.zeppelin.app.playsong.database.PlaySongDatabase;

import rest.api.PlaySongService;
import rest.json.ServiceJson;
import rest.json.SongLyricsJson;
import rest.json.SongbookJson;
import rest.json.SongsJson;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.h2.engine.Database;

import database.DatabaseHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

import play.Logger;

/**
 * Created by samuel on 12/28/15.
 */
public class PlaySongRestService extends Observable {

    private final String PLAY_SONG_REST_ADDR = "http://playsong.herokuapp.com";
    // private final String PLAY_SONG_REST_ADDR = "http://10.0.2.2:9000";
    // private final String PLAY_SONG_REST_ADDR = "http://playsong.duckdns.org:9000";
    public static final String KEY_SONGS_FETCHED = "Songs Fetched";
    public static final String KEY_LYRICS_FETCHED = "Song Lyrics Fetched";
    public static final String KEY_FAVORITES_FETCHED = "Song Favorites Fetched";

    private PlaySongService playsong;
    private DatabaseHelper db;

    public PlaySongRestService() {
        // addObserver(observer);
        setUpRestConnection();
        db = new DatabaseHelper();
    }

    private void setUpRestConnection() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(PLAY_SONG_REST_ADDR).addConverterFactory(GsonConverterFactory.create()).build();

        playsong = retrofit.create(PlaySongService.class);

    }

    private void getSongsData(String userEmail) {
        // Fetching song json data
        Call<SongsJson[]> getsongscall = playsong.getSongs();
        getsongscall.enqueue(new Callback<SongsJson[]>() {
            @Override
            public void onResponse(Response<SongsJson[]> response) {
                SongsJson[] model = response.body();

                if (model == null) {
                    // 404 or the response cannot be converted to Model.
                    ResponseBody responseBody = response.errorBody();
                    if (responseBody != null) {
                        try {
                            Logger.trace("PlaySongRestService", "responseBody = " + responseBody.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Logger.trace("PlaySongRestService", "responseBody = null");
                    }
                } else {
                    // 200
                    Logger.trace("PlaySongRestService", "Song Data successfully fetched");
                    Logger.trace("PlaySongRestService", "Writing song data to db");
                    List<Long> updatedSongs = db.writeJsonSongsToDb(Arrays.asList((SongsJson[]) model), userEmail);
                    Logger.trace("PlaySongRestService", "Notifying observer that songs are fetched");
                    setChanged();
                    // notifyObservers(updatedSongs);
                    getSongLyrics(updatedSongs);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Logger.trace("PlaySongRestService", "Failure = " + t.getMessage());
            }
        });
    }

    private void getSongLyrics(final List<Long> updatedSongs) {
        // Fetching song lyrics json data
        Call<SongLyricsJson[]> getsonglyricscall = playsong.getSongLyrics();
        getsonglyricscall.enqueue(new Callback<SongLyricsJson[]>() {
            @Override
            public void onResponse(Response<SongLyricsJson[]> response) {
                SongLyricsJson[] model = response.body();

                if (model == null) {
                    // 404 or the response cannot be converted to Model.
                    ResponseBody responseBody = response.errorBody();
                    if (responseBody != null) {
                        try {
                            Logger.trace("PlaySongRestService", "responseBody = " + responseBody.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Logger.trace("PlaySongRestService", "responseBody = null");
                    }
                } else {
                    // 200
                    Logger.trace("PlaySongRestService", "Song Lyrics successfully fetched");
                    Logger.trace("PlaySongRestService", "Writing song lyrics data to db");
                    db.writeJsonSongLyricsToDb(Arrays.asList((SongLyricsJson[]) model), updatedSongs);
                    Logger.trace("PlaySongRestService", "Notifying observer that song lyrics are fetched");
                    setChanged();
                    notifyObservers(KEY_LYRICS_FETCHED);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Logger.trace("PlaySongRestService", "Failure = " + t.getMessage());
            }
        });
    }

    private void getFavoritesSongsData() {
        // Fetching song json data
        Call<ServiceJson[]> getsongscall = playsong.getFavoritesSongs();
        getsongscall.enqueue(new Callback<ServiceJson[]>() {
            @Override
            public void onResponse(Response<ServiceJson[]> response) {
                ServiceJson[] model = response.body();

                if (model == null) {
                    // 404 or the response cannot be converted to Model.
                    ResponseBody responseBody = response.errorBody();
                    if (responseBody != null) {
                        try {
                            Logger.trace("PlaySongRestService", "responseBody = " + responseBody.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Logger.trace("PlaySongRestService", "responseBody = null");
                    }
                } else {
                    // 200
                    Logger.trace("PlaySongRestService", "Favorites Song Data successfully fetched");
                    Logger.trace("PlaySongRestService", "Writing favorites song data to db");
                    List<Long> updatedFavorites = db.writeJsonFavoritesSongsToDb(Arrays.asList(model));
                    Logger.trace("PlaySongRestService", "Notifying observer that favorites songs are fetched");
                    setChanged();
                    notifyObservers(KEY_FAVORITES_FETCHED);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Logger.trace("PlaySongRestService", "Failure = " + t.getMessage());
            }
        });
    }

    private void getSongbooks() {
        // Fetching song json data
        Call<SongbookJson[]> getsongbookscall = playsong.getSongbooks();
        getsongbookscall.enqueue(new Callback<SongbookJson[]>() {
            @Override
            public void onResponse(Response<SongbookJson[]> response) {
                SongbookJson[] model = response.body();

                if (model == null) {
                    // 404 or the response cannot be converted to Model.
                    ResponseBody responseBody = response.errorBody();
                    if (responseBody != null) {
                        try {
                            Logger.trace("PlaySongRestService", "responseBody = " + responseBody.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Logger.trace("PlaySongRestService", "responseBody = null");
                    }
                } else {
                    // 200
                    Logger.trace("PlaySongRestService", "Songbooks Data successfully fetched");
                    Logger.trace("PlaySongRestService", "Writing songbooks data to db");
                    db.writeJsonSongbooksToDb(Arrays.asList((SongbookJson[]) model));
                    Logger.trace("PlaySongRestService", "Notifying observer that songs are fetched");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Logger.trace("PlaySongRestService", "Failure = " + t.getMessage());
            }
        });
    }

    public void downloadSongsData(String userEmail) {
        getSongsData(userEmail);
    }

    public void downloadFavoritesSongsData() {
        getFavoritesSongsData();
    }

    public void downloadSongLyricsData(List<Long> updatedSongs) {
        getSongLyrics(updatedSongs);
    }

    public void downloadSongbooks() {
        getSongbooks();
    }

}
