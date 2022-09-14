# Tartan

'Tartan' is a JavaFX application for designing a Tartan patterns.

## Overview
This project has been set up as a Maven project that uses JavaFX, FXML and 
CSS to render the GUI. Maven can be run from the command line as shown below.
Maven resolves dependencies and builds the application independently of an IDE.

This application helps design Tartan and can generate a png image.

## Dependencies
'Tartan' is dependent on the following:

  * Java 15.0.1
  * Apache Maven 3.6.3

The code is structured as a standard Maven project which requires Maven and a 
JDK to be installed. A quick web search will help, alternatively
[Oracle](https://www.java.com/en/download/) and 
[Apache](https://maven.apache.org/install.html) should guide you through the
install.

Also [OpenJFX](https://openjfx.io/openjfx-docs/) can help set up your 
favourite IDE to be JavaFX compatible, however, Maven does not require this.

## Cloning and Running
The following commands clone and execute the code:

	git clone https://github.com/PhilLockett/Tartan.git
	cd Tartan/
	mvn clean javafx:run

## Points of interest
This code has the following points of interest:

  * Tartan is a Maven project that uses JavaFX.
  * Tartan is structured as an MVC project (FXML being the Video component).
  * Multi stage initialization minimizes the need for null checks. 
  * Data persistence is provided by the Serializable DataStore object.
  * The GUI is implemented in FXML using SceneBuilder.
