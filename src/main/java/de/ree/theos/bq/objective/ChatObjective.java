package de.ree.theos.bq.objective;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.betonquest.betonquest.api.DefaultObjective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.identifier.ObjectiveIdentifier;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.objective.service.ObjectiveService;
import org.betonquest.betonquest.api.service.objective.ObjectiveManager;
import org.betonquest.betonquest.quest.objective.variable.VariableObjective;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Catches the next chat message of a player.
 */
public class ChatObjective extends DefaultObjective {

    /**
     * Objective manager to get requested objective.
     */
    private final ObjectiveManager objectiveManager;

    /**
     * If the chat event should be cancelled.
     */
    private final boolean cancel;

    /**
     * A {@link VariableObjective} and key where the chat message should be stored.
     */
    @Nullable
    private final Argument<Map.Entry<ObjectiveIdentifier, String>> variable;

    /**
     * Create a new Chat Objective from an Instruction string.
     *
     * @param service the {@link ObjectiveService} for this objective
     * @param manager manager to get requested objective
     */
    public ChatObjective(final ObjectiveService service, final ObjectiveManager manager, final boolean cancel,
            @Nullable final Argument<Map.Entry<ObjectiveIdentifier, String>> variable) {
        super(service);
        this.objectiveManager = manager;
        this.cancel = cancel;
        this.variable = variable;
    }

    /**
     * Intercepts a player message to eventually store it.
     *
     * @param event the event to listen to
     */
    public void onChat(final AsyncChatEvent event, final OnlineProfile profile) throws QuestException {

        if (cancel) {
            event.setCancelled(true);
        }

        if (variable != null) {
            final Map.Entry<ObjectiveIdentifier, String> variable = this.variable.getValue(profile);
            final ObjectiveIdentifier id = variable.getKey();
            if (objectiveManager.getObjective(id) instanceof VariableObjective variableObjective) {
                if (!variableObjective.store(profile, variable.getValue(), event.signedMessage().message())) {
                    throw new QuestException("Can't store value in variable objective '" + id
                            + "' because it is not active for the player!");
                }
            } else {
                throw new QuestException("Can't store value in variable objective '" + id
                        + "' because it is not an variable objective!");
            }
        }

        getService().complete(profile);
    }
}
