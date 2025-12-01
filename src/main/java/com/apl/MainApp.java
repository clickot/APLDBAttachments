package com.apl;

import com.apl.model.ATTRecord;
import com.apl.model.DBConnection;
import com.apl.model.Field;
import com.apl.model.Form;
import com.apl.view.MainOverviewController;
import com.apl.view.RootLayoutController;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainApp extends Application {

    public static final String CONFIG_FILE = "APLDBAttachments.properties";
    public static final String LOG4J_FILE = "log4j.properties";
    public static final String SYSTEM_PROP_LOG4J_FILE = "log4j2.configurationFile";
    public static boolean cli;

    static {
        System.setProperty(SYSTEM_PROP_LOG4J_FILE, LOG4J_FILE);
    }

    private static PropertiesConfiguration readConfigProperties() {
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class).configure(
                        new org.apache.commons.configuration2.builder.fluent.Parameters().properties()
                                .setFileName(CONFIG_FILE)
                                .setEncoding("UTF-8")
                                .setBasePath(Paths.get("").toAbsolutePath().toString())
                );
        try {
            return (PropertiesConfiguration) builder.getConfiguration();
        } catch (ConfigurationException e) {
            logger.error("Error opening config file '{}': {}", CONFIG_FILE, e.getMessage());
            System.exit(1);
        }
        return null;
    }

    private static final Logger logger = LogManager.getLogger(MainApp.class);
    private static final PropertiesConfiguration config = readConfigProperties();

    private static boolean formFolder = true;
    private static boolean entryIDFolder = false;
    private static boolean fieldNameFolder = false;

    private MainOverviewController mainOverviewController;
    private Stage primaryStage;
    private RootLayoutController rootController;
    private BorderPane rootLayout;

    private static void printUsage() {
        System.out.println("java -jar APLDBAttachments.jar" + System.lineSeparator());
        System.out.println("All Command line parameters are optional, remembering if a parameter has a space, it must be surrounded by \"'s");
        System.out.println("-u: User name to connect to Database with");
        System.out.println("-p: Password to connect with");
        System.out.println("-cs: Connection string to connect to DB");
        System.out.println("-od: Output Directory, can be absolute or relative to current path");
        System.out.println("-formlist: Comma separated list of forms to pull attachments from");
        System.out.println("-fieldlist: Comma separated list of field names fields to pull attachments from");
        System.out.println("Output Files: By default the output will be that a folder for each form will be created in the directory");
        System.out.println("\tand inside that folder will be a file named for the entry id, and field name and file name from the");
        System.out.println("\tsource record, so HPD_HelpDesk\\HPD000000000123-AttField-FileName.txt.");
        System.out.println("\tYou have the choice of providing any of the below options to change things that would have");
        System.out.println("\tbeen folders into part of the file name, or vis versa");
        System.out.println("-formFile: If you want the form name to be part of the file name");
        System.out.println("-entryIDFolder: If you want the EntryID to be a folder, this will force the Form to be a folder");
        System.out.println("-fieldNameFolder: If you want the field name to be a folder, this will force both Form and EntryID to be folders");
        System.out.println("-where: If specified, this needs to be a syntactically correct where qualification (without the word where) and it");
        System.out.println("-\twill be applied to the query for the form(s) specified");
    }

    public static void main(String[] args) {
        String appName = config.getString("AppName");
        String version = config.getString("Version");

        logger.info("{} v{} started.", appName, version);

        String userName = null;
        String password = null;
        String connectionString = null;
        String outputDir = null;
        String formArray = null;
        String fieldArray = null;
        String whereClause = null;

        if (args.length > 0) {
            userName = config.getString("UserName");
            password = config.getString("Password");
            connectionString = config.getString("ConnectionString");
            outputDir = config.getString("OutputDirectory");
            formArray = config.getString("FormList");
            fieldArray = config.getString("FieldList");
            whereClause = config.getString("Where");

            int i = 0;

            while (i < args.length && args[i].startsWith("-")) {
                String arg = args[i++];
                switch (arg) {
                    case "-clm":
                        cli = true;
                        break;
                    case "-u":
                        if (i < args.length) {
                            userName = args[i++];
                        } else {
                            printUsage();
                            System.exit(1);
                        }
                        break;
                    case "-p":
                        if (i < args.length) {
                            password = args[i++];
                        } else {
                            printUsage();
                            System.exit(1);
                        }
                        break;
                    case "-cs":
                        if (i < args.length) {
                            connectionString = args[i++];
                        } else {
                            printUsage();
                            System.exit(1);
                        }
                        break;
                    case "-od":
                        if (i < args.length) {
                            outputDir = args[i++];
                        } else {
                            printUsage();
                            System.exit(1);
                        }
                        break;
                    case "-formlist":
                        if (i < args.length) {
                            formArray = args[i++];
                        } else {
                            printUsage();
                            System.exit(1);
                        }
                        break;
                    case "-fieldlist":
                        if (i < args.length) {
                            fieldArray = args[i++];
                        } else {
                            printUsage();
                            System.exit(1);
                        }
                        break;
                    case "-formFile":
                        formFolder = false;
                        entryIDFolder = false;
                        fieldNameFolder = false;
                        break;
                    case "-entryIDFolder":
                        formFolder = true;
                        entryIDFolder = true;
                        break;
                    case "-fieldNameFolder":
                        formFolder = true;
                        entryIDFolder = true;
                        fieldNameFolder = true;
                        break;
                    case "-where":
                        if (i < args.length) {
                            whereClause = args[i++];
                        } else {
                            printUsage();
                            System.exit(1);
                        }
                        break;
                    default:
                        System.out.println("Unknown parameter '" + arg + "'");
                        printUsage();
                        System.exit(1);
                }
            }
        }

        if (!cli) {
            launch(args);
        } else {
            List<String> formNameList = new ArrayList<>();
            List<String> fieldNameList = new ArrayList<>();
            if (formArray != null && !formArray.isEmpty()) {
                String[] tokens = formArray.split(",");

                for (String token : tokens) {
                    formNameList.add(token.trim());
                }
            }

            if (fieldArray != null && !fieldArray.isEmpty()) {
                String[] tokens = fieldArray.split(",");

                for (String token : tokens) {
                    if (token != null && !token.isEmpty()) {
                        fieldNameList.add(token.trim());
                    }
                }
            }

            logger.info("Starting Export with following configuration:");
            logger.info("Connecting to db using {} and connection string '{}'", userName, connectionString);
            logger.info("Exporting to directory '{}'", outputDir);
            if (!formNameList.isEmpty()) {
                logger.info("From the following forms:");

                for (String formName : formNameList) {
                    logger.info("\t" + formName);
                }
            } else {
                logger.info("From all forms containing attachment data");
            }

            if (!fieldNameList.isEmpty()) {
                logger.info("From the following fields:");

                for (String fieldName : fieldNameList) {
                    logger.info("\t" + fieldName);
                }
            } else {
                logger.info("From all attachment fields");
            }

            if (whereClause != null && !whereClause.isEmpty()) {
                logger.info("With the following where clause: {}", whereClause);
            }

            logger.info("With the following file name conventions:");
            logger.info("Form Name as Folder: {}", formFolder);
            logger.info("Entry ID as Folder: {}", entryIDFolder);
            logger.info("Field Name as Folder: {}", fieldNameFolder);

            DBConnection dbConn = new DBConnection();
            try {
                dbConn.getConnection(connectionString, userName, password);
            } catch (SQLException e) {
                logger.error("SQLException connecting to the DB: {}", e.getMessage());
                System.exit(1);
            }

            logger.info("Getting forms from server");
            Map<String, Form> formMap = dbConn.getFormData(formNameList);
            logger.info("Total of {} form retrieved", formMap.size());
            if (logger.isDebugEnabled()) {
                logger.debug("Complete list of forms that contain attachment fields");

                for (String formName : formMap.keySet()) {
                    logger.debug(formName);
                }
            }

            for (String formName : formMap.keySet()) {
                if (formNameList.contains(formName) || formNameList.isEmpty()) {
                    logger.info("Starting processing of form '{}'", formName);
                    Form form = formMap.get(formName);

                    for (Field field : form.getFieldList()) {
                        String fieldName = field.getName();
                        if (fieldNameList.contains(fieldName) || fieldNameList.isEmpty()) {
                            logger.info("Starting processing of field '{}'", fieldName);
                            String query = dbConn.getAttachmentRecordQuery(formMap, formName, fieldName, whereClause);
                            ObservableList<ATTRecord> attachmentRecordList = dbConn.getAttachmentRecords(
                                    query, form.getID(), form.getResolvedID(), field.getId(), formName, fieldName
                            );
                            if (attachmentRecordList != null) {
                                logger.info("{} record(s) found to export", attachmentRecordList.size());
                                if (!attachmentRecordList.isEmpty()) {
                                    String output = processExport(attachmentRecordList, dbConn, true, new File(outputDir), formFolder, entryIDFolder, fieldNameFolder);
                                    if (!output.isEmpty()) {
                                        logger.error(output);
                                    }

                                    logger.info("Export of records complete");
                                }
                            } else {
                                logger.error("Error occurred getting records to export");
                            }
                        }
                    }
                }
            }

            logger.info("Export Complete");
            System.exit(0);
        }
    }

    public static String processExport(
            ObservableList<ATTRecord> selectedItems,
            DBConnection dbConn,
            boolean bulk,
            File selectedDirectory,
            boolean formFolder,
            boolean entryIDFolder,
            boolean fieldNameFolder
    ) {
        FileChooser fileChooser = new FileChooser();
        StringBuilder output = new StringBuilder();
        File file;

        for (ATTRecord record : selectedItems) {
            String fileName = record.getFileName().getValue();
            byte[] fileBytes = record.getBytes(dbConn);
            if (fileBytes == null) {
                output.append("Unable to get the file ")
                        .append(record.getEntryID().getValue())
                        .append(":")
                        .append(record.getFieldName())
                        .append(":")
                        .append(fileName)
                        .append(" from the DB server, it returned 0 bytes")
                        .append(System.lineSeparator());
            } else {
                if (bulk) {
                    String saveFileFolder = "";
                    String saveFileName = "";
                    if (formFolder) {
                        saveFileFolder = stripNonOSValues(record.getFormName());
                    } else {
                        saveFileName = stripNonOSValues(record.getFormName());
                    }

                    if (entryIDFolder) {
                        saveFileFolder = saveFileFolder + File.separator + record.getEntryID().getValue();
                    } else {
                        saveFileName = saveFileName + "-" + record.getEntryID().getValue();
                    }

                    if (fieldNameFolder) {
                        saveFileFolder = saveFileFolder + File.separator + record.getFieldName();
                    } else {
                        saveFileName = saveFileName + "-" + stripNonOSValues(record.getFieldName());
                    }

                    saveFileName = saveFileName + "-" + record.getFileName().getValue();
                    if (saveFileName.startsWith("-")) {
                        saveFileName = saveFileName.substring(1);
                    }

                    File f = new File(selectedDirectory.getAbsolutePath() + File.separator + saveFileFolder);
                    if (!f.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        f.mkdirs();
                    }

                    file = new File(selectedDirectory.getAbsolutePath() + File.separator + saveFileFolder + File.separator + saveFileName);
                } else {
                    fileChooser.setInitialFileName(fileName);
                    fileChooser.setTitle("Save Attachment");
                    file = fileChooser.showSaveDialog(null);
                }

                if (file != null) {
                    try {
                        FileOutputStream fos = new FileOutputStream(file.toString());
                        fos.write(fileBytes);
                        fos.close();
                    } catch (IOException e) {
                        output.append("Error saving file: ")
                                .append(record.getEntryID().getValue())
                                .append(":")
                                .append(record.getFieldName())
                                .append(":")
                                .append(fileName)
                                .append(": ")
                                .append(e.getMessage());
                    }
                }
            }
        }

        return output.toString();
    }

    private static String stripNonOSValues(String s) {
        return s.replaceAll("[~#%&*{}\\:<>?/+|\"]", "_");
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    public void handleBuildConnectionString() {
        this.rootController.handleBuildConnectionString();
    }

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            this.rootLayout = loader.load();
            this.rootController = loader.getController();
            this.rootController.setMainApp(this);
            Scene scene = new Scene(this.rootLayout);
            this.primaryStage.setScene(scene);
            this.primaryStage.show();
        } catch (IOException e) {
            logger.error("error in initRootLayout: {}", e.getMessage(), e);
        }
    }

    public void loadConfig() {
        logger.info("Loading Configuration Settings");
        this.mainOverviewController.setUserName(new TextField(config.getString("UserName")));
        this.mainOverviewController.setPassword(new TextField(config.getString("Password")));
        this.mainOverviewController.setConnectionString(new TextField(config.getString("ConnectionString")));
    }

    public void saveConfig() {
        logger.info("Saving Configuration Settings");
        String userName = mainOverviewController.getUserName();
        if (userName != null && !userName.isEmpty()) {
            config.setProperty("UserName", userName);
        }
        String password = mainOverviewController.getPassword();
        if (password != null && !password.isEmpty()) {
            config.setProperty("Password", password);
        }
        String connectionString = mainOverviewController.getConnectionString();
        if (connectionString != null && !connectionString.isEmpty()) {
            config.setProperty("ConnectionString", connectionString);
        }
        try {
            config.write(new FileWriter(CONFIG_FILE));
        } catch (ConfigurationException | IOException e) {
            logger.error("error in saving configuration file " + CONFIG_FILE, e);
        }
    }

    public void showMainOverview() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/MainOverview.fxml"));
            AnchorPane mainOverview = loader.load();
            this.rootLayout.setCenter(mainOverview);
            this.mainOverviewController = loader.getController();
            this.mainOverviewController.setMainApp(this);
        } catch (IOException e) {
            logger.error("unable to show main overview: " + e.getMessage(), e);
        }
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("APL DB Attachments");
        this.initRootLayout();
        this.showMainOverview();
    }

    public void updateConnectionDetails(TextField userName, TextField password, TextField connectionString) {
        this.mainOverviewController.setUserName(userName);
        this.mainOverviewController.setPassword(password);
        this.mainOverviewController.setConnectionString(connectionString);
    }
}
