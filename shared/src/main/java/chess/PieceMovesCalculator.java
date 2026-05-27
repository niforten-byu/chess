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

    // move logic for rooks, bishops, and queens
    static void calculateSlidingMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> moves, int[][] directions) {
        // get info about piece
        ChessPiece currPiece = board.getPiece(position);
        ChessGame.TeamColor currColor = currPiece.getTeamColor();

        for (int[] dir : directions) {
            int currRow = position.getRow();
            int currCol = position.getColumn();

            while (true) {
                // check based on the direction of the board it can go by adding offset
                currRow += dir[0];
                currCol += dir[1];

                // check out of bounds
                if (currRow < 1 || currRow > 8 || currCol < 1 || currCol > 8) {
                    break;
                }

                // create it's new position after offset
                ChessPosition newPos = new ChessPosition(currRow, currCol);

                // see if there is already a piece in that position
                ChessPiece potentialPiece = board.getPiece(newPos);
                if (potentialPiece == null) { // there is not
                    moves.add(new ChessMove(position, newPos, null));
                } else { // there is
                    if (potentialPiece.getTeamColor() != currColor) { // they are not the same color
                        moves.add(new ChessMove(position, newPos, null));
                    }
                    // the two pieces are the same color
                    break;
                }
            }
        }
    }

    // move logic for kings and knights
    static void calculateJumpMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> moves, int[][] directions) {
        // get info about the piece we want to move
        ChessPiece currPiece = board.getPiece(position);
        ChessGame.TeamColor currColor = currPiece.getTeamColor();

        for (int[] dir : directions) {
            // check based on the direction of the board it can go by adding offset
            int currRow = position.getRow() + dir[0];
            int currCol = position.getColumn() + dir[1];

            // check out of bounds
            if (currRow < 1 || currRow > 8 || currCol < 1 || currCol > 8) {
                continue;
            }

            // create it's new position after offset
            ChessPosition newPos = new ChessPosition(currRow, currCol);

            // see if there is already a piece in that position
            ChessPiece potentialPiece = board.getPiece(newPos);
            if (potentialPiece == null) { // there is not
                moves.add(new ChessMove(position, newPos, null));
            }
            else { // there is
                if (potentialPiece.getTeamColor() != currColor) { // they are not the same color
                    moves.add(new ChessMove(position, newPos, null));
                }
                // the two pieces are the same color so we continue
            }
        }
    }
}

