package database;

public class SqlQueries {
    //@formatter:off
    public static final String sqlSelectSong = "SELECT t0.id,  "
                                            + "t0.song_name, "
                                            + "t0.song_original_title, "
                                            + "t0.song_author, "
                                            + "t0.song_link, "
                                            + "t0.song_importer, "
                                            + "t0.song_last_modified_by, "
                                            + "t0.date_created, "
                                            + "t0.date_modified, "
                                            + "t0.private_song, "
                                            + "u1.id as l_id, "
                                            + "u2.song_book_id as r_id ";

    public static final String sqlSelectSongId = "SELECT t0.id ";
    public static final String sqlFromSong = "FROM song t0 ";
    public static final String sqlJoin = "JOIN song_lyrics u1 on u1.song_id = t0.id "
                   + "JOIN song_book_song u2 on u2.song_id = t0.id ";
    public static final String sqlPrivateSongFalse = " OR t0.private_song = false ";
    //@formatter:on
}
