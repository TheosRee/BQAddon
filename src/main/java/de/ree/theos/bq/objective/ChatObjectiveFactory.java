package de.ree.theos.bq.objective;

import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.id.ObjectiveID;
import org.betonquest.betonquest.instruction.Instruction;
import org.betonquest.betonquest.instruction.argument.PackageArgument;
import org.betonquest.betonquest.instruction.variable.Variable;
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

    /**
     * Parses a string to an objective id and variable key.
     */
    private static class VariableParser implements PackageArgument<Map.Entry<ObjectiveID, String>> {
        /**
         * The default instance of {@link VariableParser}.
         */
        public static final VariableParser VARIABLE = new VariableParser();

        @Override
        public Map.Entry<ObjectiveID, String> apply(final QuestPackage questPackage, final String string) throws QuestException {
            final String[] split = string.split("#");
            if (split.length != 2) {
                throw new QuestException("Invalid variable '" + string + "' does not contain ID and Key!");
            }
            final ObjectiveID ObjectiveID = new ObjectiveID(questPackage, split[0]);
            return Map.entry(ObjectiveID, split[1]);
        }
    }
}
