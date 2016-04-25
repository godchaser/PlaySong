package models.helpers;

import java.util.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;

/**
 * Created by samuel on 4/7/15.
 */
public class URLParamEncoder {
    String input;
    Map<String, String> searchAndReplaceMap = new HashMap<String, String>();

    public URLParamEncoder(String input) {
        this.input = input;
        searchAndReplaceMap.put("đ", "d");
        searchAndReplaceMap.put("Đ", "D");
        searchAndReplaceMap.put("'", "");
        searchAndReplaceMap.put(",", "");
        searchAndReplaceMap.put(".", "");
        searchAndReplaceMap.put("!", "");
        searchAndReplaceMap.put("?", "");
        searchAndReplaceMap.put(" ", "-");
        searchAndReplaceMap.put("--", "-");
    }

    public String encode() {
        String inputWithoutDiacritics = removeDiacritics(input);
        String inputWithoutSpecialCharacters = searchAndReplace(inputWithoutDiacritics, searchAndReplaceMap);
        return encodeForUrl(inputWithoutSpecialCharacters.toLowerCase());
    }

    public String searchAndReplace(String inputString, Map<String, String> searchAndReplaceStrings) {
        String replacedString = inputString;
        for (Map.Entry<String, String> entry : searchAndReplaceStrings.entrySet()) {
            //System.out.println("Changing: " + entry.getKey() + " -> " + entry.getValue());
            replacedString = replacedString.replace(entry.getKey(), entry.getValue());
        }
        return replacedString;
    }

    public String removeDiacritics(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            String decomposed = Normalizer.normalize(String.valueOf(ch), Form.NFD);
            String normalized = decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            resultStr.append(normalized);
        }

        return resultStr.toString();
    }

    public String encodeForUrl(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    public char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    public boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0)
            return true;
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }

}
