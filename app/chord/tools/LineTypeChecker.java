/*
 * This file is part of Quelea, free projection software for churches.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package chord.tools;

/**
 * Created by samuel on 4/23/15.
 */
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import play.Logger;

/**
 * Checks the type of the line.
 *
 * @author Michael
 */
public class LineTypeChecker {

	public static String[] validKeys = new String[] { "C", "C#", "Db", "D",
			"D#", "Eb", "E", "F", "F#", "Gb", "G", "G#", "Ab", "A", "A#", "B" };

	/**
	 * Check whether this line is a line containing only chords.
	 *
	 * @return true if it's a chord line, false otherwise.
	 */
	public static boolean isChordLine(String line) {
		// to check if string has only whitespaces
		if (StringUtils.isBlank(line)) {
			return false;
		}
		// this is case where there is only . in line
		if (line.contains(".") && line.length() < 2) {
			return false;
		}
		if (line.toLowerCase().endsWith("//chords")) {
			return true;
		}
		if (line.toLowerCase().endsWith("//lyrics")) {
			return false;
		}
		// First check if this is possibly a verse marker - (C) (V)
		if (line.trim().startsWith("(") || line.trim().startsWith("[")) {
			//Logger.debug("Not chord - most likely verse marker - " + line);
			return false;
		}

		String checkLine = line.replace('-', ' ');
		checkLine = checkLine.replace('–', ' ');
		checkLine = checkLine.replace('.', ' ');
		checkLine = checkLine.replace(',', ' ');
		checkLine = checkLine.replace('/', ' ');
		checkLine = checkLine.replace('(', ' ');
		checkLine = checkLine.replace(')', ' ');
		checkLine = checkLine.replaceAll("[xX][0-9]+", "");
		checkLine = checkLine.replaceAll("[0-9]+[xX]", "");
		for (String s : checkLine.split("\\s")) {
			if (s.trim().isEmpty()) {
				continue;
			}
			if (!s.matches("([a-gA-G](#|b)?[0-9]*((sus|dim|maj|dom|min|m|aug|add)?[0-9]*){3}(#|b)?[0-9]*)(/([a-gA-G](#|b)?[0-9]*((sus|dim|maj|dom|min|m|aug|add)?[0-9]*){3}(#|b)?[0-9]*))?")) {
				//Logger.debug("This prevents this string to be chord: " + s);
				return false;
			}
		}

		return true;
	}

	public static String getSongKey(String song) {
		String[] songLines = song.split("[\r\n]+");
		String defaultKey = "C";
		String key = null;
		for (String songLine : songLines) {
			Logger.debug("Checking song lines: " + songLine);
			if (LineTypeChecker.isChordLine(songLine)) {
				Logger.trace("Checking chords " + songLine);
				if (songLine.trim().startsWith(".")) {
					Logger.debug("removing all full stops (.) with blanks");
					songLine = songLine.replace(".", "");
				}
				key = songLine.trim().substring(0, 1);
				Logger.trace("Song key:" + key);
				break;
			}
		}
		// Now checkin if this is valid Chord
		if (key != null && Arrays.asList(validKeys).contains(key)) {
			return key;
		} else {
			return defaultKey;
		}
	}
	
	public static String removeChordLines (String lyrics){
		StringBuilder lyricsWithoutChords = new StringBuilder();
		String[] songLines = lyrics.split("[\r\n]+");
		for (String songLine : songLines) {
			if (!LineTypeChecker.isChordLine(songLine)) {
				lyricsWithoutChords.append(songLine);
				lyricsWithoutChords.append("\n");
			}
		}
		return lyricsWithoutChords.toString();
	}

	public static void main(String[] args) {
		String[] testChords = { "C         D          G   -/F#   Em",
				"E H/D# C#m   E/H   A     E/H H E  H",
				".Am          D         G  Dm – G", ".(E,A,E,B7,E)",
				".     Bm7    G#dimg  G    A   D",
				".F   Em7        Asus  C/D   Dm7 C/E  F    Gm7    F/A  ",
				"(C1)" };
		for (String chordLine : testChords) {
			Logger.trace("now evaluaing this line: " + chordLine);
			boolean isValidChordLine = LineTypeChecker.isChordLine(chordLine);
			if (!isValidChordLine) {
				Logger.error("Not valid chord line: " + chordLine);
			}
		}
	}
}
