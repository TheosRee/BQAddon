package de.ree.theos.bq.playtime;

import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.instruction.argument.Argument;
import org.betonquest.betonquest.api.instruction.variable.Variable;
import org.betonquest.betonquest.api.quest.objective.ObjectiveFactory;
import org.betonquest.betonquest.quest.event.folder.TimeUnit;

/**
 * Factory for creating {@link PlaytimeObjective}s from {@link Instruction}s.
 */
public class PlaytimeObjectiveFactory implements ObjectiveFactory {

    /**
     * Creates a new instance of the playtime objective Factory.
     */
    public PlaytimeObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<Number> playtime = instruction.get(Argument.NUMBER_NOT_LESS_THAN_ZERO);
        final Variable<CountingMode> mode = instruction.get(Argument.ENUM(CountingMode.class));
        final Variable<Number> interval = instruction.getValue("interval", Argument.NUMBER_NOT_LESS_THAN_ONE, 1200);
        final Variable<TimeUnit> unit = instruction.getValue("unit", Argument.ENUM(TimeUnit.class), TimeUnit.SECONDS);
        return new PlaytimeObjective(instruction, playtime, mode, unit, interval);
    }
}
