package com.vedantu.ext.cmds.managers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.vedantu.ext.cmds.utils.commons.FileMaskProcessor;

/*
 * Takes the encrypted key and writes data encrypted and as well line by line
 */
public class JSONFileWriter extends BufferedWriter {

    private String            key;
    private FileMaskProcessor processor;

    public JSONFileWriter(File file) throws IOException {

        this(file, null);
    }

    public JSONFileWriter(File file, String key) throws IOException {

        super(new FileWriter(file));
        this.key = key;
        if (key != null) {
            processor = new FileMaskProcessor(key, key.getBytes().length);
        }
    }

    public void write(String value, boolean newLine) throws IOException {

        String writableValue = value;
        if (key != null) {
            // encrypt here
            writableValue = processor.process(value);
            writableValue = new sun.misc.BASE64Encoder().encode(writableValue.getBytes());
            writableValue = writableValue.replaceAll("\\s+", "");
        }
        super.write(writableValue);
        if (newLine) {
            super.newLine();
        }
    }
}
