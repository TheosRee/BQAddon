package de.ree.theos.bq.drop;

import org.bukkit.NamespacedKey;

/**
 * Modes to modify drop behavior of items.
 */
public enum DropMode {
    /**
     * Should only be able to picked up by the profile it was dropped for.
     */
    PERSONAL,
    /**
     * The dropped item should only be shown to the profile it was dropped for.
     */
    PRIVATE;

    /**
     * Key to indicate an item should be shown to the owner.
     */
    public static final NamespacedKey SHOW_OWNER_KEY = new NamespacedKey("betonquest", "show_owner");
}
