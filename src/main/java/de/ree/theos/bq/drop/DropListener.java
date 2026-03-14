package de.ree.theos.bq.drop;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Listener to show hidden items to its owner.
 */
public class DropListener implements Listener {
    private final Plugin plugin;

    public DropListener(final Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLoad(final PlayerChunkLoadEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        for (final Entity entity : event.getChunk().getEntities()) {
            if (!entity.isVisibleByDefault()
                    && entity instanceof Item
                    && entity.getPersistentDataContainer().has(DropMode.SHOW_OWNER_KEY)
                    && uuid.equals(((Item) entity).getOwner())) {
                player.showEntity(plugin, entity);
            }
        }
    }
}
