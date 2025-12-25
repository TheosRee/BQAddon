package de.ree.theos.bq.playtime;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.instruction.variable.Variable;
import org.betonquest.betonquest.api.quest.condition.PlayerCondition;
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory;
import org.betonquest.betonquest.quest.event.folder.TimeUnit;

/**
 * Factory for {@link PlaytimeCondition}s.
 */
public class PlaytimeConditionFactory implements PlayerConditionFactory {

    /**
     * Create the playtime condition factory.
     */
    public PlaytimeConditionFactory() {
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<Number> wantedTime = instruction.number().atLeast(1).get();
        final Variable<TimeUnit> unit = instruction.enumeration(TimeUnit.class).get("unit", TimeUnit.SECONDS);
        return new PlaytimeCondition(wantedTime, unit);
    }
}
