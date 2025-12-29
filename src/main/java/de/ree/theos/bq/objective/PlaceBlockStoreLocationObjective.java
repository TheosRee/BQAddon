package de.ree.theos.bq.objective;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.DefaultObjective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.instruction.type.BlockSelector;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.objective.ObjectiveID;
import org.betonquest.betonquest.quest.objective.variable.VariableObjective;
import org.betonquest.betonquest.quest.variable.location.LocationFormationMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Requires to place a block and stores the location in a variable.
 */
public class PlaceBlockStoreLocationObjective extends DefaultObjective implements Listener {

    /**
     * Block Selector parameter.
     */
    private final Argument<BlockSelector> selector;

    /**
     * A {@link VariableObjective} and key where the chat message should be stored.
     */
    private final Argument<Map.Entry<ObjectiveID, String>> variable;

    /**
     * Ulf mode to use for storing the location.
     */
    private final Argument<LocationFormationMode> mode;

    /**
     * Vector to add to storing location.
     */
    @Nullable
    private final Argument<Vector> vector;

    /**
     * Optional exactMatch parameter.
     */
    private final boolean exactMatch;

    /**
     * Optional location parameter.
     */
    @Nullable
    private final Argument<Location> location;

    /**
     * Optional region parameter. Used together with {@link #location} to form a cuboid region.
     */
    @Nullable
    private final Argument<Location> region;

    /**
     * Create a new Objective.
     *
     * @param instruction  the user provided instruction string
     * @param selector     the block selector to match placed block
     * @param mode         the ulf mode to use for the stored location
     * @param variable     the variable to store the location into
     * @param vector       the vector to add to the location to store
     * @param exactMatch   the exact match flag
     * @param location     the location of the block
     * @param region       the second location defining a region
     * @param ignoreCancel the ignore cancel flag
     * @throws QuestException when the Instruction is invalid or the VariableObjective does not exist
     */
    public PlaceBlockStoreLocationObjective(final Instruction instruction, final Argument<BlockSelector> selector,
            final Argument<LocationFormationMode> mode, final Argument<Map.Entry<ObjectiveID, String>> variable,
            final @Nullable Argument<Vector> vector, final boolean exactMatch,
            final @Nullable Argument<Location> location, final @Nullable Argument<Location> region, final boolean ignoreCancel
    ) throws QuestException {
        super(instruction);
        this.selector = selector;
        this.mode = mode;
        this.vector = vector;
        this.exactMatch = exactMatch;
        this.location = location;
        this.region = region;
        this.variable = variable;
    }

    /**
     * Check if the placed block is the right one.
     *
     * @param event the event that triggered this method
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        qeHandler.handle(() -> {
            final OnlineProfile onlineProfile = profileProvider.getProfile(event.getPlayer());
            final BlockSelector blockSelector = selector.getValue(onlineProfile);
            final Block block = event.getBlock();
            final Location location = block.getLocation();
            if (containsPlayer(onlineProfile)
                    && blockSelector.match(block, exactMatch)
                    && checkConditions(onlineProfile)
                    && checkLocation(location, onlineProfile)) {
                final Map.Entry<ObjectiveID, String> variable = this.variable.getValue(onlineProfile);
                final ObjectiveID id = variable.getKey();
                if (BetonQuest.getInstance().getQuestTypeApi()
                        .getObjective(id) instanceof VariableObjective variableObjective) {
                    if (vector != null) {
                        location.add(vector.getValue(onlineProfile));
                    }
                    final String serialized = mode.getValue(onlineProfile).getFormattedLocation(location, 0);
                    if (!variableObjective.store(onlineProfile, variable.getValue(), serialized)) {
                        throw new QuestException("Can't store value in variable objective '" + id
                                + "' because it is not active for the player!");
                    }
                    completeObjective(onlineProfile);
                } else {
                    throw new QuestException("Can't store value in objective '" + id
                            + "' because it is not a variable objective!");
                }
            }
        });
    }

    private boolean checkLocation(final Location loc, final Profile profile) throws QuestException {
        if (location != null) {
            if (region != null) {
                return isInRange(loc, profile, location, region);
            }
            return loc.getBlock().getLocation().equals(location.getValue(profile));
        }
        return true;
    }

    private boolean isInRange(final Location loc, final Profile profile, final Argument<Location> location, final Argument<Location> region)
            throws QuestException {
        final Location loc1 = location.getValue(profile);
        final Location loc2 = region.getValue(profile);
        return inBetween(loc1, loc2, loc);
    }

    private boolean inBetween(final Location range1, final Location range2, final Location pos) {
        return inWorld(range1, range2, pos)
                && betweenCoordinates(range1.getBlockY(), range2.getBlockY(), pos.getBlockY())
                && betweenCoordinates(range1.getBlockZ(), range2.getBlockZ(), pos.getBlockZ())
                && betweenCoordinates(range1.getBlockX(), range2.getBlockX(), pos.getBlockX());
    }

    private boolean betweenCoordinates(final int range1, final int range2, final int pos) {
        return Integer.min(range1, range2) <= pos && pos <= Integer.max(range1, range2);
    }

    private boolean inWorld(final Location range1, final Location range2, final Location pos) {
        return range1.getWorld().equals(range2.getWorld()) && range2.getWorld().equals(pos.getWorld());
    }

    @Override
    public String getProperty(final String name, final Profile profile) {
        return "";
    }

    @Override
    public String getDefaultDataInstruction(final Profile profile) {
        return "";
    }
}
