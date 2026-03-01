package de.ree.theos.bq.objective;

import org.betonquest.betonquest.api.DefaultObjective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.identifier.ObjectiveIdentifier;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.type.BlockSelector;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.objective.service.ObjectiveService;
import org.betonquest.betonquest.api.service.objective.ObjectiveManager;
import org.betonquest.betonquest.quest.objective.variable.VariableObjective;
import org.betonquest.betonquest.quest.placeholder.location.LocationFormationMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Requires to place a block and stores the location in a variable.
 */
public class PlaceBlockStoreLocationObjective extends DefaultObjective {

    /**
     * Objective manager to get requested objective.
     */
    private final ObjectiveManager manager;

    /**
     * Block Selector parameter.
     */
    private final Argument<BlockSelector> selector;

    /**
     * A {@link VariableObjective} and key where the chat message should be stored.
     */
    private final Argument<Map.Entry<ObjectiveIdentifier, String>> variable;

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
     * @param service      the {@link ObjectiveService} for this objective
     * @param manager      manager to get requested objective
     * @param selector     the block selector to match placed block
     * @param mode         the ulf mode to use for the stored location
     * @param variable     the variable to store the location into
     * @param vector       the vector to add to the location to store
     * @param exactMatch   the exact match flag
     * @param location     the location of the block
     * @param region       the second location defining a region
     * @param ignoreCancel the ignore cancel flag
     */
    public PlaceBlockStoreLocationObjective(
            final ObjectiveService service, final ObjectiveManager manager, final Argument<BlockSelector> selector,
            final Argument<LocationFormationMode> mode, final Argument<Map.Entry<ObjectiveIdentifier, String>> variable,
            final @Nullable Argument<Vector> vector, final boolean exactMatch,
            final @Nullable Argument<Location> location, final @Nullable Argument<Location> region, final boolean ignoreCancel
    ) {
        super(service);
        this.manager = manager;
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
    public void onBlockPlace(final BlockPlaceEvent event, final OnlineProfile onlineProfile) throws QuestException {
        final BlockSelector blockSelector = selector.getValue(onlineProfile);
        final Block block = event.getBlock();
        final Location location = block.getLocation();
        if (blockSelector.match(block, exactMatch) && checkLocation(location, onlineProfile)) {
            final Map.Entry<ObjectiveIdentifier, String> variable = this.variable.getValue(onlineProfile);
            final ObjectiveIdentifier id = variable.getKey();
            if (manager.getObjective(id) instanceof VariableObjective variableObjective) {
                if (vector != null) {
                    location.add(vector.getValue(onlineProfile));
                }
                final String serialized = mode.getValue(onlineProfile).getFormattedLocation(location, 0);
                if (!variableObjective.store(onlineProfile, variable.getValue(), serialized)) {
                    throw new QuestException("Can't store value in variable objective '" + id
                            + "' because it is not active for the player!");
                }
                getService().complete(onlineProfile);
            } else {
                throw new QuestException("Can't store value in objective '" + id
                        + "' because it is not a variable objective!");
            }
        }
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
}
