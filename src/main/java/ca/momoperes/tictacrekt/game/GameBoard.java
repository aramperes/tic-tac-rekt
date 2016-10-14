package ca.momoperes.tictacrekt.game;

import java.util.ArrayList;
import java.util.List;

public class GameBoard {
    public List<GameTile> tiles;
    public final GamePlayer bot, player;
    public TilePlayer active;
    public GameTile latest = null;

    public GameBoard(GamePlayer bot, GamePlayer player) {
        this.bot = bot;
        this.player = player;
        tiles = new ArrayList<GameTile>();
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tiles.add(new GameTile(x, y, null));
            }
        }
    }

    public void updateTile(GameTile tile) {
        if (tile.x >= 3 || tile.y >= 3) {
            System.out.println("Failed to set? Outbound");
            return;
        }
        for (int i = 0; i < tiles.size(); i++) {
            GameTile old = tiles.get(i);
            if (old.x == tile.x && old.y == tile.y) {
                tiles.set(i, tile);
                latest = tile;
                return;
            }
        }
        System.out.println("Failed to set?");
    }

    public GameTile tileAt(int x, int y) {
        for (GameTile tile : tiles) {
            if (tile.x == x && tile.y == y) {
                return tile;
            }
        }
        return null;
    }

    public GamePlayer getPlayer(TilePlayer tile) {
        if (bot.getTile() == tile) {
            return bot;
        }
        return player;
    }

    public GamePlayer getActive() {
        return getPlayer(active);
    }

    public void nextTurn() {
        if (active == null) {
            active = TilePlayer.random();
        } else if (active == TilePlayer.CROSS) {
            active = TilePlayer.OVAL;
            System.out.println("[!] It's bot's turn!");
        } else {
            active = TilePlayer.CROSS;
            System.out.println("[!] It's your turn!");
        }
    }
}
