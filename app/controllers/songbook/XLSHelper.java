package controllers.songbook;

import com.avaje.ebean.Expr;
import models.Song;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by samuel on 5/8/15.
 */

public class XLSHelper {
    private static class XLSSong {
        private String songName;
        private String songOriginalTitle;
        private String songAuthor;
        private String songLink;

        public String getSongName() {
            return songName;
        }

        public void setSongName(String songName) {
            this.songName = songName;
        }

        public String getSongOriginalTitle() {
            return songOriginalTitle;
        }

        public void setSongOriginalTitle(String songOriginalTitle) {
            this.songOriginalTitle = songOriginalTitle;
        }

        public String getSongAuthor() {
            return songAuthor;
        }

        public void setSongAuthor(String songAuthor) {
            this.songAuthor = songAuthor;
        }

        public String getSongLink() {
            return songLink;
        }

        public void setSongLink(String songLink) {
            this.songLink = songLink;
        }
    }

    public static List<XLSSong> importXLS2Songs() {
        List<XLSSong> songs = new ArrayList<XLSSong>();
        try
        {
            FileInputStream file = new FileInputStream(new File("resources/PJESMARICA.xlsx"));

            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);

            //Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext())
            {
                Row row = rowIterator.next();
                //For each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();
                int i = 0;
                XLSSong song = new XLSSong();
                while (cellIterator.hasNext())
                {
                    Cell cell = cellIterator.next();
                    i++;
                    //Check the cell type and format accordingly
                    switch (cell.getCellType())
                    {
                        case Cell.CELL_TYPE_NUMERIC:
                            //System.out.print(cell.getNumericCellValue() + "\t " + i + ": ");
                            break;
                        case Cell.CELL_TYPE_STRING:
                            switch(i) {
                                case 1: song.setSongName(cell.getStringCellValue());
                                case 2: song.setSongOriginalTitle(cell.getStringCellValue());
                                case 3: song.setSongAuthor(cell.getStringCellValue());
                                case 4: song.setSongLink(cell.getStringCellValue());
                            }
                            //System.out.print(cell.getStringCellValue() + "\t " + i + ": ");
                            break;
                    }
                }
                songs.add(song);
                System.out.println("");
            }
            file.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        for (XLSSong s : songs){
            System.out.println(s.getSongName() +" "+ s.getSongOriginalTitle() +" "+s.getSongAuthor() +" "+s.getSongLink());
        }
        return songs;
    }

    public static void updateSongs(List<XLSSong> xlssongs){
        int i = 0;
        int j = 0;
        ArrayList <String> notFoundSongs = new ArrayList<>();
        for (XLSSong xlssong : xlssongs){
            j++;
            String filter = xlssong.getSongName().trim().toLowerCase();
            List<Song> songs = Song.find.where(
                    Expr.ilike("songName", "%" + filter + "%")
            ).findList();
            if ((!songs.isEmpty()) && (songs.get(0) != null)){
                System.out.println(songs.get(0).id);
                songs.get(0).setSongOriginalTitle(xlssong.getSongOriginalTitle());
                songs.get(0).setSongAuthor(xlssong.getSongAuthor());
                songs.get(0).setSongLink(xlssong.getSongLink());
                songs.get(0).update();
                i++;
                continue;
            }
            else {
                notFoundSongs.add(j + ":" +filter);
                continue;
            }
        }
        for (String s : notFoundSongs){
            System.out.println("SONG NOT FOUND: " + s);
        }
        System.out.println("SONGS NOT UPDATED: " + notFoundSongs.size());
        System.out.println("SONGS UPDATED: " + i);
    }

    public static void importAndUpdateSongs( ){
        List <XLSSong> xlssongs = importXLS2Songs();
        updateSongs(xlssongs);
    }

    public static void main(String[] args)
    {
        List <XLSSong> xlssongs = importXLS2Songs();
        updateSongs(xlssongs);
    }

}
