package com.apl.view;

import com.apl.ComboBoxAutoComplete;
import com.apl.MainApp;
import com.apl.model.ATTRecord;
import com.apl.model.DBConnection;
import com.apl.model.Field;
import com.apl.model.Form;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MainOverviewController {
    private static final Logger logger = Logger.getLogger(MainApp.class);
    @FXML
    private TextField connectionString = new TextField();
    private DBConnection dbConn = new DBConnection();
    @FXML
    private TableColumn<ATTRecord, String> entryIDColumn = new TableColumn();
    private boolean entryIDFolder = false;
    @FXML
    private TableView<ATTRecord> entryTable = new TableView();
    @FXML
    private ComboBox<String> fieldComboBox = new ComboBox();
    private boolean fieldNameFolder = false;
    @FXML
    private TableColumn<ATTRecord, String> fileNameColumn = new TableColumn();
    @FXML
    private TableColumn<ATTRecord, Integer> fileSizeColumn = new TableColumn();
    @FXML
    private ComboBox<String> formComboBox = new ComboBox();
    private boolean formFolder = true;
    private Map<String, Form> formMap;
    private MainApp mainApp;
    @FXML
    private TextField password = new TextField();
    @FXML
    private Label recordCountLabel = new Label();
    @FXML
    private TextField recordsQuery = new TextField();
    @FXML
    private Label statusBar = new Label();
    @FXML
    private TextField userName = new TextField();

    public String getConnectionString() {
        return this.connectionString.getText();
    }

    public DBConnection getDbConn() {
        return this.dbConn;
    }

    public String getPassword() {
        return this.password.getText();
    }

    public String getUserName() {
        return this.userName.getText();
    }

    @FXML
    private void handleBuildConnectionString() {
        this.mainApp.handleBuildConnectionString();
    }

    @FXML
    private void handleClearSelection() {
        this.entryTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleConnect() {
        try {
            this.statusBar.setText("Starting Connection Process");
            this.dbConn.getConnection(this.connectionString.getText(), this.userName.getText(), this.password.getText());
            this.openWindow(AlertType.INFORMATION, "Success", null, "Connection Succeeded");
            this.statusBar.setText("Loading Forms List");
            this.formMap = this.dbConn.getFormData(null);
            this.statusBar.setText("Forms List Loaded");
            this.formComboBox.getItems().clear();
            this.fieldComboBox.getItems().clear();
            this.recordsQuery.clear();
            this.entryTable.getItems().clear();
            this.formComboBox.setTooltip(new Tooltip());
            if (this.formMap != null) {
                for (String formName : this.formMap.keySet()) {
                    this.formComboBox.getItems().add(formName);
                }

                new ComboBoxAutoComplete(this.formComboBox);
                this.statusBar.setText("Ready To Start");
            } else {
                this.statusBar.setText("Error loading FormMap");
            }
        } catch (SQLException e) {
            this.openWindow(AlertType.ERROR, "Error", null, "Connection Failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleExecute() {
        this.entryTable.getItems().clear();
        Form form = this.formMap.get(this.formComboBox.getValue());
        ObservableList<ATTRecord> recordList = this.dbConn
                .getAttachmentRecords(
                        this.recordsQuery.getText(),
                        form.getID(),
                        form.getResolvedID(),
                        form.getField(this.fieldComboBox.getValue()).getId(),
                        this.formComboBox.getValue(),
                        this.fieldComboBox.getValue()
                );
        if (recordList != null) {
            this.entryTable.setItems(recordList);
            if (recordList.size() == 1) {
                this.recordCountLabel.setText(recordList.size() + " record");
            } else if (recordList.size() > 1) {
                this.recordCountLabel.setText(recordList.size() + " records");
            } else {
                this.recordCountLabel.setText("0 records");
            }
        }
    }

    @FXML
    private void handleFieldSelection() {
        this.recordsQuery
                .setText(this.dbConn.getAttachmentRecordQuery(this.formMap, this.formComboBox.getValue(), this.fieldComboBox.getValue(), null));
    }

    @FXML
    private void handleFormSelection() {
        Form form = this.formMap.get(this.formComboBox.getValue());
        List<Field> fieldList = null;
        if (form != null) {
            fieldList = form.getFieldList();
        }

        this.fieldComboBox.getItems().clear();
        this.recordsQuery.clear();
        this.entryTable.getItems().clear();
        if (fieldList != null) {
            for (Field field : fieldList) {
                this.fieldComboBox.getItems().add(field.getName());
            }
        }
    }

    @FXML
    private void handleSaveBulk() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/BulkExport.fxml"));
            AnchorPane page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Bulk Export");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            BulkExportController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            if (controller.getOkClicked()) {
                this.formFolder = controller.isFormFolder();
                this.entryIDFolder = controller.isEntryIDFolder();
                this.fieldNameFolder = controller.isFieldNameFolder();
                this.saveFiles(true);
            }
        } catch (IOException e) {
            logger.error("IOException opening Bulk Export dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleSavePrompt() {
        this.saveFiles(false);
    }

    @FXML
    private void handleSelectAll() {
        this.entryTable.getSelectionModel().selectAll();
    }

    @FXML
    private void initialize() {
        this.entryIDColumn.setCellValueFactory(cellData -> cellData.getValue().getEntryID());
        this.fileNameColumn.setCellValueFactory(cellData -> cellData.getValue().getFileName());
        this.fileSizeColumn.setCellValueFactory(cellData -> cellData.getValue().getOriginalSize());
        this.entryTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void openWindow(AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.initOwner(this.mainApp.getPrimaryStage());
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

    private void saveFiles(boolean bulk) {
        this.statusBar.setText("Export Process Starting");
        ObservableList<ATTRecord> selectedItems = this.entryTable.getSelectionModel().getSelectedItems();
        File selectedDirectory = null;
        if (bulk) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory == null) {
                return;
            }
        }

        String output = MainApp.processExport(selectedItems, this.dbConn, bulk, selectedDirectory, this.formFolder, this.entryIDFolder, this.fieldNameFolder);
        if (output != null && !output.isEmpty()) {
            this.openWindow(AlertType.ERROR, "Error", null, output);
        }

        this.statusBar.setText("Export Complete");
    }

    public void setConnectionString(TextField connectionString) {
        this.connectionString.setText(connectionString.getText());
    }

    public void setDbConn(DBConnection dbConn) {
        this.dbConn = dbConn;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setPassword(TextField password) {
        this.password.setText(password.getText());
    }

    public void setUserName(TextField userName) {
        this.userName.setText(userName.getText());
    }
}
