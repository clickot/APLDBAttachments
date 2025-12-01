package com.apl.model;

import com.apl.MainApp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DBConnection {
    private static final Logger logger = LogManager.getLogger(DBConnection.class);

    private String connectionString = "";
    private Connection dbConn = null;
    private String pass = "";
    private String userName = "";

    public ResultSet getAttachmentBytes(int formID, int fieldID, String entryID) {
        String query = "select c" + fieldID + " from b" + formID + "c" + fieldID + " where entryID = '" + entryID + "'";
        logger.debug("Attachment SQL: " + query);

        try {
            Statement statement = this.dbConn.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            if (!MainApp.cli) {
                this.openAlert(AlertType.ERROR, "Error", null, "SQL Failed: " + e.getMessage());
            }

            logger.error("SQLException: " + e.getMessage());
            return null;
        }
    }

    public String getAttachmentRecordQuery(Map<String, Form> formMap, String formName, String fieldName, String whereClause) {
        String query = "";
        int formID = 0;
        int fieldID = 0;
        Form form = null;
        if (formName != null && !formName.isEmpty()) {
            form = formMap.get(formName);
            if (form.getID() == form.getResolvedID()) {
                formID = form.getID();
            } else {
                formID = form.getResolvedID();
            }

            if (fieldName != null && !fieldName.isEmpty()) {
                fieldID = form.getField(fieldName).getId();
                if (formID > 0 && fieldID > 0) {
                    query = "select b.c1 AttReqID, b.c"
                            + fieldID
                            + " AttFileName, b.co"
                            + fieldID
                            + " AttSize from b"
                            + formID
                            + " b inner join t"
                            + formID
                            + " t on t.c1 = b.c1";
                    if (whereClause != null && !whereClause.trim().isEmpty()) {
                        query = query + " where " + whereClause;
                    }
                }

                return query;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public ObservableList<ATTRecord> getAttachmentRecords(String query, int formID, int resolvedFormID, int fieldID, String formName, String fieldName) {
        ObservableList<ATTRecord> recordList = FXCollections.observableArrayList();

        try {
            Statement statement = this.dbConn.createStatement();
            logger.debug("Query being executed: " + query);
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String entryID = resultSet.getString(1);
                String fileName = resultSet.getString(2);
                int fileSize = resultSet.getInt(3);
                if (fileName != null && !fileName.isEmpty() && fileSize > 0) {
                    ATTRecord attRecord = new ATTRecord(entryID, fileName, fileSize, formID, resolvedFormID, fieldID, formName, fieldName);
                    recordList.add(attRecord);
                }
            }

            resultSet.close();
            return recordList;
        } catch (SQLException e) {
            if (!MainApp.cli) {
                this.openAlert(AlertType.ERROR, "Error", null, "SQL Failed: " + e.getMessage());
            }

            logger.error("SQLException: " + e.getMessage(), e);
            return null;
        }
    }

    public void getConnection(String connectionString, String userName, String pass) throws SQLException {
        if (!connectionString.equals(this.connectionString) || !userName.equals(this.userName) || !pass.equals(this.pass)) {
            this.dbConn = DriverManager.getConnection(connectionString, userName, pass);
            this.connectionString = connectionString;
            this.userName = userName;
            this.pass = pass;
        }
    }

    private ResultSet getFieldData(String formName) {
        String query = "select distinct f.fieldID, f.fieldName from arschema a inner join field f on a.schemaid = f.schemaId inner join field_attach fa on a.schemaId = fa.schemaId and f.fieldId = fa.fieldId where a.name = '"
                + formName
                + "' and f.fOption < 4"
                + " order by f.fieldName";
        logger.debug("Issuing field query: " + query);

        try {
            return this.dbConn.createStatement().executeQuery(query);
        } catch (SQLException e) {
            if (!MainApp.cli) {
                this.openAlert(AlertType.ERROR, "Error", null, "SQL Failed: " + e.getMessage());
            }

            logger.error("SQLException: " + e.getMessage(), e);
            return null;
        }
    }

    public Map<String, Form> getFormData(List<String> formNameList) {
        String query = "select distinct a.name, a.schemaid, a.resolvedschemaid from arschema a inner join field f on a.schemaid = f.schemaId inner join field_attach fa on a.schemaId = fa.schemaId and f.fieldId = fa.fieldId where a.schemaType = 1 and f.fOption < 4 ";
        if (formNameList != null && formNameList.size() > 0) {
            String s = "";

            for (String formName : formNameList) {
                s = s + ",'" + formName + "'";
            }

            s = s.substring(1);
            query = query + "and a.name in (" + s + ")";
        }

        query = query + " order by a.name";
        logger.debug("Issuing form query: " + query);
        Statement statement = null;

        try {
            statement = this.dbConn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            Map<String, Form> formMap = new LinkedHashMap<>();
            int formCount = 0;

            while (true) {
                if (!resultSet.next()) {
                    logger.debug("Form Query returned " + formCount + " records");
                    return formMap;
                }

                formCount++;
                String formName = resultSet.getString(1);
                int formID = resultSet.getInt(2);
                int resolvedFormID = resultSet.getInt(3);
                Form form = new Form(formName, formID, resolvedFormID);
                ResultSet fieldResults = this.getFieldData(formName);
                if (fieldResults == null) {
                    if (!MainApp.cli) {
                        this.openAlert(AlertType.ERROR, "Error", null, "Unable to get field list");
                    }

                    logger.error("Unable to get field list");
                    return null;
                }

                try {
                    int fieldCount = 0;

                    while (fieldResults.next()) {
                        fieldCount++;
                        int fieldID = fieldResults.getInt(1);
                        String fieldName = fieldResults.getString(2);
                        form.addField(fieldName, fieldID);
                    }

                    logger.debug("Field Query returned " + fieldCount + " records");
                } catch (SQLException e) {
                    if (!MainApp.cli) {
                        this.openAlert(AlertType.ERROR, "Error", null, "SQL Failed: " + e.getMessage());
                    }
                    logger.error("SQL Exception while getting field Results:" + e.getMessage(), e);
                    break;
                }

                formMap.put(formName, form);
            }
        } catch (SQLException e) {
            if (!MainApp.cli) {
                this.openAlert(AlertType.ERROR, "Error", null, "SQL Failed: " + e.getMessage());
            }

            logger.error("SQLException: " + e.getMessage(), e);
            return null;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }

        return null;
    }

    private void openAlert(AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        if (title != null) {
            alert.setTitle(title);
        }

        if (header != null) {
            alert.setHeaderText(header);
        }

        if (content != null) {
            alert.setContentText(content);
        }

        alert.showAndWait();
    }
}
