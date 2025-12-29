package de.ree.theos.bq.playtime;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.variable.online.OnlineVariable;
import org.betonquest.betonquest.quest.event.folder.TimeUnit;
import org.bukkit.Statistic;

/**
 * Resolves to a specified unit of playtime.
 */
public class PlaytimeVariable implements OnlineVariable {

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
        return switch (unit.getValue(profile)) {
            case TICKS -> String.valueOf(playedTicks);
            case SECONDS -> String.valueOf(playedTicks / 20);
            case MINUTES -> String.valueOf(playedTicks / (20 * 60));
        };
    }
}
