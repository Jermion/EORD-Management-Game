import enums.BioCondition;
import enums.Sin;

public class BiologicalSystem {
    private int pride;
    private int gluttony;
    private int sloth;
    private int wrath;

    // 50 Will be the starting value for all sins
    public BiologicalSystem() {
        pride = 50;
        gluttony = 50;
        sloth = 50;
        wrath = 50;
    }

    public int getPride() {
        return pride;
    }

    public int getSloth() {
        return sloth;
    }

    public int getGluttony() {
        return gluttony;
    }

    public int getWrath() {
        return wrath;
    }

    public void changeSin(Sin sin, int amount) {
        switch (sin) {
            case PRIDE -> pride += amount;
            case GLUTTONY -> gluttony += amount;
            case WRATH -> wrath += amount;
            case SLOTH -> sloth += amount;

        }
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(120, value));
    }

    private int getSinValue(Sin sin) {
        return switch (sin) {
            case PRIDE -> pride;
            case GLUTTONY -> gluttony;
            case SLOTH -> sloth;
            case WRATH -> wrath;
        };
    }

    private int getOtherAverage(Sin sin) {
        int total = pride + gluttony + sloth + wrath;
        return (total - getSinValue(sin)) / 3;
    }

    public BioCondition getCondition(Sin sin) {
        int value = getSinValue(sin);

        if (value > 100) {
            return BioCondition.OZLERIC;
        } else if (value >= 40 && value <= 60) {
            return BioCondition.GOOD;
        } else if ((value >= 25 && value <= 39) || (value >= 61 && value <= 75)) {
            return BioCondition.OK;
        } else {
            return BioCondition.BAD;
        }
    }
}
