package com.telsoft.cbs.encryption;

import com.telsoft.cbs.domain.Folder;
import com.telsoft.cbs.domain.FolderStructure;
import com.telsoft.thread.ManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ThreadConstant;
import com.telsoft.util.*;

import java.io.File;
import java.util.*;

/**
 * Tiến trình xử lý file
 * - Đọc danh sách file từ một thư mục input
 * - Copy file sang thư mục tạm
 * - Xử lý file tạm(convert, mã hóa, nén,...)
 * - Xử lý thành công:
 * Viết file kết quả ra thư mục output
 * Move file đầu vào ra thư mục backup
 * Xóa các file tạm
 * - Xử lý không thành công
 * Move file đầu vào ra thư mục rejected
 * Xóa các file tạm
 * - Ghi log (ra màn hình) kết quả xử lý
 * <p>
 * ** Các thư mục có thể lưu theo cây ngày tháng
 */

public abstract class FileProcessingThread extends ManageableThread {
    private Folder inputFolder = new Folder();
    private Folder backupFolder = new Folder();
    private Folder rejectFolder = new Folder();
    private Folder outputFolder = new Folder();
    private Folder tempFolder = new Folder();

    private String processDate;
    private String wildcard;
    private String dateFormat;
    private String outputFileFormat;

    @Override
    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();

        vtReturn.add(createParameter("InputDirName", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(createParameter("InputDirStructure", "", ParameterType.PARAM_COMBOBOX, FolderStructure.class, ""));
        vtReturn.add(createParameter("ProcessDate", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(createParameter("Wildcard", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));


        vtReturn.add(createParameter("OutputDirName", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(createParameter("OutputDirStructure", "", ParameterType.PARAM_COMBOBOX, FolderStructure.class, ""));
        vtReturn.add(createParameter("OutputFileFormat", "", ParameterType.PARAM_TEXTBOX_MAX, "256", "Format output file name.\n" +
                " Can use $FileName,$BaseFileName,$FileExtension as parameter"));

        vtReturn.add(createParameter("BackupDirName", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(createParameter("BackupDirStructure", "", ParameterType.PARAM_COMBOBOX, FolderStructure.class, ""));

        vtReturn.add(createParameter("RejectedDirName", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(createParameter("RejectedDirStructure", "", ParameterType.PARAM_COMBOBOX, FolderStructure.class, ""));

        vtReturn.add(createParameter("TempDirName", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));

        vtReturn.add(createParameter("DateFormat", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));

        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    @Override
    public void fillParameter() throws AppException {
        super.fillParameter();
        inputFolder.setName(loadDirectory("InputDirName", true, true));
        inputFolder.setStructure(FolderStructure.valueOf(loadString("InputDirStructure")));
        processDate = loadString("ProcessDate");
        wildcard = loadString("Wildcard");

        outputFolder.setName(loadDirectory("OutputDirName", true, true));
        outputFolder.setStructure(FolderStructure.valueOf(loadString("OutputDirStructure")));
        outputFileFormat = loadString("OutputFileFormat");

        backupFolder.setName(loadDirectory("BackupDirName", true, true));
        backupFolder.setStructure(FolderStructure.valueOf(loadString("BackupDirStructure")));


        rejectFolder.setName(loadDirectory("RejectedDirName", true, true));
        rejectFolder.setStructure(FolderStructure.valueOf(loadString("RejectedDirStructure")));


        tempFolder.setName(loadDirectory("TempDirName", true, true));

        dateFormat = loadString("DateFormat");
    }


    @Override
    protected void processSession() throws Exception {
        List<File> fileList = listFile();

        if (fileList != null && fileList.size() > 0) {
            this.beforeProcessFileList(fileList);

            for (File file : fileList) {
                if (this.miThreadCommand == ThreadConstant.THREAD_STOPPED) {
                    break;
                }
                this.process(file);
            }

            this.afterProcessFileList(fileList);
        } else if (this.miThreadCommand != ThreadConstant.THREAD_STOPPED) {
            this.changeProcessDate();
        }
    }

    protected List<File> listFile() {
        String mstrScanDir;

        if (inputFolder.getStructure() != null && inputFolder.getStructure() != FolderStructure.Directly) {
            mstrScanDir = inputFolder.getName() + this.processDate + "/";
        } else {
            mstrScanDir = inputFolder.getName();
        }

        File fl = new File(mstrScanDir);
        File[] strFileList = fl.listFiles(new WildcardFilter(this.wildcard));
        if (strFileList != null && strFileList.length > 0) {
            Arrays.sort(strFileList);
            return Arrays.asList(strFileList);
        }
        return new ArrayList<File>();
    }

    protected void changeProcessDate() throws Exception {
        if (inputFolder.getStructure() != null && inputFolder.getStructure() != FolderStructure.Directly) {
            Date dt = DateUtil.toDate(this.processDate, this.dateFormat);

            do {
                switch (inputFolder.getStructure()) {
                    case Daily:
                        dt = DateUtil.addDay(dt, 1);
                        break;
                    case Monthly:
                        dt = DateUtil.addMonth(dt, 1);
                        break;
                    case Yearly:
                        dt = DateUtil.addYear(dt, 1);
                        break;
                }

                String strNextProcessDate = StringUtil.format(dt, this.dateFormat);
                File fl = new File(this.inputFolder.getName() + strNextProcessDate + "/");
                if (fl.exists() && fl.isDirectory()) {
                    this.processDate = strNextProcessDate;
                    this.setParameter("ProcessDate", this.processDate);
                    this.storeConfig();
                    return;
                }
            } while (dt.getTime() < System.currentTimeMillis());
        }
    }

    protected abstract void processFile(String inputFilename, String outputFilename) throws Exception;

    protected abstract void beforeProcessFile(String inputFilename);

    protected abstract void afterProcessFile(String inputFilename);

    protected abstract void logProcessed(String inputFilename, String outputFilename, boolean success, long duration, String reason);

    protected void process(File file) throws Exception {
        beforeProcessFile(file.getName());
        File tempInputFile = new File(this.tempFolder.getName() + file.getName());
        File tempOutputFile = new File(this.tempFolder.getName() + file.getName() + ".out~");
        long t = System.currentTimeMillis();
        try {
            FileUtil.copyFile(this.inputFolder.getName() + file.getName(), this.tempFolder.getName() + file.getName());

            processFile(tempInputFile.getAbsolutePath(), tempOutputFile.getAbsolutePath());

            if (this.miThreadCommand == ThreadConstant.THREAD_STOPPED) {
                throw new AppException("The processing is interrupted by administrator");
            }

            FileUtil.deleteFile(tempInputFile.getAbsolutePath());
            FileUtil.backup(inputFolder.getName(), backupFolder.getName(), file.getName(), file.getName(), backupFolder.getStructure().name());
            FileUtil.backup(tempOutputFile.getParentFile().getAbsolutePath() + File.separator, outputFolder.getName(), tempOutputFile.getName(), FileUtil.formatFileName(file.getName(), outputFileFormat), outputFolder.getStructure().name());
            logProcessed(file.getName(), file.getName(), true, System.currentTimeMillis() - t, "Success");
        } catch (Exception var14) {
            FileUtil.deleteFile(tempInputFile.getAbsolutePath());
            FileUtil.deleteFile(tempOutputFile.getAbsolutePath());
            FileUtil.backup(this.inputFolder.getName(), this.rejectFolder.getName(), file.getName(), file.getName(), this.rejectFolder.getStructure().name());
            logProcessed(file.getName(), file.getName(), false, System.currentTimeMillis() - t, var14.getMessage());
        } finally {
            afterProcessFile(file.getName());
        }
    }

    protected void beforeProcessFileList(List<File> fileList) throws Exception {
    }

    protected void afterProcessFileList(List<File> fileList) throws Exception {
    }
}
