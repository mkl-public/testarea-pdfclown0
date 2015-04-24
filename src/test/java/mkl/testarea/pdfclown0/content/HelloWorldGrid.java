package mkl.testarea.pdfclown0.content;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pdfclown.documents.Document;
import org.pdfclown.documents.Page;
import org.pdfclown.documents.contents.LineCapEnum;
import org.pdfclown.documents.contents.LineDash;
import org.pdfclown.documents.contents.colorSpaces.DeviceRGBColor;
import org.pdfclown.documents.contents.composition.PrimitiveComposer;
import org.pdfclown.documents.contents.composition.XAlignmentEnum;
import org.pdfclown.documents.contents.composition.YAlignmentEnum;
import org.pdfclown.documents.contents.fonts.StandardType1Font;
import org.pdfclown.files.SerializationModeEnum;

/**
 * <a href="http://stackoverflow.com/questions/29850343/is-there-an-option-to-display-grid-for-programming-positions-of-visible-materia">
 * Is there an option to display grid, for programming positions of visible material on screen?
 * </a>
 * <p>
 * A sample showing how to add a grid to the page content.
 * </p>
 * 
 * @author mkl
 */
public class HelloWorldGrid
{
	final static File RESULT_FOLDER = new File("target/test-outputs", "content");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		RESULT_FOLDER.mkdirs();
	}

	@Test
	public void testHelloWorldWithGrid() throws IOException
	{
		// 1. Instantiate a new PDF file!
		/*
		 * NOTE: a File object is the low-level (syntactic) representation of a
		 * PDF file.
		 */
		org.pdfclown.files.File file = new org.pdfclown.files.File();

		// 2. Get its corresponding document!
		/*
		 * NOTE: a Document object is the high-level (semantic) representation
		 * of a PDF file.
		 */
		Document document = file.getDocument();

		// 3. Insert the contents into the document!
		populate(document);

		// 3.5 Add a grid to the content
		addGrid(document);
		
		// 4. Serialize the PDF file!
		file.save(new File(RESULT_FOLDER, "helloWorld-grid.pdf"),
				SerializationModeEnum.Standard);

		file.close();
	}

	/**
	 * Populates a PDF file with contents.
	 */
	private void populate(Document document)
	{
		// 1. Add the page to the document!
		Page page = new Page(document); // Instantiates the page inside the
										// document context.
		document.getPages().add(page); // Puts the page in the pages collection.
		Dimension2D pageSize = page.getSize();

		// 2. Create a content composer for the page!
		PrimitiveComposer composer = new PrimitiveComposer(page);

		// 3. Inserting contents...
		// Set the font to use!
		composer.setFont(new StandardType1Font(document,
				StandardType1Font.FamilyEnum.Courier, true, false), 30);
		// Show the text onto the page (along with its box)!
		/*
		 * NOTE: PrimitiveComposer's showText() method is the most basic way to
		 * add text to a page -- see BlockComposer for more advanced uses
		 * (horizontal and vertical alignment, hyphenation, etc.).
		 */
		composer.showText("Hello World!", new Point2D.Double(32, 48));

		composer.setLineWidth(.25);
		composer.setLineCap(LineCapEnum.Round);
		composer.setLineDash(new LineDash(new double[] { 5, 10 }));
		composer.setTextLead(1.2);
		composer.drawPolygon(composer.showText(
				"This is a primitive example" + "\nof centered, rotated multi-"
						+ "\nline text." + "\n\n\tWe recommend you to use"
						+ "\nBlockComposer instead, as it"
						+ "\nautomatically manages text"
						+ "\nwrapping and alignment with-"
						+ "\nin a specified area!",
				new Point2D.Double(pageSize.getWidth() / 2, pageSize
						.getHeight() / 2), XAlignmentEnum.Center,
				YAlignmentEnum.Middle, 15).getPoints());
		composer.stroke();

		// 4. Flush the contents into the page!
		composer.flush();
	}
	
	void addGrid(Document document)
	{
		for (Page page: document.getPages())
		{
			Dimension2D pageSize = page.getSize();
			PrimitiveComposer composer = new PrimitiveComposer(page);
			composer.beginLocalState();
			
			composer.setStrokeColor(new DeviceRGBColor(1, 0, 0));
			for (int x = 0; x < pageSize.getWidth(); x+=20)
			{
				composer.startPath(new Point2D.Float(x, 0));
				composer.drawLine(new Point2D.Double(x, pageSize.getHeight()));
			}

			for (int y = 0; y < pageSize.getHeight(); y+=20)
			{
				composer.startPath(new Point2D.Float(0, y));
				composer.drawLine(new Point2D.Double(pageSize.getWidth(), y));
			}

			composer.stroke();
			
			composer.end();
			composer.flush();
		}
	}
}
