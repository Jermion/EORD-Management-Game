import enums.ArtificialCondition;
import enums.ArtificialStat;

public class ArtificialSystem {
    private int temperature;
    private int storage;
    private int power;
    private int integrity;

    public ArtificialSystem() {
        temperature = 50;
        storage = 20;
        power = 80;
        integrity = 100;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getStorage() {
        return storage;
    }

    public int getPower() {
        return power;
    }

    public int getIntegrity() {
        return integrity;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public void changeStat(ArtificialStat stat, int amount) {
        switch (stat) {
            case TEMPERATURE -> temperature = clamp(temperature + amount);
            case STORAGE -> storage = clamp(storage + amount);
            case POWER -> power = clamp(power + amount);
            case INTEGRITY -> integrity = clamp(integrity + amount);
        }
    }

    private int getStatValue(ArtificialStat stat) {
        return switch (stat) {
            case TEMPERATURE -> temperature;
            case POWER -> power;
            case INTEGRITY -> integrity;
            case STORAGE -> storage;
        };
    }
    public ArtificialCondition getCondition(ArtificialStat stat) {
        int value = getStatValue(stat);

        return switch (stat) {
            case TEMPERATURE -> {
                if (value >= 40 && value <= 60) {
                    yield ArtificialCondition.GOOD;
                } else if ((value >= 25 && value <= 39) || (value >= 61 && value <= 75)) {
                    yield ArtificialCondition.OK;
                } else {
                    yield ArtificialCondition.BAD;
                }
            }

            case STORAGE -> {
                if (value <= 50) {
                    yield ArtificialCondition.GOOD;
                } else if (value <= 75) {
                    yield ArtificialCondition.OK;
                } else {
                    yield ArtificialCondition.BAD;
                }
            }

            case POWER, INTEGRITY -> {
                if (value >= 70) {
                    yield ArtificialCondition.GOOD;
                } else if (value >= 40) {
                    yield ArtificialCondition.OK;
                } else {
                    yield ArtificialCondition.BAD;
                }
            }
        };
    }
}
