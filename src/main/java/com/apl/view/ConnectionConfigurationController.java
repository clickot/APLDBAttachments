package com.apl.view;

import com.apl.model.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ConnectionConfigurationController {
    @FXML
    private TextField connectionString = new TextField();
    @FXML
    private TextField databaseName = new TextField();
    @FXML
    private Label databaseNameLabel = new Label();
    @FXML
    private ComboBox<String> dbTypeComboBox = new ComboBox();
    private Stage dialogStage;
    private boolean okClicked = false;
    @FXML
    private RadioButton oracleService = new RadioButton();
    @FXML
    private RadioButton oracleSID = new RadioButton();
    @FXML
    private ToggleGroup oracleTypeGroup = new ToggleGroup();
    @FXML
    private TextField password = new TextField();
    @FXML
    private TextField serverDetails = new TextField();
    @FXML
    private Label serverDetailsLabel = new Label();
    @FXML
    private TextField serverName = new TextField();
    @FXML
    private TextField serverPort = new TextField();
    @FXML
    private TextField userName = new TextField();

    private void buildConnectionString() {
        String s = "";
        if (!this.dbTypeComboBox.getSelectionModel().isEmpty()) {
            if (this.dbTypeComboBox.getSelectionModel().getSelectedItem().equals("SQL Server")) {
                s = "jdbc:sqlserver://";
                if (!this.serverName.getText().trim().isEmpty()) {
                    s = s + this.serverName.getText();
                }

                if (!this.serverPort.getText().trim().isEmpty()) {
                    s = s + ":" + this.serverPort.getText();
                }

                if (!this.databaseName.getText().trim().isEmpty()) {
                    s = s + ";databaseName=" + this.databaseName.getText();
                }

                if (!this.serverDetails.getText().trim().isEmpty()) {
                    s = s + ";instance=" + this.serverDetails.getText();
                }
            } else if (this.dbTypeComboBox.getSelectionModel().getSelectedItem().equals("Oracle")) {
                s = "jdbc:oracle:thin:";
                if (!this.serverName.getText().trim().isEmpty()) {
                    if (this.oracleSID.isSelected()) {
                        s = s + "@" + this.serverName.getText();
                    } else if (this.oracleService.isSelected()) {
                        s = s + "@//" + this.serverName.getText();
                    }
                }

                if (!this.serverPort.getText().trim().isEmpty()) {
                    s = s + ":" + this.serverPort.getText();
                }

                if (!this.serverDetails.getText().trim().isEmpty()) {
                    if (this.oracleSID.isSelected()) {
                        s = s + ":" + this.serverDetails.getText();
                    } else if (this.oracleService.isSelected()) {
                        s = s + "/" + this.serverDetails.getText();
                    }
                }
            } else if (this.dbTypeComboBox.getSelectionModel().getSelectedItem().equals("PostgreSQL")) {
                s = "jdbc:postgresql://";
                if (!this.serverName.getText().trim().isEmpty()) {
                    s = s + this.serverName.getText();
                }

                if (!this.serverPort.getText().trim().isEmpty()) {
                    s = s + ":" + this.serverPort.getText();
                }

                if (!this.databaseName.getText().trim().isEmpty()) {
                    s = s + "/" + this.databaseName.getText();
                }
            }
        }

        this.connectionString.setText(s);
    }

    public TextField getConnectionString() {
        return this.connectionString;
    }

    public boolean getOkClicked() {
        return this.okClicked;
    }

    public TextField getPassword() {
        return this.password;
    }

    public TextField getUserName() {
        return this.userName;
    }

    @FXML
    private void handleCancel() {
        this.dialogStage.close();
    }

    @FXML
    private void handleDataChange() {
        this.buildConnectionString();
    }

    @FXML
    private void handleDBType() {
        this.serverDetailsLabel.setVisible(true);
        if (this.dbTypeComboBox.getSelectionModel().getSelectedItem().equals("SQL Server")) {
            this.databaseNameLabel.setVisible(true);
            this.databaseName.setVisible(true);
            this.serverDetailsLabel.setText("Server Instance");
            this.serverDetailsLabel.setVisible(true);
            this.serverDetails.setVisible(true);
            this.oracleSID.setVisible(false);
            this.oracleService.setVisible(false);
            if (this.serverPort.getText().isEmpty() || this.serverPort.getText().equals("1521") || this.serverPort.getText().equals("5432")) {
                this.serverPort.setText("1433");
            }
        } else if (this.dbTypeComboBox.getSelectionModel().getSelectedItem().equals("Oracle")) {
            this.databaseNameLabel.setVisible(false);
            this.databaseName.setVisible(false);
            this.handleOracleTypeSelect();
            this.oracleSID.setVisible(true);
            this.oracleService.setVisible(true);
            this.serverDetails.setVisible(true);
            this.serverDetailsLabel.setVisible(true);
            if (this.serverPort.getText().isEmpty() || this.serverPort.getText().equals("1433") || this.serverPort.getText().equals("5432")) {
                this.serverPort.setText("1521");
            }
        } else if (this.dbTypeComboBox.getSelectionModel().getSelectedItem().equals("PostgreSQL")) {
            this.databaseNameLabel.setVisible(true);
            this.databaseName.setVisible(true);
            this.serverDetails.setVisible(false);
            this.serverDetailsLabel.setVisible(false);
            this.oracleSID.setVisible(false);
            this.oracleService.setVisible(false);
            if (this.serverPort.getText().isEmpty() || this.serverPort.getText().equals("1433") || this.serverPort.getText().equals("1521")) {
                this.serverPort.setText("5432");
            }
        }

        this.buildConnectionString();
    }

    @FXML
    private void handleOk() {
        this.okClicked = true;
        this.dialogStage.close();
    }

    @FXML
    private void handleOracleTypeSelect() {
        if (this.oracleSID.isSelected()) {
            this.serverDetailsLabel.setText("SID");
        }

        if (this.oracleService.isSelected()) {
            this.serverDetailsLabel.setText("Service");
        }

        this.buildConnectionString();
    }

    @FXML
    private void handleTestConnection() {
        try {
            DBConnection dbConn = new DBConnection();
            dbConn.getConnection(this.connectionString.getText(), this.userName.getText(), this.password.getText());
            this.openAlert(AlertType.INFORMATION, "Success", null, "Connection Succeeded");
        } catch (SQLException e) {
            this.openAlert(AlertType.ERROR, "Error", null, "Connection Failed: " + e.getMessage());
        }
    }

    @FXML
    private void initialize() {
        this.dbTypeComboBox.getItems().add("SQL Server");
        this.dbTypeComboBox.getItems().add("Oracle");
        this.dbTypeComboBox.getItems().add("PostgreSQL");
        this.databaseNameLabel.setVisible(false);
        this.databaseName.setVisible(false);
        this.serverDetailsLabel.setVisible(false);
        this.serverDetails.setVisible(false);
        this.oracleSID.setVisible(false);
        this.oracleService.setVisible(false);
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

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        this.dialogStage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (KeyCode.ESCAPE == event.getCode()) {
                this.handleCancel();
            }
        });
    }
}
