package ca.momoperes.tictacrekt;

import ca.momoperes.tictacrekt.bot.MoveCalculator;
import ca.momoperes.tictacrekt.game.GameBoard;
import ca.momoperes.tictacrekt.game.GamePlayer;
import ca.momoperes.tictacrekt.game.GameTile;
import ca.momoperes.tictacrekt.game.TilePlayer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static String data = "";

    public static void main(String[] args) throws IOException {

        int wins = 0, ties = 0, loss = 0;

        File gamesFile = new File("data.txt");
        if (!gamesFile.exists()) {
            gamesFile.createNewFile();
        }
        List<String> data = Files.readAllLines(gamesFile.toPath(), Charset.forName("UTF-8"));
        if (data.size() != 0) {
            String[] d = data.get(0).split(",");
            wins = Integer.parseInt(d[0]);
            ties = Integer.parseInt(d[1]);
            loss = Integer.parseInt(d[2]);
        }
        GameBoard board = doGame();
        if (board.winner == null) {
            ties++;
        } else if (board.winner.getTile() == TilePlayer.CROSS) {
            wins++;
        } else if (board.winner.getTile() == TilePlayer.OVAL) {
            loss++;
        }

        //Save
        Files.write(gamesFile.toPath(), Arrays.asList("" + wins + "," + ties + "," + loss), Charset.forName("UTF-8"));
        System.out.println("Your record: ");
        System.out.println("Wins: " + wins);
        System.out.println("Ties: " + ties);
        System.out.println("Losses: " + loss);

    }

    public static GameBoard doGame() {
        GamePlayer player = new GamePlayer(TilePlayer.CROSS, "You");
        GamePlayer bot = new GamePlayer(TilePlayer.OVAL, "Bot");
        GameBoard board = new GameBoard(player, bot);
        MoveCalculator calculator = new MoveCalculator(bot, board, 1000l);
        board.nextTurn();
        if (board.active == TilePlayer.CROSS) {
            data += "START:P-";
        } else {
            data += "START:B-";
        }
        System.out.println("[!] YOU ARE " + player.getTile());
        for(;;) {
            if (calculator.getRounds() >= 9) {
                board.done = true;
            } else if (calculator.hasWon(TilePlayer.CROSS)) {
                board.done = true;
                board.winner = player;
            } else if (calculator.hasWon(TilePlayer.OVAL)) {
                board.done = true;
                board.winner = bot;
            }
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
            if (board.done) {
                GamePlayer winner = board.winner;
                if (winner == null) {
                    System.out.println("[!] Nobody won! It's a tie.");
                    data += "END";
                } else if (winner.getTile() == TilePlayer.OVAL) {
                    System.out.println("[!] You lost! Good game.");
                    data += "WIN:B";
                } else if (winner.getTile() == TilePlayer.CROSS) {
                    System.out.println("[!] You won! Nice job.");
                    data += "WIN:P";
                }
                System.out.println("Game data: " + data);
                return board;
            }
            if (board.active != bot.getTile()) {
                boolean needInput = true;
                GameTile tile = null;
                while (needInput) {
                    System.out.println("[>] Select your move (x,y): ");
                    Scanner scanner = new Scanner(System.in);
                    String input = scanner.nextLine();
                    int x = Integer.valueOf(input.split(",")[0]), y = Integer.valueOf(input.split(",")[1]);
                    if (x > 2 || y > 2) {
                        continue;
                    }
                    tile = board.tileAt(x, y);
                    if (tile.player != null) {
                        continue;
                    }
                    needInput = false;
                }
                tile.player = player.getTile();
                board.updateTile(tile);
                data += "P(" + tile.x + "," + tile.y + ")-";
                board.nextTurn();
                continue;
            }
            GameTile move = calculator.selectMove();
            if (move == null) {
                return board;
            }
            System.out.println("[!] Bot plays " + move.x + "," + move.y);
            move.player = bot.getTile();
            board.updateTile(move);
            data += "B(" + move.x + "," + move.y + ")-";
            board.nextTurn();
        }
    }

}
