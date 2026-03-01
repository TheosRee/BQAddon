package de.ree.theos.bq.objective;

import de.ree.theos.bq.VariableParser;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.betonquest.betonquest.api.DefaultObjective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.identifier.ObjectiveIdentifier;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.quest.objective.ObjectiveFactory;
import org.betonquest.betonquest.api.quest.objective.service.ObjectiveService;
import org.betonquest.betonquest.api.service.objective.ObjectiveManager;
import org.bukkit.event.player.PlayerEvent;

import java.util.Map;

/**
 * Factory to create {@link ChatObjective}s from {@link Instruction}s.
 */
public class ChatObjectiveFactory implements ObjectiveFactory {

    /**
     * The objective manager.
     */
    private final ObjectiveManager manager;

    /**
     * Parser for variable objectives with target key.
     */
    private final VariableParser variableParser;

    public ChatObjectiveFactory(final ObjectiveManager manager, final VariableParser variableParser) {
        this.manager = manager;
        this.variableParser = variableParser;
    }

    @Override
    public DefaultObjective parseInstruction(final Instruction instruction, final ObjectiveService service)
            throws QuestException {
        final boolean cancel = instruction.bool().getFlag("cancel", true).getValue(null).orElse(false);
        final Argument<Map.Entry<ObjectiveIdentifier, String>> variable = instruction.parse(variableParser).get("variable")
                .orElse(null);
        final ChatObjective objective = new ChatObjective(service, manager, cancel, variable);
        service.request(AsyncChatEvent.class).onlineHandler(objective::onChat)
                .player(PlayerEvent::getPlayer).subscribe(true);
        return objective;
    }
}
