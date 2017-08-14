package mkl.testarea.pdfclown0.extract;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.pdfclown.documents.Document;
import org.pdfclown.documents.Page;
import org.pdfclown.documents.contents.ITextString;
import org.pdfclown.documents.contents.TextChar;
import org.pdfclown.tools.TextExtractor;

public class ExtractSize
{
    /**
     * <a href="https://stackoverflow.com/questions/45413682/pdfclown-different-font-size-in-one-line">
     * PDFClown Different font-size in one line
     * </a>
     * <br/>
     * <a href="https://www.file-upload.net/download-12650542/SameCharactersDifferentSize.pdf.html">
     * SameCharactersDifferentSize.pdf
     * </a>
     * <p>
     * The issue is caused by a bug in PDF Clown: it assumes that marked content
     * sections and save/restore graphics state blocks are properly contained in
     * each other and don't overlap.
     * </p>
     * <p>
     * The only easy way I found to repair this was to disable the marked content
     * parsing in PDF Clown.
     * </p>
     */
    @Test
    public void test() throws IOException
    {
        try (InputStream source = getClass().getResourceAsStream("SameCharactersDifferentSize.pdf");
                org.pdfclown.files.File file = new org.pdfclown.files.File(source))
        {
            file.setPath("");
            Document document = file.getDocument();

            TextExtractor extractor = new TextExtractor();
            for (Page page : document.getPages())
            {
                List<ITextString> textStrings = extractor.extract(page).get(null);
                for (ITextString textString : textStrings)
                {
                    Rectangle2D textStringBox = textString.getBox();
                    System.out.println("Text [" + "x:" + Math.round(textStringBox.getX()) + "," + "y:"
                            + Math.round(textStringBox.getY()) + "," + "w:" + Math.round(textStringBox.getWidth()) + ","
                            + "h:" + Math.round(textStringBox.getHeight()) + "]: " + textString.getText());

                    int millionIndex = textString.getText().indexOf("million");
                    if (millionIndex >= 0)
                    {
                        for (TextChar textChar : textString.getTextChars().subList(millionIndex, millionIndex + 3))
                        {
                            Rectangle2D textCharBox = textChar.getBox();
                            System.out.println("  Char [x:" + Math.round(textCharBox.getX()) + "," + "y:"
                            + Math.round(textCharBox.getY()) + "," + "w:" + Math.round(textCharBox.getWidth()) + ","
                            + "h:" + Math.round(textCharBox.getHeight()) + "]: " + textChar.getValue());
                        }
                    }
                }
            }

        }
    }

}
