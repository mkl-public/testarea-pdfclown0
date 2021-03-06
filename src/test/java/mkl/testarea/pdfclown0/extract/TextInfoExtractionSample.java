package mkl.testarea.pdfclown0.extract;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pdfclown.documents.Document;
import org.pdfclown.documents.Page;
import org.pdfclown.documents.contents.ContentScanner;
import org.pdfclown.documents.contents.LineDash;
import org.pdfclown.documents.contents.TextChar;
import org.pdfclown.documents.contents.colorSpaces.DeviceRGBColor;
import org.pdfclown.documents.contents.composition.PrimitiveComposer;
import org.pdfclown.documents.contents.objects.ContainerObject;
import org.pdfclown.documents.contents.objects.ContentObject;
import org.pdfclown.documents.contents.objects.DrawRectangle;
import org.pdfclown.documents.contents.objects.Text;
import org.pdfclown.documents.contents.objects.XObject;
import org.pdfclown.files.File;
import org.pdfclown.files.SerializationModeEnum;
import org.pdfclown.tools.PageStamper;

/**
 * An independent copy of
 * {@link org.pdfclown.samples.cli.TextInfoExtractionSample}.
 * 
 * @author mkl
 */
public class TextInfoExtractionSample
{
    final static java.io.File RESULT_FOLDER = new java.io.File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testOriginalSample() throws IOException
    {
        // 1. Opening the PDF file...
        File file;
        {
            String filePath = "d:\\Issues\\stackoverflow\\pZweites'.pdf";
            try
            {
                file = new File(filePath);
            }
            catch (Exception e)
            {
                throw new RuntimeException(filePath + " file access error.", e);
            }
        }
        Document document = file.getDocument();

        PageStamper stamper = new PageStamper(); // NOTE: Page stamper is used
                                                 // to draw contents on existing
                                                 // pages.

        // 2. Iterating through the document pages...
        for (Page page : document.getPages())
        {
            System.out.println("\nScanning page " + page.getNumber() + "...\n");

            stamper.setPage(page);

            extract(new ContentScanner(page), // Wraps the page contents into a
                                              // scanner.
                    stamper.getForeground());

            stamper.flush();
        }

        // 3. Decorated version serialization.
        file.save(new java.io.File(RESULT_FOLDER, "pZweites'-decorated.pdf"), SerializationModeEnum.Incremental);
        file.close();
    }

    /**
     * <a href="https://stackoverflow.com/questions/50398731/text-info-extraction-from-pdf">
     * Text info extraction from pdf
     * </a>
     * <br/>
     * <a href="https://nofile.io/f/Kvf2DkXvfj4/edit9.pdf">
     * edit9.pdf
     * </a>
     * <p>
     * Indeed, PDF Clown as is does not sensibly handle non-upright text.
     * Several changes in the lib were necessary for a work-around.
     * </p>
     */
    @Test
    public void testEdit9() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("edit9.pdf")  ) {
            File file = new File(resource);
            Document document = file.getDocument();
            PageStamper stamper = new PageStamper();

            for (Page page : document.getPages())
            {
                System.out.println("\nScanning page " + page.getNumber() + "...\n");

                stamper.setPage(page);

                extract(new ContentScanner(page), stamper.getForeground());

                stamper.flush();
            }

            // 3. Decorated version serialization.
            file.save(new java.io.File(RESULT_FOLDER, "edit9-decorated.pdf"), SerializationModeEnum.Standard);
            file.close();
        }
    }

    private final DeviceRGBColor[] textCharBoxColors = new DeviceRGBColor[] { new DeviceRGBColor(200 / 255d, 100 / 255d, 100 / 255d),
            new DeviceRGBColor(100 / 255d, 200 / 255d, 100 / 255d), new DeviceRGBColor(100 / 255d, 100 / 255d, 200 / 255d) };
    private final DeviceRGBColor textStringBoxColor = DeviceRGBColor.Black;

    /**
     * Scans a content level looking for text.
     */
    /*
     * NOTE: Page contents are represented by a sequence of content objects,
     * possibly nested into multiple levels.
     */
    private void extract(ContentScanner level, PrimitiveComposer composer)
    {
        if (level == null)
            return;

        while (level.moveNext())
        {
            ContentObject content = level.getCurrent();
            if (content instanceof Text)
            {
                ContentScanner.TextWrapper text = (ContentScanner.TextWrapper) level.getCurrentWrapper();
                int colorIndex = 0;
                for (ContentScanner.TextStringWrapper textString : text.getTextStrings())
                {
                    Rectangle2D textStringBox = textString.getBox();
                    System.out.println("Text [" + "x:" + Math.round(textStringBox.getX()) + "," + "y:" + Math.round(textStringBox.getY()) + "," + "w:"
                            + Math.round(textStringBox.getWidth()) + "," + "h:" + Math.round(textStringBox.getHeight()) + "] [font size:"
                            + Math.round(textString.getStyle().getFontSize()) + "]: " + textString.getText());

                    // Drawing text character bounding boxes...
                    colorIndex = (colorIndex + 1) % textCharBoxColors.length;
                    composer.setStrokeColor(textCharBoxColors[colorIndex]);
                    for (TextChar textChar : textString.getTextChars())
                    {
                        Rectangle2D box = textChar.getBox();
                        composer.beginLocalState();
                        AffineTransform rot = AffineTransform.getRotateInstance(textChar.getAlpha());
                        composer.applyMatrix(rot.getScaleX(), rot.getShearY(), rot.getShearX(), rot.getScaleY(),
                                box.getX(), composer.getScanner().getContextSize().getHeight() - box.getY());
                        composer.add(new DrawRectangle(0, - box.getHeight(), box.getWidth(), box.getHeight()));

                        composer.stroke();
                        composer.end();
                    }

                    // Drawing text string bounding box...
                    composer.beginLocalState();
                    composer.setLineDash(new LineDash(new double[] { 5 }));
                    composer.setStrokeColor(textStringBoxColor);

                    AffineTransform rot = AffineTransform.getRotateInstance(textString.getAlpha());
                    composer.applyMatrix(rot.getScaleX(), rot.getShearY(), rot.getShearX(), rot.getScaleY(),
                            textStringBox.getX(), composer.getScanner().getContextSize().getHeight() - textStringBox.getY());
                    composer.add(new DrawRectangle(0, - textStringBox.getHeight(), textStringBox.getWidth(), textStringBox.getHeight()));

                    composer.stroke();
                    composer.end();
                }
            }
            else if (content instanceof XObject)
            {
                // Scan the external level!
                extract(((XObject) content).getScanner(level), composer);
            }
            else if (content instanceof ContainerObject)
            {
                // Scan the inner level!
                extract(level.getChildLevel(), composer);
            }
            else
            {
                System.out.println(content);
            }
        }
    }
}
