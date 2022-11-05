package org.crawl.http.payload.web.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.namespace.QName;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFSDT;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTColor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTComment;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTComments;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHpsMeasure;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CommentsDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation.Enum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType;

/**
 *
 * @author Roy
 *
 * @date 2022年4月10日-上午9:48:18
 */
public class PoiWordCopyUtils{

    public static void main (String[] args) throws IOException, XmlException {
        XWPFDocument srcDoc = new XWPFDocument (new FileInputStream ("Source.docx"));
        CustomXWPFDocument destDoc = new CustomXWPFDocument ();
        // Copy document layout.
        copyLayout (srcDoc, destDoc);
        OutputStream out = new FileOutputStream ("Destination.docx");
        for (IBodyElement bodyElement : srcDoc.getBodyElements ()) {
            BodyElementType elementType = bodyElement.getElementType ();
            if (elementType == BodyElementType.PARAGRAPH) {
                XWPFParagraph srcPr = (XWPFParagraph) bodyElement;
                printParagraphHeading (srcDoc, srcPr);
                copyStyle (srcDoc, destDoc, srcDoc.getStyles ().getStyle (srcPr.getStyleID ()));
                boolean hasImage = false;
                XWPFParagraph dstPr = destDoc.createParagraph ();
                // Extract image from source docx file and insert into destination docx file.
                for (XWPFRun srcRun : srcPr.getRuns ()) {
                    // You need next code when you want to call XWPFParagraph.removeRun().
                    dstPr.createRun ();
                    if (srcRun.getEmbeddedPictures ().size () > 0)
                        hasImage = true;
                    for (XWPFPicture pic : srcRun.getEmbeddedPictures ()) {
                        byte[] img = pic.getPictureData ().getData ();
                        long cx = pic.getCTPicture ().getSpPr ().getXfrm ().getExt ().getCx ();
                        long cy = pic.getCTPicture ().getSpPr ().getXfrm ().getExt ().getCy ();
                        try {
                            // Working addPicture Code below...
                            String blipId = dstPr.getDocument ().addPictureData (new ByteArrayInputStream (img),
                                            Document.PICTURE_TYPE_PNG);
                            destDoc.createPictureCxCy (blipId, destDoc.getNextPicNameNumber (Document.PICTURE_TYPE_PNG),
                                            cx, cy);
                        } catch (InvalidFormatException e1) {
                            e1.printStackTrace ();
                        }
                    }
                }
                if (hasImage == false) {
                    int pos = destDoc.getParagraphs ().size () - 1;
                    destDoc.setParagraph (srcPr, pos);
                }
            } else if (elementType == BodyElementType.TABLE) {
                XWPFTable table = (XWPFTable) bodyElement;
                copyStyle (srcDoc, destDoc, srcDoc.getStyles ().getStyle (table.getStyleID ()));
                destDoc.createTable ();
                int pos = destDoc.getTables ().size () - 1;
                destDoc.setTable (pos, table);
            }
        }
        destDoc.write (out);
        out.close ();
    }

    // Copy Styles of Table and Paragraph.
    private static void copyStyle (XWPFDocument srcDoc, XWPFDocument destDoc, XWPFStyle style) {
        if (destDoc == null || style == null)
            return;
        if (destDoc.getStyles () == null) {
            destDoc.createStyles ();
        }
        List<XWPFStyle> usedStyleList = srcDoc.getStyles ().getUsedStyleList (style);
        for (XWPFStyle xwpfStyle : usedStyleList) {
            destDoc.getStyles ().addStyle (xwpfStyle);
        }
    }

    // Copy Page Layout.
    //
    // if next error message shows up, download "ooxml-schemas-1.1.jar" file and
    // add it to classpath.
    //
    // [Error]
    // The type org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar
    // cannot be resolved.
    // It is indirectly referenced from required .class files
    //
    // This error happens because there is no CTPageMar class in
    // poi-ooxml-schemas-3.10.1-20140818.jar.
    //
    // [ref.] http://poi.apache.org/faq.html#faq-N10025
    // [ref.] http://poi.apache.org/overview.html#components
    //
    // < ooxml-schemas 1.1 download >
    // http://repo.maven.apache.org/maven2/org/apache/poi/ooxml-schemas/1.1/
    //
    private static void copyLayout (XWPFDocument srcDoc, XWPFDocument destDoc) {
        CTPageMar pgMar = srcDoc.getDocument ().getBody ().getSectPr ().getPgMar ();
        BigInteger bottom = pgMar.getBottom ();
        BigInteger footer = pgMar.getFooter ();
        BigInteger gutter = pgMar.getGutter ();
        BigInteger header = pgMar.getHeader ();
        BigInteger left = pgMar.getLeft ();
        BigInteger right = pgMar.getRight ();
        BigInteger top = pgMar.getTop ();
        CTPageMar addNewPgMar = destDoc.getDocument ().getBody ().addNewSectPr ().addNewPgMar ();
        addNewPgMar.setBottom (bottom);
        addNewPgMar.setFooter (footer);
        addNewPgMar.setGutter (gutter);
        addNewPgMar.setHeader (header);
        addNewPgMar.setLeft (left);
        addNewPgMar.setRight (right);
        addNewPgMar.setTop (top);
        CTPageSz pgSzSrc = srcDoc.getDocument ().getBody ().getSectPr ().getPgSz ();
        BigInteger code = pgSzSrc.getCode ();
        BigInteger h = pgSzSrc.getH ();
        Enum orient = pgSzSrc.getOrient ();
        BigInteger w = pgSzSrc.getW ();
        CTPageSz addNewPgSz = destDoc.getDocument ().getBody ().addNewSectPr ().addNewPgSz ();
        addNewPgSz.setCode (code);
        addNewPgSz.setH (h);
        addNewPgSz.setOrient (orient);
        addNewPgSz.setW (w);
    }

    private static void printParagraphHeading (XWPFDocument doc, XWPFParagraph para) {
        XWPFStyles styles = doc.getStyles ();
        if (para.getStyleID () != null) {
            String styleid = para.getStyleID ();
            System.out.println ("Paragraph Heading: " + para.getText ());
            XWPFStyle style = styles.getStyle (styleid);
            if (style != null) {
                System.out.println ("Style name:" + style.getName ());
                if (style.getName ().startsWith ("heading")) {
                    System.out.println ("Heading-Style is :" + style.getName ());
                }
            }
        }
    }

    /***
     * 根据元素类型处理 handle
     * 
     * @param element
     * @param document
     */
    private static void handleXForElements (IBodyElement element, XWPFDocument document) {
        if (element instanceof XWPFParagraph) {
            XWPFParagraph paragraph = (XWPFParagraph) element;
            XWPFHeaderFooterPolicy headerFooterPolicy = null;
            if (paragraph.getCTP ().getPPr () != null) {
                CTSectPr ctSectPr = paragraph.getCTP ().getPPr ().getSectPr ();
                if (ctSectPr != null) {
                    headerFooterPolicy = new XWPFHeaderFooterPolicy (document, ctSectPr);
                    // Handle Header
                }
            }
            // Handle paragraph
            if (headerFooterPolicy != null) {
                // Handle footer
            }
        }
        if (element instanceof XWPFTable) {
            XWPFTable table = (XWPFTable) element;
            // Handle table
        }
        if (element instanceof XWPFSDT) {
            XWPFSDT sdt = (XWPFSDT) element;
            // Handle SDT
        }
    }

    // Copy all runs from one paragraph to another, keeping the style unchanged
    private static void copyAllRunsToAnotherParagraph (XWPFParagraph oldPar, XWPFParagraph newPar) {
        final int DEFAULT_FONT_SIZE = 10;
        for (XWPFRun run : oldPar.getRuns ()) {
            String textInRun = run.getText (0);
            if (textInRun == null || textInRun.isEmpty ()) {
                continue;
            }
            int fontSize = run.getFontSize ();
            System.out.println ("run text = '" + textInRun + "' , fontSize = " + fontSize);
            XWPFRun newRun = newPar.createRun ();
            // Copy text
            newRun.setText (textInRun);
            // Apply the same style
            newRun.setFontSize ( (fontSize == -1) ? DEFAULT_FONT_SIZE : run.getFontSize ());
            newRun.setFontFamily (run.getFontFamily ());
            newRun.setBold (run.isBold ());
            newRun.setItalic (run.isItalic ());
            newRun.setStrike (run.isStrike ());
            newRun.setColor (run.getColor ());
        }
    }

    public static void copySrcDocToAnotherDoc () {
        try {
            InputStream is = new FileInputStream ("Japan.docx");
            XWPFDocument doc = new XWPFDocument (is);
            List<XWPFParagraph> paras = doc.getParagraphs ();
            XWPFDocument newdoc = new XWPFDocument ();
            for (XWPFParagraph para : paras) {
                if (!para.getParagraphText ().isEmpty ()) {
                    XWPFParagraph newpara = newdoc.createParagraph ();
                    copyAllRunsToAnotherParagraph (para, newpara);
                }
            }
            FileOutputStream fos = new FileOutputStream (new File ("newJapan.docx"));
            newdoc.write (fos);
            fos.flush ();
            fos.close ();
        } catch (FileNotFoundException e) {
            e.printStackTrace ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public static void changeComments () throws Exception {
        XWPFDocument document = new XWPFDocument (new FileInputStream ("WordDocumentHavingComments.docx"));
        for (POIXMLDocumentPart.RelationPart rpart : document.getRelationParts ()) {
            String relation = rpart.getRelationship ().getRelationshipType ();
            if (relation.equals (XWPFRelation.COMMENT.getRelation ())) {
                POIXMLDocumentPart part = rpart.getDocumentPart (); // this is only POIXMLDocumentPart, not a high level
                                                                    // class extending POIXMLDocumentPart
                // provide class extending POIXMLDocumentPart for comments
                MyXWPFCommentsDocument myXWPFCommentsDocument = new MyXWPFCommentsDocument (part.getPackagePart ());
                // and registering this relation instead of only relation to POIXMLDocumentPart
                String rId = document.getRelationId (part);
                document.addRelation (rId, XWPFRelation.COMMENT, myXWPFCommentsDocument);
                // now the comments are available from the new MyXWPFCommentsDocument
                for (CTComment ctComment : myXWPFCommentsDocument.getComments ().getCommentArray ()) {
                    System.out.print ("Comment: Id: " + ctComment.getId ());
                    System.out.print (", Author: " + ctComment.getAuthor ());
                    System.out.print (", Date: " + ctComment.getDate ());
                    System.out.print (", Text: ");
                    for (CTP ctp : ctComment.getPArray ()) {
                        System.out.print (ctp.newCursor ().getTextValue ());
                    }
                    System.out.println ();
                    // and changings can be made which were committed while writing the XWPFDocument
                    if (BigInteger.ONE.equals (ctComment.getId ())) { // the second comment (Id 0 = first)
                        ctComment.setAuthor ("New Author");
                        ctComment.setInitials ("NA");
                        ctComment.setDate (new GregorianCalendar (Locale.US));
                        CTP newCTP = CTP.Factory.newInstance ();
                        newCTP.addNewR ().addNewT ().setStringValue ("The new Text for Comment with Id 1.");
                        ctComment.setPArray (new CTP[] { newCTP });
                    }
                }
            }
        }
        document.write (new FileOutputStream ("WordDocumentHavingComments.docx"));
        document.close ();
    }

    // a wrapper class for the CommentsDocument /word/comments.xml in the *.docx ZIP archive
    private static class MyXWPFCommentsDocument extends POIXMLDocumentPart{

        private CTComments comments;

        private MyXWPFCommentsDocument (PackagePart part) throws Exception {
            super (part);
            comments = CommentsDocument.Factory
                            .parse (part.getInputStream (), org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS)
                            .getComments ();
        }

        private CTComments getComments () {
            return comments;
        }

        @Override
        protected void commit () throws IOException {
            System.out.println ("============MyXWPFCommentsDocument is committed=================");
            XmlOptions xmlOptions = new XmlOptions (org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS);
            xmlOptions.setSaveSyntheticDocumentElement (
                            new QName (CTComments.type.getName ().getNamespaceURI (), "comments"));
            PackagePart part = getPackagePart ();
            OutputStream out = part.getOutputStream ();
            comments.save (out, xmlOptions);
            out.close ();
        }
    }

    /**
     * 以下两种方式设置段落格式
     * 
     * @param contents
     */
    public static void setParagraphHeadingStyle (String contents) {
        XWPFDocument document = new XWPFDocument ();
        XWPFStyles styles = document.createStyles ();
        String heading1 = "My Heading 1";
        String heading2 = "My Heading 2";
        String heading3 = "My Heading 3";
        String heading4 = "My Heading 4";
        addCustomHeadingStyle (document, styles, heading1, 1, 36, "4288BC");
        addCustomHeadingStyle (document, styles, heading2, 2, 28, "4288BC");
        addCustomHeadingStyle (document, styles, heading3, 3, 24, "4288BC");
        addCustomHeadingStyle (document, styles, heading4, 4, 20, "000000");

        XWPFParagraph paragraph = document.createParagraph ();
        XWPFRun run = paragraph.createRun ();
        run.setText ("Nice header!");
        paragraph.setStyle (heading1);

        // 第二种方式设置
        XWPFDocument docxDocument = new XWPFDocument ();
        String strStyleId = "ownstyle1";
        addCustomHeadingStyle (docxDocument, strStyleId, 1);
        XWPFParagraph paragraph1 = docxDocument.createParagraph ();
        XWPFRun run1 = paragraph1.createRun ();
        run1.setText (contents);
        paragraph1.setStyle (strStyleId);
    }

    private static void addCustomHeadingStyle (XWPFDocument docxDocument, String strStyleId, int headingLevel) {
        CTStyle ctStyle = CTStyle.Factory.newInstance ();
        ctStyle.setStyleId (strStyleId);
        CTString styleName = CTString.Factory.newInstance ();
        styleName.setVal (strStyleId);
        ctStyle.setName (styleName);
        CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance ();
        indentNumber.setVal (BigInteger.valueOf (headingLevel));
        // lower number > style is more prominent in the formats bar
        ctStyle.setUiPriority (indentNumber);
        CTOnOff onoffnull = CTOnOff.Factory.newInstance ();
        ctStyle.setUnhideWhenUsed (onoffnull);
        // style shows up in the formats bar
        ctStyle.setQFormat (onoffnull);
        // style defines a heading of the given level
        CTPPr ppr = CTPPr.Factory.newInstance ();
        ppr.setOutlineLvl (indentNumber);
        ctStyle.setPPr (ppr);
        XWPFStyle style = new XWPFStyle (ctStyle);
        // is a null op if already defined
        XWPFStyles styles = docxDocument.createStyles ();
        style.setType (STStyleType.PARAGRAPH);
        styles.addStyle (style);
    }

    private static void addCustomHeadingStyle (XWPFDocument docxDocument, XWPFStyles styles, String strStyleId,
                    int headingLevel, int pointSize, String hexColor) {
        CTStyle ctStyle = CTStyle.Factory.newInstance ();
        ctStyle.setStyleId (strStyleId);
        CTString styleName = CTString.Factory.newInstance ();
        styleName.setVal (strStyleId);
        ctStyle.setName (styleName);
        CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance ();
        indentNumber.setVal (BigInteger.valueOf (headingLevel));
        // lower number > style is more prominent in the formats bar
        ctStyle.setUiPriority (indentNumber);
        CTOnOff onoffnull = CTOnOff.Factory.newInstance ();
        ctStyle.setUnhideWhenUsed (onoffnull);
        // style shows up in the formats bar
        ctStyle.setQFormat (onoffnull);
        // style defines a heading of the given level
        CTPPr ppr = CTPPr.Factory.newInstance ();
        ppr.setOutlineLvl (indentNumber);
        ctStyle.setPPr (ppr);
        XWPFStyle style = new XWPFStyle (ctStyle);
        CTHpsMeasure size = CTHpsMeasure.Factory.newInstance ();
        size.setVal (new BigInteger (String.valueOf (pointSize)));
        CTHpsMeasure size2 = CTHpsMeasure.Factory.newInstance ();
        size2.setVal (new BigInteger ("24"));
        CTFonts fonts = CTFonts.Factory.newInstance ();
        fonts.setAscii ("Loma");
        CTRPr rpr = CTRPr.Factory.newInstance ();
        rpr.setRFonts (fonts);
        rpr.setSz (size);
        rpr.setSzCs (size2);
        CTColor color = CTColor.Factory.newInstance ();
        color.setVal (hexToBytes (hexColor));
        rpr.setColor (color);
        style.getCTStyle ().setRPr (rpr);
        // is a null op if already defined
        style.setType (STStyleType.PARAGRAPH);
        styles.addStyle (style);
    }

    public static byte[] hexToBytes (String hexString) {
        HexBinaryAdapter adapter = new HexBinaryAdapter ();
        byte[] bytes = adapter.unmarshal (hexString);
        return bytes;
    }
}
