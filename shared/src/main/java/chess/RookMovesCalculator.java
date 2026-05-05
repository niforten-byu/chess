package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMovesCalculator implements PieceMovesCalculator {

    /**
     * Calculate all valid moves for a rook
     *
     * @param board The current chessboard
     * @param position The position of the rook to calculate moves for
     * @return A collection of valid moves for a rook
     */

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

        for (int[] dir : directions) {
            int rowOffset = dir[0];
            int colOffset = dir[1];

            // get info about the piece we want to move
            int currRow = position.getRow();
            int currCol = position.getColumn();
            ChessPiece currPiece = board.getPiece(position);
            ChessGame.TeamColor currColor = currPiece.getTeamColor();


            while(true) {
                // check based on the direction of the board it can go by adding offset
                currRow += rowOffset;
                currCol += colOffset;

                // check out of bounds
                if (currRow < 1 || currRow > 8 || currCol < 1 || currCol > 8) {
                    break;
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
                        break;
                    }
                    else { // they are not the same color
                        ChessMove newMove = new ChessMove(position, newPos, null);
                        moves.add(newMove);
                        break;
                    }

                }
            }

        }
        return moves;
    }

}
