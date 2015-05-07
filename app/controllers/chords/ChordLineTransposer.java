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
package controllers.chords;
import play.Logger;


/**
 * Transposes a line of chords down or up a certain amount of semitones.
 *
 * @author Michael
 */
public class ChordLineTransposer {

    private String line;
    ChordHelper chordHelper = new ChordHelper();


    /**
     * Create a new chord line transposer.
     *
     * @param line
     *            the line to transpose.
     */
    public ChordLineTransposer(String line) {
        this.line = sanitizeLine(line);
    }

    private String sanitizeLine(String line) {
        if (line.startsWith(".")) {
            Logger.debug("removing all full stops (.) with blanks");
            line = line.replace(".", " ");
        }
        return line;
    }

    /**
     * Transpose the line by the given number of semitones.
     *
     * @param semitones
     *            the number of semitones to transpose by, positive or negative.
     * @param newKey
     *            the new key to transpose to. This can be null if not known but if it is known it means we can properly
     *            transpose chords (otherwise we can end up with things like E/Ab rather than E/G#.
     * @return the transposed line.
     */
    public String transpose(int semitones, String newKey) {
        boolean startSpace = line.startsWith(" ");
        boolean chordsComment = line.endsWith("//chords");
        if (!startSpace) {
            line = " " + line;
        }
        if (chordsComment) {
            line = line.substring(0, line.indexOf("//chords"));
        }
        String[] chords = line.split("\\s+");
        String[] whitespace = line.split("[A-Za-z0-9#/]+");
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < chords.length; i++) {
            ret.append(new ChordTransposer(chords[i]).transpose(semitones, newKey));
            if (i < whitespace.length) {
                ret.append(whitespace[i]);
            }
        }
        if (!startSpace) {
            line = line.substring(1);
        }
        String str = ret.toString();
        if (!startSpace) {
            str = str.substring(1);
        }
        if (chordsComment) {
            line = line + "//chords";
        }
        return str;
    }

    public String transpose2 (String currentKey, String targetKey){

        String[] chords = line.split("\\s+");
        String[] whitespace = line.split("[A-Za-z0-9#/]+");
        StringBuilder ret = new StringBuilder();
        System.out.println("LINE: " + line);
        for (String s : chords){
            System.out.println("S:" + s + ":!");
        }
        for (int i = 0; i < chords.length; i++) {
            //I am skipping empty string
            if (chords[i].length()>0 || !chords[i].contains("")){
                //TODO: Fix / chord transpose - and minor tail
                System.out.println("APPEND:" + chords[i].trim() + ":!");
                String s = chordHelper.transpose(currentKey, targetKey, chords[i].trim());
                System.out.println("APPEND_NEW:" + s + ":!");
                ret.append(s);
            }
            //ret.append(ch.transpose(currentKey, targetKey, chords[i].trim()));
            if (i < whitespace.length) {
                System.out.println("APPEND_WHITE: " + whitespace[i]);
                ret.append(whitespace[i].replace("-",""));
            }
        }

        String str = ret.toString();

        return str;
    }

    public static void main(String args[]) {
        String[] testChords = { "C         D          G   -/F#   Em", "E H/D# C#m   E/H   A     E/H H E  H",
                ".Am          D         G  Dm – G", ".(E,A,E,B7,E)", ".     Bm7    G#dimg  G    A   D",
                ".F   Em7        Asus  C/D   Dm7 C/E  F    Gm7    F/A  " };
        for (String chordLine : testChords) {
            Logger.debug("now transposing this line: " + chordLine);
            ChordLineTransposer cht = new ChordLineTransposer(chordLine);
            String transposedChordLine = cht.transpose(2, null);
            Logger.debug("transposed line line: " + transposedChordLine);
        }
    }
}