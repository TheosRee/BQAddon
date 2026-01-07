package de.ree.theos.bq.playtime;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.quest.placeholder.PlayerPlaceholder;
import org.betonquest.betonquest.api.quest.placeholder.PlayerPlaceholderFactory;
import org.betonquest.betonquest.api.quest.placeholder.online.OnlinePlaceholderAdapter;
import org.betonquest.betonquest.quest.action.folder.TimeUnit;

/**
 * Factory to create {@link PlaytimeVariable}s from {@link Instruction}s.
 */
public class PlaytimeVariableFactory implements PlayerPlaceholderFactory {

    /**
     * Create a new factory to create Playtime Variables.
     */
    public PlaytimeVariableFactory() {

    }

    @Override
    public PlayerPlaceholder parsePlayer(final Instruction instruction) throws QuestException {
        final Argument<TimeUnit> unit;
        if (instruction.hasNext()) {
            unit = instruction.enumeration(TimeUnit.class).get();
        } else {
            unit = profile -> TimeUnit.SECONDS;
        }

        return new OnlinePlaceholderAdapter(new PlaytimeVariable(unit));
    }
}
