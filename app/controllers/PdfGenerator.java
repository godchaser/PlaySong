package controllers;

import com.itextpdf.text.Document;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by samuel on 5/23/15.
 */
public class PdfGenerator extends PdfPageEventHelper
{
    private final Document document;
    private final PdfWriter writer;
    private final BaseFont baseFont       = BaseFont.createFont();
    private final Font chapterFont    = FontFactory.getFont(FontFactory.HELVETICA, 24, Font.NORMAL);
    private final String fontPath = "RemachineScript_Personal_Use.ttf";
    private static Font embeddedFont = null;

    // table to store placeholder for all chapters and sections
    private final Map<String, PdfTemplate> tocPlaceholder = new HashMap<>();

    // store the chapters and sections with their title here.
    private final Map<String, Integer>     pageByTitle    = new HashMap<>();

    public static void main(final String[] args) throws Exception
    {
        final PdfGenerator pdfGenerator = new PdfGenerator();
        pdfGenerator.initFonts();
        pdfGenerator.document.add(new Paragraph("This is an example to generate a TOC.", embeddedFont));
        pdfGenerator.createTOC(10);
        pdfGenerator.createChapters(10);
        pdfGenerator.document.close();
    }

    public void initFonts (){
        // Pass name of font file on command line
        FontFactory.register(fontPath, fontPath);
        System.out.println("Embedding font: " + fontPath);
        // Get the font NB. last parameter indicates font needs to be embedded
        embeddedFont = FontFactory.getFont(fontPath, BaseFont.CP1250, BaseFont.EMBEDDED);
    }

    public PdfGenerator() throws Exception
    {
        this.document = new Document(PageSize.A6);
        this.writer = PdfWriter.getInstance(this.document, new FileOutputStream("text.pdf"));
        this.writer.setPageEvent(this);
        this.document.open();
    }

    //@Override
    public void onChapter(final PdfGenerator writer, final Document document, final float paragraphPosition, final Paragraph title)
    {
        this.pageByTitle.put(title.getContent(), this.writer.getPageNumber());
    }

    //@Override
    public void onSection(final PdfGenerator writer, final Document document, final float paragraphPosition, final int depth, final Paragraph title)
    {
        this.pageByTitle.put(title.getContent(), this.writer.getPageNumber());
    }

    private void createTOC(final int count) throws DocumentException
    {
        // add a small introduction chapter the shouldn't be counted.
        final Chapter intro = new Chapter(new Paragraph("This is TOC ", this.chapterFont), 0);
        intro.setNumberDepth(0);
        this.document.add(intro);

        for (int i = 1; i < count + 1; i++)
        {
            // Write "Chapter i"
            final String title = "Chapter " + i;
            final Chunk chunk = new Chunk(title).setLocalGoto(title);
            this.document.add(new Paragraph(chunk));

            // Add a placeholder for the page reference
            this.document.add(new VerticalPositionMark() {
                @Override
                public void draw(final PdfContentByte canvas, final float llx, final float lly, final float urx, final float ury, final float y)
                {
                    final PdfTemplate createTemplate = canvas.createTemplate(50, 50);
                    PdfGenerator.this.tocPlaceholder.put(title, createTemplate);

                    canvas.addTemplate(createTemplate, urx - 50, y);
                }
            });
        }
    }

    private void createChapters(final int count) throws DocumentException
    {
        for (int i = 1; i < count + 1; i++)
        {
            // append the chapter
            final String title = "Chapter " + i;
            final Chunk chunk = new Chunk(title, this.chapterFont).setLocalDestination(title);
            final Chapter chapter = new Chapter(new Paragraph(chunk), i);
            chapter.setNumberDepth(0);

            chapter.addSection("Foobar1");
            chapter.addSection("Foobar2");
            this.document.add(chapter);

            // When we wrote the chapter, we now the pagenumber
            final PdfTemplate template = this.tocPlaceholder.get(title);
            template.beginText();
            template.setFontAndSize(this.baseFont, 12);
            template.setTextMatrix(50 - this.baseFont.getWidthPoint(String.valueOf(this.writer.getPageNumber()), 12), 0);
            template.showText(String.valueOf(this.writer.getPageNumber()));
            template.endText();

        }
    }
}
