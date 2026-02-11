package com.ns.tcpframework.logger;

/**
 * Enumeration representing different logging severity levels.
 * <p>
 * Each log level has an associated numeric value that determines its priority.
 * Higher numeric values indicate more severe log levels.
 */
public enum Loglevel {
    /**
     * Debug level - Used for detailed diagnostic information during development.
     * Lowest severity level (0).
     */
    DEBUG(0),
    /**
     * Info level - Used for informational messages about normal application flow.
     * Severity level 1.
     */
    INFO(1),
    /**
     * Warning level - Used for potentially harmful situations that don't prevent execution.
     * Severity level 2.
     */
    WARN(2),
    /**
     * Error level - Used for error events that might still allow the application to continue.
     * Severity level 3.
     */
    ERROR(3),
    /**
     * Fatal level - Used for severe error events that will presumably lead the application to abort.
     * Highest severity level (4).
     */
    FATAL(4);

    /** The numeric value representing the severity of this log level. */
    private final int level;

    /**
     * Constructs a Loglevel with the specified numeric severity value.
     *
     * @param level The numeric severity value for this log level.
     */
    Loglevel(int level) {
        this.level = level;
    }

    /**
     * Gets the numeric severity value of this log level.
     *
     * @return The numeric severity value.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Checks if this log level has equal or higher severity than the specified level.
     *
     * @param other The log level to compare against.
     * @return {@code true} if this level is higher or equal in severity, {@code false} otherwise.
     */
    public boolean isHigherOrEqual(Loglevel other) {
        return this.level >= other.level;
    }

    /**
     * Checks if this log level has lower severity than the specified level.
     *
     * @param other The log level to compare against.
     * @return {@code true} if this level is lower in severity, {@code false} otherwise.
     */
    public boolean isLowerThan(Loglevel other) {
        return this.level < other.level;
    }
}
