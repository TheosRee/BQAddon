package de.ree.theos.bq;

import de.ree.theos.bq.npc.DespawnActionFactory;
import de.ree.theos.bq.npc.PlayerCitizensNpcFactory;
import de.ree.theos.bq.npc.SpawnActionFactory;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.identifier.NpcIdentifier;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.integration.Integration;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.action.PlayerAction;
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory;
import org.betonquest.betonquest.api.service.action.ActionRegistry;
import org.betonquest.betonquest.api.service.npc.NpcManager;
import org.bukkit.Location;

public class CtizensIntegration implements Integration {
    @Override
    public void enable(final BetonQuestApi api) {
        api.npcs().registry().register("citizensTemp", new PlayerCitizensNpcFactory(CitizensAPI.getNPCRegistry()));
        final ActionRegistry actionRegistry = api.actions().registry();
        final NpcManager npcManager = api.npcs().manager();
        actionRegistry.registerCombined("npcspawn", new SpawnActionFactory(npcManager));
        actionRegistry.registerCombined("npcdespawn", new DespawnActionFactory(npcManager));
        actionRegistry.register("tmpmove", new Factory(npcManager));
    }

    @Override
    public void postEnable(final BetonQuestApi betonQuestApi) {
        // Empty
    }

    @Override
    public void disable() {
        // Empty
    }

    private record Factory(NpcManager npcManager) implements PlayerActionFactory {

        @Override
        public PlayerAction parsePlayer(final Instruction instruction) throws QuestException {
            final Argument<NpcIdentifier> identifier = instruction.identifier(NpcIdentifier.class).get();
            final Argument<Location> locationArgument = instruction.location().get();
            return new Action(npcManager, identifier, locationArgument);
        }

        private record Action(NpcManager npcManager, Argument<NpcIdentifier> identifier,
                              Argument<Location> location) implements PlayerAction {

            @Override
            public void execute(final Profile profile) throws QuestException {
                if (!(npcManager.get(profile, identifier.getValue(profile)).getOriginal() instanceof NPC npc)) {
                    throw new QuestException("Only works with Citizens NPCs!");
                }
                npc.getNavigator().setTarget(location.getValue(profile));
            }
        }
    }
}
