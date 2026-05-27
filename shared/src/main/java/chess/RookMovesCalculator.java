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
        PieceMovesCalculator.calculateSlidingMoves(board, position, moves, directions);
        return moves;
    }
}
