package sdjini.AirDMM.Dog;

public enum LogLevel {
    step(0),
    debug(1),
    info(2),
    error(3),
    fatal(4);

    private final int level;
    LogLevel(int level) {
        this.level = level;
    }

}
