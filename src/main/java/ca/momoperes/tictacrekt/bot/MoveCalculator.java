package ca.momoperes.tictacrekt.bot;

import ca.momoperes.tictacrekt.game.GameBoard;
import ca.momoperes.tictacrekt.game.GamePlayer;
import ca.momoperes.tictacrekt.game.GameTile;
import ca.momoperes.tictacrekt.game.TilePlayer;

import java.awt.*;
import java.util.ArrayList;
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
    private long waitTime;
    private GameTile memory = null;

    public MoveCalculator(GamePlayer bot, GameBoard board, long waitTime) {
        this.bot = bot;
        this.board = board;
        this.waitTime = waitTime;
    }

    public GameTile selectMove() {
        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        // Any more rounds
        memory = remaining();
        return memory;
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
            if (board.tileAt(point.x, point.y).player == null) {
                return board.tileAt(point.x, point.y);
            }
        }
        return null;
    }

    private GameTile remaining() {
        GameTile[] ours = findToes(TilePlayer.OVAL);
        if (ours.length > 0) {
            return ours[0];
        }
        // Block if in defense
        GameTile[] others = findToes(TilePlayer.CROSS);
        if (others.length > 0) {
            return others[0];
        }
        // Select a random case. todo: strategy
        GameTile[] remaining = new GameTile[board.tiles.size() - getPlayed().length];
        int i = 0;
        for (GameTile tile : board.tiles) {
            if (tile.player == null) {
                remaining[i++] = tile;
            }
        }
        return remaining[ThreadLocalRandom.current().nextInt(remaining.length)];
    }

    private GameTile[] findToes(TilePlayer player) {
        java.util.List<GameTile> tiles = new ArrayList<>();
        // Horizontal
        for (int y = 0; y < 3; y++) {
            GameTile toe = null;
            int count = 0, airCount = 0;
            xes:
            for (int x = 0; x < 3; x++) {
                if (board.tileAt(x, y).player == player) {
                    count++;
                } else if (board.tileAt(x, y).player == null) {
                    airCount++;
                    toe = board.tileAt(x, y);
                } else {
                    break;
                }
            }
            if (count == 2 && airCount == 1 && toe != null) {
                tiles.add(toe);
            }
        }// Vertical
        for (int x = 0; x < 3; x++) {
            GameTile toe = null;
            int count = 0, airCount = 0;
            xes:
            for (int y = 0; y < 3; y++) {
                if (board.tileAt(x, y).player == player) {
                    count++;
                } else if (board.tileAt(x, y).player == null) {
                    airCount++;
                    toe = board.tileAt(x, y);
                } else {
                    break;
                }
            }
            if (count == 2 && airCount == 1 && toe != null) {
                tiles.add(toe);
            }
        }
        // Diagonal bottom-to-right
        {
            GameTile toe = null;
            int count = 0, airCount = 0;
            xes:
            for (int x = 0; x < 3; x++) {
                int y = Math.abs(x - 2);
                GameTile tile = board.tileAt(x, y);
                if (tile.player == player) {
                    count++;
                } else if (tile.player == null) {
                    airCount++;
                    toe = tile;
                } else {
                    break;
                }
            }
            if (count == 2 && airCount == 1 && toe != null) {
                tiles.add(toe);
            }
        }
        // Diagonal bottom-to-right
        {
            GameTile toe = null;
            int count = 0, airCount = 0;
            xes:
            for (int y = 0; y < 3; y++) {
                GameTile tile = board.tileAt(y, y);
                if (tile.player == player) {
                    count++;
                } else if (tile.player == null) {
                    airCount++;
                    toe = tile;
                } else {
                    break;
                }
            }
            if (count == 2 && airCount == 1 && toe != null) {
                tiles.add(toe);
            }
        }
        return tiles.toArray(new GameTile[tiles.size()]);
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

    public boolean hasWon(TilePlayer who) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                GameTile tile = board.tileAt(x, y);
                if (tile.player != who) {
                    break;
                } else if (x == 2) {
                    return true;
                }
            }
        }
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                GameTile tile = board.tileAt(x, y);
                if (tile.player != who) {
                    break;
                } else if (y == 2) {
                    return true;
                }
            }
        }
        // Diagonal left-to-bottom
        for (int y = 0; y < 3; y++) {
            GameTile tile = board.tileAt(y, y);
            if (tile.player != who) {
                break;
            } else if (y == 2) {
                return true;
            }
        }
        // Diagonal bottom-to-right
        for (int x = 0; x < 3; x++) {
            int y = Math.abs(x - 2);
            GameTile tile = board.tileAt(x, y);
            if (tile.player != who) {
                break;
            } else if (x == 2) {
                return true;
            }
        }
        return false;
    }

    public int getRounds() {
        int count = 0;
        for (GameTile tile : board.tiles) {
            if (tile.player != null) {
                count++;
            }
        }
        return count;
    }
}
