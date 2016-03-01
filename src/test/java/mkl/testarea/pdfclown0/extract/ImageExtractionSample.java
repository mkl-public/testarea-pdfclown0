// $Id$
package mkl.testarea.pdfclown0.extract;

import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pdfclown.bytes.IBuffer;
import org.pdfclown.files.File;
import org.pdfclown.objects.PdfDataObject;
import org.pdfclown.objects.PdfDictionary;
import org.pdfclown.objects.PdfDirectObject;
import org.pdfclown.objects.PdfIndirectObject;
import org.pdfclown.objects.PdfName;
import org.pdfclown.objects.PdfStream;
import org.pdfclown.util.io.IOUtils;

/**
 * An independent copy of {@link org.pdfclown.samples.cli.ImageExtractionSample}.
 * 
 * @author mkl
 */
public class ImageExtractionSample
{
    final static java.io.File RESULT_FOLDER = new java.io.File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testExtractFrom22568_45() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("22568_45.pdf");  )
        {
            File file = new File(resource);
            
            int index = 0;
            for (PdfIndirectObject indirectObject : file.getIndirectObjects())
            {
                // Get the data object associated to the indirect object!
                PdfDataObject dataObject = indirectObject.getDataObject();
                // Is this data object a stream?
                if (dataObject instanceof PdfStream)
                {
                    PdfDictionary header = ((PdfStream) dataObject).getHeader();
                    // Is this stream an image?
                    if (header.containsKey(PdfName.Type) && header.get(PdfName.Type).equals(PdfName.XObject)
                            && header.get(PdfName.Subtype).equals(PdfName.Image))
                    {
                        // Which kind of image?
                        if (header.get(PdfName.Filter).equals(PdfName.DCTDecode)) // JPEG
                                                                                  // image.
                        {
                            // Get the image data (keeping it encoded)!
                            IBuffer body = ((PdfStream) dataObject).getBody(false);
                            // Export the image!
                            exportImage(body, "22568_45_" + (index++) + ".jpg");
                        }
                        else
                        // Unsupported image.
                        {
                            System.out.println("Image XObject " + indirectObject.getReference() + " couldn't be extracted (filter: "
                                    + header.get(PdfName.Filter) + ")");
                        }
                    }
                }
            }
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/35711352/get-pixeldata-from-scanned-pdf-document-in-java">
     * Get pixeldata from scanned PDF-document in Java
     * </a>
     * <br/>
     * <a href="http://www.filedropper.com/innlevering1">
     * Innlevering 1.pdf
     * </a>
     * <p>
     * This test attempted to show how to extract embedded bitmaps fram the OP's sample document.
     * Unfortunately, though, they come in JBIG2 and in flattened JPEG; the PDF Clown sample this
     * code is from does not support those formats.
     * </p>
     */
    @Test
    public void testExtractFromInnlevering1() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Innlevering 1.pdf");  )
        {
            File file = new File(resource);
            
            int index = 0;
            for (PdfIndirectObject indirectObject : file.getIndirectObjects())
            {
                // Get the data object associated to the indirect object!
                PdfDataObject dataObject = indirectObject.getDataObject();
                // Is this data object a stream?
                if (dataObject instanceof PdfStream)
                {
                    PdfDictionary header = ((PdfStream) dataObject).getHeader();
                    // Is this stream an image?
                    if (header.containsKey(PdfName.Type) && header.get(PdfName.Type).equals(PdfName.XObject)
                            && header.get(PdfName.Subtype).equals(PdfName.Image))
                    {
                        // Which kind of image?
                        PdfDirectObject filter = header.get(PdfName.Filter);
                        if (header.get(PdfName.Filter).equals(PdfName.DCTDecode)) // JPEG
                                                                                  // image.
                        {
                            // Get the image data (keeping it encoded)!
                            IBuffer body = ((PdfStream) dataObject).getBody(false);
                            // Export the image!
                            exportImage(body, "Innlevering 1_" + (index++) + ".jpg");
                        }
                        else
                        // Unsupported image.
                        {
                            System.out.println("Image XObject " + indirectObject.getReference() + " couldn't be extracted (filter: "
                                    + header.get(PdfName.Filter) + ")");
                        }
                    }
                }
            }
        }
    }

    private void exportImage(IBuffer data, String filename)
    {
        java.io.File outputFile = new java.io.File(RESULT_FOLDER, filename);
        java.io.BufferedOutputStream outputStream;
        try
        {
            outputFile.createNewFile();
            outputStream = new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputFile));
        }
        catch (Exception e)
        {
            throw new RuntimeException(outputFile.getPath() + " file couldn't be created.", e);
        }

        try
        {
            outputStream.write(data.toByteArray());
        }
        catch (Exception e)
        {
            throw new RuntimeException(outputFile.getPath() + " file writing has failed.", e);
        }
        finally
        {
            IOUtils.close(outputStream);
        }

        System.out.println("Output: " + outputFile.getPath());
    }
}
