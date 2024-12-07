# Tartan

'Tartan' is a JavaFX application for designing Tartan patterns.

**USE AT OWN RISK.**

## Overview
This project has been set up as a Maven project that uses JavaFX, FXML and 
CSS to render the GUI. Maven can be run from the command line as shown below.
Maven resolves dependencies and builds the application independently of an IDE.

This application helps design Tartan patterns and can generate a png image.

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

## Cloning
The following command clones the code:

	git clone https://github.com/PhilLockett/Tartan.git

## Running
Once cloned, change to the `Tartan` directory and execute the following 
command:

	mvn clean javafx:run

## User Guide
### "Tartan Designer" Window
#### Colours
'Tartan' is an application for designing Tartan patterns using colours from a 
configurable "Colour Palette".
Each colour swatch can be selected then changed using the "Selected Colour" 
control.
Each colour swatch can be labelled using the adjoining text field.

#### Layout
The pattern layout can be controlled in a number of ways.
"Tartan" uses a pattern that repeats after an specific number of threads.
"Column Repeat Count" defines that number of vertical threads.
Typically the pattern is the same for the vertical and horizontal threads and 
the "Duplicate the Column colours for the Rows" check box performs this 
duplication automatically.
However, if this is not desired, uncheck the box and set the vertical and 
horizontal threads independently.
This also allows the number of horizontal threads (before repeating) to be set 
independently.

Guide lines are shown, dividing the "Sample" window into eight equal parts 
vertically and horizontally.
The "Show Guide Lines" check box controls whether theses are displayed or not.
The colour of the guide lines **AND** the colour of thread highlights can be 
changed using the colour selector labelled "Guide Line Colour".

Typically multiple adjacent treads have the same colour.
"Thread Repeat Count" colours up to eight adjacent threads using the currently 
selected colour.

The size of the threads can be increased using the "Thread Size" control, to 
make life easier during the design phase, however this will effect the 
generated graphic when saved.
The thickness of the thread border can be adjusted using the "Thread Border 
Thickness" control, if needed.

#### Pull-Down Menu
The Pull-Down Menu allows tartan designs to be loaded from and saved to the 
`Tartan/swatches` directory.

### "Sample" Window
Across the top edge and down the left side of the "Sample" window are extended 
threads which are sensitive to mouse movement and clicking.
Moving the mouse pointer over these areas will cause threads to be highlighted 
based on the current selections.
Clicking on these areas will colour the highlighted threads using the currently 
selected colour from the "Colour Palette".

## Points of interest
This code has the following points of interest:

  * Tartan is a Maven project that uses JavaFX.
  * Tartan is structured as an MVC project (FXML being the Video component).
  * Multi stage initialization minimizes the need for null checks. 
  * Data persistence is provided by the Serializable DataStore object.
  * The GUI is implemented in FXML using SceneBuilder.
  * Uses a custom controller, "ColourSelect", to select desired colours.
