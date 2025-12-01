# APLDBAttachments

Java tool to extract ticket attachments from a BMC Remedy Database. 

This is a straight-forward refactoring of the Remedy Legacy Tool [DB Attachments](https://remedylegacy.com/tools/db-attachments/). 
It aims to replace outdated dependencies, especially log4j 1.x. and Oracle JDK 1.8, and make it easier 
to keep it up to date with dependency lib upgrades concerning security vulnerabilities.

To build a release zip simply execute `mvn package`. You find the resulting zip file in the target directory. 
To download the zip without building yourself go to the [release page](https://github.com/clickot/APLDBAttachments/releases).

To start the application unzip the zip file, enter the `APLDBAttachments-<version>` directory and execute `java -jar APLDBAttachments-<version>.jar`.

> Note that you need a Oracle JDK 1.8 containing the javafx class files (this is the case up to version 446) to build and run the initial 2.1.x
versions of this tool. 
