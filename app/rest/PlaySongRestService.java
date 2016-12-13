package rest;

//import com.zeppelin.app.playsong.database.PlaySongDatabase;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import okhttp3.ResponseBody;
import play.Logger;
import rest.api.PlaySongService;
import rest.json.PlaylistJson;
import rest.json.SongBookJson;
import rest.json.SongLyricsJson;
import rest.json.SongsJson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import database.DatabaseHelper;

/**
 * Created by samuel on 12/28/15.
 */
public class PlaySongRestService extends Observable {

    private final String PLAY_SONG_REST_ADDR = "http://playsong.herokuapp.com";
    //private final String PLAY_SONG_REST_ADDR = "http://localhost:9000";
    // private final String PLAY_SONG_REST_ADDR = "http://10.0.2.2:9000";
    //private final String PLAY_SONG_REST_ADDR = "http://playsong.duckdns.org:9000";
    public static final String KEY_SONGS_FETCHED = "Songs Fetched";
    public static final String KEY_LYRICS_FETCHED = "Song Lyrics Fetched";
    public static final String KEY_FAVORITES_FETCHED = "Song Favorites Fetched";

    private PlaySongService playsong;
    private DatabaseHelper db;

    
    public PlaySongRestService() {
        // addObserver(observer);
        setUpRestConnection();
        db = DatabaseHelper.getInstance();
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
                    db.writeJsonSongsToDb2(Arrays.asList((SongsJson[]) model), userEmail);
                    Logger.trace("PlaySongRestService", "Notifying observer that songs are fetched");
                    setChanged();
                    // notifyObservers(updatedSongs);
                    //getSongLyrics(updatedSongs);
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
        Call<PlaylistJson[]> getsongscall = playsong.getFavoritesSongs();
        getsongscall.enqueue(new Callback<PlaylistJson[]>() {
            @Override
            public void onResponse(Response<PlaylistJson[]> response) {
                PlaylistJson[] model = response.body();

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
                    db.writeJsonFavoritesSongsToDb(Arrays.asList(model));
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


    public void downloadSongsData(String userEmail) {
        getSongsData(userEmail);
    }

}
