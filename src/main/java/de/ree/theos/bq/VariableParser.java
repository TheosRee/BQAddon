package de.ree.theos.bq;

import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.id.ObjectiveID;
import org.betonquest.betonquest.instruction.argument.PackageArgument;

import java.util.Map;

/**
 * Parses a string to an objective id and variable key.
 */
public class VariableParser implements PackageArgument<Map.Entry<ObjectiveID, String>> {
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
