package ca.momoperes.tictacrekt.game;

public class GamePlayer {

    private final TilePlayer tile;
    private final String name;

    public GamePlayer(TilePlayer tile, String name) {
        this.tile = tile;
        this.name = name;
    }

    public TilePlayer getTile() {
        return tile;
    }
}
