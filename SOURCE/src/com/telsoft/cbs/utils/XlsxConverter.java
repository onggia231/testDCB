package com.telsoft.cbs.utils;

import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;


import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hwpf.converter.HtmlDocumentFacade;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XlsxConverter {

    private XSSFWorkbook x;
    private HtmlDocumentFacade htmlDocumentFacade;
    private Element page;
    private StringBuilder css = new StringBuilder();
    private FormulaEvaluator evaluator = null;


    public XlsxConverter(String filePath) throws IOException, InvalidFormatException, ParserConfigurationException {

        OPCPackage op = OPCPackage.open(filePath);
        x = new XSSFWorkbook(op);
        evaluator = x.getCreationHelper().createFormulaEvaluator();
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        this.htmlDocumentFacade = new HtmlDocumentFacade(document);

        Element window = htmlDocumentFacade.createBlock();
        window.setAttribute("id", "window");
        page = htmlDocumentFacade.createBlock();
        page.setAttribute("id", "page");

        window.appendChild(page);
        htmlDocumentFacade.getBody().appendChild(window);
    }

    public XlsxConverter() {
    }

    public static void main(String[] args) throws Exception {
        String name = "xml";

        String test = "day la cau test so ${code} của ${name}";

        HashMap<String, String> hm = new HashMap<>();
        hm.put("code", "456");
        hm.put("name", "Nampt");
        for (HashMap.Entry<String, String> entry : hm.entrySet()) {
            test = test.replace("${" + entry.getKey() + "}", entry.getValue());
        }
//        System.out.println(test);

//        XlsxConverter.convert("D:\\test\\report-06-10-2021.xlsx", "D:\\test\\test.html");
//        String mstrHost = "smtp.gmail.com";
//        int miPort = 465;
//        boolean mbStartTLS = true;
//        boolean mbSSLOnConnect = true;
//        String mstrUser = "cbs.telsoft@gmail.com";
//        String mstrPassword = "cbs123456";
//        String mstrSender = "CBS_SYSTEM";
//        String mstrProtocol = "smtps";
//        MailServicesUtil mMailServicesUtil = new MailServicesUtil(mstrHost, miPort, mstrUser, mstrPassword, mbStartTLS, mbSSLOnConnect, mstrSender);
//        List<String> lsEmail = new ArrayList<>();
//        String strMailSubject = "test mail";
//
//        String strMailBody = XlsxConverter.convert("D:\\test\\report-06-10-2021.xlsx", "D:\\test\\test.html");
//        List<String> listFile = new ArrayList<>();
//
//        lsEmail.add("nampt@telsoft.com.vn");
//        listFile.add("D:\\test\\report-06-10-2021.xlsx");
////        mMailServicesUtil.sendMail(lsEmail, strMailSubject, strMailBody, listFile);
//
//        java.security.Security
//                .addProvider(new com.sun.net.ssl.internal.ssl.Provider());
//        SimpleAuthenticator authenticator = new SimpleAuthenticator(mstrUser,
//                mstrPassword);
//
//        Properties props = new Properties();
//
//        props.put("mail.transport.protocol", mstrProtocol);
//        props.put("mail.pop3.starttls.enable", "true");
//        props.put("mail.pop3.host", mstrHost);
//        props.put("mail.pop3.auth", "true");
//
//        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        props.setProperty("mail.imap.socketFactory.fallback", "false");
//        props.setProperty("mail.imap.socketFactory.port", "995");
//
//        Session mailsession;
//        Transport transport;
//
//        mailsession = javax.mail.Session.getInstance(props, new MailAuthenticator(mstrUser, mstrPassword));
//        transport = mailsession.getTransport(new URLName(mstrProtocol, mstrHost,
//                miPort, null, mstrUser, mstrPassword));
//        transport.connect();
//
//        mMailServicesUtil.processMail(mailsession, transport, lsEmail, strMailSubject, strMailBody,"asv", null);
//        transport.close();

    }


    /**
     * @param filePath
     * @throws InvalidFormatException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static String convert(String filePath, HashMap<String, String> hmSheetConvert, HashMap<String, Object> hmSheetRowRecord
    ) throws InvalidFormatException, IOException, ParserConfigurationException, TransformerException {
        XlsxConverter converter = new XlsxConverter(filePath);

        Integer sheetNum = converter.x.getNumberOfSheets();
        for (int i = 0; i < sheetNum; i++) {
            if (hmSheetConvert != null || hmSheetConvert.size() > 0) {
                if (hmSheetConvert.get(converter.x.getSheetName(i)) != null) {
                    XSSFSheet sheet = converter.x.getSheet(converter.x.getSheetName(i));
                    String sheetTitle = hmSheetConvert.get(converter.x.getSheetName(i));
                    // add sheet title
                    {
                        Element title = converter.htmlDocumentFacade.createHeader2();
                        title.setTextContent(sheetTitle);
                        converter.page.appendChild(title);
                    }
                    //0 la co file template, size > 0 la khong co file template
                    int rowStartRecord = 0;
                    if (hmSheetRowRecord.size() > 0) {
                        rowStartRecord = (Integer) hmSheetRowRecord.get(converter.x.getSheetName(i));
                    }
                    converter.processSheet(converter.page, sheet, "_" + i + "_", rowStartRecord);
                }

            }
        }

        converter.htmlDocumentFacade.updateStylesheet();

        Element style = (Element) converter.htmlDocumentFacade.getDocument().getElementsByTagName("style").item(0);
        style.setTextContent(converter.css.append(style.getTextContent()).toString());
//        mailHtml = "test gửi mail";
        return converter.saveAsHtml(converter.htmlDocumentFacade.getDocument());
    }

    private void processSheet(Element container, XSSFSheet sheet, String sID, int rowStartProcess) {

        Element table = htmlDocumentFacade.createTable();
        int sIndex = sheet.getWorkbook().getSheetIndex(sheet);
        String sId = "sheet_".concat(String.valueOf(sIndex));
        table.setAttribute("id", sId);
        table.setAttribute("border", "1");
        table.setAttribute("cellpadding", "2");
        table.setAttribute("cellspacing", "0");
        table.setAttribute("width", "100%");
        table.setAttribute("style", "width:100.0%;");
//        + "border-collapse: collapse;");

        if (sheet.getDefaultRowHeightInPoints() > 0) {
//            css.append("#").append(sId).append(" tr{height:").append(sheet.getDefaultRowHeightInPoints() / 28.34).append("cm}\n");
        }
        if (sheet.getDefaultColumnWidth() > 0) {
//            css.append("#").append(sId).append(" td{width:").append(sheet.getDefaultColumnWidth() * 0.21).append("cm}\n");
        }
        // cols
        generateColumns(sheet, table);
        //rows

//        final short col_num = get_col_max(sheet);
        final int row_num = sheet.getLastRowNum() + 1;
        for (int i = 0 + rowStartProcess; i < row_num; i++) {
            Row row = sheet.getRow(i);
            short col_num = get_col_max_in_row((XSSFRow) row);
            processRow(table, (XSSFRow) row, sheet, col_num, sID, i);
        }

        container.appendChild(table);
    }

    private short get_col_max(XSSFSheet sheet) {
        short ans = -1;
        //rows
        Iterator<Row> rows = sheet.iterator();
        while (rows.hasNext()) {
            Row row = rows.next();
            if (row instanceof XSSFRow) {
                short c = (short) (row.getLastCellNum());
                if (ans < c) {
                    ans = c;
                }
            }
        }
        return ans;
    }

    private short get_col_max_in_row(XSSFRow row) {
        short ans = -1;
        //rows
        if (row != null) {
            ans = (short) (row.getLastCellNum());
        }
        return ans;
    }

    /**
     * generated
     * <code><col><code> tags.
     *
     * @param sheet
     * @param table container.
     */
    private void generateColumns(XSSFSheet sheet, Element table) {
        List<CTCols> colsList = sheet.getCTWorksheet().getColsList();
        MathContext mc = new MathContext(3);
        for (CTCols cols : colsList) {
            long oldLevel = 1;
            for (CTCol col : cols.getColArray()) {
                while (true) {
                    if (oldLevel == col.getMin()) {
                        break;
                    }
                    Element column = htmlDocumentFacade.createTableColumn();
                    // htmlDocumentFacade.addStyleClass(column, "col", "width:2cm;");
//                    column.setAttribute("style", "width:2cm;");
                    table.appendChild(column);
                    oldLevel++;
                }
                Element column = htmlDocumentFacade.createTableColumn();
                String width = new BigDecimal(getColumnWidth(sheet, col) / 1440.0, mc).toString();
//                column.setAttribute("style", "width:".concat(width).concat("cm;"));
                table.appendChild(column);

                oldLevel++;
            }
        }
    }

    public int getColumnWidth(XSSFSheet sheet, CTCol col) {
        double width = col != null && col.isSetWidth() ? col.getWidth() : (double) sheet.getDefaultColumnWidth();
        return (int) (width * 256.0D);
    }

    private void processRow(Element table, XSSFRow row, XSSFSheet sheet, final int col_num, String sID, int pos_row) {
        Element tr = htmlDocumentFacade.createTableRow();

        if (!(row instanceof XSSFRow)) {
            for (int pos_col = 0; pos_col < col_num; pos_col++) {
                processCell(tr, null, sID, pos_col, pos_row);  // empty line
            }
        } else {
            if (row.isFormatted()) {
                //TODO build row style...
            }

            if (row.getCTRow().getCustomHeight()) {
                tr.setAttribute("style", "height:".concat(String.valueOf(row.getHeightInPoints())).concat("pt;"));
            }

            for (int pos_col = 0; pos_col < col_num; pos_col++) {
                Cell cell = row.getCell(pos_col);
                if (cell instanceof XSSFCell) {
                    processCell(tr, (XSSFCell) cell, sID, pos_col, pos_row);
                } else {
                    processCell(tr, null, sID, pos_col, pos_row);
                }
            }
        }
        table.appendChild(tr);
    }

    private void processCell(Element tr, XSSFCell cell, String sID, int pos_col, int pos_row) {


        int cols = 1;
        int rows = 1;
        if (cell != null) {

            if (cell != null) {
                int num = cell.getSheet().getNumMergedRegions();
                // System.out.println(cell.getCTCell());
                for (int i = 0; i < num; i++) {

                    CellRangeAddress c = cell.getSheet().getMergedRegion(i);

                    //System.out.println(c.getFirstColumn());
                    //System.out.println(c.getLastColumn());
                    //System.out.println(c.getFirstRow());
                    //System.out.println(c.getLastRow());
                    //System.out.println();
                    //System.out.println(cell.getRowIndex());
                    //System.out.println(cell.getColumnIndex());
                    //System.out.println("\n\n\n");
                    // System.out.println(cra);
                    int x0 = c.getFirstColumn();
                    int x1 = c.getLastColumn();
                    int y0 = c.getFirstRow();
                    int y1 = c.getLastRow();
                    if (x0 == pos_col && y0 == pos_row) {
                        cols = c.getLastColumn() - c.getFirstColumn() + 1;
                        rows = c.getLastRow() - c.getFirstRow() + 1;
                    } else if ((x0 <= pos_col) && (pos_col <= x1) && (y0 <= pos_row) && (pos_row <= y1)) {
                        return;
                    }
                }
            }
        }

        Element td = htmlDocumentFacade.createTableCell();
        if (cols > 1) {
            td.setAttribute("colspan", "" + cols);
        }
        if (rows > 1) {
            td.setAttribute("rowspan", "" + rows);
        }
        Object value;
        if (cell == null) {
            // processCellStyle(td, cell.getCellStyle(), null);
            td.setTextContent("\u00a0");
        } else {
            NumberFormat formatter = null;
            int iCellType = cell.getCellType();
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_BLANK:
                    value = "\u00a0";
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if (cell.getCellStyle().getDataFormatString().equalsIgnoreCase("general")) {
                        formatter = new DecimalFormat("#,##0; -#,##0");
                        value = formatter.format(cell.getNumericCellValue());
                    } else {
                        formatter = new DecimalFormat(cell.getCellStyle().getDataFormatString());
                        value = formatter.format(cell.getNumericCellValue());
                    }
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    value = cell.getBooleanCellValue();
                    break;
                case Cell.CELL_TYPE_FORMULA:
//                    formatter = new DecimalFormat(cell.getCellStyle().getDataFormatString());
                    CellValue cellValue = evaluator.evaluate(cell);
                    iCellType = cellValue.getCellType();
                    switch (cellValue.getCellType()) {
                        case Cell.CELL_TYPE_BLANK:
                            value = "\u00a0";
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            formatter = new DecimalFormat(cell.getCellStyle().getDataFormatString());
                            value = formatter.format(cellValue.getNumberValue());
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            value = cellValue.getBooleanValue();
                            break;
                        default:
                            value = cellValue.getStringValue();
                            break;
                    }
//                    value = formatter.format(cellValue.getNumberValue());
//                    value = NumberToTextConverter.toText(cell.getNumericCellValue());
                    break;
                default:
                    value = cell.getRichStringCellValue();
                    break;
            }
            if (value instanceof XSSFRichTextString) {
                processCellStyle(td, cell.getCellStyle(), (XSSFRichTextString) value, sID, iCellType);
                td.setTextContent(value.toString());
            } else {
                processCellStyle(td, cell.getCellStyle(), null, sID, iCellType);
                td.setTextContent(value.toString());
            }
            // String s = value.toString();
            // System.out.println(s);
        }
        // System.err.println(value);
        tr.appendChild(td);
    }

    private StringBuilder alignmentByValueType(int iCellType) {
        StringBuilder result = new StringBuilder();
        switch (iCellType) {
            case Cell.CELL_TYPE_NUMERIC:
//                result.append("text-align:").append("right;");
                result.append("text-align:").append("center;");
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                result.append("text-align:").append("center;");
                break;
            default:
                result.append("text-align:").append("center;");
//                result.append("text-align:").append("left;");
        }
        return result;
    }

    private void processCellStyle(Element td, XSSFCellStyle style, XSSFRichTextString rts, String sID, int iCellType) {
        StringBuilder sb = new StringBuilder();

        if (rts != null) {
            XSSFFont font = null;
            if (rts.getFontOfFormattingRun(1) != null) {
                font = rts.getFontOfFormattingRun(1);
            } else {
                font = style.getFont();
            }
            if (font != null) {
                sb.append("font-family:").append(font.getFontName()).append(";");
                // sb.append("color:").append(font.getColor() ).append(";");
                sb.append("font-size:").append(font.getFontHeightInPoints()).append("pt;");
                if (font.getXSSFColor() != null) {
//                    String color = font.getXSSFColor().getARGBHex().substring(2);
//                    sb.append("color:#").append(color).append(";");
                }
                if (font.getItalic()) {
                    sb.append("font-style:italic;");
                }
                if (font.getBold()) {
                    sb.append("font-weight:").append(font.getBoldweight()).append(";");
                }
                if (font.getStrikeout()) {
                    sb.append("text-decoration:underline;");
                }
            }
        }
        if (style.getAlignment() != 1) {
            switch (style.getAlignment()) {
                case 2:
                    sb.append("text-align:").append("center;");
                    break;
                case 3:
                    sb.append("text-align:").append("right;");
                    break;
                case 0:
                    sb.append(alignmentByValueType(iCellType));

            }
        }

        // TODO:  set correct value for type and width of border.
        if (style.getBorderBottom() != 0) {
            sb.append("border-bottom: ").append(style.getBorderBottom()).append("px solid black;");
        }
        if (style.getBorderLeft() != 0) {
            sb.append("border-left: ").append(style.getBorderLeft()).append("px solid black;");
        }
        if (style.getBorderTop() != 0) {
            sb.append("border-top: ").append(style.getBorderTop()).append("px solid black;");
        }
        if (style.getBorderRight() != 0) {
            sb.append("border-right: ").append(style.getBorderRight()).append("px solid black;");
        }

        //if (style.getFillBackgroundXSSFColor() != null) {
        //    XSSFColor color = style.getFillBackgroundXSSFColor();
        //}

        // System.out.println(style.getFillBackgroundXSSFColor());
        if (style.getFillBackgroundXSSFColor() != null) {
//            sb.append("background:#fcc;");
        }
        td.setAttribute("style", sb.toString());
        // System.out.println(sb.toString());
        htmlDocumentFacade.addStyleClass(td, "td" + sID, sb.toString());
    }

    /**
     * @param document
     * @throws IOException
     * @throws TransformerException
     */
    private String saveAsHtml(Document document) throws IOException, TransformerException {
        String s = "";
        // check path
//        File folder = new File(getFilePath(output));
//        if (!folder.canRead()) {
//            folder.mkdirs();
//        }

        // FileWriter out = new FileWriter(output);
//        FileOutputStream fos = new FileOutputStream(output);
        DOMSource domSource = new DOMSource(document);
        StreamResult result = new StreamResult(new StringWriter());
        //StreamResult streamResult = new StreamResult(out);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        // TODO set encoding from a command argument
        // インデントを行う
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2");
        serializer.setOutputProperty(OutputKeys.METHOD, "html");
        //serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "");
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        serializer.transform(domSource, result);
        s = result.getWriter().toString();
//        OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
//        out.write(s);
//        out.close();
        return s;
    }

    public String getFilePath(String fileFullPath) {
        int sep = fileFullPath.lastIndexOf("\\") == -1 ? fileFullPath.lastIndexOf("/") : fileFullPath.lastIndexOf("\\");
        return fileFullPath.substring(0, sep + 1);
    }
}