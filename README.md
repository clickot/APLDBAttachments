# APLDBAttachments

Java tool to extract ticket attachments from a BMC Remedy Database. 

This is a straight-forward refactoring of the Remedy Legacy Tool [DB Attachments](https://remedylegacy.com/tools/db-attachments/). 
It aims to replace outdated dependencies, especially log4j 1.x. and Oracle JDK 1.8, and make it easier 
to keep it up to date with dependency lib upgrades concerning security vulnerabilities.

To build a release zip simply execute `mvn package`. You find the resulting zip file in the target directory. 
To download the zip without building yourself go to the [release page](https://github.com/clickot/APLDBAttachments/releases).

To start the application unzip the zip file, enter the `APLDBAttachments-<version>` directory and execute `apl-db-attachments` script or `apl-db-attachments.bat` batch file, depending on your platform (or click it in your explorer).

> Note that you need at least a JDK 17 and a JavaFX SDK to start the application. Don't forget to set the PATH_TO_FX environment variable, pointing to [PATH-TO-JAVAFX-SDK]/lib. 

You can download the JavaFX SDK from the [openjfx](https://openjfx.io/) site. On the [Download page](https://gluonhq.com/products/javafx/), 
we recommend to choose the latest LTS version, which is 21.x.x currently.

