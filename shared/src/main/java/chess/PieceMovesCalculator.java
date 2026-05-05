package chess;

import java.util.Collection;

public interface PieceMovesCalculator {
    /**
     * Calculate all valid moves for a piece at a given position.
     *
     * @param board The current chessboard
     * @param position The position of the piece to calculate moves for
     * @return A collection of valid moves
     */
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);
}

