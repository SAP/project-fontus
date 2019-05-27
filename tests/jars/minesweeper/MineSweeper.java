import java.security.InvalidParameterException;
import java.util.Scanner;

class MineSweeper {

    private final MineSweeperField field;
    private boolean alive;
    private final Scanner input;
    private int minesFound;

    MineSweeper(int size, int mineCount, int initiallyVisible) {
        this.field = new MineSweeperField(size, mineCount, initiallyVisible);
        this.input = new Scanner(System.in);
        this.input.useDelimiter("");
        this.alive = true;
        this.minesFound = 0;
    }

    private Coordinate processInput() {
        char column = this.input.next().charAt(0);
        int row = this.input.nextInt();
        this.input.next(); //clear return
        if(this.field.isValidCoordinate(row, column)) {
            return new Coordinate(row, column);
        }
        throw new InvalidParameterException(String.format("'%c%d' is not a valid coordinate!", column, row));
    }

    boolean run() {
        while(alive && (minesFound < this.field.getMineCount())) {
            System.out.print("Enter next coordinate (e.g., \"H4\"): ");
            Coordinate c = processInput();
            System.out.print("Enter action (1 for 'move onto', 2 for 'defuse'): ");
            int action = this.input.nextInt();
            this.input.next(); //clear return
            switch (action) {
                case 1:
                    alive = this.field.leftClick(c);
                    break;
                case 2:
                    boolean successful = this.field.rightClick(c);
                    if(successful) {
                        System.out.println("Defused a landmine!");
                        this.minesFound++;
                    } else {
                        System.out.println("Invalid defuse action!");
                    }
                    break;
                default:
                    System.out.println("Invalid input..");
                    continue;
            }
            System.out.println(this.field);
        }
        return alive;
    }

    String toDebugString() {
        return this.field.toDebugString();
    }

    @Override
    public String toString() {
        return this.field.toString();
    }
}
