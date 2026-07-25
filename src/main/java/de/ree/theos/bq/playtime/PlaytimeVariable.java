package de.ree.theos.bq.playtime;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.placeholder.OnlinePlaceholder;
import org.betonquest.betonquest.lib.argument.type.TimeUnit;
import org.bukkit.Statistic;

/**
 * Resolves to a specified unit of playtime.
 */
public class PlaytimeVariable implements OnlinePlaceholder {

    /**
     * The unit to convert playtime to.
     */
    private final Argument<TimeUnit> unit;

    /**
     * Create a new playtime variable.
     *
     * @param unit the unit to display
     */
    public PlaytimeVariable(final Argument<TimeUnit> unit) {
        this.unit = unit;
    }

    @Override
    public String getValue(final OnlineProfile profile) throws QuestException {
        final int playedTicks = profile.getPlayer().getStatistic(Statistic.TOTAL_WORLD_TIME);
        return String.valueOf(playedTicks / unit.getValue(profile).getTicks(1));
    }
}
