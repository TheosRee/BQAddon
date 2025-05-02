package de.ree.theos.bq;

import de.ree.theos.bq.objective.ChatObjectiveFactory;
import org.betonquest.betonquest.BetonQuest;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * TheosRee's BetonQuest AddOn.
 */
public final class TRBQAddon extends JavaPlugin {
    @Override
    public void onEnable() {
        BetonQuest.getInstance().getQuestRegistries().objective().register("chat", new ChatObjectiveFactory());
    }
}
