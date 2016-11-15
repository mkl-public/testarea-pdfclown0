package mkl.testarea.pdfclown0.render;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PrinterException;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pdfclown.documents.Document;
import org.pdfclown.documents.Page;
import org.pdfclown.documents.PageFormat;
import org.pdfclown.documents.contents.colorSpaces.DeviceRGBColor;
import org.pdfclown.documents.contents.composition.PrimitiveComposer;
import org.pdfclown.documents.contents.fonts.StandardType1Font;
import org.pdfclown.files.File;
import org.pdfclown.files.SerializationModeEnum;
import org.pdfclown.tools.Renderer;

/**
 * @author mkl
 */
public class RenderText
{
    final static java.io.File RESULT_FOLDER = new java.io.File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/40577657/pdf-clown-does-not-render-text">
     * PDF Clown does not render text
     * </a>
     * <p>
     * Indeed, rendering seems not yet to be properly implemented.
     * </p>
     */
    @Test
    public void testRenderLikeExponent() throws IOException, PrinterException
    {
        File file = new File();

        Document document = file.getDocument();
        document.setPageSize(PageFormat.getSize(PageFormat.SizeEnum.A4, PageFormat.OrientationEnum.Portrait));

        Page page = new Page(document);
        document.getPages().add(page);

        PrimitiveComposer composer = new PrimitiveComposer(page);


        //draw a rectangle
        composer.setFillColor(DeviceRGBColor.get(Color.magenta));
        composer.drawRectangle(new Rectangle2D.Float(30, 42, 300, 32));
        composer.fill();


        //draw some text
        composer.setFillColor(DeviceRGBColor.get(Color.black));
        composer.setFont(new StandardType1Font(document, StandardType1Font.FamilyEnum.Courier, true, false), 32);
        composer.showText("Hello World!", new Point2D.Float(32, 48));
        composer.flush();


        //save the file
        file.save(new java.io.File(RESULT_FOLDER, "rendered-like-exponent.pdf"), SerializationModeEnum.Standard);


        //and print it
        Renderer renderer = new Renderer();
        renderer.print(file.getDocument(), false);
        
        file.close();
    }

}
