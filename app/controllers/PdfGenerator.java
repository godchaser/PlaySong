package controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;

import controllers.chords.ChordLineTransposer;
import controllers.chords.LineTypeChecker;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import models.ServiceSong;
import models.SongLyrics;
import models.helpers.PdfPrintable;
//import models.Song;
//import models.SongLyrics;
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

	// table to store placeholder for all chapters and sections
	private final Map<String, PdfTemplate> tocPlaceholder = new HashMap<>();

	// store the chapters and sections with their title here.
	private final Map<String, Integer> pageByTitle = new HashMap<>();

	private SongFonts fonts = new SongFonts();

	private class SongFonts {
		String LiberationMonoFontPath = "resources/fonts/LiberationMono-Regular.ttf";
		String LiberationMonoBoldFontPath = "resources/fonts/LiberationMono-Bold.ttf";
		String TimesNewRomanFontPath = "resources/fonts/Times_New_Roman.ttf";
		String TimesNewRomanBoldFontPath = "resources/fonts/Times_New_Roman_Bold.ttf";

		int MONOSPACE_SIZE = 12;
		int NORMAL_SIZE = 12;
		int BOLD_SIZE = 14;
		Font MONOSPACE;
		Font NORMAL;
		Font BOLD;
		Font ITALIC;
		Font BOLDITALIC;

		public SongFonts() {
			FontFactory
					.register(LiberationMonoFontPath, LiberationMonoFontPath);
			FontFactory.register(LiberationMonoBoldFontPath,
					LiberationMonoBoldFontPath);
			FontFactory.register(TimesNewRomanFontPath, TimesNewRomanFontPath);
			FontFactory.register(TimesNewRomanBoldFontPath,
					TimesNewRomanFontPath);
			// Get the font NB. last parameter indicates font needs to be
			// embedded
			MONOSPACE = FontFactory.getFont(LiberationMonoFontPath,
					BaseFont.CP1250, BaseFont.EMBEDDED);
			MONOSPACE.setSize(MONOSPACE_SIZE);

			NORMAL = FontFactory.getFont(TimesNewRomanFontPath,
					BaseFont.CP1250, BaseFont.EMBEDDED);
			NORMAL.setStyle(Font.NORMAL);
			NORMAL.setSize(NORMAL_SIZE);
			BOLD = FontFactory.getFont(TimesNewRomanBoldFontPath, BaseFont.CP1250,
					BaseFont.EMBEDDED);
			BOLD.setStyle(Font.NORMAL);
			BOLD.setSize(BOLD_SIZE);
			ITALIC = FontFactory.getFont(TimesNewRomanFontPath,
					BaseFont.CP1250, BaseFont.EMBEDDED);
			ITALIC.setStyle(Font.ITALIC);
			ITALIC.setSize(12);
			BOLDITALIC = FontFactory.getFont(TimesNewRomanFontPath,
					BaseFont.CP1250, BaseFont.EMBEDDED);
			BOLDITALIC.setStyle(Font.BOLDITALIC);
			BOLDITALIC.setSize(12);
		}
	}

	public PdfGenerator(String outputPdfPath) throws Exception {
		this.document = new Document(songPageSize);
		this.document.setMargins(50, 50, 60, 40);
		this.document.setMarginMirroring(false);
		this.writer = PdfWriter.getInstance(this.document,
				new FileOutputStream(outputPdfPath));
		this.writer.setPageEvent(this);
		this.document.open();
	}

	public void onChapter(final PdfGenerator writer, final Document document,
			final float paragraphPosition, final Paragraph title) {
		this.pageByTitle.put(title.getContent(), this.writer.getPageNumber());
	}

	public void onSection(final PdfGenerator writer, final Document document,
			final float paragraphPosition, final int depth,
			final Paragraph title) {
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
		ColumnText
				.showTextAligned(total, Element.ALIGN_LEFT,
						new Phrase(String.valueOf(writer.getPageNumber() - 1)),
						2, 2, 0);
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

	private void createSongsTOC(List<? extends PdfPrintable> printObject)
			throws DocumentException {
		// add a small introduction chapter the shouldn't be counted.
		final Chapter intro = new Chapter(new Paragraph("Table Of Content",
				fonts.BOLD), 0);
		intro.setNumberDepth(0);
		this.document.add(intro);
		
		for (int i = 0; i < printObject.size(); i++) {
			
			final String title = printObject.get(i).getTitle();
			// final String songTitle = songPrintObjects.get(i).getSong();

			final Chunk chunk = new Chunk(i + ". " + title, fonts.NORMAL)
					.setLocalGoto(title);
			this.document.add(new Paragraph(chunk));

			final String songTitleId = title + i;

			// Add a placeholder for the page reference
			this.document.add(new VerticalPositionMark() {
				@Override
				public void draw(final PdfContentByte canvas, final float llx,
						final float lly, final float urx, final float ury,
						final float y) {
					final PdfTemplate createTemplate = canvas.createTemplate(
							50, 50);
					PdfGenerator.this.tocPlaceholder.put(songTitleId,
							createTemplate);

					canvas.addTemplate(createTemplate, urx - 50, y);
				}
			});
		}
	}

	private void createSongsChapters(List<? extends PdfPrintable> printObject)
			throws DocumentException {

		for (int i = 0; i < printObject.size(); i++) {
			// append the chapter
			String songTitle = printObject.get(i).getTitle();
			// final String songTitle = songPrintObjects.get(i).getSong();

			final Chunk chunk = new Chunk(songTitle, fonts.BOLD)
					.setLocalDestination(songTitle);

			final Chapter chapter = new Chapter(new Paragraph(chunk), i);
			// chapter.setNumberDepth(0);

			String content = printObject.get(i).getContent();

			System.out.println(content);

			
			chapter.addSection(new Paragraph(content, fonts.MONOSPACE), 0);
			// chapter.setNumberDepth(0);

			this.document.add(chapter);

			String songTitleId = songTitle + i;

			// When we wrote the chapter, we now the pagenumber
			final PdfTemplate template = this.tocPlaceholder.get(songTitleId);
			template.beginText();
			template.setFontAndSize(fonts.NORMAL.getBaseFont(), 12);
			template.setTextMatrix(
					50 - fonts.NORMAL.getBaseFont().getWidthPoint(
							String.valueOf(this.writer.getPageNumber()), 12), 0);
			template.showText(String.valueOf(this.writer.getPageNumber()));
			template.endText();

		}
	}
	
	public static void writeListContent(String outputPdfPath,
			List<? extends PdfPrintable> songPrintObjects) {
		PdfGenerator pdfGenerator;
		try {
			pdfGenerator = new PdfGenerator(outputPdfPath);
			pdfGenerator.createSongsTOC(songPrintObjects);
			pdfGenerator.createSongsChapters(songPrintObjects);
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
		String l = "      F    C   G/E    Am\n" + "Slavi Gospoda, dušo moja,\n"
				+ "F      C     G\n" + "ime Mu sveto je."
				+ "       F       C   F  G  Am\n"
				+ "Pjevaj kao nikada, slavi Ga, ";
		// songPrintObjects.add(new SongPrint("10000 Razloga", 123l, l));

		// Song s2 = new Song();
		// s2.setSongName("Ako Bog nije živ");
		String l2 = "      F    C   G/E    Am\n"
				+ "Slavi Gospoda, dušo moja,\n" + "F      C     G\n"
				+ "ime Mu sveto je.\n" + "       F       C   F  G  Am\n"
				+ "Pjevaj kao nikada, slavi Ga, ";
		// songPrintObjects.add(new SongPrint("Ako Bog nije živ", 124l, l2));

		// PdfGenerator.writeSongs(songPrintObjects);
	}
}
