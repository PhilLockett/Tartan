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

import java.util.ArrayList;
import java.util.Vector;

import javafx.application.Platform;
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

    private final static int NONE_ACTIVE = 0;
    private final static int DELETE_REQUEST = 1;
    private final static int INSERT_REQUEST = 2;
    
    private Model model;

    private Group group;
    private Canvas canvas;
    private GraphicsContext gc;

    private double dx;	// Difference between the size of the stage and the size of the scene.
    private double dy;

    private Vector<Thread> rowList = new Vector<Thread>(Default.HEIGHT.getInt());
    private Vector<Thread> colList = new Vector<Thread>(Default.WIDTH.getInt());
    private Vector<Line> guides = new Vector<Line>(Default.TOTAL_GUIDE_COUNT.getInt());

    private final Color defaultColour;

    private int lastZone = 0;
    private int lastPos = 0;

    private double x = 0.0;
    private double y = 0.0;

    private Label heading = new Label();



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
        private final int index;
        private final boolean row;
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
        public int getColourIndex() { return colourIndex; }
        public boolean isVisible() { return stitch.isVisible(); }


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
            if (colourIndex != model.getSelectedColourIndex())
                return false;

            final Color color = model.getSelectedColour();

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
        rowList.get(index).setColourIndex(colourIndex);
    }

    private void setColColourIndex(int index, int colourIndex) {
        colList.get(index).setColourIndex(colourIndex);
    }

    public int getRowColourIndex(int index) {
        return rowList.get(index).getColourIndex();
    }

    public int getColColourIndex(int index) {
        return colList.get(index).getColourIndex();
    }

    private void repeatRows() {
        final int ACTIVE = model.getRowCount();
        final int MAX = rowList.size();
        for (int index = ACTIVE; index < MAX; ++index) {
            final int colourIndex = getRowColourIndex(index % ACTIVE);

            setRowColourIndex(index, colourIndex);
        }
    }

    private void repeatColumns() {
        final int ACTIVE = model.getColumnCount();
        final int MAX = colList.size();
        for (int index = ACTIVE; index < MAX; ++index) {
            final int colourIndex = getColColourIndex(index % ACTIVE);

            setColColourIndex(index, colourIndex);
        }
    }

    private void rotateUp() {
        final int ACTIVE = model.getRowCount();

        final int safeIndex = getRowColourIndex(0);
        for (int index = 1; index < ACTIVE; ++index) {
            final int colourIndex = getRowColourIndex(index);

            setRowColourIndex(index-1, colourIndex);
        }
        setRowColourIndex(ACTIVE-1, safeIndex);

        repeatRows();
    }

    private void rotateDown() {
        final int ACTIVE = model.getRowCount();

        final int safeIndex = getRowColourIndex(ACTIVE-1);
        for (int index = ACTIVE-1; index > 0 ; --index) {
            final int colourIndex = getRowColourIndex(index-1);

            setRowColourIndex(index, colourIndex);
        }
        setRowColourIndex(0, safeIndex);

        repeatRows();
    }

    private void rotateLeft() {
        final int ACTIVE = model.getColumnCount();

        final int safeIndex = getColColourIndex(0);
        for (int index = 1; index < ACTIVE; ++index) {
            final int colourIndex = getColColourIndex(index);

            setColColourIndex(index-1, colourIndex);
        }
        setColColourIndex(ACTIVE-1, safeIndex);

        repeatColumns();
    }

    private void rotateRight() {
        final int ACTIVE = model.getColumnCount();

        final int safeIndex = getColColourIndex(ACTIVE-1);
        for (int index = ACTIVE-1; index > 0 ; --index) {
            final int colourIndex = getColColourIndex(index-1);

            setColColourIndex(index, colourIndex);
        }
        setColColourIndex(0, safeIndex);

        repeatColumns();
    }


    private void augmentHeading(String label) {
        if ((label == null) || (label.isBlank()))
            heading.setText(" " + this.getTitle());
        else
            heading.setText(" " + this.getTitle() + " - " + label);
    }

    private void setHeading() { augmentHeading(null); }


    private Vector<Integer> requests = new Vector<Integer>();

    private void addRequest(Integer request) {
        if (requests.contains(request) == false) {
            requests.add(request);
        }
    }

    private void removeRequest(Integer request) {
        requests.remove(request);
    }

    private int getActive() {
        if (requests.isEmpty() == true) {
            return NONE_ACTIVE;
        }
        
        return requests.firstElement();
    }

    private void updateHeading() {
        switch (getActive()) {
        case DELETE_REQUEST:
            augmentHeading("Use the mouse to highlight threads then click to delete them");
            break;

        case INSERT_REQUEST:
            augmentHeading("Use the mouse to highlight the position then click to insert threads");
            break;

        default:
            setHeading();
            break;
        }
    }

    private void deleteRequest() {
        addRequest(DELETE_REQUEST);

        final int active = getActive();
        if (active == DELETE_REQUEST) {
            updateHeading();
        }
    }

    private void insertRequest() {
        addRequest(INSERT_REQUEST);

        final int active = getActive();
        if (active == INSERT_REQUEST) {
            updateHeading();
        }
    }

    private void deleteRelease() {
        removeRequest(DELETE_REQUEST);

        updateHeading();
    }

    private void insertRelease() {
        removeRequest(INSERT_REQUEST);

        updateHeading();
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
                deleteRequest();
                break;

            case CONTROL:
                insertRequest();
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
            switch (event.getCode()) {
            case ALT:
                deleteRelease();
                break;

            case CONTROL:
                insertRelease();
                break;
    
            default:
                break;
            }
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
            if ((zone != lastZone) && (lastZone != NONE_ZONE)) {
                final int scope = model.isDuplicate() ? BOTH_ZONE : lastZone;
                clearHighlights(scope);
            }

            // Is there work to do?
            if (zone == NONE_ZONE) {
                lastZone = NONE_ZONE;

                return; // No highlights required so abort.
            }
            
            // Draw highlights.
            final int pos = (zone == ROW_ZONE) ? yPosToRow(y) : xPosToCol(x);
            final int scope = model.isDuplicate() ? BOTH_ZONE : zone;
            if (zone != lastZone) {
                lastZone = zone;
                highlightThreads(scope, pos, true);
            } else if (lastPos != pos) {
                highlightThreads(scope, lastPos, false);
                highlightThreads(scope, pos, true);
            }

            lastPos = pos;
        });

        scene.setOnMouseExited(event -> {
            final int scope = model.isDuplicate() ? BOTH_ZONE : lastZone;
            clearHighlights(scope);
            lastZone = NONE_ZONE;
        });

        scene.setOnMouseClicked(event -> {
            final double x = event.getSceneX();
            final double y = event.getSceneY();
            final int zone = getZone(x, y);

            if (zone != NONE_ZONE) {
                final int pos = (zone == ROW_ZONE) ? yPosToRow(y) : xPosToCol(x);
                final int scope = model.isDuplicate() ? BOTH_ZONE : zone;
                switch (getActive()) {
                case DELETE_REQUEST:
                    deleteThreads(scope, pos);
                    break;

                case INSERT_REQUEST:
                    insertThreads(scope, pos);
                    break;

                default:
                    setThreadColour(scope, pos);
                    break;
                }
            }
        });

    }

    private void deleteRowThreads(int pos) {
        final int COUNT = model.getThreadCount();
        final int ACTIVE = model.getRowCount();

        int source = pos + COUNT;
        for (int index = pos; source < ACTIVE; ++index) {
            final int colourIndex = rowList.get(source++).getColourIndex();
            rowList.get(index).setColourIndex(colourIndex);
        }

        final int SIZE = ((pos + COUNT) > ACTIVE ? (ACTIVE-pos) : COUNT);
        for (int index = (ACTIVE-SIZE); index < ACTIVE; ++index) {
            rowList.get(index).setVisible(false);
        }

        model.incRowCount(-SIZE);
        repeatRows();
    }

    private void deleteColumnThreads(int pos) {
        final int COUNT = model.getThreadCount();
        final int ACTIVE = model.getColumnCount();

        int source = pos + COUNT;
        for (int index = pos; source < ACTIVE; ++index) {
            final int colourIndex = colList.get(source++).getColourIndex();
            colList.get(index).setColourIndex(colourIndex);
        }

        final int SIZE = ((pos + COUNT) > ACTIVE ? (ACTIVE-pos) : COUNT);
        for (int index = (ACTIVE-SIZE); index < ACTIVE; ++index) {
            colList.get(index).setVisible(false);
        }

        model.incColumnCount(-SIZE);
        repeatColumns();
    }

    private void insertRowThreads(int pos) {
        final int COUNT = model.getThreadCount();
        final int ACTIVE = model.getRowCount();
        final int MAX = rowList.size();
        final int SIZE = ((pos + COUNT) >= MAX ? (MAX-pos-1) : COUNT);

        for (int index = ACTIVE; index < (ACTIVE+SIZE); ++index) {
            rowList.get(index).setVisible(true);
        }

        int source = ACTIVE-1;
        for (int index = ACTIVE+SIZE-1; source >= pos ; --index) {
            final int colourIndex = rowList.get(source--).getColourIndex();
            rowList.get(index).setColourIndex(colourIndex);
        }

        final int colourIndex = model.getSelectedColourIndex();
        for (int index = pos; index < (pos+SIZE); ++index) {
            rowList.get(index).setColourIndex(colourIndex);
        }

        model.incRowCount(SIZE);
        repeatRows();
    }

    private void insertColumnThreads(int pos) {
        final int COUNT = model.getThreadCount();
        final int ACTIVE = model.getColumnCount();
        final int MAX = colList.size();
        final int SIZE = ((pos + COUNT) >= MAX ? (MAX-pos-1) : COUNT);

        for (int index = ACTIVE; index < (ACTIVE+SIZE); ++index) {
            colList.get(index).setVisible(true);
        }

        int source = ACTIVE-1;
        for (int index = ACTIVE+SIZE-1; source >= pos ; --index) {
            final int colourIndex = colList.get(source--).getColourIndex();
            colList.get(index).setColourIndex(colourIndex);
        }

        final int colourIndex = model.getSelectedColourIndex();
        for (int index = pos; index < (pos+SIZE); ++index) {
            colList.get(index).setColourIndex(colourIndex);
        }

        model.incColumnCount(SIZE);
        repeatColumns();
    }

    private void deleteThreads(int scope, int pos) {
        if (scope == BOTH_ZONE) {
            deleteRowThreads(pos);
            deleteColumnThreads(pos);
        } else if (scope == ROW_ZONE) {
            deleteRowThreads(pos);
        } else if (scope == COLUMN_ZONE) {
            deleteColumnThreads(pos);
        }

        syncGuideLinePositions();
    }

    private void insertThreads(int scope, int pos) {
        if (scope == BOTH_ZONE) {
            insertRowThreads(pos);
            insertColumnThreads(pos);
        } else if (scope == ROW_ZONE) {
            insertRowThreads(pos);
        } else if (scope == COLUMN_ZONE) {
            insertColumnThreads(pos);
        }

        syncGuideLinePositions();
    }
 


    /************************************************************************
     * Support code for the Initialization of the Sample.
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

        updateHeading();
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
     */
    public Sample() {
        super();
		// System.out.println("CardSample constructed: " + title);

        model = Model.getInstance();
        defaultColour = model.getBorderColour();

        this.setTitle("Sample");
        this.resizableProperty().setValue(false);
        setOnCloseRequest(e -> Platform.exit());
        this.initStyle(StageStyle.UNDECORATED);

        initializeCardSample();

        this.show();
    }


    /**
     * Initialization after the model has been initialised.
     */
    public void init() {
    }



    /************************************************************************
     * Support code for the mouse click handler.
     */

    /**
     * Set the row (and repeat rows) to the selected swatch colour.
     * @param row to set the colour of.
     * @param colourIndex to set the row to.
     * @param count of threads.
     * @param repeat start point.
     */
    private void colourRows(int row, int colourIndex, int count, int repeat) {
        final int MAX = rowList.size();
        for (int thread = 0; thread < count; ++thread) {
            setRowColourIndex(row, colourIndex);

            for (int i = 0; i < MAX; i += repeat) {
                final int index = i + row;
                if (index >= MAX)
                    break;

                setRowColourIndex(index, colourIndex);
            }

            if (++row >= repeat)
                break;
        }
    }

    /**
     * Set the column (and repeat columns) to the selected swatch colour.
     * @param column to set the colour of.
     * @param colourIndex to set the column to.
     * @param count of threads.
     * @param repeat start point.
     */
    private void colourColumns(int column, int colourIndex, int count, int repeat) {
        final int MAX = colList.size();
        for (int thread = 0; thread < count; ++thread) {
            setColColourIndex(column, colourIndex);

            for (int i = 0; i < MAX; i += repeat) {
                final int index = i + column;
                if (index >= MAX)
                    break;

                setColColourIndex(index, colourIndex);
            }

            if (++column >= repeat)
                break;
        }
    }

    /**
     * Set the row and/or column (and repeats) to the selected swatch colour.
     * @param scope to set the colour for.
     * @param pos of row and/or column to set the colour of.
     */
    private void setThreadColour(int scope, int pos) {
        final int colourIndex = model.getSelectedColourIndex();
        final int count = model.getThreadCount();
        final int repeat = (scope == COLUMN_ZONE) ? model.getColumnCount() : model.getRowCount();

        if (scope == BOTH_ZONE) {
            colourRows(pos, colourIndex, count, repeat);
            colourColumns(pos, colourIndex, count, repeat);
        } else if (scope == ROW_ZONE) {
            colourRows(pos, colourIndex, count, repeat);
        } else if (scope == COLUMN_ZONE) {
            colourColumns(pos, colourIndex, count, repeat);
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

    private void clearHighlights(int scope) {
        if (scope == BOTH_ZONE) {
            clearRows();
            clearColumns();
        } else if (scope == ROW_ZONE) {
            clearRows();
        } else if (scope == COLUMN_ZONE) {
            clearColumns();
        }
    }

    private void highlightThreads(int scope, int pos, boolean highlight) {
        final Color colour = highlight ? model.getGuideLineColour() : defaultColour;
        final int count = model.getThreadCount();
        final int repeat = (scope == COLUMN_ZONE) ? model.getColumnCount() : model.getRowCount();

        if (scope == BOTH_ZONE) {
            for (int c = count; c > 0; c--, pos++) {
                if (pos >= repeat)
                    break;

                rowList.get(pos).setHighlight(highlight, colour);
                colList.get(pos).setHighlight(highlight, colour);
            }
        } else if (scope == ROW_ZONE) {
            for (int c = count; c > 0; c--, pos++) {
                if (pos >= repeat)
                    break;

                rowList.get(pos).setHighlight(highlight, colour);
            }
        } else if (scope == COLUMN_ZONE) {
            for (int c = count; c > 0; c--, pos++) {
                if (pos >= repeat)
                    break;

                colList.get(pos).setHighlight(highlight, colour);
            }
        }
    }



    /************************************************************************
     * Public interface.
     */

    public void setRowList(ArrayList<Integer> list) {
        final int ACTIVE = list.size();
        final int MAX = rowList.size();

        for (int index = 0; index < MAX; ++index) {
            final int colourIndex = list.get(index % ACTIVE);

            Thread stitch = rowList.get(index);
            stitch.setColourIndex(colourIndex);
            stitch.setVisible(index < ACTIVE);
        }

        syncGuideLinePositions();
    }

    public void setColumnList(ArrayList<Integer> list) {
        final int ACTIVE = list.size();
        final int MAX = colList.size();

        for (int index = 0; index < MAX; ++index) {
            final int colourIndex = list.get(index % ACTIVE);

            Thread stitch = colList.get(index);
            stitch.setColourIndex(colourIndex);
            stitch.setVisible(index < ACTIVE);
        }

        syncGuideLinePositions();
    }

    public ArrayList<Integer> getRowList() {
        ArrayList<Integer> list = new ArrayList<Integer>(model.getRowCount());

        for (Thread stitch : rowList) {
            if (!stitch.isVisible())
                break;

            list.add(stitch.getColourIndex());
        }

        return list;
    }

    public ArrayList<Integer> getColumnList() {
        ArrayList<Integer> list = new ArrayList<Integer>(model.getColumnCount());

        for (Thread stitch : colList) {
            if (!stitch.isVisible())
                break;

            list.add(stitch.getColourIndex());
        }

        return list;
    }

    public void setRowCount(int size) {
        final int ACTIVE = size;
        final int MAX = rowList.size();

        for (int index = 0; index < MAX; ++index) {
            Thread stitch = rowList.get(index);
            stitch.setVisible(index < ACTIVE);
        }

        repeatRows();
        syncGuideLinePositions();
    }

    public void setColumnCount(int size) {
        final int ACTIVE = size;
        final int MAX = colList.size();

        for (int index = 0; index < MAX; ++index) {
            Thread stitch = colList.get(index);
            stitch.setVisible(index < ACTIVE);
        }

        repeatColumns();
        syncGuideLinePositions();
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
    private void syncRowCount() {
        final int ACTIVE = model.getRowCount();
        final int MAX = rowList.size();

        for (int index = 0; index < MAX; ++index) {
            Thread stitch = rowList.get(index);
            stitch.setColourIndex(getRowColourIndex(index % ACTIVE));
            stitch.setVisible(index < ACTIVE);
        }

        syncGuideLinePositions();
    }

    /**
     * Synchronise to the column repeat count.
     */
    private void syncColumnCount() {
        final int ACTIVE = model.getColumnCount();
        final int MAX = colList.size();

        for (int index = 0; index < MAX; ++index) {
            Thread stitch = colList.get(index);
            stitch.setColourIndex(getColColourIndex(index % ACTIVE));
            stitch.setVisible(index < ACTIVE);
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
        final int ACTIVE = model.getColumnCount();
        final int MAX = colList.size();

        for (int column = 0; column < MAX; ++column) {
            final int colourIndex = getColColourIndex(column % ACTIVE);

            Thread stitch = rowList.get(column);
            stitch.setColourIndex(colourIndex);
            stitch.setVisible(column < ACTIVE);
        }

        syncGuideLinePositions();
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
