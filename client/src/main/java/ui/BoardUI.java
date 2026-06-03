package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class BoardUI {

    /**
     * draw a string representation of chessboard
     * @param board current board state
     * @param isWhitePerspective True for drawing from White's perspective
     */
    public static String drawBoard(ChessBoard board, boolean isWhitePerspective) {
        StringBuilder output = new StringBuilder();

        // define horizontal axis labels based on white or black
        String[] headers = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};
        if (!isWhitePerspective) {
            headers = new String[]{" h ", " g ", " f ", " e ", " d ", " c ", " b ", " a "};
        }

        output.append("\n").append(drawHeaders(headers));

        // determine number directions based on white or black
        int startRow;
        int endRow;
        int rowDirection;
        int startCol;
        int endCol;
        int colDirection;

        if (isWhitePerspective) {
            startRow = 8;
            endRow = 1;
            rowDirection = -1;

            startCol = 1;
            endCol = 8;
            colDirection = 1;
        } else {
            startRow = 1;
            endRow = 8;
            rowDirection = 1;

            startCol = 8;
            endCol = 1;
            colDirection = -1;
        }

        // loop over board
        for (int row = startRow; row!= endRow + rowDirection; row += rowDirection) {
            // left border
            output.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY)
                    .append(EscapeSequences.SET_TEXT_COLOR_BLACK)
                    .append(" ").append(row).append(" ");

            // draw squares and pieces
            for (int col = startCol; col != endCol + colDirection; col += colDirection) {
                // h1 (row 1, col 8) is light. a1 (row 1, col 1) is dark.
                boolean isLightSquare = (row + col) % 2 != 0;
                String bgColor;
                if (isLightSquare) {
                    bgColor = EscapeSequences.SET_BG_COLOR_LIGHT_BROWN;
                } else {
                    bgColor = EscapeSequences.SET_BG_COLOR_DARK_BROWN;
                }

                output.append(bgColor);

                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece == null) {
                    output.append(EscapeSequences.EMPTY);
                } else {
                    output.append(getPieceString(piece));
                }
            }

            // right border
            output.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY)
                    .append(EscapeSequences.SET_TEXT_COLOR_BLACK)
                    .append(" ").append(row).append(" ")
                    .append(EscapeSequences.RESET_BG_COLOR)
                    .append("\n");        }

        output.append(drawHeaders(headers));
        output.append(EscapeSequences.RESET_TEXT_COLOR);

        return output.toString();
    }

    private static String drawHeaders(String[] headers) {
        StringBuilder output = new StringBuilder();
        output.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY).append(EscapeSequences.SET_TEXT_COLOR_BLACK).append("   "); // Corner padding

        for (String h : headers) {
            output.append(h);
        }

        output.append("   ").append(EscapeSequences.RESET_BG_COLOR).append("\n");
        return output.toString();
    }

    private static String getPieceString(ChessPiece piece) {
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;

        // set piece color to black or white
        String color;
        if (isWhite) {
            color = EscapeSequences.SET_TEXT_COLOR_WHITE;
        } else {
            color = EscapeSequences.SET_TEXT_COLOR_BLACK;
        }

        String pieceChar;

        if (isWhite) {
            pieceChar = switch (piece.getPieceType()) {
                case KING -> EscapeSequences.WHITE_KING;
                case QUEEN -> EscapeSequences.WHITE_QUEEN;
                case BISHOP -> EscapeSequences.WHITE_BISHOP;
                case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
                case ROOK -> EscapeSequences.WHITE_ROOK;
                case PAWN -> EscapeSequences.WHITE_PAWN;
            };
        } else {
            pieceChar = switch (piece.getPieceType()) {
                case KING -> EscapeSequences.BLACK_KING;
                case QUEEN -> EscapeSequences.BLACK_QUEEN;
                case BISHOP -> EscapeSequences.BLACK_BISHOP;
                case KNIGHT -> EscapeSequences.BLACK_KNIGHT;
                case ROOK -> EscapeSequences.BLACK_ROOK;
                case PAWN -> EscapeSequences.BLACK_PAWN;
            };
        }

        return color + pieceChar;
    }
}

