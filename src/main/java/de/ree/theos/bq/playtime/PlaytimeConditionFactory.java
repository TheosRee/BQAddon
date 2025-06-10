package de.ree.theos.bq.playtime;

import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.condition.PlayerCondition;
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory;
import org.betonquest.betonquest.instruction.Instruction;
import org.betonquest.betonquest.instruction.argument.Argument;
import org.betonquest.betonquest.instruction.variable.Variable;
import org.betonquest.betonquest.quest.PrimaryServerThreadData;
import org.betonquest.betonquest.quest.condition.PrimaryServerThreadPlayerCondition;
import org.betonquest.betonquest.quest.condition.sneak.SneakCondition;
import org.betonquest.betonquest.quest.event.folder.TimeUnit;

/**
 * Factory for {@link SneakCondition}s.
 */
public class PlaytimeConditionFactory implements PlayerConditionFactory {

    /**
     * Data used for condition check on the primary server thread.
     */
    private final PrimaryServerThreadData data;

    /**
     * Create the playtime condition factory.
     *
     * @param data the data used for checking the condition on the main thread
     */
    public PlaytimeConditionFactory(final PrimaryServerThreadData data) {
        this.data = data;
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<Number> wantedTime = instruction.get(Argument.NUMBER_NOT_LESS_THAN_ONE);
        final Variable<TimeUnit> unit = instruction.getValue("unit", Argument.ENUM(TimeUnit.class), TimeUnit.SECONDS);
        return new PrimaryServerThreadPlayerCondition(new PlaytimeCondition(wantedTime, unit), data);
    }
}
