package mkl.testarea.pdfclown0.annotate;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pdfclown.documents.Page;
import org.pdfclown.documents.contents.ITextString;
import org.pdfclown.documents.contents.TextChar;
import org.pdfclown.documents.contents.fonts.CompositeFont;
import org.pdfclown.documents.contents.fonts.SimpleFont;
import org.pdfclown.documents.interaction.annotations.TextMarkup;
import org.pdfclown.documents.interaction.annotations.TextMarkup.MarkupTypeEnum;
import org.pdfclown.files.SerializationModeEnum;
import org.pdfclown.tools.TextExtractor;
import org.pdfclown.util.math.Interval;
import org.pdfclown.util.math.geom.Quad;

/**
 * @author mkl
 */
public class ComplexHighlight {
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/49875818/pdfclownhow-to-override-the-existing-highlighted-keyword-in-pdfclown">
     * Pdfclown:How to override the existing highlighted keyword in pdfclown
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1D7ZwDfMjWFeTk41gFWYk-tBUqp8M0WxA/view">
     * test.pdf
     * </a> as testSeshadri.pdf
     * <p>
     * This is the original code without excessive stdout outputs and commented code.
     * </p>
     */
    @Test
    public void testMarkLikeSeshadri() throws IOException {
        List<Keyword> l = new ArrayList<>();
        Keyword k1 = new Keyword();
        k1.setKey("Just. ETS");
        k1.setValue("NET");
        l.add(k1);
        Keyword k2 = new Keyword();
        k2.setKey("Test. ETS");
        k2.setValue("PROFIT");
        l.add(k2);
        Keyword k = new Keyword();
        k.setKey("ETS");
        k.setValue("LOSS");
        l.add(k);

        System.out.println("\ntestSeshadri.pdf like Seshadri");

        try (InputStream resource = getClass().getResourceAsStream("testSeshadri.pdf")) {
            @SuppressWarnings("resource")
            org.pdfclown.files.File file = new org.pdfclown.files.File(resource);

            long startTime = System.currentTimeMillis();

            TextExtractor textExtractor = new TextExtractor(true, true);
            for (final Page page : file.getDocument().getPages()) {
                Map<Rectangle2D, List<ITextString>> textStrings = textExtractor.extract(page);
                for (Keyword e : l) {
                    final String searchKey = e.getKey();
                    final String translationKeyword = e.getValue();

                    final Pattern pattern;
                    if ((searchKey.contains(")") && searchKey.contains("("))
                            || (searchKey.contains("(") && !searchKey.contains(")"))
                            || (searchKey.contains(")") && !searchKey.contains("(")) || searchKey.contains("?")
                            || searchKey.contains("*") || searchKey.contains("+")) {
                        pattern = Pattern.compile(Pattern.quote(searchKey), Pattern.CASE_INSENSITIVE);
                    } else
                        pattern = Pattern.compile("\\b" + searchKey + "\\b", Pattern.CASE_INSENSITIVE);

                    final Matcher matcher = pattern.matcher(TextExtractor.toString(textStrings).toLowerCase());

                    textExtractor.filter(textStrings, new TextExtractor.IIntervalFilter() {
                        public boolean hasNext() {
                            return matcher.find();
                        }

                        public Interval<Integer> next() {
                            return new Interval<Integer>(matcher.start(), matcher.end(), true, false);
                        }

                        public void process(Interval<Integer> interval, ITextString match) {
                            List<Quad> highlightQuads = new ArrayList<Quad>();
                            {
                                Rectangle2D textBox = null;
                                for (TextChar textChar : match.getTextChars()) {
                                    Rectangle2D textCharBox = textChar.getBox();
                                    if (textBox == null) {
                                        textBox = (Rectangle2D) textCharBox.clone();
                                    } else {
                                        if (textCharBox.getY() > textBox.getMaxY()) {
                                            highlightQuads.add(Quad.get(textBox));
                                            textBox = (Rectangle2D) textCharBox.clone();
                                        } else {
                                            textBox.add(textCharBox);
                                        }
                                    }

                                    textBox.setRect(textBox.getX(), textBox.getY(), textBox.getWidth(),
                                            textBox.getHeight());
                                    highlightQuads.add(Quad.get(textBox));
                                }

                                new TextMarkup(page, highlightQuads, translationKeyword, MarkupTypeEnum.Highlight);
                            }
                        }

                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    });
                }
            }

            SerializationModeEnum serializationMode = SerializationModeEnum.Standard;
            file.save(new File(RESULT_FOLDER, "testSeshadri-highlight.pdf"), serializationMode);

            long endTime = System.currentTimeMillis();
            System.out.println("seconds take for execution is:" + (endTime - startTime) / 1000);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/49875818/pdfclownhow-to-override-the-existing-highlighted-keyword-in-pdfclown">
     * Pdfclown:How to override the existing highlighted keyword in pdfclown
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1D7ZwDfMjWFeTk41gFWYk-tBUqp8M0WxA/view">
     * test.pdf
     * </a> as testSeshadri.pdf
     * <p>
     * This is the improved code with overlap removal, cf. {@link #removeOverlaps(List)}.
     * Beware, there are alternative approaches to this removal. 
     * </p>
     */
    @Test
    public void testMarkLikeSeshadriImproved() throws IOException {
        List<Keyword> l = new ArrayList<>();
        Keyword k1 = new Keyword();
        k1.setKey("Just. ETS");
        k1.setValue("NET");
        l.add(k1);
        Keyword k2 = new Keyword();
        k2.setKey("Test. ETS");
        k2.setValue("PROFIT");
        l.add(k2);
        Keyword k = new Keyword();
        k.setKey("ETS");
        k.setValue("LOSS");
        l.add(k);

        System.out.println("\ntestSeshadri.pdf like Seshadri improved");

        try (InputStream resource = getClass().getResourceAsStream("testSeshadri.pdf")) {
            @SuppressWarnings("resource")
            org.pdfclown.files.File file = new org.pdfclown.files.File(resource);

            long startTime = System.currentTimeMillis();

            TextExtractor textExtractor = new TextExtractor(true, true);
            for (final Page page : file.getDocument().getPages()) {
                Map<Rectangle2D, List<ITextString>> textStrings = textExtractor.extract(page);

                List<Match> matches = new ArrayList<>();

                for (Keyword e : l) {
                    final String searchKey = e.getKey();
                    final String translationKeyword = e.getValue();

                    final Pattern pattern;
                    if ((searchKey.contains(")") && searchKey.contains("("))
                            || (searchKey.contains("(") && !searchKey.contains(")"))
                            || (searchKey.contains(")") && !searchKey.contains("(")) || searchKey.contains("?")
                            || searchKey.contains("*") || searchKey.contains("+")) {
                        pattern = Pattern.compile(Pattern.quote(searchKey), Pattern.CASE_INSENSITIVE);
                    } else
                        pattern = Pattern.compile("\\b" + searchKey + "\\b", Pattern.CASE_INSENSITIVE);

                    final Matcher matcher = pattern.matcher(TextExtractor.toString(textStrings).toLowerCase());

                    textExtractor.filter(textStrings, new TextExtractor.IIntervalFilter() {
                        public boolean hasNext() {
                            return matcher.find();
                        }

                        public Interval<Integer> next() {
                            return new Interval<Integer>(matcher.start(), matcher.end(), true, false);
                        }

                        public void process(Interval<Integer> interval, ITextString match) {
                            matches.add(new Match(interval, match, translationKeyword));
                        }

                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    });
                }

                removeOverlaps(matches);

                for (Match match : matches) {
                    List<Quad> highlightQuads = new ArrayList<Quad>();
                    {
                        Rectangle2D textBox = null;
                        for (TextChar textChar : match.match.getTextChars()) {
                            Rectangle2D textCharBox = textChar.getBox();
                            if (textBox == null) {
                                textBox = (Rectangle2D) textCharBox.clone();
                            } else {
                                if (textCharBox.getY() > textBox.getMaxY()) {
                                    highlightQuads.add(Quad.get(textBox));
                                    textBox = (Rectangle2D) textCharBox.clone();
                                } else {
                                    textBox.add(textCharBox);
                                }
                            }

                            textBox.setRect(textBox.getX(), textBox.getY(), textBox.getWidth(),
                                    textBox.getHeight());
                            highlightQuads.add(Quad.get(textBox));
                        }

                        new TextMarkup(page, highlightQuads, match.tag, MarkupTypeEnum.Highlight);
                    }
                }
            }

            SerializationModeEnum serializationMode = SerializationModeEnum.Standard;
            file.save(new File(RESULT_FOLDER, "testSeshadri-highlight-improved.pdf"), serializationMode);

            long endTime = System.currentTimeMillis();
            System.out.println("seconds take for execution is:" + (endTime - startTime) / 1000);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/50129154/pdfclown-few-different-fonts-of-pdf-files-are-not-recognizing-and-also-i-am-gett">
     * Pdfclown-Few different fonts of pdf files are not recognizing and also i am getting exceptions
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1sy8R72jPNFUZUQxwkZoezqTZ5rpdDadU/view">
     * Sample_Report.pdf
     * </a>
     * <p>
     * Indeed, the {@link NullPointerException} in {@link SimpleFont} encoding loading
     * can be reproduced. The cause is a missing <b>FirstChar</b> entry in some simple
     * fonts. I cannot reproduce the {@link CompositeFont} encoding loading issue.
     * </p>
     * <p>
     * This test uses the code from {@link #testMarkLikeSeshadriImproved()}.
     * </p>
     */
    @Test
    public void testMarkLikeSeshadriImprovedSample_Report() throws IOException {
        List<Keyword> l = new ArrayList<>();
        Keyword k1 = new Keyword();
        k1.setKey("Just. ETS");
        k1.setValue("NET");
        l.add(k1);
        Keyword k2 = new Keyword();
        k2.setKey("Test. ETS");
        k2.setValue("PROFIT");
        l.add(k2);
        Keyword k = new Keyword();
        k.setKey("ETS");
        k.setValue("LOSS");
        l.add(k);

        System.out.println("\nSample_Report.pdf like Seshadri improved");

        try (InputStream resource = getClass().getResourceAsStream("Sample_Report.pdf")) {
            @SuppressWarnings("resource")
            org.pdfclown.files.File file = new org.pdfclown.files.File(resource);

            long startTime = System.currentTimeMillis();

            TextExtractor textExtractor = new TextExtractor(true, true);
            for (final Page page : file.getDocument().getPages()) {
                Map<Rectangle2D, List<ITextString>> textStrings = textExtractor.extract(page);

                List<Match> matches = new ArrayList<>();

                for (Keyword e : l) {
                    final String searchKey = e.getKey();
                    final String translationKeyword = e.getValue();

                    final Pattern pattern;
                    if ((searchKey.contains(")") && searchKey.contains("("))
                            || (searchKey.contains("(") && !searchKey.contains(")"))
                            || (searchKey.contains(")") && !searchKey.contains("(")) || searchKey.contains("?")
                            || searchKey.contains("*") || searchKey.contains("+")) {
                        pattern = Pattern.compile(Pattern.quote(searchKey), Pattern.CASE_INSENSITIVE);
                    } else
                        pattern = Pattern.compile("\\b" + searchKey + "\\b", Pattern.CASE_INSENSITIVE);

                    final Matcher matcher = pattern.matcher(TextExtractor.toString(textStrings).toLowerCase());

                    textExtractor.filter(textStrings, new TextExtractor.IIntervalFilter() {
                        public boolean hasNext() {
                            return matcher.find();
                        }

                        public Interval<Integer> next() {
                            return new Interval<Integer>(matcher.start(), matcher.end(), true, false);
                        }

                        public void process(Interval<Integer> interval, ITextString match) {
                            matches.add(new Match(interval, match, translationKeyword));
                        }

                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    });
                }

                removeOverlaps(matches);

                for (Match match : matches) {
                    List<Quad> highlightQuads = new ArrayList<Quad>();
                    {
                        Rectangle2D textBox = null;
                        for (TextChar textChar : match.match.getTextChars()) {
                            Rectangle2D textCharBox = textChar.getBox();
                            if (textBox == null) {
                                textBox = (Rectangle2D) textCharBox.clone();
                            } else {
                                if (textCharBox.getY() > textBox.getMaxY()) {
                                    highlightQuads.add(Quad.get(textBox));
                                    textBox = (Rectangle2D) textCharBox.clone();
                                } else {
                                    textBox.add(textCharBox);
                                }
                            }

                            textBox.setRect(textBox.getX(), textBox.getY(), textBox.getWidth(),
                                    textBox.getHeight());
                            highlightQuads.add(Quad.get(textBox));
                        }

                        new TextMarkup(page, highlightQuads, match.tag, MarkupTypeEnum.Highlight);
                    }
                }
            }

            SerializationModeEnum serializationMode = SerializationModeEnum.Standard;
            file.save(new File(RESULT_FOLDER, "Sample_Report-highlight-improved.pdf"), serializationMode);

            long endTime = System.currentTimeMillis();
            System.out.println("seconds take for execution is:" + (endTime - startTime) / 1000);
        }
    }

    /**
     * This method removes overlapping matches from the given list.
     * Beware, the result can differ depending on the chosen sort order.
     * 
     * @see #compareLowLengthTag(Match, Match)
     * @see #compareLengthLowTag(Match, Match)
     */
    void removeOverlaps(List<Match> matches) {
        Collections.sort(matches, ComplexHighlight::compareLowLengthTag); // or use compareLengthLowTag

        for (int i = 0; i < matches.size() - 1; i++) {
            Interval<Integer> intervalI = matches.get(i).interval;
            for (int j = i + 1; j < matches.size(); j++) {
                Interval<Integer> intervalJ = matches.get(j).interval;
                if (intervalI.getLow() < intervalJ.getHigh() && intervalJ.getLow() < intervalI.getHigh()) {
                    System.out.printf("Match %d removed as it overlaps match %d.\n", j, i);
                    matches.remove(j--);
                }
            }
        }
    }

    /**
     * Compare {@link Match} instances by start (ascending), 
     * length=end (descending), and tag (ascending)
     */
    static int compareLowLengthTag(Match a, Match b) {
        int compare = a.interval.getLow().compareTo(b.interval.getLow());
        if (compare == 0)
            compare = - a.interval.getHigh().compareTo(b.interval.getHigh());
        if (compare == 0)
            compare = a.tag.compareTo(b.tag);
        return compare;
    }

    /**
     * Compare {@link Match} instances by length (descending),
     * start (ascending), and tag (ascending)
     */
    static int compareLengthLowTag(Match a, Match b) {
        int aLength = a.interval.getHigh() - a.interval.getLow();
        int bLength = b.interval.getHigh() - b.interval.getLow();
        int compare = - Integer.compare(aLength, bLength);
        if (compare == 0)
            compare = a.interval.getLow().compareTo(b.interval.getLow());
        if (compare == 0)
            compare = a.tag.compareTo(b.tag);
        return compare;
    }
}

/**
 * A key-value pair as presumably used by the OP
 */
class Keyword {
    String key, value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

/**
 * A {@link Match} represents a match found by a regular expression
 * {@link Matcher} and holds start and end index in its interval
 * member, the matched PDF text data in its match member, and the
 * String associated with the matcher in its tag member.
 */
class Match {
    final Interval<Integer> interval;
    final ITextString match;
    final String tag;

    public Match(final Interval<Integer> interval, final ITextString match, final String tag) {
        this.interval = interval;
        this.match = match;
        this.tag = tag;
    }
}
