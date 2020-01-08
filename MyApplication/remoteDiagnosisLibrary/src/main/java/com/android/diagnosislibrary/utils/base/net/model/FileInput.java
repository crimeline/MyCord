package com.android.diagnosislibrary.utils.base.net.model;

import java.io.File;

/**
 * Created by Administrator on 2017/6/5.
 */

public class FileInput {
    public String key;
    public String filedir;
    public String filename;
    public File file;

    public FileInput(String name, File file) {
        this.key = name;
        this.file = file;
    }

    public FileInput(String dir, String name) {
        this.filedir = dir;
        this.filename = name;
    }

    public String toString() {
        return "FileInput{key=\'" + this.key + '\'' + ", filedir=\'" + this.filedir + '\'' + ", filename=\'" + this.filename + '\'' + ", file=" + this.file + '}';
    }
}
