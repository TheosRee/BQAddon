package de.ree.theos.bq.playtime;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.condition.PlayerCondition;
import org.betonquest.betonquest.quest.action.folder.TimeUnit;
import org.bukkit.Statistic;

/**
 * Returns true if the player is sneaking.
 */
public class PlaytimeCondition implements PlayerCondition {

    private final Argument<Number> amount;

    private final Argument<TimeUnit> unit;

    /**
     * Create the playtime condition.
     */
    public PlaytimeCondition(final Argument<Number> amount, final Argument<TimeUnit> unit) {
        this.amount = amount;
        this.unit = unit;
    }

    @Override
    public boolean check(final Profile profile) throws QuestException {
        final int playedTickTime = profile.getPlayer().getStatistic(Statistic.TOTAL_WORLD_TIME);
        final long wantedTickTime = unit.getValue(profile).getTicks(amount.getValue(profile).longValue());
        return playedTickTime >= wantedTickTime;
    }

    @Override
    public boolean isPrimaryThreadEnforced() {
        return true;
    }
}
