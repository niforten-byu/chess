package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMovesCalculator implements PieceMovesCalculator {

    /**
     * Calculate all valid moves for a knight
     *
     * @param board The current chessboard
     * @param position The position of the knight to calculate moves for
     * @return A collection of valid moves for a knight
     */

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        int[][] directions = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};

        // get info about the piece we want to move
        ChessPiece currPiece = board.getPiece(position);
        ChessGame.TeamColor currColor = currPiece.getTeamColor();

        for (int[] dir : directions) {
            int rowOffset = dir[0];
            int colOffset = dir[1];

            int currRow = position.getRow() + rowOffset;
            int currCol = position.getColumn() + colOffset;

            if (currRow < 1 || currRow > 8 || currCol < 1 || currCol > 8) {
                continue;
            }

            // create it's new position after offset
            ChessPosition newPos = new ChessPosition(currRow, currCol);

            // see if there is already a piece in that position
            ChessPiece potentialPiece = board.getPiece(newPos);
            if (potentialPiece == null) { // there is not
                ChessMove newMove = new ChessMove(position, newPos, null);
                moves.add(newMove);
            }
            else { // there is
                ChessGame.TeamColor potentialPieceColor = potentialPiece.getTeamColor();
                if (potentialPieceColor == currColor) { // the two pieces are the same color
                    continue;
                }
                else { // they are not the same color
                    ChessMove newMove = new ChessMove(position, newPos, null);
                    moves.add(newMove);
                    continue;
                }

            }

        }
        return moves;
    }

}