package document.tools;

import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;

import chord.tools.ChordLineTransposer;
import chord.tools.LineTypeChecker;
import helpers.ArrayHelper;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import models.ServiceSong;
import models.SongLyrics;
import models.helpers.PdfPrintable;
import models.helpers.SongPrint;

;

/**
 * Created by samuel on 5/23/15.
 */
public class PdfGenerator extends PdfPageEventHelper {
	private final Document document;
	private final PdfWriter writer;

	/** The header text. */
	String header;
	/** The template with the total number of pages. */
	PdfTemplate total;

	private String outputPdfPath = "resources/pdf/songbook.pdf";
	private Rectangle songPageSize = PageSize.A4;

	String LiberationMonoFontPath = "resources/fonts/LiberationMono-Regular.ttf";
	String LiberationMonoBoldFontPath = "resources/fonts/LiberationMono-Bold.ttf";
	String TimesNewRomanFontPath = "resources/fonts/Times_New_Roman.ttf";
	String TimesNewRomanBoldFontPath = "resources/fonts/Times_New_Roman_Bold.ttf";

	int MONOSPACE_SIZE = 10;
	int NORMAL_SIZE = 10;
	int BOLD_SIZE = 12;

	BaseColor DEFAULT_COLOR = BaseColor.BLACK;
	BaseColor CHORDS_COLOR = BaseColor.BLUE;
	BaseColor VERSE_COLOR = BaseColor.WHITE;
	BaseColor VERSE_BACKGROUND_COLOR = BaseColor.LIGHT_GRAY;

	int maxCharLenght = 40;
	int maxLineNumber = 50;

	// Verse styling
	String[] verseTypes = { "Verse", "Chorus", "Bridge", "Intro", "Ending" };

	// table to store placeholder for all chapters and sections
	private final Map<String, PdfTemplate> tocPlaceholder = new HashMap<>();

	// store the chapters and sections with their title here.
	private final Map<String, Integer> pageByTitle = new HashMap<>();

	private SongFonts fonts = new SongFonts();

	private class SongFonts {

		Font MONOSPACE;
		Font MONOSPACE_CHORDS;
		Font NORMAL;
		Font VERSETYPE_FONT;
		Font BOLD;
		Font ITALIC;
		Font BOLDITALIC;

		public SongFonts() {

			FontFactory.register(LiberationMonoFontPath, LiberationMonoFontPath);
			FontFactory.register(LiberationMonoBoldFontPath, LiberationMonoBoldFontPath);
			FontFactory.register(TimesNewRomanFontPath, TimesNewRomanFontPath);
			FontFactory.register(TimesNewRomanBoldFontPath, TimesNewRomanFontPath);
			// Get the font NB. last parameter indicates font needs to be
			// embedded

			MONOSPACE = FontFactory.getFont(LiberationMonoFontPath, BaseFont.CP1250, BaseFont.EMBEDDED);
			MONOSPACE.setSize(MONOSPACE_SIZE);
			MONOSPACE.setStyle(Font.NORMAL);
			MONOSPACE.setColor(DEFAULT_COLOR);

			MONOSPACE_CHORDS = FontFactory.getFont(LiberationMonoFontPath, BaseFont.CP1250, BaseFont.EMBEDDED);
			MONOSPACE_CHORDS.setSize(MONOSPACE_SIZE);
			MONOSPACE_CHORDS.setStyle(Font.BOLD);
			MONOSPACE_CHORDS.setColor(CHORDS_COLOR);

			NORMAL = FontFactory.getFont(TimesNewRomanFontPath, BaseFont.CP1250, BaseFont.EMBEDDED);
			NORMAL.setStyle(Font.NORMAL);
			NORMAL.setSize(NORMAL_SIZE);
			NORMAL.setColor(DEFAULT_COLOR);

			VERSETYPE_FONT = FontFactory.getFont(TimesNewRomanFontPath, BaseFont.CP1250, BaseFont.EMBEDDED);
			VERSETYPE_FONT.setStyle(Font.BOLD);
			VERSETYPE_FONT.setSize(NORMAL_SIZE);
			VERSETYPE_FONT.setColor(VERSE_COLOR);

			BOLD = FontFactory.getFont(TimesNewRomanBoldFontPath, BaseFont.CP1250, BaseFont.EMBEDDED);
			BOLD.setStyle(Font.NORMAL);
			BOLD.setSize(BOLD_SIZE);
			BOLD.setColor(DEFAULT_COLOR);

			ITALIC = FontFactory.getFont(TimesNewRomanFontPath, BaseFont.CP1250, BaseFont.EMBEDDED);
			ITALIC.setStyle(Font.ITALIC);
			ITALIC.setSize(12);

			BOLDITALIC = FontFactory.getFont(TimesNewRomanFontPath, BaseFont.CP1250, BaseFont.EMBEDDED);
			BOLDITALIC.setStyle(Font.BOLDITALIC);
			BOLDITALIC.setSize(12);

		}
	}

	public PdfGenerator(String outputPdfPath) throws Exception {
		this.document = new Document(songPageSize);
		this.document.setMargins(50, 50, 60, 40);
		this.document.setMarginMirroring(false);
		this.writer = PdfWriter.getInstance(this.document, new FileOutputStream(outputPdfPath));
		this.writer.setPageEvent(this);
		this.document.open();
	}

	public void onChapter(final PdfGenerator writer, final Document document, final float paragraphPosition,
			final Paragraph title) {
		this.pageByTitle.put(title.getContent(), this.writer.getPageNumber());
	}

	public void onSection(final PdfGenerator writer, final Document document, final float paragraphPosition,
			final int depth, final Paragraph title) {
		this.pageByTitle.put(title.getContent(), this.writer.getPageNumber());
	}

	/**
	 * Allows us to change the content of the header.
	 * 
	 * @param header
	 *            The new header String
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * Adds a header to every page
	 * 
	 * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(com.itextpdf.text.pdf.PdfWriter,
	 *      com.itextpdf.text.Document)
	 */
	public void onEndPage(PdfWriter writer, Document document) {
		PdfPTable table = new PdfPTable(3);
		try {
			table.setWidths(new int[] { 24, 24, 2 });
			table.setTotalWidth(527);
			table.setLockedWidth(true);
			table.getDefaultCell().setFixedHeight(20);
			table.getDefaultCell().setBorder(Rectangle.BOTTOM);
			table.addCell(header);
			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(String.format("Page %d of", writer.getPageNumber()));
			PdfPCell cell = new PdfPCell(Image.getInstance(total));
			cell.setBorder(Rectangle.BOTTOM);
			table.addCell(cell);
			table.writeSelectedRows(0, -1, 34, 803, writer.getDirectContent());
		} catch (DocumentException de) {
			throw new ExceptionConverter(de);
		}
	}

	/**
	 * Fills out the total number of pages before the document is closed.
	 * 
	 * @see com.itextpdf.text.pdf.PdfPageEventHelper#onCloseDocument(com.itextpdf.text.pdf.PdfWriter,
	 *      com.itextpdf.text.Document)
	 */
	public void onCloseDocument(PdfWriter writer, Document document) {
		ColumnText.showTextAligned(total, Element.ALIGN_LEFT, new Phrase(String.valueOf(writer.getPageNumber() - 1)), 2,
				2, 0);
	}

	/**
	 * Creates the PdfTemplate that will hold the total number of pages.
	 * 
	 * @see com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(com.itextpdf.text.pdf.PdfWriter,
	 *      com.itextpdf.text.Document)
	 */
	public void onOpenDocument(PdfWriter writer, Document document) {
		total = writer.getDirectContent().createTemplate(30, 16);
	}

	private void createSongsTOC(List<? extends PdfPrintable> printObject) throws DocumentException {
		// add a small introduction chapter the shouldn't be counted.
		final Chapter intro = new Chapter(new Paragraph("Table Of Content", fonts.BOLD), 0);
		intro.setNumberDepth(0);
		this.document.add(intro);

		for (int i = 0; i < printObject.size(); i++) {

			final String title = printObject.get(i).getTitle();
			// final String songTitle = songPrintObjects.get(i).getSong();
			// So that song count does not start from 0
			int idxPlusOne = i + 1;
			final Chunk chunk = new Chunk(idxPlusOne + ". " + title, fonts.NORMAL).setLocalGoto(title);
			this.document.add(new Paragraph(chunk));

			final String songTitleId = title + idxPlusOne;

			// Add a placeholder for the page reference
			this.document.add(new VerticalPositionMark() {
				@Override
				public void draw(final PdfContentByte canvas, final float llx, final float lly, final float urx,
						final float ury, final float y) {
					final PdfTemplate createTemplate = canvas.createTemplate(50, 50);
					PdfGenerator.this.tocPlaceholder.put(songTitleId, createTemplate);

					canvas.addTemplate(createTemplate, urx - 50, y);
				}
			});
		}
	}

	private class SongParagraphs {
		String name;
		ArrayList<Element> paragraphs = new ArrayList<Element>();
		int longestLine = 0;

		SongParagraphs(String name, ArrayList<Element> paragraphs, int longestLine) {
			setName(name);
			setParagraphs(paragraphs);
			setLongestLine(longestLine);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ArrayList<Element> getParagraphs() {
			return paragraphs;
		}

		public void setParagraphs(ArrayList<Element> paragraphs) {
			this.paragraphs = paragraphs;
		}

		public int getLongestLine() {
			return longestLine;
		}

		public void setLongestLine(int longestLine) {
			// System.out.println("#### Longest line" + name + " : " +
			// longestLine);
			this.longestLine = longestLine;
		}
	}

	private ArrayList<SongParagraphs> getPrintableSongs(List<? extends PdfPrintable> printObject) {
		ArrayList<SongParagraphs> printableSongs = new ArrayList<SongParagraphs>();

		for (int i = 0; i < printObject.size(); i++) {
			ArrayList<Element> paragraphs = new ArrayList<Element>();

			String songTitle = printObject.get(i).getTitle();
			String content = printObject.get(i).getContent();

			int longestLine = 0;

			for (String line : content.split("\\r?\\n")) {
				// verse recognition
				boolean lineStartsWithBrace = line.startsWith("[");
				Paragraph styledParagraph = null;
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
					Chunk c = new Chunk(line.trim(), fonts.VERSETYPE_FONT);
					c.setBackground(VERSE_BACKGROUND_COLOR, 1.5f, 0f, 1.5f, 1.5f);
					styledParagraph = new Paragraph(c);
				} else if (LineTypeChecker.isChordLine(line)) {
					// CHORD STYLING
					styledParagraph = new Paragraph(line, fonts.MONOSPACE_CHORDS);
				} else {
					// STANDARD STYLING
					styledParagraph = new Paragraph(line, fonts.MONOSPACE);
				}
				// find longest line
				longestLine = (line.length() > longestLine) ? line.length() : longestLine;
				paragraphs.add(styledParagraph);
			}
			printableSongs.add(new SongParagraphs(songTitle, paragraphs, longestLine));
		}
		return printableSongs;
	}
	
	private void createSongsChapters(List<? extends PdfPrintable> printObject, boolean useColumns)
			throws DocumentException {

		PdfContentByte cb = writer.getDirectContent();
		ArrayList<SongParagraphs> printableSongs = getPrintableSongs(printObject);

		int numberOfSongs = printableSongs.size();

		boolean skipNextSong = false;

		// TODO: implement spliting one song onto multiple columns
		for (int i = 0; i < numberOfSongs; i++) {
			// skip song if it is already merged
			if (skipNextSong) {
				skipNextSong = false;
				continue;
			}

			SongParagraphs currentSong = printableSongs.get(i);
			SongParagraphs nextSong = null;

			String songTitle = currentSong.getName();

			int idxPlusOne = i + 1;
			int idxPlusTwo = idxPlusOne + 1;
			String songTitleId = songTitle + idxPlusOne;

			int songLongestLine = currentSong.getLongestLine();
			int songNumberOfLines = currentSong.getParagraphs().size();

			boolean isThereColumnForAnotherSongOnPage = (songLongestLine < maxCharLenght) ? true : false;
			boolean isThereLinesForAnotherSongOnPage = (songNumberOfLines < maxLineNumber) ? true : false;

			System.out.println("### " + songTitleId + ":" + songLongestLine + ":" + songNumberOfLines + ":");
			// first handle multi song page scenario
			if (isThereColumnForAnotherSongOnPage && isThereLinesForAnotherSongOnPage && useColumns) {
				System.out.println("Multi-Page");

				ColumnText ct = new ColumnText(cb);
				ct.setSimpleColumn(60f, 60f, 280f, 760f);
				ColumnText ct2 = new ColumnText(cb);
				ct2.setSimpleColumn(300f, 60f, 520f, 740f);
									
				final Chunk chunk = new Chunk(songTitle, fonts.BOLD).setLocalDestination(songTitle);
				Paragraph firstTitleParagraph = new Paragraph(chunk);
				final Chapter chapter = new Chapter(firstTitleParagraph, idxPlusOne);
				this.document.add(chapter);

				for (Element songParagraph : currentSong.getParagraphs()) {
					if (songParagraph != null) {
						ct.addElement(songParagraph);
					}
				}
				ct.go();
				// this is to check if this is the last song and that next song does not have too many char in line
				if (idxPlusOne < numberOfSongs) {
					
					nextSong = printableSongs.get(i + 1);
					
					System.out.println("Adding next song: " + nextSong.getName());
					
					
					final Chunk chunk2 = new Chunk(nextSong.getName(), fonts.BOLD)
							.setLocalDestination(nextSong.getName());
					Paragraph secondTitleParagraph = new Paragraph(chunk2);
					secondTitleParagraph.setFirstLineIndent(250f);
					final Chapter chapter2 = new Chapter(secondTitleParagraph, idxPlusTwo);		
					chapter2.setTriggerNewPage(false);
					this.document.add(chapter2);
					
					skipNextSong = true;			
									
					int nextSongLongestLine = nextSong.getLongestLine();
					if (nextSongLongestLine < maxCharLenght) {
						for (Element songParagraph : nextSong.getParagraphs()) {
							if (songParagraph != null) {
								ct2.addElement(songParagraph);
							}
						}
						ct2.go();
					}
				}

				// this is single song per page scenario
			} else {
				final Chunk chunk = new Chunk(songTitle, fonts.BOLD).setLocalDestination(songTitle);
				final Chapter chapter = new Chapter(new Paragraph(chunk), idxPlusOne);
				System.out.println("Single Page");
				chapter.addAll(printableSongs.get(i).getParagraphs());
				this.document.add(chapter);
			}

			// When we wrote the chapter, we now the pagenumber

			final PdfTemplate template = this.tocPlaceholder.get(songTitleId);
			String pgNumber = String.valueOf(this.writer.getPageNumber()-1);
			
			template.beginText();
			template.setFontAndSize(fonts.NORMAL.getBaseFont(), 12);
			template.setTextMatrix(
					50 - fonts.NORMAL.getBaseFont().getWidthPoint(pgNumber, 12), 0);
			template.showText(pgNumber);
			template.endText();
			
			// Add additional song page number to new chapter
			if (skipNextSong && nextSong != null) {

				String nextSongTitleId = nextSong.getName() + idxPlusTwo;
				final PdfTemplate template2 = this.tocPlaceholder.get(nextSongTitleId);
				pgNumber = String.valueOf(this.writer.getPageNumber()-1);
				
				template2.beginText();
				template2.setFontAndSize(fonts.NORMAL.getBaseFont(), 12);
				template2.setTextMatrix(
						50 - fonts.NORMAL.getBaseFont().getWidthPoint(pgNumber, 12),
						0);
				template2.showText(pgNumber);
				template2.endText();
				
			}
			
		}

	}

	private void createChapters(List<? extends PdfPrintable> printObject) throws DocumentException {

		for (int i = 0; i < printObject.size(); i++) {
			// append the chapter
			String title = printObject.get(i).getTitle();
			// final String songTitle = songPrintObjects.get(i).getSong();

			final Chunk chunk = new Chunk(title, fonts.BOLD).setLocalDestination(title);

			final Chapter chapter = new Chapter(new Paragraph(chunk), i);
			// chapter.setNumberDepth(0);

			String content = printObject.get(i).getContent();

			// System.out.println(content);

			chapter.addSection(new Paragraph(content, fonts.MONOSPACE), 0);
			// chapter.setNumberDepth(0);

			this.document.add(chapter);

			String titleId = title + i;

			// When we wrote the chapter, we now the pagenumber
			final PdfTemplate template = this.tocPlaceholder.get(titleId);
			template.beginText();
			template.setFontAndSize(fonts.NORMAL.getBaseFont(), 12);
			template.setTextMatrix(
					50 - fonts.NORMAL.getBaseFont().getWidthPoint(String.valueOf(this.writer.getPageNumber()), 12), 0);
			template.showText(String.valueOf(this.writer.getPageNumber()));
			template.endText();

		}
	}

	public static void writeListContent(String outputPdfPath, List<? extends PdfPrintable> songPrintObjects,
			boolean useColumns) {
		PdfGenerator pdfGenerator;
		try {
			pdfGenerator = new PdfGenerator(outputPdfPath);
			pdfGenerator.createSongsTOC(songPrintObjects);
			pdfGenerator.createSongsChapters(songPrintObjects, useColumns);
			pdfGenerator.document.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void simpleTest() {
		PdfGenerator pdfGenerator;
		try {
			pdfGenerator = new PdfGenerator("resources/pdf/test.pdf");
			// pdfGenerator.document.add(new Paragraph(
			// "This is an example to generate a TOC.", new SongFonts()
			// .getMonospaceFont()));
			// pdfGenerator.createTOC(10);
			// pdfGenerator.createChapters(10);
			pdfGenerator.document.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Rectangle getSongPageSize() {
		return songPageSize;
	}

	public void setSongPageSize(Rectangle songPageSize) {
		this.songPageSize = songPageSize;
	}

	public static void main(final String[] args) throws Exception {
		List<SongPrint> songPrintObjects = new ArrayList<SongPrint>();
		// Song s = new Song();
		// s.setSongName("10000 Razloga");
		String l = "      F    C   G/E    Am\n" + "Slavi Gospoda, dušo moja,\n" + "F      C     G\n"
				+ "ime Mu sveto je." + "       F       C   F  G  Am\n" + "Pjevaj kao nikada, slavi Ga, ";
				// songPrintObjects.add(new SongPrint("10000 Razloga", 123l,
				// l));

		// Song s2 = new Song();
		// s2.setSongName("Ako Bog nije živ");
		String l2 = "      F    C   G/E    Am\n" + "Slavi Gospoda, dušo moja,\n" + "F      C     G\n"
				+ "ime Mu sveto je.\n" + "       F       C   F  G  Am\n" + "Pjevaj kao nikada, slavi Ga, ";
				// songPrintObjects.add(new SongPrint("Ako Bog nije živ", 124l,
				// l2));

		// PdfGenerator.writeSongs(songPrintObjects);

	}
}
