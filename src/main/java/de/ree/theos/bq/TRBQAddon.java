package de.ree.theos.bq;

import de.ree.theos.bq.objective.ChatObjective;
import org.betonquest.betonquest.BetonQuest;
import org.bukkit.plugin.java.JavaPlugin;

public final class TRBQAddon extends JavaPlugin {
    @Override
    public void onEnable() {
        BetonQuest.getInstance().registerObjectives("chat", ChatObjective.class);
    }
}
