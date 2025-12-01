package com.apl.model;

import com.bmc.arsys.arcompress.ARDecompressor;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.zip.DataFormatException;

public class ATTRecord {
    private static final Logger logger = LogManager.getLogger(ATTRecord.class);

    public StringProperty entryID;
    public StringProperty fileName;
    private final int formID;
    private final int resolvedFormID;
    private final int fieldID;
    private final String formName;
    private final String fieldName;
    public ObservableValue<Integer> originalSize;

    ATTRecord(String entryID, String fileName, int originalSize, int formID, int resolvedFormID, int fieldID, String formName, String fieldName) {
        this.entryID = new SimpleStringProperty(entryID);
        this.originalSize = new ReadOnlyObjectWrapper<>(originalSize);
        this.formID = formID;
        this.resolvedFormID = resolvedFormID;
        this.fieldID = fieldID;
        this.formName = formName;
        this.fieldName = fieldName;
        if (fileName.contains(File.separator)) {
            fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
        }

        this.fileName = new SimpleStringProperty(fileName);
    }

    public byte[] getBytes(DBConnection dbConn) {
        byte[] bytes = null;
        int formID = this.formID;
        if (formID != this.resolvedFormID) {
            formID = this.resolvedFormID;
        }

        ResultSet byteResults = dbConn.getAttachmentBytes(formID, this.fieldID, this.entryID.getValue());

        try {
            while (byteResults.next()) {
                bytes = byteResults.getBytes(1);
                if (bytes != null) {
                    ARDecompressor arDecompressor = new ARDecompressor();
                    arDecompressor.setInput(bytes);

                    try {
                        bytes = arDecompressor.inflate();
                    } catch (DataFormatException e) {
                        logger.error("Error Decompressing: " + e.getMessage());
                        bytes = new byte[0];
                    }
                } else {
                    logger.error("Bytes null");
                    bytes = new byte[0];
                }
            }

            byteResults.close();
        } catch (SQLException e) {
            logger.error("error in getBytes", e);
        }

        return bytes;
    }

    public StringProperty getEntryID() {
        return this.entryID;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public StringProperty getFileName() {
        return this.fileName;
    }

    public String getFormName() {
        return this.formName;
    }

    public ObservableValue<Integer> getOriginalSize() {
        return this.originalSize;
    }

    public void setEntryID(String entryID) {
        this.entryID = new SimpleStringProperty(entryID);
    }

    public void setFileName(String fileName) {
        this.fileName = new SimpleStringProperty(fileName);
    }

    public void setOriginalSize(int originalSize) {
        this.originalSize = new ReadOnlyObjectWrapper(originalSize);
    }
}
