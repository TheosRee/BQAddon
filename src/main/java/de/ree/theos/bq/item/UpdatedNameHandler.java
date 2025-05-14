package de.ree.theos.bq.item;

import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.item.typehandler.Existence;
import org.betonquest.betonquest.item.typehandler.NameHandler;
import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Handles de-/serialization of (Display) Names.
 */
public class UpdatedNameHandler extends NameHandler {

    /**
     * The Item Display Name.
     */
    @Nullable
    private String name;

    /**
     * The required existence.
     */
    private Existence existence = Existence.WHATEVER;

    /**
     * The Item Name.
     */
    @Nullable
    private String itemName;

    /**
     * The required item name existence.
     */
    private Existence itemNameE = Existence.WHATEVER;

    /**
     * The empty default Constructor.
     */
    public UpdatedNameHandler() {
    }

    /**
     * Replaces all underscores with spaces, except for those that are escaped with a backslash.
     *
     * @param input The input string.
     * @return The input string with all underscores replaced with spaces, except for those that are escaped with a backslash.
     */
    protected static String replaceUnderscore(final String input) {
        return input.replaceAll("(?<!\\\\)_", " ").replaceAll("\\\\_", "_");
    }

    @Override
    public Class<ItemMeta> metaClass() {
        return ItemMeta.class;
    }

    @Override
    public Set<String> keys() {
        return Set.of("name", "item-name");
    }

    @Override
    @Nullable
    public String serializeToString(final ItemMeta meta) {
        String name = null;
        if (meta.hasDisplayName()) {
            name = "name:" + meta.getDisplayName().replace(" ", "_");
        }
        if (meta.hasItemName()) {
            return (name == null ? "" : name + " ") + meta.getItemName().replace(" ", "_");
        }
        return name;
    }

    @Override
    public void set(final String key, final String data) throws QuestException {
        if (data.isEmpty()) {
            throw new QuestException("Name cannot be empty");
        }
        switch (key) {
            case "name" -> {
                if (Existence.NONE_KEY.equalsIgnoreCase(data)) {
                    existence = Existence.FORBIDDEN;
                } else {
                    this.name = ChatColor.translateAlternateColorCodes('&', replaceUnderscore(data));
                    existence = Existence.REQUIRED;
                }
            }
            case "item-name" -> {
                if (Existence.NONE_KEY.equalsIgnoreCase(data)) {
                    itemNameE = Existence.FORBIDDEN;
                } else {
                    this.itemName = ChatColor.translateAlternateColorCodes('&', replaceUnderscore(data));
                    itemNameE = Existence.REQUIRED;
                }
            }
            default -> throw new QuestException("Invalid item name");
        }
    }

    @Override
    public void populate(final ItemMeta meta) {
        meta.setDisplayName(name);
        meta.setItemName(itemName);
    }

    @Override
    public boolean check(final ItemMeta meta) {
        final String displayName = meta.hasDisplayName() ? meta.getDisplayName() : null;
        final String itemName = meta.hasItemName() ? meta.getItemName() : null;
        return switch (existence) {
            case WHATEVER -> true;
            case REQUIRED -> displayName != null && displayName.equals(this.name);
            case FORBIDDEN -> displayName == null;
        } && switch (itemNameE) {
            case WHATEVER -> true;
            case REQUIRED -> itemName != null && itemName.equals(this.name);
            case FORBIDDEN -> itemName == null;
        };
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    @Override
    @Nullable
    public String get() {
        return name == null ? itemName : name;
    }
}
