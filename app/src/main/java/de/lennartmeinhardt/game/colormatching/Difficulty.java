package de.lennartmeinhardt.game.colormatching;

public enum Difficulty {

    EASY(15),
    MEDIUM(5),
    HARD(1);

    private final int stepSize;

    Difficulty(int stepSize) {
        this.stepSize = stepSize;
    }

    public int getStepSize() {
        return stepSize;
    }

    public int getStepCount() {
        return 255 / stepSize;
    }


    public static Difficulty ofIndex(int index) {
        return values()[index];
    }

    public static Difficulty ofIndex(int index, Difficulty fallback) {
        try {
            return ofIndex(index);
        } catch(Exception e) {
            return fallback;
        }
    }

    public static Difficulty ofStepSize(int stepSize, Difficulty fallback) {
        for(Difficulty difficulty : Difficulty.values())
            if(difficulty.getStepSize() == stepSize)
                return difficulty;
        return fallback;
    }
}
