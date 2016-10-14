package ca.momoperes.tictacrekt;

import ca.momoperes.tictacrekt.bot.MoveCalculator;
import ca.momoperes.tictacrekt.game.GameBoard;
import ca.momoperes.tictacrekt.game.GamePlayer;
import ca.momoperes.tictacrekt.game.GameTile;
import ca.momoperes.tictacrekt.game.TilePlayer;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        GamePlayer player = new GamePlayer(TilePlayer.CROSS, "You");
        GamePlayer bot = new GamePlayer(TilePlayer.OVAL, "Bot");
        GameBoard board = new GameBoard(player, bot);
        MoveCalculator calculator = new MoveCalculator(bot, board);
        board.nextTurn();
        System.out.println("[!] YOU ARE " + player.getTile());
        for(;;) {
            System.out.println("=================");
            for (int y = 0; y < 3; y++) {
                String build = "";
                for (int x = 0; x < 3; x++) {
                    GameTile tile = board.tileAt(x, y);
                    String rep = " ";
                    if (tile.player == TilePlayer.CROSS) {
                        rep = "x";
                    } else if (tile.player == TilePlayer.OVAL) {
                        rep = "o";
                    }
                    build += "[" + rep + "] ";
                }
                System.out.println(build);
            }
            if (board.active != bot.getTile()) {
                boolean needInput = true;
                GameTile tile = null;
                while (needInput) {
                    System.out.println("[>] Select your move (x,y): ");
                    Scanner scanner = new Scanner(System.in);
                    String input = scanner.nextLine();
                    int x = Integer.valueOf(input.split(",")[0]), y = Integer.valueOf(input.split(",")[1]);
                    tile = board.tileAt(x, y);
                    if (tile.player != null) {
                        continue;
                    }
                    needInput = false;
                }
                tile.player = player.getTile();
                board.updateTile(tile);
                board.nextTurn();
                continue;
            }
            GameTile move = calculator.selectMove();
            if (move == null) {
                return;
            }
            System.out.println("[!] BOT PLAYS " + move.x + "," + move.y);
            move.player = bot.getTile();
            board.updateTile(move);
            board.nextTurn();
        }
    }

}
