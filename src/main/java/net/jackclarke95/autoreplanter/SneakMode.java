package net.jackclarke95.autoreplanter;

/**
 * Enumeration defining when auto-replanting should occur based on the player's
 * sneak state.
 * <p>
 * This enum provides three different modes for controlling auto-replanting
 * behavior:
 * </p>
 * <ul>
 * <li>{@link #ALWAYS} - Auto-replanting works regardless of sneak state</li>
 * <li>{@link #ONLY_SNEAKING} - Auto-replanting only works while sneaking</li>
 * <li>{@link #ONLY_STANDING} - Auto-replanting only works while not
 * sneaking</li>
 * </ul>
 * 
 * @author jackclarke95
 * @since 1.0.0
 * @see AutoReplanterConfig#sneakMode
 */
public enum SneakMode {
    /**
     * Auto-replanting works regardless of whether the player is sneaking or not.
     * <p>
     * This is the default behavior and provides the most flexibility.
     * </p>
     */
    ALWAYS,

    /**
     * Auto-replanting only works when the player is holding the sneak key.
     * <p>
     * This mode is useful when you want precise control over when crops are
     * auto-replanted, preventing accidental replanting when just walking through
     * your farm.
     * </p>
     */
    ONLY_SNEAKING,

    /**
     * Auto-replanting only works when the player is NOT sneaking.
     * <p>
     * This mode allows you to disable auto-replanting by sneaking, which can be
     * useful when you want to manually break crops without replanting them
     * (for example, when relocating your farm).
     * </p>
     */
    ONLY_STANDING
}