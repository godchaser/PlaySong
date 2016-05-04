package models.helpers;

import chord.tools.LineTypeChecker;


/**
 * Created by samuel on 12/30/15.
 */
public class AndroidLyricsHtmlBuilder {

    // Verse styling
    private static String[] verseTypes = { "Verse", "Chorus", "Bridge", "Intro", "Ending" };
    private static String startMonospaceOpenTag = "<font face=\"monospace\"><small>";
    private static String startMonospaceCloseTag = "</small></font>";
    private static String chordOpenTag = "<font color=\"#376180\" face=\"monospace\"><b><small>";
    private static String chordCloseTag = "</b></small></font>";
    private static String verseTypeOpenTag = "<font color=\"#9A504F\"><small>";
    private static String verseTypeCloseTag = "</small></font>";
    private static String verseOpenTag = "<font face=\"monospace\"><small>";
    private static String verseCloseTag = "</small></font>";

    /*
    <font color="#c5c5c5">" + "Competitor ID: " +
    "<font color=\"#47a842\" face=\"monospace\">" + songLyrics + "</font>"
     */
    public static String htmlDecorator(String prependStr, String orgStr, String appendStr){
            StringBuilder builder = new StringBuilder();
            builder.append(prependStr);
            builder.append(orgStr.replaceAll(" ", "&nbsp;"));
            builder.append(appendStr);
            builder.append("<br/>");
            // replace all spaces with html signature
        return builder.toString();
    }


    public static String buildHtmlFromSongLyrics(String songlyrics) {
        StringBuilder songLyricsHtml = new StringBuilder();
        songLyricsHtml.append(startMonospaceOpenTag);

        for (String line : songlyrics.split("\\r?\\n")) {
            // verse recognition
            boolean lineStartsWithBrace = line.startsWith("[");
            if (lineStartsWithBrace || ArrayHelper.stringContainsItemFromList(line, verseTypes)) {
                // expand verse type name if necessary
                if (lineStartsWithBrace) {
                    switch ("" + line.charAt(1)) {
                        case "C":
                            line = line.replace("C", "Chorus ");
                            break;
                        case "V":
                            line = line.replace("V", "Verse ");
                            break;
                        case "B":
                            line = line.replace("B", "Bridge ");
                            break;
                        case "I":
                            line = line.replace("I", "Intro ");
                            break;
                        case "E":
                            line = line.replace("E", "Ending ");
                            break;
                        default:
                            break;
                    }
                    // remove braces
                    line = line.substring(1, line.length() - 1);
                }
                // VERSETYPE STYLING
                //fonts.MONOSPACE.setColor(BaseColor.WHITE);
                //trim line
                songLyricsHtml.append(htmlDecorator(verseTypeOpenTag, line.trim(), verseTypeCloseTag));
            } else if (LineTypeChecker.isChordLine(line)) {
                // CHORD STYLING
                // BLUE line
                songLyricsHtml.append(htmlDecorator(chordOpenTag, line, chordCloseTag));
            } else {
                // STANDARD STYLING
                // line
                songLyricsHtml.append(htmlDecorator(verseOpenTag, line, verseCloseTag));
            }
        }
        songLyricsHtml.append(startMonospaceCloseTag);
        //Log.i("LyricsHtmlBuilder", "HTML: " + songLyricsHtml.toString());
        return songLyricsHtml.toString();
    }
}
