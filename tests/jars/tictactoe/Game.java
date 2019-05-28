import java.util.Scanner;

/**
 * The class managing the tic tac toe game.
 */
class Game {
    private GameArea area;
    private Scanner input;
    private Player currentPlayer = Player.PlayerTwo;

    /**
     * Initializes the game state.
     */
    public Game() {
        this.area = new GameArea();
        this.input = new Scanner(System.in);
    }

    /**
     * Switches the currently active player.
     */
    private void changePlayer() {
        if (this.currentPlayer == Player.PlayerOne) {
            this.currentPlayer = Player.PlayerTwo;
        } else {
            this.currentPlayer = Player.PlayerOne;
        }
        System.out.println("Jetzt am Zug: " + this.currentPlayer);
    }

    /**
     * Runs the game main loop.
     */
    public void run() {
        while (!this.area.isGameOver()) {
            changePlayer();
            System.out.println(this.area);
            pickField();
        }
        if (this.area.isWinner(this.currentPlayer)) {
            System.out.println("Yay, gewonnen!");
        } else if (this.area.isFieldFull()) {
            System.out.println("Unentschieden!");
        } else {
            System.out.println("Booh, verloren!");
        }
        System.out.println(this.area);
    }

    /**
     * Prompts the user to enter a new coordinate and performs their next move.
     *
     * This is repeated until a successful move has taken place (i.e., a currently free field has been selected).
     */
    private void pickField() {
        boolean successful = false;
        while (!successful) {
            System.out.println("Bitte die Koordinate (Reihe und Spalte, jeweils 0-2) eingeben:");
            Coordinate coordinate = readCoordinate();
            successful = this.area.pickField(coordinate, this.currentPlayer);
            if (!successful) {
                System.out.println("Feld bereits belegt oder ungültig, bitte erneut eingeben");
            }
        }
    }

    /**
     * Reads a new coordinate pair from standard input.
     * @return The coordinate object holding the index pair.
     */
    private Coordinate readCoordinate() {
        int row = input.nextInt();
        int column = input.nextInt();
        return new Coordinate(row, column);
    }
}
