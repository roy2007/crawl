package org.crawl.http.payload.web.utils;

import java.io.FileInputStream;
import java.io.IOException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlToken;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;

/**
 * [ref] https://issues.apache.org/bugzilla/show_bug.cgi?id=49765
 * [ref] http://pastebin.com/index/CbQ3iw8t, http://pastebin.com/2YAneYgt
 */
/**
 *
 * @author Roy
 *
 * @date 2022年4月10日-上午9:49:41
 */
public class CustomXWPFDocument extends XWPFDocument{

    public CustomXWPFDocument () throws IOException {
        super ();
    }

    public CustomXWPFDocument (String filePath) throws IOException {
        super (new FileInputStream (filePath));
    }

    public void createPictureCxCy (String blipId, int id, long cx, long cy) {
        CTInline inline = createParagraph ().createRun ().getCTR ().addNewDrawing ().addNewInline ();
        String picXml = "" +
                        "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">" +
                        "   <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
                        "      <pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
                        "         <pic:nvPicPr>" +
                        "            <pic:cNvPr id=\"" + id + "\" name=\"Generated\"/>" +
                        "            <pic:cNvPicPr/>" +
                        "         </pic:nvPicPr>" +
                        "         <pic:blipFill>" +
                        "            <a:blip r:embed=\"" + blipId
                        + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/>" +
                        "            <a:stretch>" +
                        "               <a:fillRect/>" +
                        "            </a:stretch>" +
                        "         </pic:blipFill>" +
                        "         <pic:spPr>" +
                        "            <a:xfrm>" +
                        "               <a:off x=\"0\" y=\"0\"/>" +
                        "               <a:ext cx=\"" + cx + "\" cy=\"" + cy + "\"/>" +
                        "            </a:xfrm>" +
                        "            <a:prstGeom prst=\"rect\">" +
                        "               <a:avLst/>" +
                        "            </a:prstGeom>" +
                        "         </pic:spPr>" +
                        "      </pic:pic>" +
                        "   </a:graphicData>" +
                        "</a:graphic>";
        // CTGraphicalObjectData graphicData = inline.addNewGraphic().addNewGraphicData();
        XmlToken xmlToken = null;
        try {
            xmlToken = XmlToken.Factory.parse (picXml);
        } catch (XmlException xe) {
            xe.printStackTrace ();
        }
        inline.set (xmlToken);
        // graphicData.set(xmlToken);
        inline.setDistT (0);
        inline.setDistB (0);
        inline.setDistL (0);
        inline.setDistR (0);
        CTPositiveSize2D extent = inline.addNewExtent ();
        extent.setCx (cx);
        extent.setCy (cy);
        CTNonVisualDrawingProps docPr = inline.addNewDocPr ();
        docPr.setId (id);
        docPr.setName ("Picture " + id);
        docPr.setDescr ("Generated");
    }

    public void createPicture (String blipId, int id, int width, int height) {
        final int EMU = 9525;
        width *= EMU;
        height *= EMU;
        // String blipId = getAllPictures().get(id).getPackageRelationship().getId();
        createPictureCxCy (blipId, id, width, height);
    }
}
