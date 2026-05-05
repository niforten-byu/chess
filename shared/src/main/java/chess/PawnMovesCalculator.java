package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovesCalculator implements PieceMovesCalculator {

    /**
     * Calculate all valid moves for a pawn
     *
     * @param board The current chessboard
     * @param position The position of the pawn to calculate moves for
     * @return A collection of valid moves for a pawn
     */

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        // get info about the piece we want to move
        ChessPiece currPiece = board.getPiece(position);
        ChessGame.TeamColor currColor = currPiece.getTeamColor();

        // setup depending on color of pawn
        int direction, startRow, promotionRow;
        if (currColor == ChessGame.TeamColor.BLACK) {
            direction = -1;
            startRow = 7;
            promotionRow = 1;
        }
        else { // white
            direction = 1;
            startRow = 2;
            promotionRow = 8;
        }

        int currRow = position.getRow();
        int currCol = position.getColumn();

        // checking for moves forward
        ChessPosition newPosOneForward = new ChessPosition(currRow + direction, currCol);
        ChessPiece potentialPieceOneForward = board.getPiece(newPosOneForward);
        if (potentialPieceOneForward == null) { // there is not a piece in front
            ChessMove newMoveOneForward = new ChessMove(position, newPosOneForward, );
            moves.add(newMoveOneForward);
            if (currRow == startRow) { // pawn in starting position, can potentially double move
                ChessPosition newPosTwoForward = new ChessPosition(currRow + direction * 2, currCol);
                ChessPiece potentialPieceTwoForward = board.getPiece(newPosTwoForward);
                if (potentialPieceTwoForward == null) { // there is not
                    ChessMove newMoveTwoForward = new ChessMove(position, newPosTwoForward, );
                    moves.add(newMoveTwoForward);
                }
            }
        }

        // check for moves by capturing up diagonally left or right
        if ((currCol - 1) < 9 && ((currCol - 1) > 0 )){
            ChessPosition newPosLeft = new ChessPosition(currRow + direction, currCol - 1);
            ChessPiece potentialPieceLeft = board.getPiece(newPosLeft);
            if (potentialPieceLeft != null) {
                ChessGame.TeamColor potentialPieceColorLeft = potentialPieceLeft.getTeamColor();
                if (potentialPieceColorLeft != currColor) { // the two pieces are the enemy colors
                    ChessMove newMoveLeft = new ChessMove(position, newPosLeft, );
                    moves.add(newMoveLeft);
                }
            }
        }
        if ((currCol + 1) < 9 && ((currCol + 1) > 0 )){
            ChessPosition newPosRight = new ChessPosition(currRow + direction, currCol + 1);
            ChessPiece potentialPieceRight = board.getPiece(newPosRight);
            if (potentialPieceRight != null) {
                ChessGame.TeamColor potentialPieceColorRight = potentialPieceRight.getTeamColor();
                if (potentialPieceColorRight != currColor) { // the two pieces are the enemy colors
                    ChessMove newMoveRight = new ChessMove(position, newPosRight, );
                    moves.add(newMoveRight);
                }
            }
        }

        return moves;
    }

}