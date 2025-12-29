package de.ree.theos.bq.objective;

import de.ree.theos.bq.VariableParser;
import org.betonquest.betonquest.api.DefaultObjective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.kernel.TypeFactory;
import org.betonquest.betonquest.api.quest.objective.ObjectiveID;

import java.util.Map;

/**
 * Factory to create {@link ChatObjective}s from {@link Instruction}s.
 */
public class ChatObjectiveFactory implements TypeFactory<DefaultObjective> {
    @Override
    public DefaultObjective parseInstruction(final Instruction instruction) throws QuestException {
        final boolean cancel = instruction.bool().getFlag("cancel", true).getValue(null).orElse(false);
        final Argument<Map.Entry<ObjectiveID, String>> variable = instruction.parse(VariableParser.VARIABLE).get("variable").orElse(null);
        return new ChatObjective(instruction, cancel, variable);
    }
}
