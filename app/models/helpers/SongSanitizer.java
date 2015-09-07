package models.helpers;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by samuel on 4/7/15.
 */
public class SongSanitizer {
	public static String sanitizeSong(String songLyrics) {
		String lyricsWithoutTabs = songLyrics.replaceAll("\\t", "    ");
		StringBuilder sb = new StringBuilder();
		String[] lines = lyricsWithoutTabs.split("\n");
		for (String line : lines) {
			sb.append(StringUtils.stripEnd(line, " "));
			sb.append("\n");
		}
		return (sb.toString());
	}
}
