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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import play.Logger;

/**
 * Transposes a line of chords down or up a certain amount of semitones.
 *
 * @author Michael
 */
public class ChordLineTransposer {

	private String chordLine;
	ChordTransposerEngine1 chordHelper = new ChordTransposerEngine1();

	/**
	 * Create a new chord line transposer.
	 *
	 * @param line
	 *            the line to transpose.
	 */
	public ChordLineTransposer(String line) {
		setChordLine(sanitizeLine(line));
	}

	private String sanitizeLine(String line) {
		if (line.trim().startsWith(".")) {
			Logger.debug("removing all full stops (.) with blanks");
			line = line.replace(".", " ");
		}
		return line;
	}

	public String getChordLine() {
		return chordLine;
	}

	public void setChordLine(String line) {
		this.chordLine = line;
	}

	/**
	 * Transpose the line by the given number of semitones.
	 *
	 * @param semitones
	 *            the number of semitones to transpose by, positive or negative.
	 * @param newKey
	 *            the new key to transpose to. This can be null if not known but
	 *            if it is known it means we can properly transpose chords
	 *            (otherwise we can end up with things like E/Ab rather than
	 *            E/G#.
	 * @return the transposed line.
	 */
	public String transpose(int semitones, String newKey) {
		boolean startSpace = chordLine.startsWith(" ");
		boolean chordsComment = chordLine.endsWith("//chords");
		if (!startSpace) {
			chordLine = " " + chordLine;
		}
		if (chordsComment) {
			chordLine = chordLine.substring(0, chordLine.indexOf("//chords"));
		}
		String[] chords = chordLine.split("\\s+");
		String[] whitespace = chordLine.split("[A-Za-z0-9#/]+");
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < chords.length; i++) {
			ret.append(new ChordTransposerEngine2(chords[i]).transpose(semitones,
					newKey));
			if (i < whitespace.length) {
				ret.append(whitespace[i]);
			}
		}
		if (!startSpace) {
			chordLine = chordLine.substring(1);
		}
		String str = ret.toString();
		if (!startSpace) {
			str = str.substring(1);
		}
		if (chordsComment) {
			chordLine = chordLine + "//chords";
		}
		return str;
	}

	public String transpose2(String currentKey, String targetKey) {

		boolean startSpace = chordLine.startsWith(" ");
		if (!startSpace) {
			chordLine = " " + chordLine;
		}

		String[] chords = chordLine.split("\\s+");
		String[] whitespace = chordLine.split("[A-Za-z0-9#/]+");
		StringBuilder ret = new StringBuilder();
		Logger.debug("LINE: " + chordLine);

		for (int i = 0; i < chords.length; i++) {
			// I am skipping empty string
			if (chords[i].length() > 0 || !chords[i].contains("")) {
				// TODO: Fix / chord transpose - and minor tail
				// System.out.println("APPEND:" + chords[i].trim() + ":!");
				String s = chordHelper.transpose(currentKey, targetKey,
						chords[i]);
				Logger.debug("New chord: " + s);
				ret.append(s);
			}
			if (i < whitespace.length) {
				ret.append(whitespace[i].replace("-", ""));
			}
		}

		String str = ret.toString();
		if (!startSpace) {
			str = str.substring(1);
		}
		Logger.debug("New chord line: " + str);
		return str;
	}

	public static String transposeLyrics(String origKey, String newKey,
			String songText) {
		// String[] songLines = songText.split("[\r\n]");
		String[] songLines = songText.split("\n");
		for (String line : songLines) {
			System.out.println(line);
		}

		StringBuilder transposedSong = new StringBuilder();
		for (String songLine : songLines) {
			Logger.trace("Checking song lines: " + songLine);
			String updatedSongLine = songLine;
			if (LineTypeChecker.isChordLine(songLine)) {
				// Logger.trace("Transposing by ammount: " + transposeAmmount);
				ChordLineTransposer clt = new ChordLineTransposer(songLine);
				updatedSongLine = clt.transpose2(origKey, newKey);
				Logger.trace(updatedSongLine);
			}
			transposedSong.append(updatedSongLine + "\r\n");
		}
		Logger.trace(transposedSong.toString());
		return transposedSong.toString();
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static boolean isLetters(String name) {
		return name.matches("[a-zA-Z]+");
	}

	public static void test() {
		String songText = null;
		System.out.println("transposer test 1");
		try {
			songText = readFile("resources/testSong.txt",
					Charset.defaultCharset());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String transposedSong = ChordLineTransposer.transposeLyrics("G", "A",
				songText);
		System.out.println(transposedSong);
	}

	public static void main(String args[]) {
		/*
		 * String[] testChords = { "C         D          G   -/F#   Em",
		 * "E H/D# C#m   E/H   A     E/H H E  H",
		 * ".Am          D         G  Dm – G", ".(E,A,E,B7,E)",
		 * ".     Bm7    G#dimg  G    A   D",
		 * ".F   Em7        Asus  C/D   Dm7 C/E  F    Gm7    F/A  " }; for
		 * (String chordLine : testChords) {
		 * Logger.debug("now transposing this line: " + chordLine);
		 * ChordLineTransposer cht = new ChordLineTransposer(chordLine); String
		 * transposedChordLine = cht.transpose(2, null);
		 * Logger.debug("transposed line line: " + transposedChordLine); }
		 */

		String songText = null;
		System.out.println("transposer test 1");
		try {
			songText = readFile("resources/testSong.txt",
					Charset.defaultCharset());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ChordLineTransposer.transposeLyrics("G", "A", songText);
	}
}