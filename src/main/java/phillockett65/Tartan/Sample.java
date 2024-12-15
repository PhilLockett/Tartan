/*  Tartan - a JavaFX based playing card image generator.
 *
 *  Copyright 2022 Philip Lockett.
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
 * Sample is a class that is responsible for creating the Stage, drawing 
 * and refreshing the Tartan Swatch and accepting user input.
 */
package phillockett65.Tartan;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;


public class Sample extends Stage {

    private final static int NONE_ZONE = 0;
    private final static int ROW_ZONE = 1;
    private final static int COLUMN_ZONE = 2;
    private final static int BOTH_ZONE = 3;

    private Model model;

    private Group group;
    private Canvas canvas;
    private GraphicsContext gc;

    private double dx;	// Difference between the size of the stage and the size of the scene.
    private double dy;

    private ObservableList<Thread> colList = FXCollections.observableArrayList();
    private ObservableList<Thread> rowList = FXCollections.observableArrayList();
    private ObservableList<Line> guides = FXCollections.observableArrayList();

    private final Color defaultColour;

    private int lastZone = 0;
    private int lastPos = 0;

    private double x = 0.0;
    private double y = 0.0;

    // private boolean result = false;


    /************************************************************************
     * General support code.
     */

    private static double OFFSET = Default.BORDER_WIDTH.getFloat();
    private static double TOPBARSIZE = Default.TOP_BAR_HEIGHT.getFloat();

    /**
     * Calculate the column from the mouse x pos in the scene.
     * Accounts for the thread selector size.
     * @param x pos of the mouse in the scene.
     * @return the column number (0 to getColumnCount()-1)
     */
    private int xPosToCol(double x) {
        final double size = model.getThreadSize();
        return (int)((x - OFFSET) / size);
    }

    /**
     * Calculate the row from the mouse y pos in the scene.
     * Accounts for the thread selector size and the top-bar size.
     * @param y pos of the mouse in the scene.
     * @return the row number (0 to getRowCount()-1)
     */
    private int yPosToRow(double y) {
        final double size = model.getThreadSize();
        return (int)((y - (OFFSET + TOPBARSIZE)) / size);
    }

    /**
     * Calculate the zone from the mouse x & y pos in the scene.
     * Accounts for the thread selector size and the top-bar size.
     * @param x pos of the mouse in the scene.
     * @param y pos of the mouse in the scene.
     * @return ROW_ZONE if the pos is in the row thread selector, 
     * COLUMN_ZONE if the pos is in the column thread selector or
     * NONE_ZONE otherwise.
     */
    private int getZone(double x, double y) {
        // In the top-bar?
        y -= TOPBARSIZE;
        if (y < 0) {
            return NONE_ZONE;
        }

        // Adjust for the thread selection border size (and the canvas origin).
        x -= OFFSET;
        y -= OFFSET;

        // In the top-left corner of background, but not on thread selector?
        if ((x < 0) && (y < 0)) {
            return NONE_ZONE;
        }

        // On the far side of either thread selector?
        if (x >= model.getSwatchWidth()) {
            return NONE_ZONE;
        }
        if (y >= model.getSwatchHeight()) {
            return NONE_ZONE;
        }

        // On the row thread selector?
        if (x < 0) {
            return ROW_ZONE;
        }

        // On the column thread selector?
        if (y < 0) {
            return COLUMN_ZONE;
        }

        // Must be in the tartan swatch, so ignore.
        return NONE_ZONE;
    }



    /************************************************************************
     * Support code for the Thread sub-class.
     */

    /**
     * Class to represent a single thread as a sequence of rectangles.
     */
    private class Thread {
        private boolean highlight;
        private int index;
        private boolean row;
        private int colourIndex;
        private Rectangle stitch;

        /**
         * The Constructor is singularly responsible for adding the Rectangles
         * to the Group.
         * @param index of the row or column.
         * @param row if true, column otherwise.
         */
        public Thread(int index, boolean row) {
            highlight = false;

            this.index = index;
            this.row = row;
            this.colourIndex = 0;
            ObservableList<Node> items = group.getChildren();

            final Color fillColour = model.getSwatchColour(colourIndex);
            stitch = new Rectangle();
            stitch.setFill(fillColour);
            stitch.setStroke(defaultColour);

            items.add(stitch);
        }

        public boolean isRow() { return row; }

        /**
         * Draw the thread using the current gc colours.
         */
        private void _draw() {
            if (isRow())
                drawRow();
            else
                drawCol();
        }

        /**
         * Set the gc colours for the thread and then draw them.
         * @param border colour to use.
         */
        private void draw(Color border) {
            gc.setFill(model.getSwatchColour(colourIndex));
            gc.setStroke(border);
            _draw();
        }

        /**
         * Set the gc colours for the thread and then draw them.
         */
        private void draw() {
            final Color border = highlight ? model.getGuideLineColour() : defaultColour;
            draw(border);
        }

        /**
         * Set the colour of the thread to the given swatch colour.
         * @param index of the selected swatch.
         */
        public void setColourIndex(int index) {
            colourIndex = index;
            stitch.setFill(model.getSwatchColour(colourIndex));

            draw();
        }

        public void setHighlight(boolean state, Color border) {
            if (state == highlight) {
                return;
            }

            highlight = state;
            stitch.setStroke(border);

            draw(border);
        }

        /**
         * Synchronise the colour of the thread to the updated swatch colour.
         * @return true if this thread needed to be updated, false otherwise.
         */
        public boolean syncCurrentColour() {
            if (colourIndex != model.getSelectedColour())
                return false;

            final Color color = model.getCurrentColour();

            stitch.setFill(color);

            draw();

            return true;
        }


        /**
         * Set the visibilty of the lead thread.
         * @param visible 
         */
        public void setVisible(boolean visible) {
            stitch.setVisible(visible);
        }


        private void drawRow() {
            final double size = model.getThreadSize();
            final double size2 = size * 2;
            final double size4 = size * 4;
            final double yPos = index * size;
            final int count = (int)(Default.WIDTH.getFloat() / 4);

            int c = index % 4;
            c = (4 - c) % 4;
            double xPos = c * size;
            for (int j = 0; j < count; ++j) {
                gc.fillRect(xPos, yPos, size2, size);
                gc.strokeRect(xPos, yPos, size2, size);

                xPos += size4;
            }
        }

        private void drawCol() {
            final double size = model.getThreadSize();
            final double size2 = size * 2;
            final double size4 = size * 4;
            final double xPos = index * size;
            final int count = (int)(Default.HEIGHT.getFloat() / 4);

            int r = index % 4;
            r = (6 - r) % 4;
            double yPos = r * size;
            for (int j = 0; j < count; ++j) {
                gc.fillRect(xPos, yPos, size, size2);
                gc.strokeRect(xPos, yPos, size, size2);

                yPos += size4;
            }
        }

        /**
         * Synchronise the thread size for a row. Sets the size and position of
         * the Rectangles.
         */
        public void syncRowSize() {
            final double size = model.getThreadSize();
            final double yPos = (index * size);
            final double thickness = model.getBorderThickness();

            int c = index % 4;
            stitch.setWidth(OFFSET + ((c == 1) ? size : 0));
            stitch.setHeight(size);
            stitch.setX(0D);
            stitch.setY(OFFSET + yPos);
            stitch.setVisible(index < model.getRowCount());
            stitch.setStrokeWidth(thickness);

            final Color fillColour = model.getSwatchColour(colourIndex);
            gc.setFill(fillColour);

            drawRow();
        }

        /**
         * Synchronise the thread size for a column. Sets the size and position
         * of the Rectangles.
         */
        public void syncColSize() {
            final double size = model.getThreadSize();
            final double xPos = (index * size);
            final double thickness = model.getBorderThickness();

            int r = index % 4;
            stitch.setWidth(size);
            stitch.setHeight(OFFSET + ((r == 3) ? size : 0));
            stitch.setX(OFFSET + xPos);
            stitch.setY(0D);
            stitch.setVisible(index < model.getColumnCount());
            stitch.setStrokeWidth(thickness);

            final Color fillColour = model.getSwatchColour(colourIndex);
            gc.setFill(fillColour);

            drawCol();
        }

    }


    /************************************************************************
     * Support code for the handlers. 
     */

    private void setRowColourIndex(int index, int colourIndex) {
        model.setRowColourIndex(index, colourIndex);
        rowList.get(index).setColourIndex(colourIndex);
    }

    private void setColColourIndex(int index, int colourIndex) {
        model.setColColourIndex(index, colourIndex);
        colList.get(index).setColourIndex(colourIndex);
    }

    private void duplicateRows() {
        final int ACTIVE = model.getRowCount();
        final int Max = Default.HEIGHT.getInt();
        for (int index = ACTIVE; index < Max; ++index) {
            final int colourIndex = model.getRowColourIndex(index);

            rowList.get(index).setColourIndex(colourIndex);
        }
    }

    private void duplicateCols() {
        final int ACTIVE = model.getColumnCount();
        final int Max = Default.WIDTH.getInt();
        for (int index = ACTIVE; index < Max; ++index) {
            final int colourIndex = model.getColColourIndex(index);

            colList.get(index).setColourIndex(colourIndex);
        }
    }

    private void rotateUp() {
        final int ACTIVE = model.getRowCount();

        final int safeIndex = model.getRowColourIndex(0);
        for (int index = 1; index < ACTIVE; ++index) {
            final int colourIndex = model.getRowColourIndex(index);

            setRowColourIndex(index-1, colourIndex);
        }
        setRowColourIndex(ACTIVE-1, safeIndex);

        duplicateRows();
    }

    private void rotateDown() {
        final int ACTIVE = model.getRowCount();

        final int safeIndex = model.getRowColourIndex(ACTIVE-1);
        for (int index = ACTIVE-1; index > 0 ; --index) {
            final int colourIndex = model.getRowColourIndex(index-1);

            setRowColourIndex(index, colourIndex);
        }
        setRowColourIndex(0, safeIndex);

        duplicateRows();
    }

    private void rotateLeft() {
        final int ACTIVE = model.getColumnCount();

        final int safeIndex = model.getColColourIndex(0);
        for (int index = 1; index < ACTIVE; ++index) {
            final int colourIndex = model.getColColourIndex(index);

            setColColourIndex(index-1, colourIndex);
        }
        setColColourIndex(ACTIVE-1, safeIndex);

        duplicateCols();
    }

    private void rotateRight() {
        final int ACTIVE = model.getColumnCount();

        final int safeIndex = model.getColColourIndex(ACTIVE-1);
        for (int index = ACTIVE-1; index > 0 ; --index) {
            final int colourIndex = model.getColColourIndex(index-1);

            setColColourIndex(index, colourIndex);
        }
        setColColourIndex(0, safeIndex);

        duplicateCols();
    }

    private void delete() {
    }

    private void insert() {
    }

    private void release() {
    }


    /************************************************************************
     * Support code for initialization of the Key and Mouse handlers.
     */

    /**
     * Initializes the Key handlers for the Sample scene.
     */
    private void initializeSampleKeyHandlers(Scene scene) {

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
            case ALT:
                delete();
                break;

            case CONTROL:
                insert();
                break;

            case UP:
                rotateUp();
                if (model.isDuplicate()) {
                    rotateLeft();
                }
                break;

            case DOWN:
                rotateDown();
                if (model.isDuplicate()) {
                    rotateRight();
                }
                break;

            case LEFT:
                rotateLeft();
                if (model.isDuplicate()) {
                    rotateUp();
                }
                break;

            case RIGHT:
                rotateRight();
                if (model.isDuplicate()) {
                    rotateDown();
                }
                break;

            default:
                break;
            }
        });

        scene.setOnKeyReleased(event -> {
            release();
        });
    }


     /**
     * Initializes the Mouse handlers for the Sample scene.
     */
    private void initializeSampleMouseHandlers(Scene scene) {

        scene.setOnMouseMoved(event -> {
            final double x = event.getSceneX();
            final double y = event.getSceneY();

            final int zone = getZone(x, y);

            // Clean up previous highlights.
            if (zone != lastZone) {
                if (lastZone != NONE_ZONE) {
                    if (model.isDuplicate()) {
                        clearRows();
                        clearColumns();
                    } else if (lastZone == ROW_ZONE) {
                        clearRows();
                    } else {
                        clearColumns();
                    }
                }
            }

            // Is there work to do?
            int pos = 0;
            if (zone == ROW_ZONE) {
                pos = yPosToRow(y);
            } else if (zone == COLUMN_ZONE) {
                pos = xPosToCol(x);
            } else {
                lastZone = zone;

                return; // No highlights required so abort..
            }

            // Draw highlights.
            if (zone != lastZone) {
                lastZone = zone;
                if (zone != NONE_ZONE) {
                    if (model.isDuplicate()) {
                        highlightThreads(BOTH_ZONE, pos, true, model.getThreadCount());
                    } else {
                        highlightThreads(zone, pos, true, model.getThreadCount());
                    }
                }
            } else if (lastPos != pos) {
                if (zone != NONE_ZONE) {
                    if (model.isDuplicate()) {
                        highlightThreads(BOTH_ZONE, lastPos, false, model.getThreadCount());
                        highlightThreads(BOTH_ZONE, pos, true, model.getThreadCount());
                    } else {
                        highlightThreads(zone, lastPos, false, model.getThreadCount());
                        highlightThreads(zone, pos, true, model.getThreadCount());
                    }
                }
            }

            lastPos = pos;
        });

        scene.setOnMouseExited(event -> {
            if (model.isDuplicate()) {
                clearRows();
                clearColumns();
            } else if (lastZone == ROW_ZONE) {
                clearRows();
            } else {
                clearColumns();
            }
            lastZone = NONE_ZONE;
        });

        scene.setOnMouseClicked(event -> {
            final double x = event.getSceneX();
            final double y = event.getSceneY();
            final int zone = getZone(x, y);
            final int colour = model.getSelectedColour();

            if (zone == ROW_ZONE) {
                setRowColour(yPosToRow(y), colour);
            } else if (zone == COLUMN_ZONE) {
                setColColour(xPosToCol(x), colour);
            }

        });

    }



    /************************************************************************
     * Support code for the Initialization of the Sample.
     */

    Label heading = new Label();
    private void augmentHeading(String label) {
        if ((label == null) || (label.isBlank()))
            heading.setText(" " + this.getTitle());
        else
            heading.setText(" " + this.getTitle() + " - " + label);
    }

    private void setHeading() { augmentHeading(null); }

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

        // Label heading = new Label(" " + this.getTitle());
        setHeading();
        Region region = new Region();

        Pane cancel = Model.buildCancelButton();
        cancel.setOnMouseClicked(event -> model.close());

        topBar.getChildren().add(imageView);
        topBar.getChildren().add(heading);
        topBar.getChildren().add(region);
        topBar.getChildren().add(cancel);
        
        HBox.setHgrow(region, Priority.ALWAYS);

        return topBar;
    }

    /**
     * Builds the tartan swatch display as a Group.
     * @return the Group that displays the tartan swatch.
     */
    private Group buildTartanDisplay(double width, double height) {
        group = new Group();

        // Use a silver background.
        Rectangle background = new Rectangle(width, height, Color.SILVER);

        canvas = new Canvas(width-OFFSET, height-OFFSET);
        canvas.setLayoutX(OFFSET);
        canvas.setLayoutY(OFFSET);

        group.getChildren().add(background);
        group.getChildren().add(canvas);

        // Grab the graphics context while we are here.
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, width-OFFSET, height-OFFSET);

        return group;
    }

    /**
     * Initializes the stage and adds handlers to the scene.
     */
    private void initializeCardSample() {

        final double WIDTH = Default.MPC_WIDTH.getFloat();
        final double HEIGHT = Default.MPC_HEIGHT.getFloat();
        // System.out.println("initializeCardSample(" + WIDTH + ", " + HEIGHT + ")");

        VBox root = new VBox();
        root.setPrefSize(WIDTH, HEIGHT);

        root.getChildren().add(buildTopBar());
        root.getChildren().add(buildTartanDisplay(WIDTH, HEIGHT));

        drawBlankLoom();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        this.setScene(scene);
        this.setX(0);
        this.setY(0);

        dx = this.getWidth() - WIDTH;
        dy = this.getHeight() - HEIGHT;

        this.setMinWidth(Default.MIN_WIDTH.getFloat() + dx);
        this.setMinHeight(Default.MIN_HEIGHT.getFloat() + dy);
        this.setMaxWidth(Default.MAX_WIDTH.getFloat() + dx);
        this.setMaxHeight(Default.MAX_HEIGHT.getFloat() + dy);

        initializeSampleKeyHandlers(scene);
        initializeSampleMouseHandlers(scene);
    }

    /**
     * Called on initialization to set up the blank loom.
     */
    private void drawBlankLoom() {
        final int ROWS = Default.HEIGHT.getInt();
        for (int row = 0; row < ROWS; ++row) {
            Thread thread = new Thread(row, true);
            rowList.add(thread);
        }

        final int COLS = Default.WIDTH.getInt();
        for (int col = 0; col < COLS; ++col) {
            Thread thread = new Thread(col, false);
            colList.add(thread);
        }

        final Color colour = model.getGuideLineColour();
        ObservableList<Node> items =  group.getChildren();
        final int GUIDES = Default.TOTAL_GUIDE_COUNT.getInt();
        for (int index = 0; index < GUIDES; ++index) {
            Line guide = new Line();
            guide.setStroke(colour);
            guides.add(guide);
            items.add(guide);
        }

        syncGuideLineColour();
        syncGuideLinePositions();
        syncThreadSize();
    }

    /**
     * Constructor.
     * @param title - string displayed as the heading of the Stage.
     */
    public Sample(String title) {
        super();
		// System.out.println("CardSample constructed: " + title);

        model = Model.getInstance();
        defaultColour = model.getBorderColour();

        this.setTitle(title);
        this.resizableProperty().setValue(false);
        setOnCloseRequest(e -> Platform.exit());
        this.initStyle(StageStyle.UNDECORATED);

        this.setTitle(title);
        initializeCardSample();

        this.show();
    }


    /**
     * Initialization after the model has been initialised.
     */
    public void init() {
        for (int i = 0; i < model.getRowCount(); ++i)
            rowList.get(i).setColourIndex(model.getRowColourIndex(i));

        for (int i = 0; i < model.getColumnCount(); ++i)
            colList.get(i).setColourIndex(model.getColColourIndex(i));
    }



    /************************************************************************
     * Support code for the mouse click handler.
     */

    /**
     * Set the row (and repeat rows) to the selected swatch colour.
     * @param row to set the colour of.
     * @param colour index to set the row to.
     * @param count of threads.
     * @param repeat position.
     */
    private void colourRows(int row, int colour, int count, int repeat) {
        final int ROWS = Default.HEIGHT.getInt();
        for (int c = 0; c < count; ++c) {
            model.setRowColourIndex(row, colour);

            for (int i = 0; i < ROWS; i += repeat) {
                final int index = i + row;
                if (index >= ROWS)
                    break;
                
                rowList.get(index).setColourIndex(colour);
            }

            if (++row >= repeat)
                break;
        }
    }

    /**
     * Set the column (and repeat columns) to the selected swatch colour.
     * @param column to set the colour of.
     * @param colour index to set the column to.
     * @param count of threads.
     * @param repeat position.
     */
    private void colourColumns(int column, int colour, int count, int repeat) {
        final int COLS = Default.WIDTH.getInt();
        for (int c = 0; c < count; ++c) {
            model.setColColourIndex(column, colour);

            for (int i = 0; i < COLS; i += repeat) {
                final int index = i + column;
                if (index >= COLS)
                    break;
                
                colList.get(index).setColourIndex(colour);
            }

            if (++column >= repeat)
                break;
        }
    }

    /**
     * Set the row (and repeat rows) to the selected swatch colour.
     * @param row to set the colour of.
     * @param colour index to set the row to.
     */
    private void setRowColour(int row, int colour) {
        final int count = model.getThreadCount();
        final int repeat = model.getRowCount();

        colourRows(row, colour, count, repeat);

        if (model.isDuplicate()) {
            colourColumns(row, colour, count, repeat);
        }
    }

    /**
     * Set the column (and repeat columns) to the selected swatch colour.
     * @param column to set the colour of.
     * @param colour index to set the column to.
     */
    private void setColColour(int column, int colour) {
        final int count = model.getThreadCount();
        final int repeat = model.getColumnCount();

        colourColumns(column, colour, count, repeat);

        if (model.isDuplicate()) {
            colourRows(column, colour, count, repeat);
        }
    }



    /************************************************************************
     * Support code for the mouse move handler.
     */

    private void clearRows() {
        for (Thread thread : rowList) {
            thread.setHighlight(false, defaultColour);
        }
    }

    private void clearColumns() {
        for (Thread thread : colList) {
            thread.setHighlight(false, defaultColour);
        }
    }

    private void highlightThreads(int zone, int pos, boolean highlight, int count) {
        final Color colour = highlight ? model.getGuideLineColour() : defaultColour;

        if (zone == BOTH_ZONE) {
            final int repeat = model.getRowCount();
            for (int c = count; c > 0; c--) {
                if (pos >= repeat)
                    break;

                rowList.get(pos).setHighlight(highlight, colour);
                colList.get(pos++).setHighlight(highlight, colour);
            }
        } else if (zone == ROW_ZONE) {
            final int repeat = model.getRowCount();
            for (int c = count; c > 0; c--) {
                if (pos >= repeat)
                    break;

                rowList.get(pos++).setHighlight(highlight, colour);
            }
        } else
        if (zone == COLUMN_ZONE) {
            final int repeat = model.getColumnCount();
            for (int c = count; c > 0; c--) {
                if (pos >= repeat)
                    break;

                colList.get(pos++).setHighlight(highlight, colour);
            }
        }
    }



    /************************************************************************
     * Synchronize interface.
     */

    /**
     * Synchronise to the current swatch colour.
     */
    public void syncColour() {
        for (Thread stitch : rowList) {
            stitch.syncCurrentColour();
        }
        for (Thread stitch : colList) {
            stitch.syncCurrentColour();
        }
    }

    /**
     * Synchronise to the thread size.
     */
    public void syncThreadSize() {
        gc.setFill(Color.GRAY);
        final double WIDTH = Default.MPC_WIDTH.getFloat()-OFFSET;
        final double HEIGHT = Default.MPC_HEIGHT.getFloat()-OFFSET;
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setStroke(defaultColour);
        gc.setLineWidth(model.getBorderThickness());

        for (Thread stitch : rowList) {
            stitch.syncRowSize();
        }

        for (Thread stitch : colList) {
            stitch.syncColSize();
        }

        syncGuideLinePositions();
    }

    /**
     * Synchronise to the row repeat count.
     */
    public void syncRowCount() {
        final int COUNT = model.getRowCount();
        final int ROWS = Default.HEIGHT.getInt();
        int index = 0;
        boolean visible = true;

        for (int row = 0; row < ROWS; ++row) {
            Thread stitch = rowList.get(row);
            stitch.setColourIndex(model.getRowColourIndex(index));
            stitch.setVisible(visible);
            if (++index >= COUNT) {
                visible = false;
                index = 0;
            }
        }

        syncGuideLinePositions();
    }

    /**
     * Synchronise to the column repeat count.
     */
    public void syncColumnCount() {
        final int COUNT = model.getColumnCount();
        final int COLS = Default.WIDTH.getInt();
        int index = 0;
        boolean visible = true;

        for (int col = 0; col < COLS; ++col) {
            Thread stitch = colList.get(col);
            stitch.setColourIndex(model.getColColourIndex(index));
            stitch.setVisible(visible);
            if (++index >= COUNT) {
                visible = false;
                index = 0;
            }
        }

        syncGuideLinePositions();
    }

    /**
     * Convenience method to synchronise to both the row and column repeat 
     * counts.
     */
    public void syncCount() {
        syncRowCount();
        syncColumnCount();
    }

    /**
     * Synchronize to duplicate the column threads for the rows.
     * Assumes that the row count has been set to the column count.
     */
    public void syncDuplicateThreads() {
        final int COUNT = model.getColumnCount();
        final int COLS = Default.WIDTH.getInt();

        for (int column = 0; column < COLS; ++column) {
            final int colourIndex = model.getColColourIndex(column);

            model.setRowColourIndex(column, colourIndex);
            rowList.get(column).setColourIndex(colourIndex);
            rowList.get(column).setVisible(column < COUNT);
        }
    }

    /**
     * Synchronise the positions of the guide lines to the thread size and 
     * thread counts.
     */
    private void syncGuideLinePositions() {

        final double CSIZE = model.getSwatchWidth();
        final double RSIZE = model.getSwatchHeight();

        final double CSTEP = CSIZE / (Default.GUIDE_COUNT.getFloat() + 1);
        final double RSTEP = RSIZE / (Default.GUIDE_COUNT.getFloat() + 1);

        final double CLENGTH = OFFSET + RSIZE;
        final double RLENGTH = OFFSET + CSIZE;

        final double STARTPOS = OFFSET + (model.getThreadSize() / 2);
        double xPos = STARTPOS + CSTEP;
        double yPos = STARTPOS + RSTEP;
        for (int i = 0; i < Default.GUIDE_COUNT.getInt(); ++i) {
            Line guide = guides.get(i);

            guide.setStartX(xPos);
            guide.setStartY(OFFSET);
            guide.setEndX(xPos);
            guide.setEndY(CLENGTH);

            guide = guides.get(i + Default.GUIDE_COUNT.getInt());

            guide.setStartX(OFFSET);
            guide.setStartY(yPos);
            guide.setEndX(RLENGTH);
            guide.setEndY(yPos);

            xPos += CSTEP;
            yPos += RSTEP;
        }
    }

    /**
     * Synchronize the displaying of the guide lines with the model.
     */
    public void syncGuideVisible() {
        final boolean show = model.isShowGuide();
        for (Line guide : guides) {
            guide.setVisible(show);
        }
    }

    /**
     * Synchronise to the guide line colour.
     */
    public void syncGuideLineColour() {
        final Color colour = model.getGuideLineColour();
        for (Line guide : guides) {
            guide.setStroke(colour);
        }
    }

}
