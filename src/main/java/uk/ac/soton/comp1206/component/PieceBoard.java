package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * GameBoard component that are used to show the next two game peices.
 */
public class PieceBoard extends GameBoard{
    /**
     * Visual representation of next game piece
     * @param cols number of cols
     * @param rows number of rows
     * @param width number of width
     * @param height number of height
     */
    public PieceBoard(int cols, int rows, double width, double height) {
        super(cols,rows, width, height);
    }

    /**
     * Play the current gamePiece to the piece board at (1,1)
     * @param gamePiece GamePiece that's going to be placed
     */
    public void setPiece(GamePiece gamePiece){
        //Clear the grid
        for (int i = 0; i < this.getColumnCount(); i++){
            for (int j = 0; j < this.getRowCount(); j++ ){
                grid.set(j,i,0);
            }
        }
        grid.playPiece(gamePiece,1,1);

        //Draw a semi-transparent circle at the center if it's at the center of a pieceBoard
        this.getBlock(1,1).drawCircle();

    }

}
