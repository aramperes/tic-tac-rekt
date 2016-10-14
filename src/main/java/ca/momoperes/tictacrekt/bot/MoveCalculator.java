package ca.momoperes.tictacrekt.bot;

import ca.momoperes.tictacrekt.game.GameBoard;
import ca.momoperes.tictacrekt.game.GamePlayer;
import ca.momoperes.tictacrekt.game.GameTile;
import ca.momoperes.tictacrekt.game.TilePlayer;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MoveCalculator {

    public final Point[] CORNERS = new Point[]{
            new Point(0, 0),
            new Point(0, 2),
            new Point(2, 0),
            new Point(2, 2),
    };
    public final Point CENTER = new Point(1, 1);
    public final Point[] EDGES = new Point[]{
            new Point(0, 1),
            new Point(1, 0),
            new Point(1, 2),
            new Point(2, 1),
    };

    public final GamePlayer bot;
    private GameBoard board;
    private GameTile memory = null;

    public MoveCalculator(GamePlayer bot, GameBoard board) {
        this.bot = bot;
        this.board = board;
    }

    public GameTile selectMove() {
        int rounds = getRounds();
        if (rounds == 0) {
            // First round (initiating)
            memory = initRound1();
            return memory;
        }
        if (rounds == 1) {
            // First round (responding)
            memory = respondRound1();
            return memory;
        }
        if (rounds == 2) {
            // Second round (initiating)
            memory = initRound2();
            return memory;
        }
        if (rounds == 3) {
            memory = respondRound2();
            return memory;
        }
        return null;
    }

    private GameTile initRound1() {
        Random random = ThreadLocalRandom.current();
        // Play a corner, because it's the best move for 1st round
        Point point = CORNERS[random.nextInt(CORNERS.length)];
        return board.tileAt(point.x, point.y);
    }

    private GameTile respondRound1() {
        Random random = ThreadLocalRandom.current();
        GameTile taken = getPlayed()[0];
        if (isCorner(taken)) {
            return board.tileAt(CENTER.x, CENTER.y);
        } else if (isCenter(taken)) {
            Point point = CORNERS[random.nextInt(CORNERS.length)];
            return board.tileAt(point.x, point.y);
        } else {
            // Edge is taken
            int move = random.nextInt(3);
            if (move == 0) { // Center move
                return board.tileAt(CENTER.x, CENTER.y);
            } else if (move == 1) { // Edge move (opposite of taken)
                return getOpposite(taken);
            } else { // Corner move (next to taken)
                for (Point point : CORNERS) {
                    if (point.x == taken.x || point.y == taken.y) {
                        return board.tileAt(point.x, point.y);
                    }
                }
            }
            return null;
        }
    }

    private GameTile initRound2() {
        if (isCenter(board.latest)) {
            return getOpposite(memory);
        }
        if (isEdge(board.latest)) {
            return board.tileAt(CENTER.x, CENTER.y);
        }
        // Corner
        for (Point point : CORNERS) {
            if (board.tileAt(point.x, point.y).player != null) {
                return board.tileAt(point.x, point.y);
            }
        }
        return null;
    }

    private GameTile respondRound2() {
        //todo
        return null;
    }

    private GameTile findToe(TilePlayer player) {
        //todo
        {
            // Horizontal
            for (int y = 0; y < 3; y++) {
                int cnt = 0;

                for (int x = 0; x < 3; x++) {
                    if (board.tileAt(x, y).player != player) {
                        break;
                    }
                }
                if (cnt == 2) {

                }
            }
        }
        return null;
    }

    private GameTile getOpposite(GameTile tile) {
        if (isCenter(tile)) {
            return null;
        }
        if (isEdge(tile)) {
            if (tile.x == EDGES[0].x && tile.y == EDGES[0].y) {
                return board.tileAt(EDGES[3].x, EDGES[3].y);
            }
            if (tile.x == EDGES[3].x && tile.y == EDGES[3].y) {
                return board.tileAt(EDGES[0].x, EDGES[0].y);
            }
            if (tile.x == EDGES[1].x && tile.y == EDGES[1].y) {
                return board.tileAt(EDGES[2].x, EDGES[2].y);
            }
            return board.tileAt(EDGES[1].x, EDGES[1].y);
        }
        // Corner
        if (tile.x == CORNERS[0].x && tile.y == CORNERS[0].y) {
            return board.tileAt(CORNERS[3].x, CORNERS[3].y);
        }
        if (tile.x == CORNERS[3].x && tile.y == CORNERS[3].y) {
            return board.tileAt(CORNERS[0].x, CORNERS[0].y);
        }
        if (tile.x == CORNERS[1].x && tile.y == CORNERS[1].y) {
            return board.tileAt(CORNERS[2].x, CORNERS[2].y);
        }
        return board.tileAt(CORNERS[1].x, CORNERS[1].y);
    }

    private GameTile[] getPlayed() {
        GameTile[] tiles = new GameTile[getRounds()];
        int i = 0;
        for (GameTile tile : board.tiles) {
            if (tile.player != null) {
                tiles[i++] = tile;
            }
        }
        return tiles;
    }

    private boolean isCorner(GameTile tile) {
        for (Point point : CORNERS) {
            if (point.x == tile.x && point.y == tile.y) {
                return true;
            }
        }
        return false;
    }

    private boolean isCenter(GameTile tile) {
        return CENTER.x == tile.x && CENTER.y == tile.y;
    }

    private boolean isEdge(GameTile tile) {
        for (Point point : EDGES) {
            if (point.x == tile.x && point.y == tile.y) {
                return true;
            }
        }
        return false;
    }

    private int getRounds() {
        int count = 0;
        for (GameTile tile : board.tiles) {
            if (tile.player != null) {
                count++;
            }
        }
        return count;
    }
}
