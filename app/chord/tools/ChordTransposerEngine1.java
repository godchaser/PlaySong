package chord.tools;

import java.util.ArrayList;
import java.util.List;

import play.Logger;

/**
 * Created by samuel on 5/1/15.
 */
public class ChordTransposerEngine1 {
    private List<Key> keys;

    private class Key {
        String name;
        int value;
        String type;

        public Key(String name, int value, String type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }
    }

    public ChordTransposerEngine1() {
        keys = new ArrayList<Key>();
        keys.add(new Key("Ab", 0, "F"));
        keys.add(new Key("A", 1, "N"));
        keys.add(new Key("A#", 2, "S"));
        keys.add(new Key("Bb", 2, "F"));
        keys.add(new Key("B", 3, "N"));
        keys.add(new Key("C", 4, "N"));
        keys.add(new Key("C#", 5, "S"));
        keys.add(new Key("Db", 5, "F"));
        keys.add(new Key("D", 6, "N"));
        keys.add(new Key("D#", 7, "S"));
        keys.add(new Key("Eb", 7, "F"));
        keys.add(new Key("E", 8, "N"));
        keys.add(new Key("F", 9, "N"));
        keys.add(new Key("F#", 10, "S"));
        keys.add(new Key("Gb", 10, "F"));
        keys.add(new Key("G", 11, "N"));
        keys.add(new Key("G#", 0, "S"));
    }

    Key getKeyByName(String name) {
        if ("m".equals(name.charAt(name.length() - 1))) {
            name = name.substring(0, name.length() - 1);
        }
        for (int i = 0; i < keys.size(); i++) {
            if (name.equals(keys.get(i).name)) {
                return keys.get(i);
            }
        }
        // System.out.println("NAME: " + name + "!!!");
        return null;
    }

    String getChordRoot(String input) {
        String firstChar = null;
        if (input.length() > 1) {
            firstChar = input.substring(1, 2);
        }
        // System.out.println("INPUT: " + firstChar + "!!!");
        if (("b".equals(firstChar) || "#".equals(firstChar)))
            return input.substring(0, 2);
        else
            return input.substring(0, 1);
    }

    String getNewKey(String oldKey, int delta, String targetKey) {
        int keyValue = getKeyByName(oldKey).value + delta;
        // System.out.println("targetKey: " + targetKey);

        if (keyValue > 11) {
            keyValue -= 12;
        } else if (keyValue < 0) {
            keyValue += 12;
        }
        // System.out.println("keyValue: " + keyValue);
        int i = 0;
        if (keyValue == 0 || keyValue == 2 || keyValue == 5 || keyValue == 7 || keyValue == 10) {
            // Return the Flat or Sharp Key
            switch (targetKey) {
            case "A":
            case "A#":
            case "B":
            case "C":
            case "C#":
            case "D":
            case "D#":
            case "E":
            case "F#":
            case "G":
            case "G#":
                for (; i < keys.size(); i++) {
                    if (keys.get(i).value == keyValue && keys.get(i).type == "S") {
                        return keys.get(i).name;
                    }
                }
            default:
                for (; i < keys.size(); i++) {
                    if (keys.get(i).value == keyValue && keys.get(i).type == "F") {
                        return keys.get(i).name;
                    }
                }
            }
        } else {
            // Return the Natural Key
            for (; i < keys.size(); i++) {
                if (keys.get(i).value == keyValue) {
                    return keys.get(i).name;
                }
            }
        }
        return null;
    }

    String getChordType(String key) {
        switch (key.charAt(key.length() - 1)) {
        case 'b':
            return "F";
        case '#':
            return "S";
        default:
            return "N";
        }
    }

    int getDelta(int oldIndex, int newIndex) {
        if (oldIndex > newIndex)
            return 0 - (oldIndex - newIndex);
        else if (oldIndex < newIndex)
            return 0 + (newIndex - oldIndex);
        else
            return 0;
    }

    String transpose(String currentKey, String targetKey, String chord) {
        Logger.debug("Curr key: " + currentKey);
        Logger.debug("Target key: " + targetKey);
        Logger.debug("Chord: " + chord);

        int delta = getDelta(getKeyByName(currentKey.trim()).value, getKeyByName(targetKey.trim()).value);
        Logger.debug("Key delta: " + delta);

        StringBuilder newChord = new StringBuilder();

        // Case when only 1 chord is in brackets (A)
        if (chord.startsWith("(") && chord.endsWith(")")) {
            chord = chord.substring(1, chord.length() - 1);
            // Case when multiple chords in brackets (A...
        } else if (chord.startsWith("(") && !chord.endsWith(")")) {
            chord = chord.substring(1, chord.length());
        } // Case when multiple chords in brackets ...C)
        else if (!chord.startsWith("(") && chord.endsWith(")")) {
            chord = chord.substring(0, chord.length() - 1);
        }

        Logger.debug("Clean chord (without brackets): " + chord);

        if (chord.contains("/")) {
            String[] parts = chord.split("/");
            String chordFirstTransposed;
            String chordFirst = parts[0];
            // System.out.println("#" +parts[0] + ":PARTS:" +parts[1]+"#");
            if ("-".equals(chordFirst)) {
                chordFirstTransposed = chordFirst;
            } else {
                String chordRootFirstChord = getChordRoot(chordFirst);
                chordFirstTransposed = getNewChord(delta, chordFirst, chordRootFirstChord, targetKey);
            }
            String chordSecond = parts[1];
            String chordRootSecondChord = getChordRoot(chordSecond);
            String chordSecondTransposed = getNewChord(delta, chordSecond, chordRootSecondChord, targetKey);

            newChord.append(chordFirstTransposed).append("/").append(chordSecondTransposed);
        } else {
            String chordRoot = getChordRoot(chord);
            String newChordRoot = getNewKey(chordRoot, delta, targetKey.trim());
            // System.out.println("len:" + chord.substring(chordRoot.length())+"$");
            String chordTail = chord.substring(chordRoot.length()).replace("#", "");
            chordTail = chordTail.replace("b", "");
            newChord.append(newChordRoot).append(chordTail);
        }

        return newChord.toString();
    }

    private String getNewChord(int delta, String chord, String chordRoot, String targetKey) {
        String newChordRoot = getNewKey(chordRoot, delta, targetKey.trim());
        String chordTail = chord.substring(chordRoot.length()).replace("#", "");
        chordTail = chordTail.replace("b", "");
        String newChord = newChordRoot + chordTail;
        // System.out.println("len:" + chord.substring(chordRoot.length())+"$");
        return newChord;
    }

    public static void main(String[] args) {
        String currentKey = "D";
        String targetKey = "C";
        // String chord = "-/F#";
        // String chord = "F#";
        // String chord = "A/C#";
        // String chord = "F#";
        String chord = "Am7";
        ChordTransposerEngine1 ch = new ChordTransposerEngine1();
        String transposed = ch.transpose(currentKey, targetKey, chord);
        System.out.println("key: " + currentKey + "\n targ: " + targetKey + "\n chord: " + chord + "\n trans: " + transposed);
    }
}
