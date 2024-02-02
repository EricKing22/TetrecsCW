package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * Next piece listener is used to play the next game piece on the piece board,
 * when the next piece is generated.
 *
 */
public interface NextPieceListener {

    /**
     * Handle a game piece which is the next piece to be played
     * @param nextPiece GamePiece that is going to be played next
     * @param followingPiece GamePiece that is after the next piece
     */
    public void nextPiece(GamePiece nextPiece,GamePiece followingPiece);


}
