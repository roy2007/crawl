package org.crawl.http.payload.web.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.model.StyleDescription;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

/**
 *
 * @author Roy
 *
 * @date 2021年10月31日-下午5:10:43
 */
public class WordProcess{

    public static List<String> getWordTitles2003 (String path) throws IOException {
        File file = new File (path);
        String filename = file.getName ();
        filename = filename.substring (0, filename.lastIndexOf ("."));
        InputStream is = new FileInputStream (path);
        HWPFDocument doc = new HWPFDocument (is);
        Range r = doc.getRange ();
        List<String> list = new ArrayList<String> ();
        for (int i = 0; i < r.numParagraphs (); i++) {
            Paragraph p = r.getParagraph (i);
            /** check if style index is greater than total number of styles*/
            int numStyles = doc.getStyleSheet ().numStyles ();
            int styleIndex = p.getStyleIndex ();
            if (numStyles > styleIndex) {
                StyleSheet style_sheet = doc.getStyleSheet ();
                StyleDescription style = style_sheet.getStyleDescription (styleIndex);
                String styleName = style.getName ();
                if (styleName != null && styleName.contains ("标题")) {
                    /** write style name and associated text*/
                    System.out.println (styleName + "->" + p.text ());
                    System.out.println (p.text ());
                    String text = p.text ();
                    list.add (text);
                }
            }
        }
        /** 图表跟图片不一样，需另外处理 //得到word数据流*/
        byte[] dataStream = doc.getDataStream ();
        /**用于在一段范围内获得段落数*/
        int numCharacterRuns = r.numCharacterRuns ();
        System.out.println ("CharacterRuns 数:" + numCharacterRuns);
        /**负责图像提取 和 确定一些文件某块是否包含嵌入的图像。*/
        PicturesTable table = new PicturesTable (doc, dataStream, null, null, null);
        /**文章图片编号*/
        int i = 1;
        for (int j = 0; j < numCharacterRuns; j++) {
            /**这个类表示一个文本运行，有着共同的属性。*/
            CharacterRun run = r.getCharacterRun (j);
            /**是否存在图片*/
            boolean bool = table.hasPicture (run);
            if (bool) {
                /**返回图片对象绑定到指定的CharacterRun*/
                Picture pic = table.extractPicture (run, true);
                /**图片的内容字节写入到指定的输出流。*/
                pic.writeImageContent (new FileOutputStream ("E:\\temp\\" + filename + "_" + i + ".jpg"));
                i++;
            }
        }
        return list;
    }

    public static List<String> getWordTitles2007 (String path) throws IOException {
        InputStream is = new FileInputStream (path);
        /**2007*/
        // OPCPackage p = POIXMLDocument.openPackage (path);
        // XWPFWordExtractor e = new XWPFWordExtractor (p);
        // POIXMLDocument doc = e.getDocument ();
        List<String> list = new ArrayList<String> ();
        XWPFDocument doc = new XWPFDocument (is);
        List<XWPFParagraph> paras = doc.getParagraphs ();
        for (XWPFParagraph graph : paras) {
            String text = graph.getParagraphText ();
            String style = graph.getStyle ();
            if ("1".equals (style)) {
                System.out.println (text + "--[" + style + "]");
            } else if ("2".equals (style)) {
                System.out.println (text + "--[" + style + "]");
            } else if ("3".equals (style)) {
                System.out.println (text + "--[" + style + "]");
            } else {
                continue;
            }
            list.add (text);
        }
        return list;
    }

    public static void main (String[] args) throws IOException {
        String path = "E:/temp/poi_test.doc";
        List<String> list = new ArrayList<String> ();
        if (path.endsWith (".doc")) {
            list = getWordTitles2003 (path);
        } else if (path.endsWith (".docx")) {
            list = getWordTitles2007 (path);
        }
        for (String title : list) {
            System.out.println (title);
        }
    }
}
