// $Id$
package mkl.testarea.pdfclown0.content;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pdfclown.documents.Document;
import org.pdfclown.documents.Page;
import org.pdfclown.documents.contents.composition.BlockComposer;
import org.pdfclown.documents.contents.composition.PrimitiveComposer;
import org.pdfclown.documents.contents.composition.XAlignmentEnum;
import org.pdfclown.documents.contents.composition.YAlignmentEnum;
import org.pdfclown.documents.contents.fonts.Font;
import org.pdfclown.documents.contents.fonts.StandardType1Font;
import org.pdfclown.files.SerializationModeEnum;
import org.pdfclown.util.math.geom.Dimension;

/**
 * @author mkl
 */
public class MultiPageTextFlow
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35531396/when-printing-a-pdf-with-pdfclown-the-print-runs-off-the-page-without-a-pdfclow">
     * When printing a pdf with PdfClown, the print runs off the page without a PdfClown creating a new page, how to fix?
     * </a>
     * <p>
     * This sample shows how to draw text with a multi-page text flow. It essentially is a
     * cut-down version of the {@link org.pdfclown.samples.cli.ComplexTypesettingSample}
     * from the PDFClown cli samples.
     * </p>
     */
    @Test
    public void testShowMultiPageText() throws IOException
    {
        org.pdfclown.files.File file = new org.pdfclown.files.File();
        Document document = file.getDocument();

        Font font = new StandardType1Font(document,StandardType1Font.FamilyEnum.Courier, false, false);
        Page page = new Page(document);
        document.getPages().add(page);
        Dimension2D pageSize = page.getSize();
        PrimitiveComposer composer = new PrimitiveComposer(page);
        BlockComposer blockComposer = new BlockComposer(composer);
        Rectangle2D frame = new Rectangle2D.Double(30, 30, (pageSize.getWidth() - 60), pageSize.getHeight() - 60);
        blockComposer.begin(frame,XAlignmentEnum.Left,YAlignmentEnum.Top);
        composer.setFont(font, 15);
        
        String[] paragraphs = new String[]
                {
                  "We maintain this free software definition to show clearly what must be true about a particular software program for it to be considered free software.",
                  "\"Free software\" is a matter of liberty, not price. To understand the concept, you should think of \"free\" as in \"free speech\", not as in \"free beer\".",
                  "Free software is a matter of the users' freedom to run, copy, distribute, study, change and improve the software. More precisely, it refers to four kinds of freedom, for the users of the software:",
                  "* The freedom to run the program, for any purpose (freedom 0).",
                  "* The freedom to study how the program works, and adapt it to your needs (freedom 1). Access to the source code is a precondition for this.",
                  "* The freedom to redistribute copies so you can help your neighbor (freedom 2).",
                  "* The freedom to improve the program, and release your improvements to the public, so that the whole community benefits (freedom 3). Access to the source code is a precondition for this.",
                  "A program is free software if users have all of these freedoms. Thus, you should be free to redistribute copies, either with or without modifications, either gratis or charging a fee for distribution, to anyone anywhere. Being free to do these things means (among other things) that you do not have to ask or pay for permission.",
                  "You should also have the freedom to make modifications and use them privately in your own work or play, without even mentioning that they exist. If you do publish your changes, you should not be required to notify anyone in particular, or in any particular way.",
                  "The freedom to use a program means the freedom for any kind of person or organization to use it on any kind of computer system, for any kind of overall job, and without being required to communicate subsequently with the developer or any other specific entity.",
                  "The freedom to redistribute copies must include binary or executable forms of the program, as well as source code, for both modified and unmodified versions. (Distributing programs in runnable form is necessary for conveniently installable free operating systems.) It is ok if there is no way to produce a binary or executable form for a certain program (since some languages don't support that feature), but you must have the freedom to redistribute such forms should you find or develop a way to make them.",
                  "In order for the freedoms to make changes, and to publish improved versions, to be meaningful, you must have access to the source code of the program. Therefore, accessibility of source code is a necessary condition for free software.",
                  "In order for these freedoms to be real, they must be irrevocable as long as you do nothing wrong; if the developer of the software has the power to revoke the license, without your doing anything to give cause, the software is not free.",
                  "However, certain kinds of rules about the manner of distributing free software are acceptable, when they don't conflict with the central freedoms. For example, copyleft (very simply stated) is the rule that when redistributing the program, you cannot add restrictions to deny other people the central freedoms. This rule does not conflict with the central freedoms; rather it protects them.",
                  "You may have paid money to get copies of free software, or you may have obtained copies at no charge. But regardless of how you got your copies, you always have the freedom to copy and change the software, even to sell copies.",
                  "\"Free software\" does not mean \"non-commercial\". A free program must be available for commercial use, commercial development, and commercial distribution. Commercial development of free software is no longer unusual; such free commercial software is very important.",
                  "Rules about how to package a modified version are acceptable, if they don't substantively block your freedom to release modified versions. Rules that \"if you make the program available in this way, you must make it available in that way also\" can be acceptable too, on the same condition. (Note that such a rule still leaves you the choice of whether to publish the program or not.) It is also acceptable for the license to require that, if you have distributed a modified version and a previous developer asks for a copy of it, you must send one, or that you identify yourself on your modifications.",
                  "In the GNU project, we use \"copyleft\" to protect these freedoms legally for everyone. But non-copylefted free software also exists. We believe there are important reasons why it is better to use copyleft, but if your program is non-copylefted free software, we can still use it.",
                  "See Categories of Free Software for a description of how \"free software,\" \"copylefted software\" and other categories of software relate to each other.",
                  "Sometimes government export control regulations and trade sanctions can constrain your freedom to distribute copies of programs internationally. Software developers do not have the power to eliminate or override these restrictions, but what they can and must do is refuse to impose them as conditions of use of the program. In this way, the restrictions will not affect activities and people outside the jurisdictions of these governments.",
                  "Most free software licenses are based on copyright, and there are limits on what kinds of requirements can be imposed through copyright. If a copyright-based license respects freedom in the ways described above, it is unlikely to have some other sort of problem that we never anticipated (though this does happen occasionally). However, some free software licenses are based on contracts, and contracts can impose a much larger range of possible restrictions. That means there are many possible ways such a license could be unacceptably restrictive and non-free.",
                  "We can't possibly list all the possible contract restrictions that would be unacceptable. If a contract-based license restricts the user in an unusual way that copyright-based licenses cannot, and which isn't mentioned here as legitimate, we will have to think about it, and we will probably decide it is non-free.",
                  "When talking about free software, it is best to avoid using terms like \"give away\" or \"for free\", because those terms imply that the issue is about price, not freedom. Some common terms such as \"piracy\" embody opinions we hope you won't endorse. See Confusing Words and Phrases that are Worth Avoiding for a discussion of these terms. We also have a list of translations of \"free software\" into various languages.",
                  "Finally, note that criteria such as those stated in this free software definition require careful thought for their interpretation. To decide whether a specific software license qualifies as a free software license, we judge it based on these criteria to determine whether it fits their spirit as well as the precise words. If a license includes unconscionable restrictions, we reject it, even if we did not anticipate the issue in these criteria. Sometimes a license requirement raises an issue that calls for extensive thought, including discussions with a lawyer, before we can decide if the requirement is acceptable. When we reach a conclusion about a new issue, we often update these criteria to make it easier to see why certain licenses do or don't qualify.",
                  "If you are interested in whether a specific license qualifies as a free software license, see our list of licenses. If the license you are concerned with is not listed there, you can ask us about it by sending us email at <licensing@fsf.org>.",
                  "If you are contemplating writing a new license, please contact the FSF by writing to that address. The proliferation of different free software licenses means increased work for users in understanding the licenses; we may be able to help you find an existing Free Software license that meets your needs.",
                  "If that isn't possible, if you really need a new license, with our help you can ensure that the license really is a Free Software license and avoid various practical problems.",
                  "Another group has started using the term \"open source\" to mean something close (but not identical) to \"free software\". We prefer the term \"free software\" because, once you have heard it refers to freedom rather than price, it calls to mind freedom. The word \"open\" never does that."
                };

        for (String paragraph : paragraphs)
        {
            int paragraphTextIndex = 0;
            
            while ((paragraphTextIndex += blockComposer.showText(paragraph.substring(paragraphTextIndex))) < paragraph.length())
            {
                blockComposer.end();
                composer.flush();

                document.getPages().add(page = new Page(document));
                composer = new PrimitiveComposer(page);
                blockComposer = new BlockComposer(composer);
                blockComposer.begin(frame,XAlignmentEnum.Left,YAlignmentEnum.Top);
                composer.setFont(font, 15);
            }
            blockComposer.showBreak(new Dimension(10, 10));
        }

        blockComposer.end();
        composer.flush();

        file.save(new File(RESULT_FOLDER, "multiPage.pdf"), SerializationModeEnum.Standard);
        file.close();
    }

}
