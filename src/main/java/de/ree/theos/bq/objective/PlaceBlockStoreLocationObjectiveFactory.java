package de.ree.theos.bq.objective;

import de.ree.theos.bq.VariableParser;
import org.betonquest.betonquest.api.DefaultObjective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.instruction.type.BlockSelector;
import org.betonquest.betonquest.api.kernel.TypeFactory;
import org.betonquest.betonquest.api.quest.objective.ObjectiveID;
import org.betonquest.betonquest.quest.variable.location.LocationFormationMode;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Map;

/**
 * Factory to create {@link PlaceBlockStoreLocationObjective}s from {@link Instruction}s.
 */
public class PlaceBlockStoreLocationObjectiveFactory implements TypeFactory<DefaultObjective> {
    @Override
    public DefaultObjective parseInstruction(final Instruction instruction) throws QuestException {
        final Argument<BlockSelector> selector = instruction.blockSelector().get();
        final Argument<Map.Entry<ObjectiveID, String>> variable = instruction.parse(VariableParser.VARIABLE).get("variable").orElse(null);
        if (variable == null) {
            throw new QuestException("A 'variable' is required");
        }
        final Argument<LocationFormationMode> mode = instruction.parse(LocationFormationMode::getMode)
                .get("mode", LocationFormationMode.ULF_SHORT);
        final Argument<Vector> vector = instruction.vector().get("vector", null);
        final boolean exactMatch = instruction.bool().getFlag("exactMatch", true).getValue(null).orElse(false);
        final Argument<Location> location = instruction.location().get("loc", null);
        final Argument<Location> region = instruction.location().get("region", null);
        final boolean ignoreCancel = instruction.bool().getFlag("ignorecancel", true).getValue(null).orElse(false);
        return new PlaceBlockStoreLocationObjective(instruction, selector, mode, variable, vector, exactMatch, location, region,
                ignoreCancel);
    }
}
