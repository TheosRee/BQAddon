package de.ree.theos.bq.drop;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.instruction.type.ItemWrapper;
import org.betonquest.betonquest.api.profile.ProfileProvider;
import org.betonquest.betonquest.api.quest.action.*;
import org.betonquest.betonquest.lib.instruction.argument.DefaultArguments;
import org.betonquest.betonquest.quest.action.OnlineProfileGroupPlayerlessActionAdapter;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

/**
 * Factory to create {@link PrivateDropAction}s for items from {@link Instruction}s.
 */
public class PrivateDropActionFactory implements PlayerActionFactory, PlayerlessActionFactory {

    /**
     * Plugin to show the item when hidden.
     */
    private final Plugin plugin;

    /**
     * The profile provider instance.
     */
    private final ProfileProvider profileProvider;

    /**
     * Creates the drop action factory.
     *
     * @param plugin          the plugin to show the item when hidden
     * @param profileProvider the profile provider instance
     */
    public PrivateDropActionFactory(final Plugin plugin, final ProfileProvider profileProvider) {
        this.plugin = plugin;
        this.profileProvider = profileProvider;
    }

    @Override
    public PlayerAction parsePlayer(final Instruction instruction) throws QuestException {
        return createDropAction(instruction);
    }

    @Override
    public PlayerlessAction parsePlayerless(final Instruction instruction) throws QuestException {
        return createPlayerlessDropAction(instruction);
    }

    private PlayerlessAction createPlayerlessDropAction(final Instruction instruction) throws QuestException {
        final NullableActionAdapter dropAction = createDropAction(instruction);
        if (instruction.location().get("location").isEmpty()) {
            return new OnlineProfileGroupPlayerlessActionAdapter(profileProvider::getOnlineProfiles, dropAction);
        }
        return dropAction;
    }

    private NullableActionAdapter createDropAction(final Instruction instruction) throws QuestException {
        final Argument<List<ItemWrapper>> items = instruction.item().list().notEmpty().get("items", Collections.emptyList());
        final Argument<Location> location = instruction.location().get("location").orElse(DefaultArguments.PLAYER_LOCATION);
        final Argument<List<DropMode>> modes = instruction.enumeration(DropMode.class).list().get("mode", Collections.emptyList());
        return new NullableActionAdapter(new PrivateDropAction(plugin, items, location, modes));
    }
}
