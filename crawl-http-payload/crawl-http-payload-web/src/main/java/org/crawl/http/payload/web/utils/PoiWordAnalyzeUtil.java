package org.crawl.http.payload.web.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.stream.FileImageOutputStream;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.model.StyleDescription;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;
import org.springframework.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Roy
 * @date 2021/01/08
 */
@Slf4j
public enum PoiWordAnalyzeUtil {
    /**
     * ????????????
     */
    POI_WORD_ANALYZE_INSTANCE;

    /**
     * ?????????ASCII???
     */
    private static final short ENTER_ASCII      = 13;
    /**
     * ?????????ASCII???
     */
    private static final short SPACE_ASCII      = 32;
    // private static String outputUrl ="D:\\test-word\\2020_12_28\\out\\test_out_(%d).docx";
    private static String      outputUrl        = "F:\\wkdir\\word_test\\test_out_(%d).docx";
    /**
     * ???????????????ASCII???
     */
    private static final short TABULATION_ASCII = 9;

    public static void parserWordByBodyElement (String fileFullPath) throws Exception {
        log.info ("PoiWordAnalyzeUtil.parserWord the parameter : {}", fileFullPath);
        FileInputStream fs = new FileInputStream (fileFullPath);
        XWPFDocument doc = new XWPFDocument (OPCPackage.open (fs));
        List<IBodyElement> listBodyElements = doc.getBodyElements ();
        int pageTotal = 1;
        int line = 1;
        int pIndex = 0;
        int tIndex = 0;

        for (int i = 0; i < listBodyElements.size (); i++) {
            IBodyElement bodyElement = listBodyElements.get (i);
            XWPFParagraph paragraph = null;
            XWPFTable xwpfTable = null;
            String runStr = "";
            if (bodyElement.getElementType ().equals (BodyElementType.PARAGRAPH)) {
                paragraph = (XWPFParagraph) bodyElement;
                // XWPFParagraph pa = bodyElement.getBody ().getParagraphArray (i);
                // // ??????????????????
                // List<CTR> rList = pa.getCTP ().getRList ();
                // for (int j = 0; j < rList.size (); j++) {
                // CTR r = rList.get (j);
                // List<CTEmpty> pageBreakList = r.getLastRenderedPageBreakList ();
                // if (pageBreakList.size () > 0) {
                // pageTotal++;
                // }
                // }
                // if (pa != null) {
                // List<XWPFRun> runs = pa.getRuns ();
                // StringBuilder pContents = new StringBuilder ();
                // for (XWPFRun run : runs) {
                // pContents.append (run.text ());
                // }
                // runStr = pContents.toString ();
                // // System.out.println (pContents.toString ());
                // if (pContents.length () > 0) {
                // pContents.delete (0, pContents.length () - 1);
                // }
                // }

            }

            if (bodyElement.getElementType ().equals (BodyElementType.TABLE)) {
                xwpfTable = (XWPFTable) bodyElement;
            }
            if (bodyElement.getElementType ().equals (BodyElementType.CONTENTCONTROL)) {
                // TODO content control
            }
            runStr = paragraph != null ? paragraph.getText () : "";
            if (StringUtils.isEmpty (runStr)) {
                boolean isIamge = exctractImages (paragraph);
                runStr = isIamge ? "This images...." : "";
            }
            if (StringUtils.isEmpty (runStr)) {
                runStr = exctractTables (xwpfTable);
            }

            log.info ("line: {} ?????????| {}", line, runStr);
            line++;
        }
        System.out.println ("----------------????????????:" + pageTotal);
    }

    public static List<WordNodeSO> parserWord (String fileFullPath) throws Exception {
        log.info ("PoiWordAnalyzeUtil.parserWord the parameter : {}", fileFullPath);
        File tempDir = com.google.common.io.Files.createTempDir ();
        String specifyDir = tempDir.getAbsolutePath ();
        FileInputStream fs = new FileInputStream (fileFullPath);
        XWPFDocument doc = new XWPFDocument (OPCPackage.open (fs));
        CTStyles originStyles = doc.getStyle ();
        List<XWPFParagraph> paragraphs = doc.getParagraphs ();
        List<IBodyElement> listBodyElements = doc.getBodyElements ();
        List<Integer> spaceLineRunListIndex = Lists.newArrayList ();
        int n = 0;
        // for (int i = 0; i < listBodyElements.size (); i++) {
        // XWPFParagraph paragraph = paragraphs.get (i);
        // if (StringUtils.isEmpty (paragraph.getText ())
        // && exctractImages (paragraph)) {
        // spaceLineRunListIndex.add (i);
        // }
        // n++;
        // if (!listBodyElements.get (i).getElementType ().equals (BodyElementType.PARAGRAPH)) {
        // n--;
        // }
        // }
        // for (int k = spaceLineRunListIndex.size () - 1; k >= 0; k--) {
        // doc.removeBodyElement (spaceLineRunListIndex.get (k));
        // }
        int line = 1;
        XWPFDocument docNew = new XWPFDocument ();
        docNew.createStyles ().setStyles (originStyles);
        StringBuilder lineContent = new StringBuilder ();
        int index = 0;
        int tableIndex = 0;
        if (!CollectionUtils.isEmpty (paragraphs)) {
            for (XWPFParagraph paragraph : paragraphs) {
                docNew.createParagraph ();
                docNew.setParagraph (paragraph, index);
                index++;
                String runStr = paragraph.getText ();
                if (StringUtils.isEmpty (runStr)) {
                    exctractImages (paragraph);
                }
                String paraNumLevelText = paragraph.getNumLevelText ();
                String paraLevelStyleId = paragraph.getStyleID ();
                String paraLevelStyle = paragraph.getStyle ();
                // if (StringUtils.isEmpty (runStr)) {
                // exctractTables (doc, paragraph, tableIndex);
                // tableIndex++;
                // }
                lineContent.append (paragraph.getText ());
                log.info ("line: {} ?????????: {} |?????????{} | ??????id:{}|?????????{} ", line, runStr, paraNumLevelText, paraLevelStyleId,
                                paraLevelStyle);
                if (runStr.equals ("\n")) {
                    // ????????????word
                    File file = new File (String.format (outputUrl, line));
                    FileOutputStream stream = new FileOutputStream (file);
                    docNew.write (stream);
                    stream.close ();
                    // ?????????????????????
                    docNew = new XWPFDocument ();
                    docNew.createStyles ().setStyles (originStyles);
                    index = 0;
                }
                line++;
            }
        }
        docNew.close ();
        return null;
    }

    private static String exctractTables (XWPFTable xwpfTable) {
        if (xwpfTable == null)
            return "";
        // for (int i = 0; i < xwpfTable.getRows ().size (); i++) {
        // }
        XWPFTableRow fristRow = xwpfTable.getRow (0);
        StringBuilder tableFristLineContents = new StringBuilder ("??????????????????????????????");
        for (XWPFTableCell cell : fristRow.getTableCells ()) {
            tableFristLineContents.append (cell.getText ()).append ("|");
        }
        return tableFristLineContents.toString ();
    }

    private static boolean exctractImages (XWPFParagraph paragraph) {
        if (paragraph == null)
            return false;
        List<XWPFRun> xwpfRunList = paragraph.getRuns ();
        if (xwpfRunList.isEmpty ()) {
            return false;
        }
        for (XWPFRun xwpfRun : xwpfRunList) {
            List<XWPFPicture> xwpfPictureList = xwpfRun.getEmbeddedPictures ();
            if (xwpfPictureList.isEmpty ()) {
                return false;
            }
            for (XWPFPicture xwpfPicture : xwpfPictureList) {
                // byte[] imageData = xwpfPicture.getPictureData ().getData ();
                String imageName = xwpfPicture.getPictureData ().getFileName ();
                System.out.println ("Make Picture success,Please find image in " + imageName);
                // byte2image (imageData, "F:\\wkdir\\word_test\\image\\" + imageName);
            }
        }
        return true;
    }

    public static void byte2image (byte[] data, String path) {
        if (data.length < 3 || path.equals (""))
            return;
        FileImageOutputStream imageOutput = null;
        try {
            imageOutput = new FileImageOutputStream (new File (path));
            imageOutput.write (data, 0, data.length);
            System.out.println ("Make Picture success,Please find image in " + path);
        } catch (Exception ex) {
            System.out.println ("Exception: " + ex);
            ex.printStackTrace ();
        } finally {
            try {
                imageOutput.close ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }
    }

    /**
     * ??????03???word?????????????????????
     * @param path ????????????
     * @param type 1?????????????????????2?????????????????????3???????????????????????????
     * @return list
     * @throws IOException
     */
    public static List<String> getWordTitlesAndContext2003 (String path, Integer type) throws IOException {
        InputStream is = new FileInputStream (path);
        HWPFDocument doc = new HWPFDocument (is);
        Range r = doc.getRange ();
        List<String> list = new ArrayList<String> ();
        List<String> titles = new ArrayList<String> ();
        List<String> context = new ArrayList<String> ();
        System.out.println ("this 2003 word:" + path + "|????????????|" + r.numParagraphs ());
        // ????????????????????????
        StyleSheet styleSheet = doc.getStyleSheet ();
        int numStyles = doc.getStyleSheet ().numStyles ();
        System.out.println ("this 2003 parser ???????????? | " + numStyles);
        for (int i = 0; i < r.numParagraphs (); i++) {
            org.apache.poi.hwpf.usermodel.Paragraph p = r.getParagraph (i);
            // ????????????????????????
            int styleIndex = p.getStyleIndex ();
            System.out.println ("this 2003 parser ???????????????| " + styleIndex);
            String contexts = p.text ();
            list.add (contexts); // ??????+??????
            // check if style index is greater than total number of styles
            if (numStyles > styleIndex) {
                StyleDescription style = styleSheet.getStyleDescription (styleIndex);
                String styleName = style.getName ();
                System.out.println ("this 2003 parser styleName | " + styleName);
                String text = p.text ();
                if (styleName != null && styleName.contains ("??????")) {
                    System.out.println ("this 2003 parser ????????????| " + text);
                    titles.add (text);
                } else if (styleName != null && styleName.contains ("??????")) {
                    System.out.println ("this 2003 parser ??????|" + text);
                    context.add (text);
                }
                // ??????????????????
                char fristChar = text.charAt (0);
                // ????????????????????????
                if (fristChar == SPACE_ASCII) {
                    System.out.println ("this 2003 parser ??????|" + String.valueOf (fristChar));
                } else if (fristChar == TABULATION_ASCII) {
                    // ??????????????????????????????
                    System.out.println ("this 2003 parser ???????????????|" + String.valueOf (fristChar));
                }
                CharacterRun characterRun = p.getCharacterRun (0);
                System.out.println ("this 2003 parser characterRun |" + characterRun.text ());
                characterRun.text ().chars ().count ();
                doc.getRange ().getSection (0).getCharacterRun (0);
                p.getCharacterRun (0);
            }
        }
        // ??????word?????????
        byte[] dataStream = doc.getDataStream ();
        // ???????????????????????????????????????
        int numCharacterRuns = r.numCharacterRuns ();
        // System.out.println("CharacterRuns ???:"+numCharacterRuns);
        // ?????????????????? ??? ??????????????????????????????????????????????????????
        PicturesTable table = new PicturesTable (doc, dataStream, null, null, null);
        // ??????????????????
        /*
         * int i = 1;
         * for(int j=0 ; j<numCharacterRuns ; j++){
         * //????????????????????????????????????????????????????????????
         * CharacterRun run = r.getCharacterRun(j);
         * //??????????????????
         * boolean bool = table.hasPicture(run);
         * if(bool) {
         * //????????????????????????????????????CharacterRun
         * Picture pic = table.extractPicture(run, true);
         * //???????????????????????????????????????????????????
         * pic.writeImageContent(new FileOutputStream("D:temp"+filename+"_"+i+".jpg"));
         * i++;
         * }
         * }
         */
        if (type == 1) {
            return titles;
        } else if (type == 2) {
            return context;
        }
        return list;
    }

    // word????????????
    private static CTStyles wordStyles = null;

    public static void getWordStyle (String filepath) {
        XWPFDocument template;
        try {
            // ??????????????????
            template = new XWPFDocument (new FileInputStream (filepath));
            // ?????????????????????????????????
            wordStyles = template.getStyle ();
        } catch (FileNotFoundException e) {
            log.error ("???????????????", e);
        } catch (IOException e) {
            log.error ("", e);
        } catch (XmlException e) {
            log.error ("XML????????????", e);
        }
    }

    /**
     * ??????word????????????
     * @param filepath
     * @return
     * @throws IOException
     */
    public static List<String> getWordTitles (String filepath) throws IOException {
        String filename = getWordFileName (filepath);
        if (".docx".equals (filename)) {
            return getWordTitles2007 (filepath);
        } else {
            // 1?????????????????????2?????????????????????3??????????????????
            return getWordTitlesAndContext2003 (filepath, 1);
        }
    }

    /**
     * ??????word?????????????????????
     * @param filepath
     * @return
     * @throws Exception
     */
    public static List<String> getWordText (String filepath) throws Exception {
        String filename = getWordFileName (filepath);
        if (".docx".equals (filename)) {
            return getParagraphText2007 (filepath);
        } else {
            return getWordTitlesAndContext2003 (filepath, 3);
        }
    }

    /**
     * ???????????????????????????
     * @param filepath
     * @return
     */
    public static String getWordFileName (String filepath) {
        File file = new File (filepath);
        String filename = file.getName ();
        // return filename.substring(0, filename.lastIndexOf("."));
        return filename.substring (filename.lastIndexOf ("."), filename.length ());
    }

    // ??????2007???word?????? ?????????????????????????????????
    public static List<String> getWordTitles2007 (String path) throws IOException {
        InputStream is = new FileInputStream (path);
        XWPFDocument doc = new XWPFDocument (is);
        // HWPFDocument doc = new HWPFDocument(is);
        // Range r = doc.getRange();
        List<XWPFRun> listRun;
        List<XWPFParagraph> listParagraphs = doc.getParagraphs ();// ??????????????????
        List<String> list = new ArrayList<String> ();
        /*
         * for (int i = 0; i<listParagraphs.size(); i++) {
         * System.out.println(listParagraphs.get(i).getRuns().get(0).getText(0));
         * String str = listParagraphs.get(i).getRuns().get(0).getText(0);
         * list.add(str);
         * }
         */
        List<XWPFParagraph> paras = doc.getParagraphs ();
        for (XWPFParagraph para : paras) {
            // ?????????????????????
            // CTPPr pr = para.getCTP().getPPr();
            if (para.getText () != null && !"".equals (para.getText ()) && !"r".equals (para.getText ())) {
                System.out.println (para.getText ().trim ());
                String str = para.getText ();
                String str1 = "  " + para.getText ().replaceAll ("\\n", "").replaceAll ("\\t", "") + "\n";
                list.add (str);
            }
        }
        /*
         * XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
         * String text = extractor.getText();
         * // System.out.println(text);
         * POIXMLProperties.CoreProperties coreProps = extractor.getCoreProperties();
         * String title = coreProps.getTitle();
         * System.out.println(title);
         */
        // ??????????????????????????????
        /*
         * List<XWPFTable> tables = doc.getTables();
         * List<XWPFTableRow> rows;
         * List<XWPFTableCell> cells;
         * for (XWPFTable table : tables) {
         * // ????????????
         * // CTTblPr pr = table.getCTTbl().getTblPr();
         * // ????????????????????????
         * rows = table.getRows();
         * for (XWPFTableRow row : rows) {
         * //???????????????????????????
         * cells = row.getTableCells();
         * for (XWPFTableCell cell : cells) {
         * System.out.println(cell.getText());;
         * }
         * }
         * }
         */
        close (is);
        doc.close ();
        return list;
    }

    /**
     * ??????2007???word??????????????????????????????
     * @param filePath
     * @return
     * @throws Exception
     */
    public static List<String> getParagraphText2007 (String filePath) throws Exception {
        InputStream is = new FileInputStream (filePath);
        XWPFDocument doc = new XWPFDocument (is);
        List<String> context = new ArrayList<String> ();
        List<XWPFParagraph> paras = doc.getParagraphs ();
        for (XWPFParagraph para : paras) {
            String str = "  " + para.getText ().replaceAll ("\\n", "").replaceAll ("\\t", "") + "\n";
            context.add (str);
        }
        // ??????????????????????????????
        /*
         * List<XWPFTable> tables = doc.getTables();
         * List<XWPFTableRow> rows;
         * List<XWPFTableCell> cells;
         * for (XWPFTable table : tables) {
         * // ????????????
         * // CTTblPr pr = table.getCTTbl().getTblPr();
         * // ????????????????????????
         * rows = table.getRows();
         * for (XWPFTableRow row : rows) {
         * //???????????????????????????
         * cells = row.getTableCells();
         * for (XWPFTableCell cell : cells) {
         * context.add(cell.getText());
         * }
         * }
         * }
         * //??????????????????
         * XWPFPictureData pic = document.getPictureDataByID("PICId");
         * //??????docx???????????????document??????
         * XWPFDocument document = new XWPFDocument(POIXMLDocument.openPackage(inputUrl));
         * //????????????????????????
         * List<XWPFTable> allTable = document.getTables();
         * for (XWPFTable xwpfTable : allTable) {
         * //?????????????????????
         * List<XWPFTableRow> rows = xwpfTable.getRows();
         * for (XWPFTableRow xwpfTableRow : rows) {
         * //???????????????????????????
         * List<XWPFTableCell> cells = xwpfTableRow.getTableCells();
         * for (XWPFTableCell xwpfTableCell : cells) {
         * List<XWPFParagraph> paragraphs = xwpfTableCell.getParagraphs();
         * for (XWPFParagraph xwpfParagraph : paragraphs) {
         * List<XWPFRun> runs = xwpfParagraph.getRuns();
         * for(int i = 0; i < runs.size();i++){
         * XWPFRun run = runs.get(i);
         * tableText.append(run.toString());
         * }
         * }
         * }
         * }
         * }
         * System.out.println(tableText.toString());
         */
        close (is);
        doc.close ();
        return context;
    }

    /**
     * ???????????????????????????
     * @param size ??????list size
     * @param object ??????????????????
     * @throws Exception
     */
    public static void writeTable (int size, List<MatchingObject> object, String returnPath, XWPFStyles originStyles)
                    throws Exception {
        XWPFDocument doc = new XWPFDocument ();
        // ?????????????????????????????????
        XWPFStyles newStyles = (originStyles == null) ? doc.createStyles () : originStyles;
        // ????????? // ?????????????????????????????????????????????????????????
        // newStyles.setStyles(wordStyles);
        // ??????????????????
        XWPFTable table = doc.createTable (size, 2);
        // ??????????????????????????????????????????????????????getTableCells()???????????????????????????????????????row?????????????????????
        // table.addNewCol(); //?????????????????????
        // table.createRow(); //?????????????????????
        List<XWPFTableRow> rows = table.getRows ();
        // ????????????
        CTTblPr tablePr = table.getCTTbl ().addNewTblPr ();
        // ????????????
        CTTblWidth width = tablePr.addNewTblW ();
        width.setW (BigInteger.valueOf (9000));
        XWPFTableRow row;
        List<XWPFTableCell> cells;
        XWPFTableCell cell;
        int rowSize = rows.size ();
        int cellSize;
        for (int i = 0; i < rowSize; i++) {
            row = rows.get (i);
            // ???????????????
            // row.addNewTableCell();
            // ??????????????????
            row.setHeight (400);
            // ?????????
            // CTTrPr rowPr = row.getCtRow().addNewTrPr();
            // ???????????????????????????????????????cell??????
            // List<CTTc> list = row.getCtRow().getTcList();
            cells = row.getTableCells ();
            cellSize = cells.size ();
            for (int j = 0; j < cellSize; j++) {
                cell = cells.get (j);
                if (object.get (i).getMark () != 0) {
                    // ????????????????????????
                    cell.setColor ("ff0000"); // ??????
                }
                // ???????????????
                CTTcPr cellPr = cell.getCTTc ().addNewTcPr ();
                cellPr.addNewVAlign ().setVal (STVerticalJc.CENTER);
                if (j == 0) {
                    cellPr.addNewTcW ().setW (BigInteger.valueOf (4500));
                    if (object.get (i).getMark () == 2) { // ??????
                        cell.setText ("");
                    } else { // ????????????????????????
                        cell.setText (object.get (i).getData ());
                    }
                } else if (j == 1) {
                    cellPr.addNewTcW ().setW (BigInteger.valueOf (4500));
                    if (object.get (i).getMark () == 3) { // ??????
                        cell.setText (object.get (i).getDataAds ());
                    } else if (object.get (i).getMark () == 1) { // ??????
                        cell.setText ("");
                    } else {
                        cell.setText (object.get (i).getData ());
                    }
                }
            }
        }
        // ?????????????????????????????????
        OutputStream os = new FileOutputStream (returnPath);
        // ????????????
        doc.write (os);
        close (os);
    }

    // ?????????????????????word
    public static void formatDoc () throws IOException {
        // ?????????word????????????
        XWPFDocument doc = new XWPFDocument ();
        // ?????????????????????????????????
        XWPFStyles newStyles = doc.createStyles ();
        // ?????????// ?????????????????????????????????????????????????????????
        newStyles.setStyles (wordStyles);
        // ??????????????????
        // ??????1???1?????????
        XWPFParagraph para1 = doc.createParagraph ();
        // ?????????// 1?????????
        para1.setStyle ("1");
        XWPFRun run1 = para1.createRun ();
        // ????????????
        run1.setText ("?????? 1");
        // ??????2
        XWPFParagraph para2 = doc.createParagraph ();
        // ?????????// 2?????????
        para2.setStyle ("2");
        XWPFRun run2 = para2.createRun ();
        // ????????????
        run2.setText ("?????? 2");
        // ??????
        XWPFParagraph paraX = doc.createParagraph ();
        XWPFRun runX = paraX.createRun ();
        // ????????????
        runX.setText ("??????");
        // word???????????????
        FileOutputStream fos = new FileOutputStream ("D://myDoc1.docx");
        doc.write (fos);
        fos.close ();
    }

    // ??????????????????word
    public static void writeSimpleDocxFile () throws IOException {
        XWPFDocument docxDocument = new XWPFDocument ();
        // ???????????????????????????????????????????????????????????????word????????????????????????????????????????????????
        addCustomHeadingStyle (docxDocument, "?????? 1", 1);
        addCustomHeadingStyle (docxDocument, "?????? 2", 2);
        // ??????1
        XWPFParagraph paragraph = docxDocument.createParagraph ();
        XWPFRun run = paragraph.createRun ();
        run.setText ("?????? 1");
        paragraph.setStyle ("?????? 1");
        // ??????2
        XWPFParagraph paragraph2 = docxDocument.createParagraph ();
        XWPFRun run2 = paragraph2.createRun ();
        run2.setText ("?????? 2");
        paragraph2.setStyle ("?????? 2");
        // ??????
        XWPFParagraph paragraphX = docxDocument.createParagraph ();
        XWPFRun runX = paragraphX.createRun ();
        runX.setText ("??????");
        // word???????????????
        FileOutputStream fos = new FileOutputStream ("D:/myDoc2.docx");
        docxDocument.write (fos);
        fos.close ();
    }

    // ?????????????????????
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

    /**
     * ???????????????
     * @param is ?????????
     */
    private static void close (InputStream is) {
        if (is != null) {
            try {
                is.close ();
            } catch (IOException e) {
                log.error ("???????????????", e);
            }
        }
    }

    /**
     * ???????????????
     * @param os ?????????
     */
    private static void close (OutputStream os) throws Exception {
        if (os != null) {
            try {
                os.close ();
            } catch (IOException e) {
                log.error ("???????????????", e);
            }
        }
    }

    public static void main (String[] args) {
        // paragraph.g
        // if (paragraph instanceof XWPFPicture) {
        // System.out.println ("// ????????????");
        // }
        // if (paragraph instanceof XWPFTable) {
        // System.out.println ("// ????????????");
        // }
        // if (paragraph instanceof XWPFTableRow) {
        // System.out.println ("// ?????????????????????");
        // }
        // if (paragraph instanceof XWPFTableCell) {
        // System.out.println ("// ??????????????????????????????");
        // }
        try {
            // String pdfFilePath = "F:\\??????\\??????OSSIM??????????????????????????????????????????????????????.pdf";
            // PoiWordAnalyzeUtil.parserWord (pdfFilePath); will throw exceptions
            // PoiWordAnalyzeUtil.parserWordByBodyElement ("F:\\wkdir\\word_test\\??????????????????.docx");// tables.docx
            PoiWordAnalyzeUtil.parserWord ("F:\\wkdir\\word_test\\??????????????????????????????(????????????).docx");// ??????????????????.docx
            // String filePath2007 = "F:\\wkdir\\word_test\\mysql_name_rule.docx";
            String filePath2003 = "F:\\wkdir\\word_test\\TSUNG??????????????????????????????.doc";
            // String filePath20071 = "D:\\test-word\\2020_12_28\\2040STC50118_??????????????????-????????????????????????????????????????????????200212.docx";
            // String filePath20031 = "D:\\test-word\\2020_12_28\\?????????????????????????????????????????????.doc";
            // String filePath20072 = "D:\\test-word\\2020_12_28\\???????????????100M?????? - ??????.docx";
            String filePath20033 = "D:\\test-word\\2020_12_28\\?????????????????????????????????????????????.doc";
            String docPath = "F:\\wkdir\\word_test\\??????????????????????????????(????????????).doc";
            List<String> wordTexts = PoiWordAnalyzeUtil.getWordText (docPath);
            int i = 1;
            for (String string : wordTexts) {
                System.out.println (i + " ???|" + string);
                i++;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }
}
