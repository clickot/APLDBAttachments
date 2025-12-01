package com.apl.view;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

public class BulkExportController {
    @FXML
    private RadioButton formFolderRadio = new RadioButton();
    @FXML
    private RadioButton formFileRadio = new RadioButton();
    @FXML
    private RadioButton entryIDFolderRadio = new RadioButton();
    @FXML
    private RadioButton entryIDFileRadio = new RadioButton();
    @FXML
    private RadioButton fieldNameFolderRadio = new RadioButton();
    @FXML
    private RadioButton fieldNameFileRadio = new RadioButton();
    private Stage dialogStage;
    private boolean okClicked = false;
    private boolean formFolder = true;
    private boolean entryIDFolder = false;
    private boolean fieldNameFolder = false;

    @FXML
    private void initialize() {
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleFormSelection() {
        if (this.formFolderRadio.isSelected()) {
            this.formFolder = true;
        } else {
            this.formFolder = false;
            this.entryIDFolder = false;
            this.fieldNameFolder = false;
            this.entryIDFileRadio.setSelected(true);
            this.fieldNameFileRadio.setSelected(true);
        }
    }

    @FXML
    private void handleEntryIDSelection() {
        if (this.entryIDFolderRadio.isSelected()) {
            this.formFolder = true;
            this.entryIDFolder = true;
            this.formFolderRadio.setSelected(true);
        } else {
            this.entryIDFolder = false;
            this.fieldNameFolder = false;
            this.fieldNameFileRadio.setSelected(true);
        }
    }

    @FXML
    private void handleFieldNameSelection() {
        if (this.fieldNameFolderRadio.isSelected()) {
            this.formFolder = true;
            this.entryIDFolder = true;
            this.fieldNameFolder = true;
            this.formFolderRadio.setSelected(true);
            this.entryIDFolderRadio.setSelected(true);
        } else {
            this.fieldNameFolder = false;
        }
    }

    @FXML
    private void handleOk() {
        this.okClicked = true;
        this.dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        this.dialogStage.close();
    }

    public boolean getOkClicked() {
        return this.okClicked;
    }

    public boolean isFormFolder() {
        return this.formFolder;
    }

    public boolean isEntryIDFolder() {
        return this.entryIDFolder;
    }

    public boolean isFieldNameFolder() {
        return this.fieldNameFolder;
    }
}
