package mkl.testarea.pdfclown0.content;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pdfclown.documents.Page;
import org.pdfclown.documents.contents.composition.PrimitiveComposer;
import org.pdfclown.documents.contents.entities.Image;
import org.pdfclown.documents.contents.xObjects.XObject;
import org.pdfclown.files.SerializationModeEnum;

/**
 * @author mkl
 */
public class ShowImage
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/34383062/how-to-add-picture-to-pdf-file-using-pdfclown">
     * How to add picture to pdf file using PDFClown
     * </a>
     * <p>
     * Cannot reproduce the issue of the OP using my sample jpg.
     * </p>
     */
    @Test
    public void testAddPicture() throws IOException
    {
        org.pdfclown.files.File file = new org.pdfclown.files.File();

        Page page = new Page(file.getDocument());
        file.getDocument().getPages().add(page);
        PrimitiveComposer primitiveComposer = new PrimitiveComposer(page);

        Image image = Image.get("src\\test\\resources\\mkl\\testarea\\pdfclown0\\content\\Willi-1.jpg");
        XObject imageXObject = image.toXObject(file.getDocument());
        primitiveComposer.showXObject(imageXObject, new Point2D.Double(100,100), new Dimension(300, 300));                 
        
        primitiveComposer.flush();
        
        file.save(new File(RESULT_FOLDER, "PdfWithImage.pdf"), SerializationModeEnum.Standard);
        file.close();
    }
}
