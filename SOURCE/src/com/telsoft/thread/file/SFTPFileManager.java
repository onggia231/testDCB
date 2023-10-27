//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.telsoft.thread.file;

import com.telsoft.thread.ManageableThread;
import com.telsoft.thread.ParameterUtil;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import com.telsoft.util.WildcardFilter;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3DirectoryEntry;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import org.apache.commons.net.ftp.FTPFileEntryParser;

public class SFTPFileManager implements FileManager {
    private SFTPv3Client sftp = null;
    private Connection conn = null;
    private String strWorkingDir = null;
    private String mstrHost;
    private int miPort;
    private String mstrUser;
    private String mstrPassword;
    private int miTimeout;
    private String mstrPrivateKey;

    public SFTPFileManager() {
    }

    public void changeWorkingDirectory(String strPath) throws IOException {
        this.strWorkingDir = this.sftp.canonicalPath(strPath);
    }

    public void connect(String mstrHost, int miPort) throws SocketException, IOException {
        this.conn = new Connection(mstrHost, miPort);
        this.conn.connect();
    }

    public String getRealPath(String strPath) throws IOException {
        return strPath.startsWith("/") ? strPath : this.strWorkingDir + strPath;
    }

    public void deleteFile(String strPath) throws IOException {
        this.sftp.rm(this.getRealPath(strPath));
    }

    public void disconnect() throws IOException {
        if (this.sftp != null) {
            this.sftp.close();
        }

        this.conn.close();
        this.sftp = null;
        this.conn = null;
    }

    public FileEntry[] listFiles(String strPath) throws IOException {
        try {
            String strFilter = "";
            String strFullPath = this.getRealPath(strPath);

            try {
                SFTPv3FileAttributes attr = this.sftp.stat(strFullPath);
                if (attr.isRegularFile()) {
                    FileEntry fEntry = new FileEntry();
                    fEntry.filename = strPath;
                    fEntry.longEntry = "";
                    fEntry.size = attr.size;
                    fEntry.atime = attr.atime * 1000L;
                    fEntry.mtime = attr.mtime * 1000L;
                    fEntry.mbIsDirectory = attr.isDirectory();
                    fEntry.mbIsFile = attr.isRegularFile();
                    return new FileEntry[]{fEntry};
                }
            } catch (IOException var9) {
                String[] pathElement = strFullPath.split("/");
                if (pathElement[pathElement.length - 1].length() == 0) {
                    throw var9;
                }

                strFilter = pathElement[pathElement.length - 1];
                strFullPath = pathElement[pathElement.length - 2];
            }

            Vector<SFTPv3DirectoryEntry> vtFiles = this.sftp.ls(strFullPath);
            Vector<FileEntry> vtFile = new Vector();
            Iterator i$ = vtFiles.iterator();

            while(true) {
                SFTPv3DirectoryEntry f;
                do {
                    if (!i$.hasNext()) {
                        FileEntry[] returnFiles = new FileEntry[vtFile.size()];
                        vtFile.toArray(returnFiles);
                        return returnFiles;
                    }

                    f = (SFTPv3DirectoryEntry)i$.next();
                } while(strFilter.length() > 0 && !WildcardFilter.match(strFilter, f.filename));

                if (!f.filename.equals(".") && !f.filename.equals("..")) {
                    FileEntry fEntry = new FileEntry();
                    fEntry.filename = f.filename;
                    fEntry.longEntry = f.longEntry;
                    fEntry.size = f.attributes.size;
                    fEntry.atime = f.attributes.atime * 1000L;
                    fEntry.mtime = f.attributes.mtime * 1000L;
                    fEntry.mbIsDirectory = f.attributes.isDirectory();
                    fEntry.mbIsFile = f.attributes.isRegularFile();
                    vtFile.add(fEntry);
                }
            }
        } catch (IOException var10) {
            return null;
        }
    }

    public FileEntry[] listFiles(FTPFileEntryParser parser) throws IOException {
        return this.listFiles(this.strWorkingDir);
    }

    public void login(String mstrUser, String mstrPassword, Properties prop) throws IOException {
        this.conn.authenticateWithPassword(mstrUser, mstrPassword);
        this.sftp = new SFTPv3Client(this.conn);

        try {
            this.strWorkingDir = this.sftp.canonicalPath(".");
        } catch (IOException var5) {
            this.strWorkingDir = "/";
        }

        if (!this.strWorkingDir.endsWith("/")) {
            this.strWorkingDir = this.strWorkingDir + "/";
        }

    }

    public void makeDirectory(String strPath) throws IOException {
        this.sftp.mkdir(this.getRealPath(strPath), 511);
    }

    public void rename(String strSource, String strDest) throws IOException {
        this.sftp.mv(this.getRealPath(strSource), this.getRealPath(strDest));
    }

    public void retrieveFile(String strPath, FileOutputStream os) throws IOException {
        SFTPv3FileHandle file = this.sftp.openFileRO(this.getRealPath(strPath));

        try {
            long fileOffset = 0L;
            byte[] buf = new byte[8192];
            boolean var7 = false;

            int ireaden;
            do {
                ireaden = this.sftp.read(file, fileOffset, buf, 0, 8192);
                if (ireaden > 0) {
                    os.write(buf, 0, ireaden);
                }

                fileOffset += (long)ireaden;
            } while(ireaden > 0);
        } finally {
            this.sftp.closeFile(file);
        }

    }

    public void setTimeout(int miTimeout) {
    }

    public void storeFile(String strPath, FileInputStream is) throws IOException {
        SFTPv3FileHandle file = this.sftp.createFile(this.getRealPath(strPath));

        try {
            long fileOffset = 0L;
            byte[] buf = new byte[8192];
            boolean var7 = false;

            while(is.available() > 0) {
                int ireaden = is.read(buf);
                if (ireaden > 0) {
                    this.sftp.write(file, fileOffset, buf, 0, ireaden);
                    fileOffset += (long)ireaden;
                }
            }
        } finally {
            this.sftp.closeFile(file);
        }

    }

    public void forceChangeWorkingDirectory(String strPath) throws IOException {
        try {
            this.changeWorkingDirectory(strPath);
        } catch (IOException var7) {
            try {
                this.makeDirectory(strPath);
            } catch (IOException var6) {
                try {
                    this.changeWorkingDirectory(strPath);
                } catch (IOException var5) {
                    throw new IOException("Could not create foler " + strPath + " on sftp server");
                }
            }
        }

    }

    public void fillParameter(ManageableThread thread) throws AppException {
        this.mstrHost = thread.loadString("Host");
        this.miPort = thread.loadUnsignedInteger("Port");
        this.mstrUser = StringUtil.nvl(thread.getParameter("User"), "");
        this.mstrPassword = StringUtil.nvl(thread.getParameter("Password"), "");
        this.mstrPrivateKey = StringUtil.nvl(thread.getParameter("PrivateKeyFileName"), "Private key file name in PEM format");
        this.miTimeout = thread.loadUnsignedInteger("Timeout") * 1000;
    }

    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.add(ParameterUtil.createParameter("Host", "", 7, "/\\[]{}()`~!@#$%^&*;,?'\"\t ", ""));
        vtReturn.add(ParameterUtil.createParameter("Port", "", 1, "99990", ""));
        vtReturn.add(ParameterUtil.createParameter("User", "", 2, "256", ""));
        vtReturn.add(ParameterUtil.createParameter("Password", "", 3, "100", "Password to login into SSH Server or password of Public Key if Public Key is used"));
        vtReturn.add(ParameterUtil.createParameter("PrivateKeyFileName", "", 2, "999999", ""));
        vtReturn.add(ParameterUtil.createParameter("Timeout", "", 1, "99990", ""));
        return vtReturn;
    }

    public void connect() throws Exception {
        this.conn = new Connection(this.mstrHost, this.miPort);
        this.conn.connect();
        if (this.mstrPrivateKey.length() > 0) {
            this.conn.authenticateWithPublicKey(this.mstrUser, new File(this.mstrPrivateKey), this.mstrPassword);
        } else {
            this.conn.authenticateWithPassword(this.mstrUser, this.mstrPassword);
        }

        this.sftp = new SFTPv3Client(this.conn);

        try {
            this.strWorkingDir = this.sftp.canonicalPath(".");
        } catch (IOException var2) {
            this.strWorkingDir = "/";
        }

        if (!this.strWorkingDir.endsWith("/")) {
            this.strWorkingDir = this.strWorkingDir + "/";
        }

    }
}
