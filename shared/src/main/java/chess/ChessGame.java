package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard gameBoard = new ChessBoard();
    private TeamColor currentTurnColor;

    public ChessGame() {
        gameBoard.resetBoard();
        currentTurnColor = TeamColor.WHITE;

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurnColor;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurnColor = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece potentialPiece = gameBoard.getPiece(startPosition);
        if (potentialPiece == null) {
            return null;
        }
        Collection<ChessMove> potentialMoves = potentialPiece.pieceMoves(gameBoard, startPosition);
        Collection<ChessMove> actualMoves = new ArrayList<>();
        for(ChessMove move : potentialMoves){
            if(isMoveSafe(move)) {
                actualMoves.add(move);
            }
        }
        return actualMoves;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> possibleMoves = validMoves(move.getStartPosition());
        if(possibleMoves != null && possibleMoves.contains(move)) {
            ChessPiece currentPiece = gameBoard.getPiece(move.getStartPosition());
            if (currentPiece.getTeamColor().equals(currentTurnColor)){
                if(move.getPromotionPiece() == null) {
                    gameBoard.addPiece(move.getEndPosition(), currentPiece);
                    gameBoard.removePiece(move.getStartPosition());
                }
                else {
                    ChessPiece promotedPiece = new ChessPiece(currentPiece.getTeamColor(), move.getPromotionPiece());
                    gameBoard.addPiece(move.getEndPosition(), promotedPiece);
                    gameBoard.removePiece(move.getStartPosition());
                }

                if(currentTurnColor == TeamColor.WHITE) {
                    currentTurnColor = TeamColor.BLACK;
                }
                else {
                    currentTurnColor = TeamColor.WHITE;

                }
            }
            else{
                throw new InvalidMoveException();
            }
        }
        else{
            throw new InvalidMoveException();
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */

    // check if king would be in check based on moves. true means would be in check, false means would not be in check
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, this.gameBoard);
    }

    // for simulations
    private boolean isInCheck(TeamColor teamColor, ChessBoard board) {

        ChessPosition kingPosition = getKingPosition(board, teamColor);

        TeamColor opponentColor;

        if (teamColor == TeamColor.WHITE) {
            opponentColor = TeamColor.BLACK;
        } else {
            opponentColor = TeamColor.WHITE;
        }

        for(int row = 1; row <= 8; row++) {
            for(int column = 1; column <= 8; column++) {
                ChessPiece potentialEnemy = board.getPiece(new ChessPosition(row, column));
                if(potentialEnemy != null && potentialEnemy.getTeamColor() == opponentColor){
                    Collection<ChessMove> moves = potentialEnemy.pieceMoves(board, new ChessPosition(row, column));
                    for(ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }

        }
        return false;
    }

    public ChessPosition getKingPosition(ChessBoard currentBoard, TeamColor kingColor) {
        for(int row = 1; row <= 8; row++) {
            for(int column = 1; column <= 8; column++) {
                ChessPiece potentialKing = currentBoard.getPiece(new ChessPosition(row, column));
                if(potentialKing != null) {
                    ChessPiece.PieceType potentialType = potentialKing.getPieceType();
                    if (potentialType == ChessPiece.PieceType.KING) {
                        TeamColor potentialColor = potentialKing.getTeamColor();
                        if (potentialColor == kingColor) {
                            return new ChessPosition(row, column);
                        }
                    }
                }

            }
        }
        throw new RuntimeException("King not found on the board for the following color: " + kingColor);
    }

    public boolean isMoveSafe(ChessMove move) {
        ChessBoard testingBoard = gameBoard.clone();
        ChessPiece desiredPiece = testingBoard.getPiece(move.getStartPosition());
        testingBoard.removePiece(move.getStartPosition());
        if(move.getPromotionPiece() == null) {
            testingBoard.addPiece(move.getEndPosition(), desiredPiece);
            return !isInCheck(desiredPiece.getTeamColor(), testingBoard);
        }
        else {
            ChessPiece promotedPiece = new ChessPiece(desiredPiece.getTeamColor(), move.getPromotionPiece());
            testingBoard.addPiece(move.getEndPosition(), promotedPiece);
            return !isInCheck(promotedPiece.getTeamColor(), testingBoard);
        }
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(gameBoard, chessGame.gameBoard) && currentTurnColor == chessGame.currentTurnColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameBoard, currentTurnColor);
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }
}
