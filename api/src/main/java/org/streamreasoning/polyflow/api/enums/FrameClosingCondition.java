package org.streamreasoning.polyflow.api.enums;

import java.util.Objects;

/**
 * Comparison used by {@code FrameOp} to decide when a frame closes.
 * <p>
 * For example, {@link #GREATER_OR_EQUAL} means the frame closes when the
 * observed value is greater than or equal to the configured frame parameter.
 */
public enum FrameClosingCondition {
    GREATER_THAN(">"),
    LESS_THAN("<"),
    LESS_OR_EQUAL("<="),
    GREATER_OR_EQUAL(">="),
    EQUAL("=");

    private final String symbol;

    FrameClosingCondition(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public boolean matches(double left, double right) {
        return switch (this) {
            case GREATER_THAN -> left > right;
            case LESS_THAN -> left < right;
            case LESS_OR_EQUAL -> left <= right;
            case GREATER_OR_EQUAL -> left >= right;
            case EQUAL -> Objects.equals(left, right);
        };
    }

    public static FrameClosingCondition fromSymbol(String symbol) {
        for (FrameClosingCondition condition : values()) {
            if (condition.symbol.equals(symbol)) {
                return condition;
            }
        }
        if ("==".equals(symbol)) {
            return EQUAL;
        }
        throw new IllegalArgumentException("Unsupported frame closing condition: " + symbol);
    }
}
