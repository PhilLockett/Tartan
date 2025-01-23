/*  Tartan - a JavaFX based Tartan image generator.
 *
 *  Copyright 2024 Philip Lockett.
 *
 *  This file is part of Tartan.
 *
 *  Tartan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Tartan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Tartan.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * HelpControl is a class that is responsible for displaying the User Guide.
 */
package phillockett65.Tartan;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class HelpControl extends Stage {



    /************************************************************************
     * Support code for "Help" pop-up. 
     */

    private Scene scene;

    private VBox root;

    private Button cancel;

    private double x = 0.0;
    private double y = 0.0;

    private boolean result = false;



    /************************************************************************
     * Support code for the Embedded Help page.
     */

    private class BodyFlow extends TextFlow {
        public BodyFlow(Text... text) {
            super(text);

            this.setTextAlignment(TextAlignment.JUSTIFY);
        }

    }
    private class Body extends Text {
        public Body(String text) {
            super(text);

            this.setFont(Font.font("Arial", FontWeight.NORMAL, 17));

            final Color fore = Color.web("#e8e8e8");
            this.setFill(fore);
        }

    }

    private class H1Flow extends BodyFlow {
        public H1Flow(Text... text) {
            super(text);

            this.setPadding(new Insets(0, 0, 12, 0));
        }

    }
    private class H1 extends Body {
        public H1(String text) {
            super(text);
            this.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        }
    }

    private class H2Flow extends BodyFlow {
        public H2Flow(Text... text) {
            super(text);

            this.setPadding(new Insets(8, 0, 10, 0));
        }

    }
    private class H2 extends Body {
        public H2(String text) {
            super(text);
            this.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        }
    }

    private class H3Flow extends BodyFlow {
        public H3Flow(Text... text) {
            super(text);

            this.setPadding(new Insets(8, 0, 10, 0));
        }

    }
    private class H3 extends Body {
        public H3(String text) {
            super(text);
            this.setFont(Font.font("Arial", FontWeight.BOLD, 19));
        }
    }

    private class PFlow extends BodyFlow {
        public PFlow(Text... text) {
            super(text);

            this.setPadding(new Insets(0, 0, 16, 0));
            this.setPrefWidth(470);
            this.setMaxWidth(470);
        }

    }
    private class P extends Body {
        public P(String text) {
            super(text);
            this.setFont(Font.font("Arial", FontWeight.NORMAL, 17));
            }
    }
    private class Code extends Body {
        public Code(String text) {
            super(text);
            this.setFont(Font.font("Courier New", FontWeight.NORMAL, 17));
            }
    }
    private class Bold extends Body {
        public Bold(String text) {
            super(text);
            this.setFont(Font.font("Arial", FontWeight.BOLD, 17));
            }
    }

    private class ListFlow extends BodyFlow {
        public ListFlow(Text... text) {
            super(text);

            this.setPadding(new Insets(0, 0, 16, 30));
            this.setPrefWidth(440);
            this.setMaxWidth(440);
        }

    }
    private class ListItem extends Body {
        public ListItem(String text) {
            super(text);
            this.setFont(Font.font("Arial", FontWeight.NORMAL, 17));
            }
    }

    VBox help = new VBox(
        new H1Flow(new H1("Tartan User Guide")),
        new H2Flow(new H2("“Tartan Designer” Window")),
        new H3Flow(new H3("“Colour Palette” Pane")),
        new PFlow(
            new P("‘Tartan’ is an application for designing Tartan patterns using colours from a configurable “Colour Palette”. "),
            new P("Each colour swatch in the “Colour Palette” can be selected then changed using the “Selected Colour” control. "),
            new P("Optionally, each colour swatch can be labelled using the adjoining text field. ")
            ),
        new H3Flow(new H3("“Selected Colour” Pane")),
        new PFlow(
            new P("Colours can be defined using a number of different models. "),
            new P("Two popular models are RGB (Red Green Blue) and HSB (Hue Saturation Brightness). "),
            new P("The “Selected Colour” control allows colours to be defined using either of these models, in a number of ways. ")
            ),
        new PFlow(
            new P("The Colour Wheel allows the Hue to be selected along with the Saturation and Brightness sliders below it. "),
            new P("Alternatively the Red, Green and Blue sliders can be used to define a colour. ")
            ),
        new PFlow(
            new P("However, if for example the colour “Coral” is required and the RGB triplet is given as rgb(255, 127, 80), these values can be entered into the corresponding text fields. "),
            new P("If, instead of the decimal values, the hex values are given (FF7F50), the “Hex” check box can be selected to enter valid hexadecimal values. ")
            ),
        new PFlow(
            new P("If HSB values are given, the “HSB” check box can be selected to enter values for Hue, Saturation and Brightness. ")
            ),
        new PFlow(
            new P("These values are specified within default ranges. "),
            new P("RGB values are typically 0 to 255, Hue is 0 to 360 and Saturation and Brightness are usually 0 to 1, but here are 0 to 100 (i.e. as percentages). "),
            new P("Any of theses values ranges can be changed to one of: 100, 255, 360 or 1000 as needed. "),
            new P("However, note that within the colour object the resolution of the values is limited to 255, so values using higher ranges will be rounded. ")
            ),
        new PFlow(
            new P("Once the values have been entered, click the “Apply” button to use the defined colour. ")
            ),
        new H3Flow(new H3("“Layout” Pane")),
        new PFlow(
            new P("“Tartan” is a pattern that repeats after a specific number of threads. "),
            new P("This pattern layout can be controlled in a number of ways. "),
            new P("Typically the pattern is the same for the vertical and horizontal threads and the “Duplicate the Column colours for the Rows” check box controls whether this duplication is automatically performed. "),
            new P("If this duplication is not desired, un-check the box and set the vertical and horizontal thread colours and thread counts (before repeating) independently. ")
            ),
        new PFlow(
            new P("“Column Repeat Count” defines the number of vertical threads. ")
            ),
        new PFlow(
            new P("If duplication is not being used, “Row Repeat Count” defines the number of horizontal threads. ")
            ),
        new PFlow(
            new P("The “Sample” window (i.e. the pattern before repeating) can be divided by guide lines into eight equal parts, vertically and horizontally. "),
            new P("The “Show Guide Lines” check box controls whether these are displayed or not. "),
            new P("The colour of the guide lines "),
            new Bold("AND "),
            new P("the colour of thread highlighting can be changed using the colour selector labelled “Guide Line Colour”. ")
            ),
        new PFlow(
            new P("Typically multiple adjacent threads have the same colour. "),
            new P("Up to eight adjacent threads can be set (to the currently selected colour) using the “Thread Repeat Count” spinner. ")
            ),
        new PFlow(
            new P("The size of the threads can be increased using the “Thread Size” control, to make life easier during the design phase, however this will effect the generated graphic when saved. "),
            new P("The thickness of the thread border can be adjusted using the “Thread Border Thickness” control, if needed. ")
            ),
        new H3Flow(new H3("Pull-Down Menu")),
        new PFlow(
            new P("The Pull-Down Menu allows tartan designs to be loaded from and saved to the "),
            new Code("Tartan/swatches "),
            new P("directory. "),
            new P("Each tartan design is saved in it’s own sub-directory as a settings file and includes a “.png” image file showing the design repeated in a 2 by 2 layout. ")
            ),
        new H2Flow(new H2("“Sample” Window")),
        new H3Flow(new H3("Colouring")),
        new PFlow(
            new P("Across the top edge and down the left side of the “Sample” window are extended threads which are sensitive to mouse movement and clicking. "),
            new P("Moving the mouse pointer over these areas will cause a number of threads to be highlighted based on the current selections. "),
            new P("Clicking on these areas will colour the highlighted threads using the currently selected colour from the “Colour Palette”. ")
            ),
        new H3Flow(new H3("Adjusting")),
        new PFlow(
            new P("There are a few ways to adjust the tartan design, firstly it can be rotated as needed. "),
            new Bold("With the focus on the “Sample” window"),
            new P(", use the arrow keys to rotate the pattern up, down, left or right. "),
            new P("Note that with the “Duplicate the Column colours for the Rows” check box selected, the rotation is duplicated resulting in a diagonal movement. ")
            ),
        new PFlow(
            new P("Additionally, threads can be inserted or deleted. "),
            new Bold("With the focus on the “Sample” window"),
            new P(", holding down the “Alt” or “Ctrl” key will display a helpful message in the “Sample” heading bar to indicate these functional options: ")
            ),
        new ListFlow(
            new ListItem("With the “Alt” key held down, clicking on the extended threads will "),
            new Bold("delete "),
            new ListItem("the highlighted threads. ")
            ),
        new ListFlow(
            new ListItem("With the “Ctrl” key held down, clicking on the extended threads will "),
            new Bold("insert "),
            new ListItem("the highlighted threads. ")
            )
    );



    /************************************************************************
     * Support code for the Initialization of the Controller.
     */

    /**
     * Builds the top-bar as a HBox and includes the cancel button the mouse 
     * press and drag handlers.
     * @return the HBox that represents the top-bar.
     */
    private HBox buildTopBar() {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER);
        topBar.setPrefHeight(Default.TOP_BAR_HEIGHT.getFloat());

        // Make window dragable.
        topBar.setOnMousePressed(mouseEvent -> {
            x = mouseEvent.getSceneX();
            y = mouseEvent.getSceneY();
        });

        topBar.setOnMouseDragged(mouseEvent -> {
            this.setX(mouseEvent.getScreenX() - x);
            this.setY(mouseEvent.getScreenY() - y);
        });

        Image image = new Image(getClass().getResourceAsStream("icon32.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(28);
        imageView.setFitWidth(28);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);

        Label heading = new Label(" " + this.getTitle());
        Region region = new Region();

        Pane cancel = Model.buildCancelButton();
        cancel.setOnMouseClicked(event -> {
            result = false;
            this.close();
        });

        topBar.getChildren().add(imageView);
        topBar.getChildren().add(heading);
        topBar.getChildren().add(region);
        topBar.getChildren().add(cancel);
        
        HBox.setHgrow(region, Priority.ALWAYS);

        return topBar;
    }


    /**
     * Builds the options button as a HBox and includes the action event 
     * handler for cancel button.
     * @return the HBox that represents the options button.
     */
    private HBox buildOptions() {
        HBox options = new HBox();

        cancel = new Button("Cancel");

        cancel.setMnemonicParsing(false);
    
        cancel.setOnAction(event -> {
            result = false;
            this.close();
        });

        cancel.setTooltip(new Tooltip("Cancel Help"));

        options.getChildren().add(cancel);

        return options;
    }


    /**
     * Builds a ScrollPane and fills it with "help".
     * @return the ScrollPane filled with "help".
     */
    private ScrollPane buildHelpPage() {

        final Color back = Color.web("#101010");
        help.setBackground(Background.fill(back));
        help.setPadding(new Insets(4, 16, 4, 4));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setMaxSize(500, 600);
        scrollPane.setContent(help);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);

        return scrollPane;
    }


    /**
     * Builds the User controls as a VBox.
     * @return the VBox that captures the User controls.
     */
    private VBox buildControlPanel() {
        VBox panel = new VBox();

        panel.setSpacing(10);
        panel.setPadding(new Insets(10.0));

        panel.getChildren().add(buildHelpPage());
        panel.getChildren().add(buildOptions());

        return panel;
    }

    /**
     * Initialize the control.
     */
    private void init(String title) {
        this.setTitle(title);
        this.resizableProperty().setValue(false);
        this.initStyle(StageStyle.UNDECORATED);
        this.initModality(Modality.APPLICATION_MODAL);

        root = new VBox();

        root.getChildren().add(buildTopBar());
        root.getChildren().add(buildControlPanel());
        root.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        scene = new Scene(root);

        this.setScene(scene);
    }



    /************************************************************************
     * Support code for the Operation of the Controller.
     */

    /**
     * Constructor.
     */
    private HelpControl() {
        super();
    }


    /**
     * Construct and launch the User Guide and wait for user input.
     * @return false when cancelled.
     */
    public static boolean showControl(String title) {
        HelpControl control = new HelpControl();

        control.init(title);
        control.showAndWait();

        return control.result;
    }

}
