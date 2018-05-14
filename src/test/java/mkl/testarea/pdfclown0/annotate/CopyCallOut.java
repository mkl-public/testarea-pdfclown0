package mkl.testarea.pdfclown0.annotate;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pdfclown.documents.Document;
import org.pdfclown.documents.Page;
import org.pdfclown.documents.contents.LineDash;
import org.pdfclown.documents.contents.colorSpaces.DeviceRGBColor;
import org.pdfclown.documents.interaction.annotations.Annotation;
import org.pdfclown.documents.interaction.annotations.Border;
import org.pdfclown.documents.interaction.annotations.StaticNote;
import org.pdfclown.files.SerializationModeEnum;
import org.pdfclown.objects.PdfName;
import org.pdfclown.objects.PdfString;

/**
 * @author mkl
 */
public class CopyCallOut {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/50090277/pdfclown-copy-annotations-and-then-manipulate-them">
     * PDFClown Copy annotations and then manipulate them
     * </a>
     * <p>
     * This test shows how one can copy and (to some degree) manipulate some
     * Callout annotation. Different values for <code>keepAppearanceStream</code>
     * and <code>keepRichText</code> result in different kinds of easily possible
     * changes.
     * </p> 
     */
    @Test
    public void testCopyCallout() throws IOException {
        boolean keepAppearanceStream = false;
        boolean keepRichText = false;

        try (   InputStream sourceResource = getClass().getResourceAsStream("Callout-Yellow.pdf");
                InputStream targetResource = getClass().getResourceAsStream("test123.pdf");
                org.pdfclown.files.File sourceFile = new org.pdfclown.files.File(sourceResource);
                org.pdfclown.files.File targetFile = new org.pdfclown.files.File(targetResource); ) {
            Document sourceDoc = sourceFile.getDocument();
            Page sourcePage = sourceDoc.getPages().get(0);
            Annotation<?> sourceAnnotation = sourcePage.getAnnotations().get(0);

            Document targetDoc = targetFile.getDocument();
            Page targetPage = targetDoc.getPages().get(0);

            StaticNote targetAnnotation = (StaticNote) sourceAnnotation.clone(targetDoc);

            if (keepAppearanceStream) {
                // changing properties of an appearance
                // rotating the appearance in the appearance rectangle
                targetAnnotation.getAppearance().getNormal().get(null).setMatrix(AffineTransform.getRotateInstance(100, 10));
            } else {
                // removing the appearance to allow lower level properties changes
                targetAnnotation.setAppearance(null);
            }
 
            // changing text background color
            targetAnnotation.setColor(new DeviceRGBColor(0, 0, 1));

            if (keepRichText) {
                // changing rich text properties
                PdfString richText = (PdfString) targetAnnotation.getBaseDataObject().get(PdfName.RC);
                String richTextString = richText.getStringValue();
                // replacing the font family
                richTextString = richTextString.replaceAll("font-family:Helvetica", "font-family:Courier");
                richText = new PdfString(richTextString);
                targetAnnotation.getBaseDataObject().put(PdfName.RC, richText);
            } else {
                targetAnnotation.getBaseDataObject().remove(PdfName.RC);
                targetAnnotation.getBaseDataObject().remove(PdfName.DS);
            }

            // changing default appearance properties
            PdfString defaultAppearance = (PdfString) targetAnnotation.getBaseDataObject().get(PdfName.DA);
            String defaultAppearanceString = defaultAppearance.getStringValue();
            // replacing the font
            defaultAppearanceString = defaultAppearanceString.replaceFirst("Helv", "HeBo");
            // replacing the text and line color
            defaultAppearanceString = defaultAppearanceString.replaceFirst(". . . rg", ".5 g");
            defaultAppearance = new PdfString(defaultAppearanceString);
            targetAnnotation.getBaseDataObject().put(PdfName.DA, defaultAppearance);

            // changing the text value
            PdfString contents = (PdfString) targetAnnotation.getBaseDataObject().get(PdfName.Contents);
            String contentsString = contents.getStringValue();
            contentsString = contentsString.replaceFirst("text", "text line");
            contents = new PdfString(contentsString);
            targetAnnotation.getBaseDataObject().put(PdfName.Contents, contents);

            // change the line width and style
            targetAnnotation.setBorder(new Border(0, new LineDash(new double[] {3, 2})));

            targetPage.getAnnotations().add(targetAnnotation);

            targetFile.save(new File(RESULT_FOLDER, "test123-withCalloutCopy.pdf"),  SerializationModeEnum.Standard);
        }
    }

}
