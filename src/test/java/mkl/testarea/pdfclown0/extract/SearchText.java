package mkl.testarea.pdfclown0.extract;

import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pdfclown.documents.Page;
import org.pdfclown.documents.contents.ITextString;
import org.pdfclown.documents.contents.TextChar;
import org.pdfclown.documents.interaction.annotations.TextMarkup;
import org.pdfclown.documents.interaction.annotations.TextMarkup.MarkupTypeEnum;
import org.pdfclown.files.SerializationModeEnum;
import org.pdfclown.tools.TextExtractor;
import org.pdfclown.util.math.Interval;
import org.pdfclown.util.math.geom.Quad;

/**
 * @author mkl
 */
public class SearchText
{
    final static java.io.File RESULT_FOLDER = new java.io.File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/45498469/pdf-clown-highlight-multiple-search-word-is-failing-for-pdf-contains-images-col">
     * PDF Clown Highlight multiple search word is failing for PDF contains images, color text, Complex Diagrams
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B-nuOO6Zsa4rXy1DS2JjX1RnYmM/view?usp=sharing">
     * 751-07-T-A-2X-1-101.pdf
     * </a>
     * <p>
     * As it turns out, the search fails due to a bug in PDF Clown:
     * For sorting it uses a custom {@link Comparator} implementation, the
     * {@link org.pdfclown.tools.TextExtractor.TextStringPositionComparator}.
     * Unfortunately this implementation does not fulfill the {@link Comparator}
     * contract. Former Java versions ignored this but the {@link java.util.TimSort}
     * by default used in Java 8 recognizes this and fails.
     * </p>
     * <p>
     * If this test is run with the <code>-Djava.util.Arrays.useLegacyMergeSort=true</code>,
     * the legacy sorting method is used instead and the test succeeds.
     * </p>
     */
    @Test
    public void testSearchLikeGarima() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("751-07-T-A-2X-1-101.pdf");
                OutputStream result = new FileOutputStream(new java.io.File(RESULT_FOLDER, "751-07-T-A-2X-1-101-marked.pdf")))
        {
            searchWordInPdf(resource, "Glasfacade", result);
        }
    }

    public void searchWordInPdf(InputStream source, String searchWord, OutputStream target) throws IOException
    {
        try (org.pdfclown.files.File file = new org.pdfclown.files.File(source))
        {
            file.setPath("");
            List<String> matchList = new ArrayList<String>();
            // Pattern regex =
            // Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
            Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
            Matcher regexMatcher = regex.matcher(searchWord);
            while (regexMatcher.find())
            {
                if (regexMatcher.group(1) != null)
                {
                    // Add double-quoted string without the quotes
                    matchList.add(regexMatcher.group(1));
                }
                else if (regexMatcher.group(2) != null)
                {
                    // Add single-quoted string without the quotes
                    matchList.add(regexMatcher.group(2));
                }
                else
                {
                    // Add unquoted word
                    matchList.add(regexMatcher.group());
                }
            }

            for (String key : matchList)
            {
                Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
                // 2. Iterating through the document pages...
                TextExtractor textExtractor = new TextExtractor(true, true);
                for (final Page page : file.getDocument().getPages())
                {
                    System.out.println("\nScanning page " + (page.getIndex() + 1) + "...\n");

                    // 2.1. Extract the page text!
                    Map<Rectangle2D, List<ITextString>> textStrings = textExtractor.extract(page);
                    // 2.2. Find the text pattern matches!
                    final Matcher matcher = pattern.matcher(TextExtractor.toString(textStrings));

                    // 2.3. Highlight the text pattern matches!
                    textExtractor.filter(textStrings, new TextExtractor.IIntervalFilter()
                    {
                        @Override
                        public boolean hasNext()
                        {
                            if (matcher.find())
                            {
                                // count++;
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public Interval<Integer> next()
                        {
                            return new Interval<Integer>(matcher.start(), matcher.end());
                        }

                        @Override
                        public void process(Interval<Integer> interval, ITextString match)
                        {
                            Rectangle2D textBox = null;
                            // Defining the highlight box of the text pattern
                            // match...
                            List<Quad> highlightQuads = new ArrayList<Quad>();
                            {
                                /*
                                 * NOTE: A text pattern match may be split
                                 * across multiple contiguous lines, so we have
                                 * to define a distinct highlight box for each
                                 * text chunk.
                                 */

                                for (TextChar textChar : match.getTextChars())
                                {
                                    Rectangle2D textCharBox = textChar.getBox();
                                    if (textBox == null)
                                    {
                                        textBox = (Rectangle2D) textCharBox.clone();
                                    }
                                    else
                                    {
                                        if (textCharBox.getY() > textBox.getMaxY())
                                        {
                                            highlightQuads.add(Quad.get(textBox));
                                            textBox = (Rectangle2D) textCharBox.clone();
                                        }
                                        else
                                        {
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
                        public void remove()
                        {
                            throw new UnsupportedOperationException();
                        }
                    });
                }
            }

            file.save(target, SerializationModeEnum.Standard);
        }
    }
}
