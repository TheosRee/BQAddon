package de.ree.theos.bq.objective;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.instruction.variable.Variable;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.objective.ObjectiveID;
import org.betonquest.betonquest.quest.objective.variable.VariableObjective;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Catches the next chat message of a player.
 */
public class ChatObjective extends Objective implements Listener {

    /**
     * If the chat event should be cancelled.
     */
    private final boolean cancel;

    /**
     * A {@link VariableObjective} and key where the chat message should be stored.
     */
    @Nullable
    private final Variable<Map.Entry<ObjectiveID, String>> variable;

    /**
     * Create a new Chat Objective from an Instruction string.
     *
     * @param instruction the user provided instruction string
     * @throws QuestException when the Instruction is invalid or the VariableObjective does not exist
     */
    public ChatObjective(final Instruction instruction, final boolean cancel,
            @Nullable final Variable<Map.Entry<ObjectiveID, String>> variable) throws QuestException {
        super(instruction);
        this.cancel = cancel;
        this.variable = variable;
    }

    @Override
    public String getProperty(final String name, final Profile profile) {
        return "";
    }

    /**
     * Intercepts a player message to eventually store it.
     *
     * @param event the event to listen to
     */
    @EventHandler(ignoreCancelled = true)
    public void onChat(final AsyncPlayerChatEvent event) {
        final OnlineProfile onlineProfile = profileProvider.getProfile(event.getPlayer());
        if (!containsPlayer(onlineProfile) || !checkConditions(onlineProfile)) {
            return;
        }

        if (cancel) {
            event.setCancelled(true);
        }

        if (variable != null) {
            qeHandler.handle(() -> {
                final Map.Entry<ObjectiveID, String> variable = this.variable.getValue(onlineProfile);
                final ObjectiveID id = variable.getKey();
                if (BetonQuest.getInstance().getQuestTypeApi()
                        .getObjective(id) instanceof VariableObjective variableObjective) {
                    if (!variableObjective.store(onlineProfile, variable.getValue(), event.getMessage())) {
                        throw new QuestException("Can't store value in variable objective '" + id
                                + "' because it is not active for the player!");
                    }
                } else {
                    throw new QuestException("Can't store value in variable objective '" + id
                            + "' because it is not an variable objective!");
                }
            });
        }

        completeObjective(onlineProfile);
    }

    @Override
    public String getDefaultDataInstruction() {
        return "";
    }
}
