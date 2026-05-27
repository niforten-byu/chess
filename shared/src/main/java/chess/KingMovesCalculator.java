package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KingMovesCalculator implements PieceMovesCalculator {

    /**
     * Calculate all valid moves for a king
     *
     * @param board The current chessboard
     * @param position The position of the king to calculate moves for
     * @return A collection of valid moves for a king
     */
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        PieceMovesCalculator.calculateJumpMoves(board, position, moves, directions);
        return moves;
    }
}