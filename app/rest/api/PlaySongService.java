package rest.api;

import rest.json.PlaylistJson;
import rest.json.SongBookJson;
import rest.json.SongLyricsJson;
import rest.json.SongsJson;
import retrofit2.Call;
import retrofit2.http.GET;


public interface PlaySongService {
    @GET("/assets/json/songs")
    //@GET("/json/songs")
    Call<SongsJson[]> getSongs();

    @GET("/assets/json/songlyrics")
    //@GET("/json/songlyrics")
    Call<SongLyricsJson[]> getSongLyrics();

    @GET("/assets/json/playlists")
    //@GET("/json/favoritesongs")
    Call<PlaylistJson[]> getFavoritesSongs();
    
    @GET("/json/songbooks")
    Call<SongBookJson[]> getSongbooks();
}
