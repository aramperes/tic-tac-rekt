package ca.momoperes.tictacrekt.game;

public class GameTile {

    public final int x, y;
    public TilePlayer player;

    public GameTile(int x, int y, TilePlayer player) {
        this.x = x;
        this.y = y;
        this.player = player;
    }
}
