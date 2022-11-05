package org.crawl.http.payload.web.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

/**
 *
 * @author Roy
 *
 * @date 2021年10月30日-下午4:45:39
 */
public class CreateWordTemplate{

    /**
      * 跨列合并
      * @param table
      * @param row    所合并的行
      * @param fromCell    起始列
      * @param toCell    终止列
      * @Description
      */
    public void mergeCellsHorizontal (XWPFTable table, int row, int fromCell, int toCell) {
        for (int cellIndex = fromCell; cellIndex <= toCell; cellIndex++) {
            XWPFTableCell cell = table.getRow (row).getCell (cellIndex);
            if (cellIndex == fromCell) {
                cell.getCTTc ().addNewTcPr ().addNewHMerge ().setVal (STMerge.RESTART);
            } else {
                cell.getCTTc ().addNewTcPr ().addNewHMerge ().setVal (STMerge.CONTINUE);
            }
        }
    }

    /**
     * 跨行合并
     * @param table
     * @param col    合并的列
     * @param fromRow    起始行
     * @param toRow    终止行
     * @Description
     */
    public void mergeCellsVertically (XWPFTable table, int col, int fromRow, int toRow) {
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow (rowIndex).getCell (col);
            // 第一个合并单元格用重启合并值设置
            if (rowIndex == fromRow) {
                cell.getCTTc ().addNewTcPr ().addNewVMerge ().setVal (STMerge.RESTART);
            } else {
                // 合并第一个单元格的单元被设置为“继续”
                cell.getCTTc ().addNewTcPr ().addNewVMerge ().setVal (STMerge.CONTINUE);
            }
        }
    }

    /**
     * @Description: 设置表格总宽度与水平对齐方式
     */
    public void setTableWidthAndHAlign (XWPFTable table, String width,
                    STJc.Enum enumValue) {
        CTTblPr tblPr = getTableCTTblPr (table);
        // 表格宽度
        CTTblWidth tblWidth = tblPr.isSetTblW () ? tblPr.getTblW () : tblPr.addNewTblW ();
        if (enumValue != null) {
            CTJc cTJc = tblPr.addNewJc ();
            cTJc.setVal (enumValue);
        }
        // 设置宽度
        tblWidth.setW (new BigInteger (width));
        tblWidth.setType (STTblWidth.DXA);
    }

    /**
     * @Description: 得到Table的CTTblPr,不存在则新建
     */
    public CTTblPr getTableCTTblPr (XWPFTable table) {
        CTTbl ttbl = table.getCTTbl ();
        // 表格属性
        CTTblPr tblPr = ttbl.getTblPr () == null ? ttbl.addNewTblPr () : ttbl.getTblPr ();
        return tblPr;
    }

    public void downloadWord () throws IOException {
        String filePath = "F:\\wkdir\\word_test\\image\\"; // 生成一个文档
        XWPFDocument document = new XWPFDocument ();
        XWPFParagraph para; // 代表文档、表格、标题等种的段落，由多个XWPFRun组成
        XWPFRun run; // 代表具有同样风格的一段文本
        XWPFTableRow row;// 代表表格的一行
        XWPFTableCell cell;// 代表表格的一个单元格
        CTTcPr cellPr; // 单元格属性
        // 设置提案封面内容
        XWPFParagraph proCoverParagraph = document.createParagraph (); // 新建一个标题段落对象（就是一段文字）
        proCoverParagraph.setAlignment (ParagraphAlignment.LEFT);// 样式居中
        XWPFRun coverRun0 = proCoverParagraph.createRun (); // 创建文本对象
        coverRun0.setBold (true); // 加粗
        coverRun0.setFontSize (12); // 字体大小
        coverRun0.setText (String.valueOf ("附件：\r"));

        XWPFParagraph proCoverParagraph1 = document.createParagraph (); // 新建一个标题段落对象（就是一段文字）
        proCoverParagraph1.setAlignment (ParagraphAlignment.CENTER);// 样式左对齐
        XWPFRun coverRun1 = proCoverParagraph1.createRun (); // 创建文本对象
        coverRun1.setBold (true); // 加粗
        coverRun1.setFontSize (41); // 字体大小
        coverRun1.setText (String.valueOf ("不如意事常八九\r可与人言无二三\r\r提\r\r案\r\r表\r\r"));

        XWPFParagraph proCoverParagraph2 = document.createParagraph (); // 新建一个标题段落对象（就是一段文字）
        proCoverParagraph2.setAlignment (ParagraphAlignment.LEFT);// 样式居中
        XWPFRun coverRun2 = proCoverParagraph2.createRun (); // 创建文本对象
        coverRun2.setBold (true); // 加粗
        coverRun2.setFontSize (16); // 字体大小
        coverRun2.setText (String.valueOf ("\r编号：" + "2020001" + "\r填表时间：" + "2020" + "年" + "10" + "月" + "20" + "日"));// 给文本赋值

        // 创建表格
        XWPFTable table = document.createTable (10, 9); // 10行9格
        table.setCellMargins (3, 5, 3, 5);// 在表级别设置单元格边距
        // 设置表格总宽度，字体居中
        setTableWidthAndHAlign (table, "10000", STJc.CENTER);
        // 固定表格宽度，关闭自动伸缩
        CTTblPr tblPr = table.getCTTbl ().getTblPr ();
        tblPr.getTblW ().setType (STTblWidth.DXA);
        // 设置表格样式
        List<XWPFTableRow> rowList = table.getRows ();
        // 扫描行
        for (int i = 0; i < rowList.size (); i++) {
            XWPFTableRow infoTableRow = rowList.get (i);
            List<XWPFTableCell> cellList = infoTableRow.getTableCells ();
            // 扫描列
            for (int j = 0; j < cellList.size (); j++) {
                XWPFParagraph cellParagraph = cellList.get (j).getParagraphArray (0);
                cellParagraph.setAlignment (ParagraphAlignment.CENTER);// 设置表格内容水平居中
                XWPFRun cellParagraphRun = cellParagraph.createRun ();// 在段落创建一个文本
                cellParagraphRun.setFontSize (12);// 设置字体大小
                cellParagraphRun.setBold (true);// 加粗
            }
        }
        XWPFTableCell cell1 = null;
        cell1 = table.getRow (2).getCell (0);
        cell1.setVerticalAlignment (XWPFTableCell.XWPFVertAlign.CENTER); // 设置第3~7行第1列单元格的内容垂直居中
        // 设置表格每列宽度
        for (int i = 0; i < 10; i++) {
            row = table.getRow (i);
            // 设置每行的格式(多少格)
            for (int j = 0; j < 9; j++) {
                cell = row.getCell (j);// 获取行中第j块单元格
                // 设置单元格列宽
                CTTcPr tcpr = cell.getCTTc ().addNewTcPr ();
                CTTblWidth cellw = tcpr.addNewTcW ();
                if (j == 0) {
                    cellw.setW (BigInteger.valueOf (1900));
                }
                if (j == 1 || j == 5) {
                    cellw.setW (BigInteger.valueOf (550));
                }
                if (j == 2 || j == 4 || j == 6 || j == 8) {
                    cellw.setW (BigInteger.valueOf (1280));
                }
                if (j == 3 || j == 7) {
                    cellw.setW (BigInteger.valueOf (940));
                }
            }
        }
        // 合并表格
        mergeCellsHorizontal (table, 0, 1, 8);// 合并第一行，第2~9列
        mergeCellsHorizontal (table, 2, 1, 8);
        mergeCellsHorizontal (table, 3, 1, 8);
        mergeCellsHorizontal (table, 4, 1, 8);
        mergeCellsHorizontal (table, 5, 1, 8);
        mergeCellsHorizontal (table, 6, 1, 8);
        mergeCellsVertically (table, 0, 2, 6);// 合并第1列，第3~7行
        mergeCellsVertically (table, 1, 2, 6);// 合并第2列,3~7行
        // 对赋值表格
        List<XWPFTableRow> rowList1 = table.getRows ();
        List<XWPFTableCell> cellList = new ArrayList<XWPFTableCell> ();
        XWPFParagraph cellParagraph = null;// 存一行的列
        XWPFRun cellParagraphRun = null;// 单元格
        // 第1行第1列
        cellList = rowList1.get (0).getTableCells ();
        cellParagraph = cellList.get (0).getParagraphArray (0);
        cellParagraphRun = cellParagraph.getRuns ().get (0);
        cellParagraphRun.setText (String.valueOf ("案名"));
        cellParagraph = cellList.get (1).getParagraphArray (0);// 第2格的段落
        // 第一行第2列
        cellParagraphRun = cellParagraph.getRuns ().get (0);
        cellParagraphRun.setText (String.valueOf ("案名是习惯过了头地方案名是习惯过了头"));
        // 第3行第1列
        cellList = rowList1.get (2).getTableCells ();
        cellParagraph = cellList.get (0).getParagraphArray (0);// 第2格的段落
        cellParagraphRun = cellParagraph.getRuns ().get (0);
        cellParagraphRun.setText (String.valueOf ("习惯过了头"));
        // 第3行第2列
        cellParagraph = cellList.get (1).getParagraphArray (0);// 第2格的段落
        cellParagraph.setAlignment (ParagraphAlignment.LEFT);// 设置左对齐
        cellParagraphRun = cellParagraph.getRuns ().get (0);
        cellParagraphRun.setText (String.valueOf ("习惯过了头："));
        // 添加图片
        try (FileInputStream is = new FileInputStream (filePath + "0_image13.png")) {
            cellParagraphRun.addPicture (is, XWPFDocument.PICTURE_TYPE_JPEG,
                            filePath + "1.jpg",
                            Units.toEMU (120), Units.toEMU (20)); // 200x200 pixels
        } catch (Exception e) {
            e.printStackTrace ();
        }
        cellParagraphRun.addBreak ();// 换行
        cellParagraphRun.addBreak ();
        XWPFRun cellParagraphRun1 = cellParagraph.createRun ();
        cellParagraphRun1.setFontFamily ("微软雅黑");// 设置字体
        cellParagraphRun1.setText ("c15宿舍要消毒");
        // 导出word文档
        String fileName = "F:\\wkdir\\word_test\\poi导出word文档.docx";
        File outputFolder = new File ("F:\\wkdir\\word_test\\");
        if (!outputFolder.exists ()) {
            outputFolder.mkdir ();
        }
        String encode = System.getProperty ("file.encoding");
        try {
            fileName = new String (fileName.getBytes ("UTF-8"), encode);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace ();
        }
        try {
            FileOutputStream fout = new FileOutputStream (fileName);
            document.write (fout);
            fout.close ();
        } catch (FileNotFoundException e) {
            e.printStackTrace ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public static void main (String[] args) throws IOException {
        CreateWordTemplate dw = new CreateWordTemplate ();
        dw.downloadWord ();
    }
}
