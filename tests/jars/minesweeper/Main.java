public class Main {

    public static void main(String[] args) {
        MineSweeper game = new MineSweeper(10, 23, 5);
        System.out.println(game.toDebugString());
        System.out.println(game);
        boolean won = game.run();
        if(won) {
            System.out.println("Congratulations, you won!");
        } else {
            System.out.println("Boom, stepped on a mine :(");
        }
    }
}
