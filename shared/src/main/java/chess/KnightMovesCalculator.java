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
        PieceMovesCalculator.calculateJumpMoves(board, position, moves, directions);
        return moves;
    }
}