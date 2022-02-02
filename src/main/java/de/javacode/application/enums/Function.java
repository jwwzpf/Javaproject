package de.itdesign.application.enums;

/**
 * Enum for the operations of calculation from operations.xml.
 */
public enum Function {
    AVERAGE("average"),
    SUM("sum"),
    MIN("min"),
    MAX("max");

    private final String function;

    Function(String function) {
        this.function = function;
    }

    public static Function fromString(String text) {
        for (Function f : Function.values()) {
            if(f.function.equalsIgnoreCase(text)) {
                return f;
            }
        }
        return null;
    }
}
