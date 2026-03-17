package de.ree.theos.bq;

import de.ree.theos.bq.animation.SwingArmEventFactory;
import de.ree.theos.bq.drop.DropListener;
import de.ree.theos.bq.drop.PrivateDropActionFactory;
import de.ree.theos.bq.objective.ChatObjectiveFactory;
import de.ree.theos.bq.objective.PlaceBlockStoreLocationObjectiveFactory;
import de.ree.theos.bq.playtime.PlaytimeConditionFactory;
import de.ree.theos.bq.playtime.PlaytimeObjectiveFactory;
import de.ree.theos.bq.playtime.PlaytimeVariableFactory;
import org.betonquest.betonquest.api.BetonQuestApi;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.identifier.IdentifierFactory;
import org.betonquest.betonquest.api.identifier.ObjectiveIdentifier;
import org.betonquest.betonquest.api.integration.Integration;
import org.betonquest.betonquest.api.integration.IntegrationService;
import org.betonquest.betonquest.api.service.action.ActionRegistry;
import org.betonquest.betonquest.api.service.objective.ObjectiveManager;
import org.betonquest.betonquest.api.service.objective.ObjectiveRegistry;
import org.betonquest.betonquest.api.service.objective.Objectives;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * TheosRee's BetonQuest AddOn.
 */
public final class TRBQAddon extends JavaPlugin implements Integration {
    @Override
    public void onLoad() {
        final IntegrationService service = getServer().getServicesManager().load(IntegrationService.class);
        service.withPolicy(null).register(this, () -> this);
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
