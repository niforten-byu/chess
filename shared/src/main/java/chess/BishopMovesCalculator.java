package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMovesCalculator implements PieceMovesCalculator {

    /**
     * Calculate all valid moves for a bishop
     *
     * @param board The current chessboard
     * @param position The position of the bishop to calculate moves for
     * @return A collection of valid moves for a bishop
     */
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        PieceMovesCalculator.calculateSlidingMoves(board, position, moves, directions);
        return moves;
    }
}
