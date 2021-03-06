package models.helpers;

import java.util.List;

import chord.tools.LineTypeChecker;

/**
 * Created by samuel on 12/30/15.
 */
public class HtmlBuilder {

    // Verse styling
    private static String[] verseTypes = { "Verse", "Chorus", "Bridge", "Intro",
            "Ending" };
    private static String startMonospaceOpenTag = "<font face=\"monospace\"><small>";
    private static String startMonospaceCloseTag = "</small></font>";
    private static String chordOpenTag = "<font color=\"#376180\" face=\"monospace\"><b><small>";
    private static String chordCloseTag = "</b></small></font>";
    private static String verseTypeOpenTag = "<font color=\"#9A504F\"><small>";
    private static String verseTypeCloseTag = "</small></font>";
    private static String verseOpenTag = "<font face=\"monospace\"><small>";
    private static String verseCloseTag = "</small></font>";
    private static String verseOpenChorusTag = "<font face=\"monospace\"><b><small>";
    private static String verseCloseChorusTag = "</b></small></font>";

    /*
     * <font color="#c5c5c5">" + "Competitor ID: " + "<font color=\"#47a842\" face=\"monospace\">" + songLyrics + "</font>"
     */
    public static String androidViewHtmlDecorator(String prependStr,
            String orgStr, String appendStr) {
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

        boolean isChorus = false;
        for (String line : songlyrics.split("\\r?\\n")) {
            // verse recognition
            boolean lineStartsWithBrace = line.startsWith("[");
            if (lineStartsWithBrace || ArrayHelper
                    .stringContainsItemFromList(line, verseTypes)) {
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
                // fonts.MONOSPACE.setColor(BaseColor.WHITE);
                // trim line
                songLyricsHtml.append(androidViewHtmlDecorator(verseTypeOpenTag,
                        line.trim(), verseTypeCloseTag));
                isChorus = line.contains("Chorus") ? true : false;
            } else if (LineTypeChecker.isChordLine(line)) {
                // CHORD STYLING
                // BLUE line
                songLyricsHtml.append(androidViewHtmlDecorator(chordOpenTag,
                        line, chordCloseTag));
            } else {
                // STANDARD STYLING
                // line
                // chorus verses are bold
                if (isChorus) {
                    songLyricsHtml.append(androidViewHtmlDecorator(
                            verseOpenChorusTag, line, verseCloseChorusTag));
                } else {
                    songLyricsHtml.append(androidViewHtmlDecorator(verseOpenTag,
                            line, verseCloseTag));
                }
            }
        }
        songLyricsHtml.append(startMonospaceCloseTag);
        // Log.i("LyricsHtmlBuilder", "HTML: " + songLyricsHtml.toString());
        return songLyricsHtml.toString();
    }

    public static String buildHtmlSongButtonLinks(List<String> songLyricsIds,
            String songName) {
        StringBuilder sb = new StringBuilder(
                "<div class=\"paginationBar\"> <button type=\"button\" class=\"btn btn-link lyrics-link\" id=\'");
        sb.append(songLyricsIds.get(0));
        sb.append("\'>");
        sb.append(songName);
        sb.append("</button>");
        if (songLyricsIds.size() > 1) {
            int i = 0;
            for (String songlyricsId : songLyricsIds) {
                if (i > 0) {
                    i++;
                    sb.append("<button class=\"lyrics-link\" id=\'");
                    sb.append(songlyricsId);
                    sb.append(">\' ");
                    sb.append(i);
                    sb.append("\'</button>\'");
                    // sLinks = sLinks.concat('<button class="lyrics-link" id=' + aData.songLyricsIDs[i] + '>' + idx +
                    // '</button>');
                }

            }
        }
        sb.append("</div>");
        return sb.toString();
    }

    public static String buildHtmlVideoButtonLink(String videoLink) {
        if (videoLink != null && URLParamEncoder.isUrl(videoLink)) {
            StringBuilder sb = new StringBuilder(
                    "<button type=\"button\" class=\"ytlinkbutton\" id=\"");
            sb.append(videoLink);
            sb.append(
                    "\" onclick=\"openYtLink(this.id)\"><i class=\"fa fa-play-circle fa-lg\"></i></button>");
            return sb.toString();
        } else {
            return "";
        }
    }
}