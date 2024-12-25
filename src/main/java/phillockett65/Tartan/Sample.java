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

    private Warp rowList = new Warp(true, Default.MIN_THREAD_COUNT.getInt(), Default.HEIGHT.getInt());
    private Warp colList = new Warp(false, Default.MIN_THREAD_COUNT.getInt(), Default.WIDTH.getInt());

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
     * Get the width of the swatch in pixels.
     * @return the width of the swatch in pixels.
     */
    public double getSwatchWidth() { return model.getThreadSize() * getColumnCount(); }

    /**
     * Get the height of the swatch in pixels.
     * @return the height of the swatch in pixels.
     */
    public double getSwatchHeight() { return model.getThreadSize() * getRowCount(); }


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
        if (x >= getSwatchWidth()) {
            return NONE_ZONE;
        }
        if (y >= getSwatchHeight()) {
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
     * Support code for the handlers. 
     */

    public int getRowColourIndex(int index) {
        return rowList.getColourIndex(index);
    }

    public int getColColourIndex(int index) {
        return colList.getColourIndex(index);
    }

    private void rotateUp() {
        rowList.rotateIncrease();
        if (model.isDuplicate()) {
            colList.rotateIncrease();
        }
}

    private void rotateDown() {
        rowList.rotateDecrease();
        if (model.isDuplicate()) {
            colList.rotateDecrease();
        }
}

    private void rotateLeft() {
        colList.rotateIncrease();
        if (model.isDuplicate()) {
            rowList.rotateIncrease();
        }
}

    private void rotateRight() {
        colList.rotateDecrease();
        if (model.isDuplicate()) {
            rowList.rotateDecrease();
        }
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
                break;

            case DOWN:
                rotateDown();
                break;

            case LEFT:
                rotateLeft();
                break;

            case RIGHT:
                rotateRight();
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

    private void deleteThreads(int scope, int pos) {
        if (scope == BOTH_ZONE) {
            rowList.deleteThreads(pos);
            colList.deleteThreads(pos);
            model.syncRowCountSVF();
            model.syncColumnCountSVF();
        } else if (scope == ROW_ZONE) {
            rowList.deleteThreads(pos);
            model.syncRowCountSVF();
        } else if (scope == COLUMN_ZONE) {
            colList.deleteThreads(pos);
            model.syncColumnCountSVF();
        }

        syncGuideLinePositions();
    }

    private void insertThreads(int scope, int pos) {
        if (scope == BOTH_ZONE) {
            rowList.insertThreads(pos);
            colList.insertThreads(pos);
            model.syncRowCountSVF();
            model.syncColumnCountSVF();
        } else if (scope == ROW_ZONE) {
            rowList.insertThreads(pos);
            model.syncRowCountSVF();
        } else if (scope == COLUMN_ZONE) {
            colList.insertThreads(pos);
            model.syncColumnCountSVF();
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

        final Color colour = model.getGuideLineColour();
        ObservableList<Node> items =  group.getChildren();
        final int GUIDES = Default.TOTAL_GUIDE_COUNT.getInt();
        for (int index = 0; index < GUIDES; ++index) {
            Line guide = new Line();
            guide.setStroke(colour);
            guides.add(guide);
            items.add(guide);
        }
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
        rowList.init(group, gc);
        colList.init(group, gc);

        syncGuideLineColour();
        syncGuideLinePositions();
        syncThreadSize();
    }



    /************************************************************************
     * Support code for the mouse click handler.
     */

    /**
     * Set the row and/or column (and repeats) to the selected swatch colour.
     * @param scope to set the colour for.
     * @param pos of row and/or column to set the colour of.
     */
    private void setThreadColour(int scope, int pos) {
        final int COLOURINDEX = model.getSelectedColourIndex();
        final int COUNT = model.getThreadCount();
        final int REPEAT = (scope == COLUMN_ZONE) ? getColumnCount() : getRowCount();

        if (scope == BOTH_ZONE) {
            rowList.colourThreads(pos, COLOURINDEX, COUNT, REPEAT);
            colList.colourThreads(pos, COLOURINDEX, COUNT, REPEAT);
        } else if (scope == ROW_ZONE) {
            rowList.colourThreads(pos, COLOURINDEX, COUNT, REPEAT);
        } else if (scope == COLUMN_ZONE) {
            colList.colourThreads(pos, COLOURINDEX, COUNT, REPEAT);
        }
    }



    /************************************************************************
     * Support code for the mouse move handler.
     */

    private void clearHighlights(int scope) {
        if (scope == BOTH_ZONE) {
            rowList.clearThreads();
            colList.clearThreads();
        } else if (scope == ROW_ZONE) {
            rowList.clearThreads();
        } else if (scope == COLUMN_ZONE) {
            colList.clearThreads();
        }
    }

    private void highlightThreads(int scope, int pos, boolean highlight) {
        final Color COLOUR = highlight ? model.getGuideLineColour() : defaultColour;
        final int COUNT = model.getThreadCount();
        final int REPEAT = (scope == COLUMN_ZONE) ? getColumnCount() : getRowCount();

        if (scope == BOTH_ZONE) {
            for (int c = COUNT; c > 0; c--, pos++) {
                if (pos >= REPEAT)
                    break;

                rowList.highlightThread(pos, highlight, COLOUR);
                colList.highlightThread(pos, highlight, COLOUR);
            }
        } else if (scope == ROW_ZONE) {
            for (int c = COUNT; c > 0; c--, pos++) {
                if (pos >= REPEAT)
                    break;

                rowList.highlightThread(pos, highlight, COLOUR);
            }
        } else if (scope == COLUMN_ZONE) {
            for (int c = COUNT; c > 0; c--, pos++) {
                if (pos >= REPEAT)
                    break;

                colList.highlightThread(pos, highlight, COLOUR);
            }
        }
    }



    /************************************************************************
     * Public interface.
     */

    public void setRowList(ArrayList<Integer> list) {
        rowList.setList(list);

        syncGuideLinePositions();
    }

    public void setColumnList(ArrayList<Integer> list) {
        colList.setList(list);

        syncGuideLinePositions();
    }

    public ArrayList<Integer> getRowList() {
        return rowList.getList();
    }

    public ArrayList<Integer> getColumnList() {
        return colList.getList();
    }

    public void setRowCount(int size) {
        rowList.setActive(size);

        syncGuideLinePositions();
    }

    public void setColumnCount(int size) {
        colList.setActive(size);

        syncGuideLinePositions();
    }

    public int getRowCount() { return rowList.getActive(); }
    public int getColumnCount() { return colList.getActive(); }



    /************************************************************************
     * Synchronize interface.
     */

    /**
     * Synchronise to the current swatch colour.
     */
    public void syncColour() {
        rowList.syncColour();
        colList.syncColour();
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

        rowList.syncThreadSize();
        colList.syncThreadSize();

        syncGuideLinePositions();
    }


    /**
     * Synchronise to both the row and column repeat counts.
     */
    public void syncCount() {
        rowList.setActive(getRowCount());
        colList.setActive(getColumnCount());

        syncGuideLinePositions();
    }

    /**
     * Synchronize to duplicate the column threads for the rows.
     * Assumes that the row count has been set to the column count.
     */
    public void syncDuplicateThreads() {
        final int ACTIVE = colList.getActive();

        for (int index = 0; index < ACTIVE; ++index) {
            final int colourIndex = colList.getColourIndex(index);

            rowList.setColourIndex(index, colourIndex);
        }
        rowList.setActive(ACTIVE);

        syncGuideLinePositions();
    }

    /**
     * Synchronise the positions of the guide lines to the thread size and 
     * thread counts.
     */
    private void syncGuideLinePositions() {

        final double CSIZE = getSwatchWidth();
        final double RSIZE = getSwatchHeight();

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
