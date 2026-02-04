package com.ns.tcpframework.logger;

public enum Loglevel {
    DEBUG(0),
    INFO(1),
    WARN(2),
    ERROR(3),
    FATAL(4);

    private final int level;

    Loglevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHigherOrEqual(Loglevel other) {
        return this.level >= other.level;
    }

    public boolean isLowerThan(Loglevel other) {
        return this.level < other.level;
    }
}
