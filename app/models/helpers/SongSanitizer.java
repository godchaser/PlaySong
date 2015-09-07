package models.helpers;

/**
 * Created by samuel on 4/7/15.
 */
public class SongSanitizer {
	public static String sanitizeSong(String songLyrics) {
		String lyricsWithoutTabs = removeTabs(songLyrics);
		StringBuilder sb = new StringBuilder();
		String[] lines = lyricsWithoutTabs.split("\n");
		for (String line : lines) {
			sb.append(trimTrailingSpaces(line));
			sb.append("\n");
		}
		return (sb.toString());
	}

	public static String trimTrailingSpaces(String str) {
		return str.replaceFirst("\\s+$", "");
	}

	public static String removeTabs(String str) {
		return str.replaceAll("\\t", "    ");
	}
}
