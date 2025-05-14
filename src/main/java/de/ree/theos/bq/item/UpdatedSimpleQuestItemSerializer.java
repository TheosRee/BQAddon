package de.ree.theos.bq.item;

import org.betonquest.betonquest.item.SimpleQuestItemFactory;
import org.betonquest.betonquest.item.SimpleQuestItemSerializer;
import org.betonquest.betonquest.item.typehandler.*;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Converts {@link ItemStack}s into the simple BQ format, parsable by a {@link SimpleQuestItemFactory}.
 */
public class UpdatedSimpleQuestItemSerializer extends SimpleQuestItemSerializer {

    /**
     * Constructs a new Simple Serializer with updated {@link ItemMetaHandler}s.
     */
    public UpdatedSimpleQuestItemSerializer() {
        super(List.of(
                new DurabilityHandler(), new UpdatedNameHandler(), new LoreHandler(), new EnchantmentsHandler(),
                new BookHandler(), new UpdatedPotionHandler(), new ColorHandler(), new HeadHandler(),
                new FireworkHandler(), new UnbreakableHandler(), new UpdatedCustomModelDataHandler(), new FlagHandler()
        ));
    }
}
