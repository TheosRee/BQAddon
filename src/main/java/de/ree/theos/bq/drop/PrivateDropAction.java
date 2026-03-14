package de.ree.theos.bq.drop;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.type.ItemWrapper;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.action.NullableAction;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Action that drops items at a location that
 * supports a "private" state in addition to the standard implementation.
 */
public class PrivateDropAction implements NullableAction {

    /**
     * Plugin to show the item when hidden.
     */
    private final Plugin plugin;

    /**
     * Items to be dropped.
     */
    private final Argument<List<ItemWrapper>> items;

    /**
     * Location to drop the items at.
     */
    private final Argument<Location> location;

    /**
     * Custom drop modes for the items.
     */
    private final Argument<List<DropMode>> modes;

    /**
     * Creates an action that drops the given items at a location selected by the given selector.
     *
     * @param plugin   the plugin to show the item when hidden
     * @param items    items to be dropped
     * @param location the location to drop the items at
     * @param modes    the custom drop modes for the items
     */
    public PrivateDropAction(final Plugin plugin, final Argument<List<ItemWrapper>> items, final Argument<Location> location,
            final Argument<List<DropMode>> modes) {
        this.plugin = plugin;
        this.items = items;
        this.location = location;
        this.modes = modes;
    }

    @Override
    public void execute(@Nullable final Profile profile) throws QuestException {
        final Location location = this.location.getValue(profile);
        final World world = location.getWorld();
        final Consumer<Item> itemConsumer = getItemConsumer(profile);

        for (final ItemWrapper itemDefinition : items.getValue(profile)) {
            final ItemStack item = itemDefinition.generate(profile);

            int remaining = itemDefinition.getAmount().getValue(profile).intValue();
            while (remaining > 0) {
                final int stackSize = Math.min(remaining, item.getMaxStackSize());
                world.dropItem(location, item.asQuantity(stackSize), itemConsumer);
                remaining -= stackSize;
            }
        }
    }

    private Consumer<Item> getItemConsumer(final @Nullable Profile profile) throws QuestException {
        final List<DropMode> modes = this.modes.getValue(profile);
        if (profile == null || modes.isEmpty()) {
            return item -> {};
        }
        return item -> {
            item.setOwner(profile.getPlayerUUID());
            if (modes.contains(DropMode.PERSONAL)) {
                item.setVisibleByDefault(false);
                item.getPersistentDataContainer().set(DropMode.SHOW_OWNER_KEY, PersistentDataType.BOOLEAN, true);
                profile.getOnlineProfile()
                        .map(OnlineProfile::getPlayer)
                        .ifPresent(player -> player.showEntity(plugin, item));
            }
        };
    }

    @Override
    public boolean isPrimaryThreadEnforced() {
        return true;
    }
}
