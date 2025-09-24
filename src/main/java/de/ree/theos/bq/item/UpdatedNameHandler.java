package de.ree.theos.bq.item;

import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.text.TextParser;
import org.betonquest.betonquest.item.typehandler.Existence;
import org.betonquest.betonquest.item.typehandler.HandlerUtil;
import org.betonquest.betonquest.item.typehandler.NameHandler;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Handles de-/serialization of (Display) Names.
 */
public class UpdatedNameHandler extends NameHandler {

    /**
     * The text parser used to parse text.
     */
    private final TextParser textParser;

    /**
     * The Item Display Name.
     */
    @Nullable
    private Component name;

    /**
     * The required existence.
     */
    private Existence existence = Existence.WHATEVER;

    /**
     * The Item Name.
     */
    @Nullable
    private Component itemName;

    /**
     * The required item name existence.
     */
    private Existence itemNameE = Existence.WHATEVER;

    /**
     * Creates an empty NameHandler with also an 'itemName'.
     *
     * @param textParser the text parser used to parse text
     */
    public UpdatedNameHandler(final TextParser textParser) {
        super(textParser);
        this.textParser = textParser;
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
            name = HandlerUtil.toKeyValue("name", meta.displayName());
        }
        if (meta.hasItemName()) {
            return (name == null ? "" : name + " ") + HandlerUtil.toKeyValue("item-name", meta.itemName());
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
                    this.name = textParser.parse(data);
                    existence = Existence.REQUIRED;
                }
            }
            case "item-name" -> {
                if (Existence.NONE_KEY.equalsIgnoreCase(data)) {
                    itemNameE = Existence.FORBIDDEN;
                } else {
                    this.itemName = textParser.parse(data);
                    itemNameE = Existence.REQUIRED;
                }
            }
            default -> throw new QuestException("Invalid item name");
        }
    }

    @Override
    public void populate(final ItemMeta meta) {
        meta.displayName(name);
        meta.itemName(itemName);
    }

    @Override
    public boolean check(final ItemMeta meta) {
        final Component displayName = meta.hasDisplayName() ? meta.displayName() : null;
        final Component itemName = meta.hasItemName() ? meta.itemName() : null;
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
    public Component get() {
        return name == null ? itemName : name;
    }
}
