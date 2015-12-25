package document.tools;

import com.avaje.ebean.Expr;

import models.Song;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by samuel on 5/8/15.
 */

public class XlsHelper {
	private static class XLSSong {
		private String songName;
		private String songOriginalTitle;
		private String songAuthor;
		private String songLink;
		private String ID;

		public String getID() {
			return ID;
		}

		public void setID(String iD) {
			ID = iD;
		}

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

	public static List<XLSSong> importXLS2Songs2() {
		List<XLSSong> songs = new ArrayList<XLSSong>();
		try {
			FileInputStream file = new FileInputStream(new File(
					"resources/PJESMARICA.xlsx"));

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				// For each row, iterate through all the columns
				Iterator<Cell> cellIterator = row.cellIterator();
				int i = 0;
				XLSSong song = new XLSSong();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					i++;
					System.out.println(i);
					System.out.println(cell.getStringCellValue());
					// Check the cell type and format accordingly
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_BLANK:
						// System.out.print(cell.getNumericCellValue() + "\t " +
						// i + ": ");
						break;
					case Cell.CELL_TYPE_NUMERIC:
						// System.out.print(cell.getNumericCellValue() + "\t " +
						// i + ": ");
						break;
					case Cell.CELL_TYPE_STRING:
						switch (i) {
						case 1:
							song.setSongName(cell.getStringCellValue() + " : "
									+ i);
							break;
						case 2:
							song.setSongOriginalTitle(cell.getStringCellValue()
									+ " : " + i);
							break;
						case 3:
							song.setSongAuthor(cell.getStringCellValue()
									+ " : " + i);
							break;
						case 4:
							song.setSongLink(cell.getHyperlink() + " : " + i);
							break;
						}
						// System.out.print(cell.getStringCellValue() + "\t " +
						// i + ": ");
						break;
					}
				}
				songs.add(song);
				// System.out.println("");
			}
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (XLSSong s : songs) {
			System.out.println(s.getSongName() + " " + s.getSongOriginalTitle()
					+ " " + s.getSongAuthor() + " " + s.getSongLink());
		}
		return songs;
	}

	public static List<XLSSong> importXLS2Songs() {
		int MY_MINIMUM_COLUMN_COUNT = 4;
		List<XLSSong> songs = new ArrayList<>();
		try {
			FileInputStream file = new FileInputStream(new File(
					"resources/PJESMARICA.xlsx"));

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			// Decide which rows to process
			int rowStart = Math.min(0, sheet.getFirstRowNum());
			int rowEnd = Math.max(2000, sheet.getLastRowNum());

			for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
				Row r = sheet.getRow(rowNum);
				XLSSong song = new XLSSong();

				int lastColumn = Math.max(r.getLastCellNum(),
						MY_MINIMUM_COLUMN_COUNT);
				for (int cn = 0; cn < lastColumn; cn++) {
					System.out.println(cn);
					Cell c = r.getCell(cn, Row.RETURN_BLANK_AS_NULL);
					if (c == null) {
						// The spreadsheet is empty in this cell
						System.out.println("THIS IS NULL");
					} else {
						// Do something useful with the cell's contents
						switch (c.getCellType()) {
						case Cell.CELL_TYPE_BLANK:
							// System.out.print(cell.getNumericCellValue() +
							// "\t " + i + ": ");
							break;
						case Cell.CELL_TYPE_NUMERIC:
							// System.out.print(cell.getNumericCellValue() +
							// "\t " + i + ": ");
							break;
						case Cell.CELL_TYPE_STRING:
							switch (cn) {
							case 0:
								song.setSongName(c.getStringCellValue());
								break;
							case 1:
								song.setSongOriginalTitle(c
										.getStringCellValue());
								break;
							case 2:
								song.setSongAuthor(c.getStringCellValue());
								break;
							case 3:
								song.setSongLink(c.getStringCellValue());
								break;
							}
							// System.out.print(cell.getStringCellValue() +
							// "\t " + i + ": ");
							break;
						}
					}
					songs.add(song);
				}
			}
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (XLSSong s : songs) {
			System.out.println(s.getSongName() + " " + s.getSongOriginalTitle()
					+ " " + s.getSongAuthor() + " " + s.getSongLink());
		}

		return songs;
	}

	public static List<XLSSong> importXLS2Songs3() {
		int MY_MINIMUM_COLUMN_COUNT = 5;
		List<XLSSong> songs = new ArrayList<>();
		try {
			FileInputStream file = new FileInputStream(new File(
					"resources/upload/songs.xlsx"));

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			// Decide which rows to process
			int rowStart = Math.min(0, sheet.getFirstRowNum());
			int rowEnd = Math.max(2000, sheet.getLastRowNum());

			for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
				Row r = sheet.getRow(rowNum);
				XLSSong song = new XLSSong();

				int lastColumn = Math.max(r.getLastCellNum(),
						MY_MINIMUM_COLUMN_COUNT);
				for (int cn = 0; cn < lastColumn; cn++) {
					System.out.println(cn);
					Cell c = r.getCell(cn, Row.RETURN_BLANK_AS_NULL);
					if (c == null) {
						// The spreadsheet is empty in this cell
						System.out.println("THIS IS NULL");
					} else {
						// Do something useful with the cell's contents
						switch (c.getCellType()) {
						case Cell.CELL_TYPE_BLANK:
							// System.out.print(cell.getNumericCellValue() +
							// "\t " + i + ": ");
							break;
						case Cell.CELL_TYPE_NUMERIC:
							// System.out.print(cell.getNumericCellValue() +
							// "\t " + i + ": ");
							break;
						case Cell.CELL_TYPE_STRING:
							switch (cn) {
							case 0:
								song.setSongName(c.getStringCellValue());
								break;
							case 1:
								song.setSongOriginalTitle(c
										.getStringCellValue());
								break;
							case 2:
								song.setSongAuthor(c.getStringCellValue());
								break;
							case 3:
								song.setSongLink(c.getStringCellValue());
								break;
							case 4:
								song.setID(c.getStringCellValue());
								break;
							}
							// System.out.print(cell.getStringCellValue() +
							// "\t " + i + ": ");
							break;
						}
					}
					songs.add(song);
				}
			}
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (XLSSong s : songs) {
			System.out.println(s.getSongName() + " " + s.getSongOriginalTitle()
					+ " " + s.getSongAuthor() + " " + s.getSongLink());
		}

		return songs;
	}

	public static void updateSongs(List<XLSSong> xlssongs) {
		int i = 0;
		int j = 0;
		ArrayList<String> notFoundSongs = new ArrayList<>();
		for (XLSSong xlssong : xlssongs) {
			j++;
			String filter = xlssong.getSongName().trim().toLowerCase();
			String id = xlssong.getID();
			if (id != null) {
				System.out.println("FOUND SONG BY ID " + id);
				Song song = Song.find.byId(Long.parseLong(id));				
				song.setSongName(xlssong.getSongName());
				song.setSongOriginalTitle(xlssong.getSongOriginalTitle());
				song.setSongAuthor(xlssong.getSongAuthor());
				song.setSongLink(xlssong.getSongLink());				
				song.update();
				i++;
			} else {
				System.out.println("SEARCHING BY FILTER");
				List<Song> songs = Song.find.where().add(
						Expr.ilike("songName", "%" + filter + "%")).findList();
				if ((!songs.isEmpty()) && (songs.get(0) != null)) {
					System.out.println(songs.get(0).id);
					songs.get(0).setSongOriginalTitle(
							xlssong.getSongOriginalTitle());
					songs.get(0).setSongAuthor(xlssong.getSongAuthor());
					songs.get(0).setSongLink(xlssong.getSongLink());
					songs.get(0).update();
					i++;
					continue;
				} else {
					notFoundSongs.add(j + ":" + filter);
					continue;
				}
			}
		}
		for (String s : notFoundSongs) {
			System.out.println("SONG NOT FOUND: " + s);
		}
		System.out.println("SONGS NOT UPDATED: " + notFoundSongs.size());
		System.out.println("SONGS UPDATED: " + i);
	}

	public static void importAndUpdateSongs() {
		List<XLSSong> xlssongs = importXLS2Songs();
		updateSongs(xlssongs);
	}

	public static void importAndUpdateSongs3() {
		List<XLSSong> xlssongs = importXLS2Songs3();
		updateSongs(xlssongs);
	}

	public static void main(String[] args) {
		List<XLSSong> xlssongs = importXLS2Songs();
		updateSongs(xlssongs);
	}

	public static void dumpSongs(List<Song> all) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("PlaySong Songs");
		ArrayList<Song> songs = new ArrayList<Song>();
		songs.addAll(all);

		int rownum = 0;
		for (Song song : songs) {
			Row row = sheet.createRow(rownum++);
			List<String> songRecords = new ArrayList<String>();
			songRecords.add(song.getSongName());
			songRecords.add(song.getSongOriginalTitle());
			songRecords.add(song.getSongAuthor());
			songRecords.add(song.getSongLink());
			songRecords.add(song.getId().toString());
			int cellnum = 0;
			for (String songrecord : songRecords) {
				Cell cell = row.createCell(cellnum++);
				if (songrecord instanceof String) {
					cell.setCellValue(songrecord);
				}
			}
		}

		try {
			FileOutputStream out = new FileOutputStream(new File(
					"resources/xlsx/songs.xlsx"));
			workbook.write(out);
			out.close();
			System.out.println("Excel written successfully..");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
