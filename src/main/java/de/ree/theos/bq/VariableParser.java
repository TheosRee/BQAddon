package de.ree.theos.bq;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.config.quest.QuestPackageManager;
import org.betonquest.betonquest.api.identifier.IdentifierFactory;
import org.betonquest.betonquest.api.identifier.ObjectiveIdentifier;
import org.betonquest.betonquest.api.instruction.argument.InstructionArgumentParser;
import org.betonquest.betonquest.api.service.placeholder.PlaceholderManager;

import java.util.Map;

/**
 * Parses a string to an objective id and variable key.
 */
public class VariableParser implements InstructionArgumentParser<Map.Entry<ObjectiveIdentifier, String>> {

    private final IdentifierFactory<ObjectiveIdentifier> identifierFactory;

    public VariableParser(final IdentifierFactory<ObjectiveIdentifier> identifierFactory) {
        this.identifierFactory = identifierFactory;
    }

    @Override
    public Map.Entry<ObjectiveIdentifier, String> apply(final PlaceholderManager placeholders,
            final QuestPackageManager questPackageManager,
            final QuestPackage questPackage, final String string) throws QuestException {
        final String[] split = string.split("#");
        if (split.length != 2) {
            throw new QuestException("Invalid variable '" + string + "' does not contain ID and Key!");
        }
        final ObjectiveIdentifier ObjectiveID = identifierFactory.parseIdentifier(questPackage, split[0]);
        return Map.entry(ObjectiveID, split[1]);
    }
}
