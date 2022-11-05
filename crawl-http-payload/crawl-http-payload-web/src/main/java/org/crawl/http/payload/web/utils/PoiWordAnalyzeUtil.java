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
     * 单例实例
     */
    POI_WORD_ANALYZE_INSTANCE;

    /**
     * 回车符ASCII码
     */
    private static final short ENTER_ASCII      = 13;
    /**
     * 空格符ASCII码
     */
    private static final short SPACE_ASCII      = 32;
    // private static String outputUrl ="D:\\test-word\\2020_12_28\\out\\test_out_(%d).docx";
    private static String      outputUrl        = "F:\\wkdir\\word_test\\test_out_(%d).docx";
    /**
     * 水平制表符ASCII码
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
                // // 统计分页总数
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

            log.info ("line: {} 的内容| {}", line, runStr);
            line++;
        }
        System.out.println ("----------------分页总数:" + pageTotal);
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
                log.info ("line: {} 的内容: {} |层级：{} | 样式id:{}|样式：{} ", line, runStr, paraNumLevelText, paraLevelStyleId,
                                paraLevelStyle);
                if (runStr.equals ("\n")) {
                    // 生成新的word
                    File file = new File (String.format (outputUrl, line));
                    FileOutputStream stream = new FileOutputStream (file);
                    docNew.write (stream);
                    stream.close ();
                    // 重新生成另一个
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
        StringBuilder tableFristLineContents = new StringBuilder ("这是表格第一行内容：");
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
     * 获取03版word文档标题和内容
     * @param path 文件路径
     * @param type 1：只获取标题；2：只获取内容；3：标题和内容都获取
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
        System.out.println ("this 2003 word:" + path + "|段落总数|" + r.numParagraphs ());
        // 获取所有样式容器
        StyleSheet styleSheet = doc.getStyleSheet ();
        int numStyles = doc.getStyleSheet ().numStyles ();
        System.out.println ("this 2003 parser 样式总数 | " + numStyles);
        for (int i = 0; i < r.numParagraphs (); i++) {
            org.apache.poi.hwpf.usermodel.Paragraph p = r.getParagraph (i);
            // 当前段落样式引号
            int styleIndex = p.getStyleIndex ();
            System.out.println ("this 2003 parser 样式索引号| " + styleIndex);
            String contexts = p.text ();
            list.add (contexts); // 标题+内容
            // check if style index is greater than total number of styles
            if (numStyles > styleIndex) {
                StyleDescription style = styleSheet.getStyleDescription (styleIndex);
                String styleName = style.getName ();
                System.out.println ("this 2003 parser styleName | " + styleName);
                String text = p.text ();
                if (styleName != null && styleName.contains ("标题")) {
                    System.out.println ("this 2003 parser 标题内容| " + text);
                    titles.add (text);
                } else if (styleName != null && styleName.contains ("正文")) {
                    System.out.println ("this 2003 parser 正文|" + text);
                    context.add (text);
                }
                // 取第一个字符
                char fristChar = text.charAt (0);
                // 判断是否为空格符
                if (fristChar == SPACE_ASCII) {
                    System.out.println ("this 2003 parser 空格|" + String.valueOf (fristChar));
                } else if (fristChar == TABULATION_ASCII) {
                    // 判断是否为水平制表符
                    System.out.println ("this 2003 parser 水平制表符|" + String.valueOf (fristChar));
                }
                CharacterRun characterRun = p.getCharacterRun (0);
                System.out.println ("this 2003 parser characterRun |" + characterRun.text ());
                characterRun.text ().chars ().count ();
                doc.getRange ().getSection (0).getCharacterRun (0);
                p.getCharacterRun (0);
            }
        }
        // 得到word数据流
        byte[] dataStream = doc.getDataStream ();
        // 用于在一段范围内获得段落数
        int numCharacterRuns = r.numCharacterRuns ();
        // System.out.println("CharacterRuns 数:"+numCharacterRuns);
        // 负责图像提取 和 确定一些文件某块是否包含嵌入的图像。
        PicturesTable table = new PicturesTable (doc, dataStream, null, null, null);
        // 文章图片编号
        /*
         * int i = 1;
         * for(int j=0 ; j<numCharacterRuns ; j++){
         * //这个类表示一个文本运行，有着共同的属性。
         * CharacterRun run = r.getCharacterRun(j);
         * //是否存在图片
         * boolean bool = table.hasPicture(run);
         * if(bool) {
         * //返回图片对象绑定到指定的CharacterRun
         * Picture pic = table.extractPicture(run, true);
         * //图片的内容字节写入到指定的输出流。
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

    // word整体样式
    private static CTStyles wordStyles = null;

    public static void getWordStyle (String filepath) {
        XWPFDocument template;
        try {
            // 读取模板文档
            template = new XWPFDocument (new FileInputStream (filepath));
            // 获得模板文档的整体样式
            wordStyles = template.getStyle ();
        } catch (FileNotFoundException e) {
            log.error ("未找到文件", e);
        } catch (IOException e) {
            log.error ("", e);
        } catch (XmlException e) {
            log.error ("XML转换异常", e);
        }
    }

    /**
     * 获取word文档标题
     * @param filepath
     * @return
     * @throws IOException
     */
    public static List<String> getWordTitles (String filepath) throws IOException {
        String filename = getWordFileName (filepath);
        if (".docx".equals (filename)) {
            return getWordTitles2007 (filepath);
        } else {
            // 1：只获取标题；2：只获取内容；3：标题和内容
            return getWordTitlesAndContext2003 (filepath, 1);
        }
    }

    /**
     * 获取word文档内容及标题
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
     * 获取文件名包括后缀
     * @param filepath
     * @return
     */
    public static String getWordFileName (String filepath) {
        File file = new File (filepath);
        String filename = file.getName ();
        // return filename.substring(0, filename.lastIndexOf("."));
        return filename.substring (filename.lastIndexOf ("."), filename.length ());
    }

    // 获取2007版word标题 （这个方法有一点问题）
    public static List<String> getWordTitles2007 (String path) throws IOException {
        InputStream is = new FileInputStream (path);
        XWPFDocument doc = new XWPFDocument (is);
        // HWPFDocument doc = new HWPFDocument(is);
        // Range r = doc.getRange();
        List<XWPFRun> listRun;
        List<XWPFParagraph> listParagraphs = doc.getParagraphs ();// 得到段落信息
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
            // 当前段落的属性
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
        // 获取文档中所有的表格
        /*
         * List<XWPFTable> tables = doc.getTables();
         * List<XWPFTableRow> rows;
         * List<XWPFTableCell> cells;
         * for (XWPFTable table : tables) {
         * // 表格属性
         * // CTTblPr pr = table.getCTTbl().getTblPr();
         * // 获取表格对应的行
         * rows = table.getRows();
         * for (XWPFTableRow row : rows) {
         * //获取行对应的单元格
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
     * 获取2007版word文档内容，按段落获取
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
        // 获取文档中所有的表格
        /*
         * List<XWPFTable> tables = doc.getTables();
         * List<XWPFTableRow> rows;
         * List<XWPFTableCell> cells;
         * for (XWPFTable table : tables) {
         * // 表格属性
         * // CTTblPr pr = table.getCTTbl().getTblPr();
         * // 获取表格对应的行
         * rows = table.getRows();
         * for (XWPFTableRow row : rows) {
         * //获取行对应的单元格
         * cells = row.getTableCells();
         * for (XWPFTableCell cell : cells) {
         * context.add(cell.getText());
         * }
         * }
         * }
         * //获取图片对象
         * XWPFPictureData pic = document.getPictureDataByID("PICId");
         * //解析docx模板并获取document对象
         * XWPFDocument document = new XWPFDocument(POIXMLDocument.openPackage(inputUrl));
         * //获取全部表格对象
         * List<XWPFTable> allTable = document.getTables();
         * for (XWPFTable xwpfTable : allTable) {
         * //获取表格行数据
         * List<XWPFTableRow> rows = xwpfTable.getRows();
         * for (XWPFTableRow xwpfTableRow : rows) {
         * //获取表格单元格数据
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
     * 将对比结果写入表格
     * @param size 对比list size
     * @param object 短句对比结果
     * @throws Exception
     */
    public static void writeTable (int size, List<MatchingObject> object, String returnPath, XWPFStyles originStyles)
                    throws Exception {
        XWPFDocument doc = new XWPFDocument ();
        // 获取新建文档对象的样式
        XWPFStyles newStyles = (originStyles == null) ? doc.createStyles () : originStyles;
        // 关键行 // 修改设置文档样式为静态块中读取到的样式
        // newStyles.setStyles(wordStyles);
        // 创建一个表格
        XWPFTable table = doc.createTable (size, 2);
        // 这里增加的列原本初始化创建的行在通过getTableCells()方法获取时获取不到，但通过row新增的就可以。
        // table.addNewCol(); //给表格增加一列
        // table.createRow(); //给表格新增一行
        List<XWPFTableRow> rows = table.getRows ();
        // 表格属性
        CTTblPr tablePr = table.getCTTbl ().addNewTblPr ();
        // 表格宽度
        CTTblWidth width = tablePr.addNewTblW ();
        width.setW (BigInteger.valueOf (9000));
        XWPFTableRow row;
        List<XWPFTableCell> cells;
        XWPFTableCell cell;
        int rowSize = rows.size ();
        int cellSize;
        for (int i = 0; i < rowSize; i++) {
            row = rows.get (i);
            // 新增单元格
            // row.addNewTableCell();
            // 设置行的高度
            row.setHeight (400);
            // 行属性
            // CTTrPr rowPr = row.getCtRow().addNewTrPr();
            // 这种方式是可以获取到新增的cell的。
            // List<CTTc> list = row.getCtRow().getTcList();
            cells = row.getTableCells ();
            cellSize = cells.size ();
            for (int j = 0; j < cellSize; j++) {
                cell = cells.get (j);
                if (object.get (i).getMark () != 0) {
                    // 设置单元格的颜色
                    cell.setColor ("ff0000"); // 红色
                }
                // 单元格属性
                CTTcPr cellPr = cell.getCTTc ().addNewTcPr ();
                cellPr.addNewVAlign ().setVal (STVerticalJc.CENTER);
                if (j == 0) {
                    cellPr.addNewTcW ().setW (BigInteger.valueOf (4500));
                    if (object.get (i).getMark () == 2) { // 新增
                        cell.setText ("");
                    } else { // 不变、新增、修改
                        cell.setText (object.get (i).getData ());
                    }
                } else if (j == 1) {
                    cellPr.addNewTcW ().setW (BigInteger.valueOf (4500));
                    if (object.get (i).getMark () == 3) { // 修改
                        cell.setText (object.get (i).getDataAds ());
                    } else if (object.get (i).getMark () == 1) { // 删除
                        cell.setText ("");
                    } else {
                        cell.setText (object.get (i).getData ());
                    }
                }
            }
        }
        // 文件不存在时会自动创建
        OutputStream os = new FileOutputStream (returnPath);
        // 写入文件
        doc.write (os);
        close (os);
    }

    // 模板方式实现写word
    public static void formatDoc () throws IOException {
        // 新建的word文档对象
        XWPFDocument doc = new XWPFDocument ();
        // 获取新建文档对象的样式
        XWPFStyles newStyles = doc.createStyles ();
        // 关键行// 修改设置文档样式为静态块中读取到的样式
        newStyles.setStyles (wordStyles);
        // 开始内容输入
        // 标题1，1级大纲
        XWPFParagraph para1 = doc.createParagraph ();
        // 关键行// 1级大纲
        para1.setStyle ("1");
        XWPFRun run1 = para1.createRun ();
        // 标题内容
        run1.setText ("标题 1");
        // 标题2
        XWPFParagraph para2 = doc.createParagraph ();
        // 关键行// 2级大纲
        para2.setStyle ("2");
        XWPFRun run2 = para2.createRun ();
        // 标题内容
        run2.setText ("标题 2");
        // 正文
        XWPFParagraph paraX = doc.createParagraph ();
        XWPFRun runX = paraX.createRun ();
        // 正文内容
        runX.setText ("正文");
        // word写入到文件
        FileOutputStream fos = new FileOutputStream ("D://myDoc1.docx");
        doc.write (fos);
        fos.close ();
    }

    // 自定义样式写word
    public static void writeSimpleDocxFile () throws IOException {
        XWPFDocument docxDocument = new XWPFDocument ();
        // 老外自定义了一个名字，中文版的最好还是按照word给的标题名来，否则级别上可能会乱
        addCustomHeadingStyle (docxDocument, "标题 1", 1);
        addCustomHeadingStyle (docxDocument, "标题 2", 2);
        // 标题1
        XWPFParagraph paragraph = docxDocument.createParagraph ();
        XWPFRun run = paragraph.createRun ();
        run.setText ("标题 1");
        paragraph.setStyle ("标题 1");
        // 标题2
        XWPFParagraph paragraph2 = docxDocument.createParagraph ();
        XWPFRun run2 = paragraph2.createRun ();
        run2.setText ("标题 2");
        paragraph2.setStyle ("标题 2");
        // 正文
        XWPFParagraph paragraphX = docxDocument.createParagraph ();
        XWPFRun runX = paragraphX.createRun ();
        runX.setText ("正文");
        // word写入到文件
        FileOutputStream fos = new FileOutputStream ("D:/myDoc2.docx");
        docxDocument.write (fos);
        fos.close ();
    }

    // 增加自定义标题
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
     * 关闭输入流
     * @param is 输入流
     */
    private static void close (InputStream is) {
        if (is != null) {
            try {
                is.close ();
            } catch (IOException e) {
                log.error ("流关闭异常", e);
            }
        }
    }

    /**
     * 关闭输出流
     * @param os 输出流
     */
    private static void close (OutputStream os) throws Exception {
        if (os != null) {
            try {
                os.close ();
            } catch (IOException e) {
                log.error ("流关闭异常", e);
            }
        }
    }

    public static void main (String[] args) {
        // paragraph.g
        // if (paragraph instanceof XWPFPicture) {
        // System.out.println ("// 代表图片");
        // }
        // if (paragraph instanceof XWPFTable) {
        // System.out.println ("// 代表表格");
        // }
        // if (paragraph instanceof XWPFTableRow) {
        // System.out.println ("// 代表表格的一行");
        // }
        // if (paragraph instanceof XWPFTableCell) {
        // System.out.println ("// 代表表格的一个单元格");
        // }
        try {
            // String pdfFilePath = "F:\\文档\\基于OSSIM技术的信息安全集成管理系统分析与设计.pdf";
            // PoiWordAnalyzeUtil.parserWord (pdfFilePath); will throw exceptions
            // PoiWordAnalyzeUtil.parserWordByBodyElement ("F:\\wkdir\\word_test\\测试环境要求.docx");// tables.docx
            PoiWordAnalyzeUtil.parserWord ("F:\\wkdir\\word_test\\数据处理软件使用手册(国家版本).docx");// 测试环境要求.docx
            // String filePath2007 = "F:\\wkdir\\word_test\\mysql_name_rule.docx";
            String filePath2003 = "F:\\wkdir\\word_test\\TSUNG压力测试工具使用总结.doc";
            // String filePath20071 = "D:\\test-word\\2020_12_28\\2040STC50118_招标文件终稿-中国金币总公司数字化营销平台项目200212.docx";
            // String filePath20031 = "D:\\test-word\\2020_12_28\\商用密码产品品种和型号申请材料.doc";
            // String filePath20072 = "D:\\test-word\\2020_12_28\\超大但未超100M文件 - 副本.docx";
            String filePath20033 = "D:\\test-word\\2020_12_28\\商用密码产品品种和型号申请材料.doc";
            String docPath = "F:\\wkdir\\word_test\\数据处理软件使用手册(国家版本).doc";
            List<String> wordTexts = PoiWordAnalyzeUtil.getWordText (docPath);
            int i = 1;
            for (String string : wordTexts) {
                System.out.println (i + " 行|" + string);
                i++;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }
}
