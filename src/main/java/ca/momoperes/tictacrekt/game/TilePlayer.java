package ca.momoperes.tictacrekt.game;

import java.util.concurrent.ThreadLocalRandom;

public enum TilePlayer {
    CROSS, OVAL;

    public static TilePlayer random() {
        if (ThreadLocalRandom.current().nextBoolean()) {
            return CROSS;
        }
        return OVAL;
    }
}
