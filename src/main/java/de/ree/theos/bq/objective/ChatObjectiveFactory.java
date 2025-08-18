package de.ree.theos.bq.objective;

import de.ree.theos.bq.VariableParser;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.instruction.variable.Variable;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.objective.ObjectiveID;
import org.betonquest.betonquest.kernel.registry.TypeFactory;

import java.util.Map;

/**
 * Factory to create {@link ChatObjective}s from {@link Instruction}s.
 */
public class ChatObjectiveFactory implements TypeFactory<Objective> {
    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final boolean cancel = instruction.hasArgument("cancel");
        final Variable<Map.Entry<ObjectiveID, String>> variable = instruction.getValue("variable", VariableParser.VARIABLE);
        return new ChatObjective(instruction, cancel, variable);
    }
}
