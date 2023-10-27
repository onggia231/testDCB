package com.telsoft.cbs.utils;

import com.opencsv.CSVWriter;
import com.telsoft.util.StringUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viz on 28/10/2016.
 */
public class ExportCSVFile {

    private FileWriter mFw = null;
    private char mstrSeparator = CSVWriter.DEFAULT_SEPARATOR;
    private String mstrLineEnd = CSVWriter.DEFAULT_LINE_END;
    private char mcQuote = CSVWriter.DEFAULT_QUOTE_CHARACTER;
    private boolean mbForceQuote = false;
    private int miSuccessRecords = 0;
    private int miErrorRecords = 0;

    public ExportCSVFile() throws Exception
    {
    }

    public void newFile(String strFileName) throws Exception
    {
        try
        {
            mFw = new FileWriter(strFileName);
        }
        catch (Exception ex)
        {
            closeFile();
            throw new Exception("Error creating new file: " + ex.getMessage());
        }
    }

    public void closeFile() throws Exception
    {
        if (mFw != null)
        {
            mFw.close();
        }
    }

    public void writeResultSet(ResultSet rsData, boolean bIncludeColumnNames) throws Exception
    {
        CSVWriter writer = new CSVWriter(mFw,mstrSeparator,mcQuote,mstrLineEnd);
        miSuccessRecords = writer.writeAll(rsData,bIncludeColumnNames);
        writer.flush();
    }

    public ArrayList writeResultSetAndUpdateBack(ResultSet rsData, boolean bIncludeHeaderColumns) throws Exception
    {
        try
        {
            ResultSetMetaData rsmd = rsData.getMetaData();
            int iColumnCount = rsmd.getColumnCount();
            if(bIncludeHeaderColumns){
                List<String> listHeader = new ArrayList<>();
                for(int i = 1; i < iColumnCount; i++){
                    listHeader.add(rsmd.getColumnName(i));
                }
                CSVUtils.writeLine(mFw,listHeader,mstrSeparator,mcQuote,mbForceQuote,mstrLineEnd);
            }
            ArrayList arrRowId = new ArrayList();
            int miTotalRecords = 0;
            while (rsData.next())
            {
                miTotalRecords++;
                try
                {
                    List<String> listRow = new ArrayList<>();
                    for (int i = 0; i < iColumnCount; i++)
                    {
                        String strData = StringUtil.nvl(rsData.getString(i + 1), "");
                        if (i == 0)
                        {
                            arrRowId.add(strData);
                            continue;
                        }
                        else{
                            listRow.add(strData);
                        }

                    }
                    CSVUtils.writeLine(mFw,listRow,mstrSeparator,mcQuote,mbForceQuote,mstrLineEnd);
                    miSuccessRecords++;
                }
                catch (IOException ex1)
                {
                    miErrorRecords++;
                }
            }
            if (miTotalRecords > 0)
            {
                mFw.flush();
            }
            return arrRowId;
        }
        catch (Exception ex)
        {
            throw new Exception("Could not write file content: " + ex.getMessage());
        }
    }

    public char getMstrSeparator() {
        return mstrSeparator;
    }

    public void setMstrSeparator(char mstrSeparator) {
        this.mstrSeparator = mstrSeparator;
    }

    public String getMstrLineEnd() {
        return mstrLineEnd;
    }

    public void setMstrLineEnd(String mstrLineEnd) {
        this.mstrLineEnd = mstrLineEnd;
    }

    public char getMcQuote() {
        return mcQuote;
    }

    public void setMcQuote(char mcQuote) {
        this.mcQuote = mcQuote;
    }

    public boolean isMbForceQuote() {
        return mbForceQuote;
    }

    public void setMbForceQuote(boolean mbForceQuote) {
        this.mbForceQuote = mbForceQuote;
    }

    public int getMiSuccessRecords() {
        return miSuccessRecords;
    }

    public void setMiSuccessRecords(int miSuccessRecords) {
        this.miSuccessRecords = miSuccessRecords;
    }

    public int getMiErrorRecords() {
        return miErrorRecords;
    }

    public void setMiErrorRecords(int miErrorRecords) {
        this.miErrorRecords = miErrorRecords;
    }
}
