package mkl.testarea.pdfclown0.form;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pdfclown.documents.Document;
import org.pdfclown.documents.interaction.forms.FieldWidgets;
import org.pdfclown.files.SerializationModeEnum;
import org.pdfclown.tools.FormFlattener;

/**
 * @author mkl
 */
public class FlattenForm
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/31952089/flattening-form-using-pdfclown-throws-indexoutofbounds-exception">
     * Flattening form using PDFClown throws IndexOutOfBounds exception
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/label">label.pdf</a>
     * <p>
     * Having debugged into the code it looks like a PdfClown bug:
     * </p>
     * <p>
     * The Iterator returned by {@link FieldWidgets#iterator()} does not recognize that the widget
     * collection underneath has changed (gotten smaller) and so tries to read beyond its size.
     * </p>
     */
    @Test
    public void testOriginalLabel() throws IOException
    {
        flattenOriginal("src/test/resources/mkl/testarea/pdfclown0/form/label.pdf", new File(RESULT_FOLDER, "label-flatten.pdf"));
    }

    /**
     * <a href="http://stackoverflow.com/questions/31952089/flattening-form-using-pdfclown-throws-indexoutofbounds-exception">
     * Flattening form using PDFClown throws IndexOutOfBounds exception
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/label">label.pdf</a>
     * <p>
     * Essentially the original code by the OP:
     * </p>
     */
    public static void flattenOriginal(String source, File result) throws IOException
    {
        org.pdfclown.files.File f = new org.pdfclown.files.File(source);
        Document doc = f.getDocument();

        FormFlattener formFlattener = new FormFlattener();
        formFlattener.flatten(doc);
        f.save(result, SerializationModeEnum.Standard);
        f.close();
    }    
}
