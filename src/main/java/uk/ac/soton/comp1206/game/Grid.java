package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uk.ac.soton.comp1206.component.GameBlock;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;


    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Return ture or false if the piece can be player
     *
     * @param gamePiece GamePiece
     * @param x x-position of selected block
     * @param y y-position of selected block
     * @return boolean if can play piece at this position
     */
    public Boolean canPlayPiece(GamePiece gamePiece, int x, int y){

        SimpleBooleanProperty available = new SimpleBooleanProperty(true);

        //Initialise the requiring space for the chosen gamePiece
        int[][] space = new int[3][3];
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                space[i][j] = 0;
            }
        }

        //Fill in the space around the chosen position
        space[0][0] = this.get(x-1,y-1);
        space[0][1] = this.get(x-1, y);
        space[0][2] = this.get(x-1,y+1);
        space[1][0] = this.get(x,y-1);
        space[1][1] = this.get(x, y);
        space[1][2] = this.get(x,y+1);
        space[2][0] = this.get(x+1,y-1);
        space[2][1] = this.get(x+1, y);
        space[2][2] = this.get(x+1,y+1);


        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                //If no available space
                if (gamePiece.getBlocks()[i][j] != 0 && space[i][j] != 0){
                    available.set(false);
                }
            }
        }


        return available.get();
    }

    /**
     * Place the piece in the grid
     * @param gamePiece selected GamePiece
     * @param x x-position
     * @param y y-position
     */

    public void playPiece(GamePiece gamePiece, int x, int y){
        //Avoid game continue after ESC pressed
        if(gamePiece == null){return;}

        if(this.get(x-1,y-1)==0)this.set(x-1,y-1, gamePiece.getBlocks()[0][0]);
        if(this.get(x,y-1)==0)this.set(x,y-1,gamePiece.getBlocks()[1][0]);
        if(this.get(x+1,y-1)==0)this.set(x+1,y-1,gamePiece.getBlocks()[2][0]);

        if(this.get(x-1,y)==0)this.set(x-1,y,gamePiece.getBlocks()[0][1]);
        if(this.get(x,y)==0)this.set(x,y,gamePiece.getBlocks()[1][1]);
        if(this.get(x+1,y)==0)this.set(x+1,y,gamePiece.getBlocks()[2][1]);

        if(this.get(x-1,y+1)==0)this.set(x-1,y+1,gamePiece.getBlocks()[0][2]);
        if(this.get(x,y+1)==0)this.set(x,y+1,gamePiece.getBlocks()[1][2]);
        if(this.get(x+1,y+1)==0)this.set(x+1,y+1,gamePiece.getBlocks()[2][2]);

    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

}
