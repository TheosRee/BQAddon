package de.ree.theos.bq.playtime;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.quest.variable.PlayerVariable;
import org.betonquest.betonquest.api.quest.variable.PlayerVariableFactory;
import org.betonquest.betonquest.api.quest.variable.online.OnlineVariableAdapter;
import org.betonquest.betonquest.quest.event.folder.TimeUnit;

/**
 * Factory to create {@link PlaytimeVariable}s from {@link Instruction}s.
 */
public class PlaytimeVariableFactory implements PlayerVariableFactory {

    /**
     * Create a new factory to create Playtime Variables.
     */
    public PlaytimeVariableFactory() {

    }

    @Override
    public PlayerVariable parsePlayer(final Instruction instruction) throws QuestException {
        final Argument<TimeUnit> unit;
        if (instruction.hasNext()) {
            unit = instruction.enumeration(TimeUnit.class).get();
        } else {
            unit = profile -> TimeUnit.SECONDS;
        }

        return new OnlineVariableAdapter(new PlaytimeVariable(unit));
    }
}
