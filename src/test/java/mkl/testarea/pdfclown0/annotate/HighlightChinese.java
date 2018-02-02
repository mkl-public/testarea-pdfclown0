package mkl.testarea.pdfclown0.annotate;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pdfclown.documents.Page;
import org.pdfclown.documents.contents.ITextString;
import org.pdfclown.documents.contents.TextChar;
import org.pdfclown.documents.contents.objects.ShowText;
import org.pdfclown.documents.contents.objects.ShowTextToNextLine;
import org.pdfclown.documents.interaction.annotations.TextMarkup;
import org.pdfclown.documents.interaction.annotations.TextMarkup.MarkupTypeEnum;
import org.pdfclown.files.SerializationModeEnum;
import org.pdfclown.tools.TextExtractor;
import org.pdfclown.util.math.Interval;
import org.pdfclown.util.math.geom.Quad;

/**
 * @author mkl
 */
public class HighlightChinese {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/48395771/pdf-clown-not-highlighting-specific-search-keyword">
     * pdf clown- not highlighting specific search keyword
     * </a>
     * <p>
     * chinese1.pdf - via e-mail
     * </p>
     * <p>
     * Indeed, there are issues with the example PDF but they are different from
     * the OP's description. This might be due to him not using the current PDF
     * Clown version. The issues observed are due to an error in the PDF Clown
     * {@link ShowText} class. A special treatment of {@link ShowTextToNextLine}
     * instances at the end of its <code>scan</code> method results in errors
     * calculating the text line matrix and as a result later text matrices.
     * </p>
     */
    @Test
    public void testHighLightChinese1() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("chinese1.pdf")   ) {
            // Enter FilePath, Page Number, StartsWith, EndsWith
            @SuppressWarnings("resource")
            org.pdfclown.files.File file = new org.pdfclown.files.File(resource);    
            // Define the text pattern to look for!
            String textRegEx = "京";

            Pattern pattern = Pattern.compile(textRegEx, Pattern.CASE_INSENSITIVE);
            TextExtractor textExtractor = new TextExtractor();
            for(final Page page : file.getDocument().getPages())
            {
                // Extract the page text!
                Map<Rectangle2D,List<ITextString>> textStrings = textExtractor.extract(page);

                // Find the text pattern matches!
                final Matcher matcher = pattern.matcher(TextExtractor.toString(textStrings));
                textExtractor.filter(
                        textStrings,
                        new TextExtractor.IIntervalFilter() {
                            @Override
                            public boolean hasNext() {
                                return matcher.find();
                            }

                            @Override
                            public Interval<Integer> next() {
                                return new Interval<>(matcher.start(), matcher.end());
                            }

                            @Override
                            public void process(Interval<Integer> interval, ITextString match) {
                                // Defining the highlight box of the text pattern match...
                                List<Quad> highlightQuads = new ArrayList<>();
                                {
                                    /*
                                    NOTE: A text pattern match may be split across multiple contiguous lines,
                                    so we have to define a distinct highlight box for each text chunk.
                                     */

                                    Rectangle2D textBox = null;

                                    for(TextChar textChar : match.getTextChars())
                                    {
                                        Rectangle2D textCharBox = textChar.getBox();
                                        if(textBox == null) {
                                            textBox = (Rectangle2D)textCharBox.clone();
                                        } else {
                                            if(textCharBox.getY() > textBox.getMaxY()) {
                                                highlightQuads.add(Quad.get(textBox));
                                                textBox = (Rectangle2D)textCharBox.clone();
                                            } else {
                                                textBox.add(textCharBox);
                                            }
                                        }
                                    }
                                    highlightQuads.add(Quad.get(textBox));
                                }
                                // Highlight the text pattern match!
                                new TextMarkup(page, highlightQuads, null, MarkupTypeEnum.Highlight);
                            }

                            @Override
                            public void remove() {
                                throw new UnsupportedOperationException();
                            }
                        }
                        );
            }
            file.save(new File(RESULT_FOLDER, "chinese1-Highlighted.pdf"), SerializationModeEnum.Incremental);
        }
    }
}
