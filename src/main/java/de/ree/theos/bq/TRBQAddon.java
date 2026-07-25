package de.ree.theos.bq;

import de.ree.theos.bq.animation.SwingArmEventFactory;
import de.ree.theos.bq.drop.DropListener;
import de.ree.theos.bq.drop.PrivateDropActionFactory;
import de.ree.theos.bq.npc.DespawnActionFactory;
import de.ree.theos.bq.npc.PlayerCitizensNpcFactory;
import de.ree.theos.bq.npc.SpawnActionFactory;
import de.ree.theos.bq.objective.ChatObjectiveFactory;
import de.ree.theos.bq.objective.PlaceBlockStoreLocationObjectiveFactory;
import de.ree.theos.bq.playtime.PlaytimeConditionFactory;
import de.ree.theos.bq.playtime.PlaytimeObjectiveFactory;
import de.ree.theos.bq.playtime.PlaytimeVariableFactory;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.identifier.IdentifierFactory;
import org.betonquest.betonquest.api.identifier.NpcIdentifier;
import org.betonquest.betonquest.api.identifier.ObjectiveIdentifier;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.integration.Integration;
import org.betonquest.betonquest.api.integration.IntegrationService;
import org.betonquest.betonquest.api.quest.action.PlayerAction;
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory;
import org.betonquest.betonquest.api.service.action.ActionRegistry;
import org.betonquest.betonquest.api.service.npc.NpcManager;
import org.betonquest.betonquest.api.service.objective.ObjectiveManager;
import org.betonquest.betonquest.api.service.objective.ObjectiveRegistry;
import org.betonquest.betonquest.api.service.objective.Objectives;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * TheosRee's BetonQuest AddOn.
 */
public final class TRBQAddon extends JavaPlugin implements Integration {
    @Override
    public void onLoad() {
        final IntegrationService service = getServer().getServicesManager().load(IntegrationService.class);
        assert service != null;
        service.withPolicies().register(this, () -> this);
    }

    @Override
    public void enable(final BetonQuestApi api) throws QuestException {
        final IdentifierFactory<ObjectiveIdentifier> identifierFactory = api.identifiers().getFactory(ObjectiveIdentifier.class);
        final VariableParser variableParser = new VariableParser(identifierFactory);
        final Objectives objectives = api.objectives();
        final ObjectiveRegistry objectiveRegistry = objectives.registry();
        final ObjectiveManager objectiveManager = objectives.manager();
        objectiveRegistry.register("chat", new ChatObjectiveFactory(objectiveManager, variableParser));
        objectiveRegistry.register("locStore", new PlaceBlockStoreLocationObjectiveFactory(objectiveManager, variableParser));

        api.conditions().registry().register("playtime", new PlaytimeConditionFactory());
        objectiveRegistry.register("playtime", new PlaytimeObjectiveFactory());
        api.placeholders().registry().register("playtime", new PlaytimeVariableFactory());

        final ActionRegistry actionRegistry = api.actions().registry();
        actionRegistry.register("swingArm", new SwingArmEventFactory());

        actionRegistry.registerCombined("drop", new PrivateDropActionFactory(this, api.profiles()));
        api.bukkit().registerEvents(new DropListener(this));

        api.npcs().registry().register("citizensTemp", new PlayerCitizensNpcFactory(CitizensAPI.getNPCRegistry()));
        final NpcManager npcManager = api.npcs().manager();
        actionRegistry.registerCombined("npcspawn", new SpawnActionFactory(npcManager));
        actionRegistry.registerCombined("npcdespawn", new DespawnActionFactory(npcManager));
        record A(NpcManager npcManager) implements PlayerActionFactory {

            @Override
            public PlayerAction parsePlayer(final Instruction instruction) throws QuestException {
                final Argument<NpcIdentifier> identifier = instruction.identifier(NpcIdentifier.class).get();
                final Argument<Location> locationArgument = instruction.location().get();
                return profile -> {
                    final NPC npc = (NPC) A.this.npcManager.get(profile, identifier.getValue(profile)).getOriginal();
                    npc.getNavigator().setTarget(locationArgument.getValue(profile));
                };
            }
        }
        actionRegistry.register("tmpmove", new A(npcManager));
    }

    @Override
    public void postEnable(final BetonQuestApi api) {
        // Empty
    }

    @Override
    public void disable() {
        // Empty
    }
}
