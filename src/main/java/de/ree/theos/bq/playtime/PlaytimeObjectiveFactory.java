package de.ree.theos.bq.playtime;

import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Instruction;
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
        final Variable<Number> playtime = instruction.number().atLeast(0).get();
        final Variable<CountingMode> mode = instruction.enumeration(CountingMode.class).get();
        final Variable<Number> interval = instruction.number().atLeast(1).get("interval", 1200);
        final Variable<TimeUnit> unit = instruction.enumeration(TimeUnit.class).get("unit", TimeUnit.SECONDS);
        return new PlaytimeObjective(instruction, playtime, mode, unit, interval);
    }
}
