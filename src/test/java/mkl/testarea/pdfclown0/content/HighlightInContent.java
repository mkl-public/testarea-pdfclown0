// $Id$
package mkl.testarea.pdfclown0.content;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pdfclown.documents.Page;
import org.pdfclown.documents.contents.BlendModeEnum;
import org.pdfclown.documents.contents.ExtGState;
import org.pdfclown.documents.contents.ITextString;
import org.pdfclown.documents.contents.TextChar;
import org.pdfclown.documents.contents.colorSpaces.DeviceRGBColor;
import org.pdfclown.documents.contents.composition.PrimitiveComposer;
import org.pdfclown.documents.interaction.annotations.TextMarkup;
import org.pdfclown.documents.interaction.annotations.TextMarkup.MarkupTypeEnum;
import org.pdfclown.files.SerializationModeEnum;
import org.pdfclown.tools.TextExtractor;
import org.pdfclown.util.math.Interval;
import org.pdfclown.util.math.geom.Quad;

/**
 * @author mkl
 */
public class HighlightInContent
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * To compare other results with, this essentially is the original sample
     * {@link org.pdfclown.samples.cli.TextHighlightSample} merely taken out of
     * the PDF Clown sample framework.
     */
    @Test
    public void testHighlightWithAnnotation() throws IOException
    {
        try (InputStream resource = getClass().getResourceAsStream("multiPage.pdf");)
        {
            // 1. Opening the PDF file...
            @SuppressWarnings("resource")
            org.pdfclown.files.File file = new org.pdfclown.files.File(resource);

            // Define the text pattern to look for!
            Pattern pattern = Pattern.compile("S", Pattern.CASE_INSENSITIVE);

            // 2. Iterating through the document pages...
            TextExtractor textExtractor = new TextExtractor(true, true);
            for (final Page page : file.getDocument().getPages())
            {
                System.out.println("\nScanning page " + page.getNumber() + "...\n");

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
                        return matcher.find();
                    }

                    @Override
                    public Interval<Integer> next()
                    {
                        return new Interval<Integer>(matcher.start(), matcher.end());
                    }

                    @Override
                    public void process(Interval<Integer> interval, ITextString match)
                    {
                        // Defining the highlight box of the text pattern
                        // match...
                        List<Quad> highlightQuads = new ArrayList<Quad>();
                        {
                            /*
                             * NOTE: A text pattern match may be split across
                             * multiple contiguous lines, so we have to define a
                             * distinct highlight box for each text chunk.
                             */
                            Rectangle2D textBox = null;
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

            // 3. Highlighted file serialization.
            file.save(new File(RESULT_FOLDER, "multiPage-highlight-annot.pdf"), SerializationModeEnum.Incremental);
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/37021130/text-highlighting-with-pdfclown-without-using-pdf-annotations">
     * Text Highlighting with PDFClown without using PDF Annotations
     * </a>
     * <br/>
     * multiPage.pdf, <i>a sample result of {@link MultiPageTextFlow}</i>
     * 
     * <p>
     * This method shows how to highlight in the page content stream.
     * </p>
     * <p>
     * TODO: reset graphics state before adding the markings.
     * This can be done either by applying save/restore state or by
     * actually counteracting unwanted changed state entries.
     * </p>
     */
    @Test
    public void testHighlightInContent() throws IOException
    {
        try (InputStream resource = getClass().getResourceAsStream("multiPage.pdf");)
        {
            // 1. Opening the PDF file...
            @SuppressWarnings("resource")
            org.pdfclown.files.File file = new org.pdfclown.files.File(resource);

            // Define the text pattern to look for!
            Pattern pattern = Pattern.compile("S", Pattern.CASE_INSENSITIVE);

            // 2. Iterating through the document pages...
            TextExtractor textExtractor = new TextExtractor(true, true);
            for (final Page page : file.getDocument().getPages())
            {
                // Defining the highlight box of the text pattern
                // match...
                List<Quad> highlightQuads = new ArrayList<Quad>();

                System.out.println("\nScanning page " + page.getNumber() + "...\n");

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
                        return matcher.find();
                    }

                    @Override
                    public Interval<Integer> next()
                    {
                        return new Interval<Integer>(matcher.start(), matcher.end());
                    }

                    @Override
                    public void process(Interval<Integer> interval, ITextString match)
                    {
                        {
                            /*
                             * NOTE: A text pattern match may be split across
                             * multiple contiguous lines, so we have to define a
                             * distinct highlight box for each text chunk.
                             */
                            Rectangle2D textBox = null;
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
                    }

                    @Override
                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                });

                // Highlight the text pattern match!
                ExtGState defaultExtGState = new ExtGState(file.getDocument());
                defaultExtGState.setAlphaShape(false);
                defaultExtGState.setBlendMode(Arrays.asList(BlendModeEnum.Multiply));

                PrimitiveComposer composer = new PrimitiveComposer(page);
                composer.getScanner().moveEnd();
                // TODO: reset graphics state here.
                composer.applyState(defaultExtGState);
                composer.setFillColor(new DeviceRGBColor(1, 1, 0));
                {
                    for (Quad markupBox : highlightQuads)
                    {
                        Point2D[] points = markupBox.getPoints();
                        double markupBoxHeight = points[3].getY() - points[0].getY();
                        double markupBoxMargin = markupBoxHeight * .25;
                        composer.drawCurve(new Point2D.Double(points[3].getX(), points[3].getY()),
                                new Point2D.Double(points[0].getX(), points[0].getY()),
                                new Point2D.Double(points[3].getX() - markupBoxMargin, points[3].getY() - markupBoxMargin),
                                new Point2D.Double(points[0].getX() - markupBoxMargin, points[0].getY() + markupBoxMargin));
                        composer.drawLine(new Point2D.Double(points[1].getX(), points[1].getY()));
                        composer.drawCurve(new Point2D.Double(points[2].getX(), points[2].getY()),
                                new Point2D.Double(points[1].getX() + markupBoxMargin, points[1].getY() + markupBoxMargin),
                                new Point2D.Double(points[2].getX() + markupBoxMargin, points[2].getY() - markupBoxMargin));
                        composer.fill();
                    }
                }
                composer.flush();
            }

            // 3. Highlighted file serialization.
            file.save(new File(RESULT_FOLDER, "multiPage-highlight-content.pdf"), SerializationModeEnum.Incremental);
        }
    }
}
