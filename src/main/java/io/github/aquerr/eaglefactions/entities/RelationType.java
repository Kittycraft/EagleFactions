package io.github.aquerr.eaglefactions.entities;

public enum RelationType {
    ALLY(0), ENEMY(1), TRUCE(2), NEUTRAL(3), SAME(4);

    private final int identifier;

    RelationType(int identifier){
        this.identifier = identifier;
    }

    public static RelationType parseType(String type){
        try {
            switch (Integer.valueOf(type)) {
                case 0:
                    return RelationType.ALLY;
                case 1:
                    return RelationType.ENEMY;
                case 2:
                    return RelationType.TRUCE;
                case 3:
                    return RelationType.NEUTRAL;
                case 4:
                    return RelationType.SAME;
                default:
                    return RelationType.NEUTRAL;
            }
        }catch (NumberFormatException e){
            return RelationType.NEUTRAL;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(identifier);
    }
}
