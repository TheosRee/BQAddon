package de.ree.theos.bq.objective;

import de.ree.theos.bq.VariableParser;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.id.ObjectiveID;
import org.betonquest.betonquest.instruction.Instruction;
import org.betonquest.betonquest.instruction.argument.Argument;
import org.betonquest.betonquest.instruction.variable.Variable;
import org.betonquest.betonquest.kernel.registry.TypeFactory;
import org.betonquest.betonquest.quest.variable.location.LocationFormationMode;
import org.betonquest.betonquest.util.BlockSelector;
import org.bukkit.Location;

import java.util.Map;

/**
 * Factory to create {@link PlaceBlockStoreLocationObjective}s from {@link Instruction}s.
 */
public class PlaceBlockStoreLocationObjectiveFactory implements TypeFactory<Objective> {
    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<BlockSelector> selector = instruction.get(Argument.BLOCK_SELECTOR);
        final Variable<Map.Entry<ObjectiveID, String>> variable = instruction.getValue("variable", VariableParser.VARIABLE);
        if (variable == null) {
            throw new QuestException("A 'variable' is required");
        }
        final Variable<LocationFormationMode> mode = instruction.getValue("mode",
                LocationFormationMode::getMode, LocationFormationMode.ULF_SHORT);
        final boolean exactMatch = instruction.hasArgument("exactMatch");
        final Variable<Location> location = instruction.getValue("loc", Argument.LOCATION);
        final Variable<Location> region = instruction.getValue("region", Argument.LOCATION);
        final boolean ignoreCancel = instruction.hasArgument("ignorecancel");
        return new PlaceBlockStoreLocationObjective(instruction, selector, mode, variable, exactMatch, location, region, ignoreCancel);
    }
}
