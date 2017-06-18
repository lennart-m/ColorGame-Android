package de.lennartmeinhardt.game.colormatching;

public interface ValueStepHandler {

    void increaseBy(int steps);

    void decreaseBy(int steps);

    class Reverser implements ValueStepHandler {

        private final ValueStepHandler valueStepHandler;

        public Reverser(ValueStepHandler valueStepHandler) {
            this.valueStepHandler = valueStepHandler;
        }

        @Override
        public void decreaseBy(int steps) {
            valueStepHandler.increaseBy(steps);
        }

        @Override
        public void increaseBy(int steps) {
            valueStepHandler.decreaseBy(steps);
        }
    }
}
