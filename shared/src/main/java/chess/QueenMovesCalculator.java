package chess;

import java.util.ArrayList;
import java.util.Collection;

public class QueenMovesCalculator implements PieceMovesCalculator {

    /**
     * Calculate all valid moves for a queen
     *
     * @param board The current chessboard
     * @param position The position of the queen to calculate moves for
     * @return A collection of valid moves for a queen
     */
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        // call rook and bishop and combine move list
        moves.addAll(new RookMovesCalculator().pieceMoves(board, position));
        moves.addAll(new BishopMovesCalculator().pieceMoves(board, position));

        return moves;
    }
}