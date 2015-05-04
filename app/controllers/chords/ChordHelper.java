package controllers.chords;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by samuel on 5/1/15.
 */
public class ChordHelper {
    private List<Key> keys = new ArrayList();
    private class Key {
        String name;
        int value;
        String type;
        public Key(String name, int value, String type){
            this.name=name;
            this.value=value;
            this.type=type;
        }
    }

    public ChordHelper (){
        keys.add(new Key("Ab",0,"F"));
        keys.add(new Key("A",1,"N"));
        keys.add(new Key("A#",2,"S"));
        keys.add(new Key("Bb",2,"F"));
        keys.add(new Key("B",3,"N"));
        keys.add(new Key("C",4,"N"));
        keys.add(new Key("C#",5,"S"));
        keys.add(new Key("Db",5,"F"));
        keys.add(new Key("D",6,"N"));
        keys.add(new Key("D#",7,"F"));
        keys.add(new Key("Eb",7,"F"));
        keys.add(new Key("E",8,"N"));
        keys.add(new Key("F",9,"N"));
        keys.add(new Key("F#",10,"S"));
        keys.add(new Key("Gb",10,"F"));
        keys.add(new Key("G",11,"N"));
        keys.add(new Key("G#",0,"S"));
    }

    Key getKeyByName (String name) {
        if ("m".equals(name.charAt(name.length()-1))) {
            name = name.substring(0, name.length()-1);
        }
        for (int i = 0; i < keys.size(); i++) {
            if (name.equals(keys.get(i).name)) {
                return keys.get(i);
            }
        }
        System.out.println("NAME: " + name + "!!!");
        return null;
    }

    String getChordRoot (String input) {
        System.out.println("INPUT: " + input + "!!!");
        if (input.length() > 1 && ("b".equals(input.charAt(1)) || "#".equals(input.charAt(1))))
            return input.substring(0, 2);
        else
            return input.substring(0, 1);
    }

    String getNewKey (String oldKey, int delta, String targetKey) {
        int keyValue = getKeyByName(oldKey).value + delta;

        if (keyValue > 11) {
            keyValue -= 12;
        } else if (keyValue < 0) {
            keyValue += 12;
        }

        int i=0;
        if (keyValue == 0 || keyValue == 2 || keyValue == 5 || keyValue == 7 || keyValue == 10) {
            // Return the Flat or Sharp Key
            switch(targetKey) {
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
                    for (;i<keys.size();i++) {
                        if (keys.get(i).value == keyValue && keys.get(i).type == "S") {
                            return keys.get(i).name;
                        }
                    }
                default:
                    for (;i<keys.size();i++) {
                        if (keys.get(i).value == keyValue && keys.get(i).type == "F") {
                            return keys.get(i).name;
                        }
                    }
            }
        }
        else {
            // Return the Natural Key
            for (;i<keys.size();i++) {
                if (keys.get(i).value == keyValue) {
                    return keys.get(i).name;
                }
            }
        }
        return null;
    }

    String getChordType (String key) {
        switch (key.charAt(key.length() - 1)) {
            case 'b':
                return "F";
            case '#':
                return "S";
            default:
                return "N";
        }
    }

    int getDelta (int oldIndex, int newIndex) {
        if (oldIndex > newIndex)
            return 0 - (oldIndex - newIndex);
        else if (oldIndex < newIndex)
            return 0 + (newIndex - oldIndex);
        else
            return 0;
    }

    String transpose (String currentKey, String targetKey, String chord){
        System.out.println("AAAA");
        System.out.println("curr: " + currentKey);
        System.out.println("target: " + targetKey);
        System.out.println("chord: " + chord);

        int delta = getDelta(getKeyByName(currentKey.trim()).value, getKeyByName(targetKey.trim()).value);
        String chordRoot = getChordRoot(chord);
        String newChordRoot = getNewKey(chordRoot, delta, targetKey.trim());
        String newChord = newChordRoot + chord.substring(chordRoot.length());
        return newChord;
    }

    public static void main (String[] args){
        String currentKey = "C";
        String targetKey = "D";
        String chord = "G/B";
        ChordHelper ch = new ChordHelper();
        String transposed = ch.transpose(currentKey, targetKey, chord);
        System.out.println("key: " + currentKey + "\n targ: " +targetKey + "\n chord: " + chord +"\n trans: " + transposed);
    }
}
