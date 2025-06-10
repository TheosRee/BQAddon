package de.ree.theos.bq.playtime;

/**
 * Setting if already present progress/states should be counted against the objective completion.
 */
public enum CountingMode {
    /**
     * The given value is absolute.
     */
    TOTAL,
    /**
     * The value is added to the current value at start.
     */
    RELATIVE
}
