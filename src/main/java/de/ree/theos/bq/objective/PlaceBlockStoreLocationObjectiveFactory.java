package de.ree.theos.bq.objective;

import de.ree.theos.bq.VariableParser;
import org.betonquest.betonquest.api.DefaultObjective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.identifier.ObjectiveIdentifier;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.instruction.type.BlockSelector;
import org.betonquest.betonquest.api.quest.objective.ObjectiveFactory;
import org.betonquest.betonquest.api.quest.objective.service.ObjectiveService;
import org.betonquest.betonquest.api.service.objective.ObjectiveManager;
import org.betonquest.betonquest.quest.placeholder.location.LocationFormationMode;
import org.bukkit.Location;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

import java.util.Map;

/**
 * Factory to create {@link PlaceBlockStoreLocationObjective}s from {@link Instruction}s.
 */
public class PlaceBlockStoreLocationObjectiveFactory implements ObjectiveFactory {
    /**
     * The objective manager.
     */
    private final ObjectiveManager manager;

    /**
     * Parser for variable objectives with target key.
     */
    private final VariableParser variableParser;

    public PlaceBlockStoreLocationObjectiveFactory(final ObjectiveManager manager, final VariableParser variableParser) {
        this.manager = manager;
        this.variableParser = variableParser;
    }

    @Override
    public DefaultObjective parseInstruction(final Instruction instruction, final ObjectiveService service)
            throws QuestException {
        final Argument<BlockSelector> selector = instruction.blockSelector().get();
        final Argument<Map.Entry<ObjectiveIdentifier, String>> variable = instruction.parse(variableParser).get("variable")
                .orElseThrow(() -> new QuestException("A 'variable' is required"));
        final Argument<LocationFormationMode> mode = instruction.parse(LocationFormationMode::getMode)
                .get("mode", LocationFormationMode.ULF_SHORT);
        final Argument<Vector> vector = instruction.vector().get("vector", null);
        final boolean exactMatch = instruction.bool().getFlag("exactMatch", true).getValue(null).orElse(false);
        final Argument<Location> location = instruction.location().get("loc", null);
        final Argument<Location> region = instruction.location().get("region", null);
        final boolean ignoreCancel = instruction.bool().getFlag("ignorecancel", true).getValue(null).orElse(false);
        final PlaceBlockStoreLocationObjective objective = new PlaceBlockStoreLocationObjective(service, manager, selector,
                mode, variable, vector, exactMatch, location, region, ignoreCancel);
        service.request(BlockPlaceEvent.class).onlineHandler(objective::onBlockPlace)
                .player(BlockPlaceEvent::getPlayer).priority(EventPriority.MONITOR).subscribe(true);
        return objective;
    }
}
