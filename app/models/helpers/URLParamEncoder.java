package models.helpers;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by samuel on 4/7/15.
 */
public class URLParamEncoder {
    
    public static String stripAccentsAndEncode(String input){
        // remove all punctuation - ! . ,
        input = input.replaceAll("[^a-zA-Z0-9\\s]", "");
        // remove diacritic letters
        input = StringUtils.stripAccents(input);
        input = input.replace("đ", "d").replace("Đ", "d"); 

        return encode(input);
    }
    public static String encode(String input) {
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

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0)
            return true;
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }

}
