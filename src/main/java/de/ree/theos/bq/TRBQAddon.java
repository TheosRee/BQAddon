package de.ree.theos.bq;

import de.ree.theos.bq.objective.ChatObjectiveFactory;
import de.ree.theos.bq.playtime.PlaytimeConditionFactory;
import de.ree.theos.bq.playtime.PlaytimeObjectiveFactory;
import de.ree.theos.bq.playtime.PlaytimeVariableFactory;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.kernel.registry.quest.QuestTypeRegistries;
import org.betonquest.betonquest.quest.PrimaryServerThreadData;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * TheosRee's BetonQuest AddOn.
 */
public final class TRBQAddon extends JavaPlugin {
    @Override
    public void onEnable() {
        final BetonQuest betonQuest = BetonQuest.getInstance();
        final Server server = betonQuest.getServer();
        final PrimaryServerThreadData data = new PrimaryServerThreadData(server, server.getScheduler(), betonQuest);
        final QuestTypeRegistries questRegistries = betonQuest.getQuestRegistries();
        questRegistries.objective().register("chat", new ChatObjectiveFactory());

        questRegistries.condition().register("playtime", new PlaytimeConditionFactory(data));
        questRegistries.objective().register("playtime", new PlaytimeObjectiveFactory());
        questRegistries.variable().register("playtime", new PlaytimeVariableFactory());
    }
}
