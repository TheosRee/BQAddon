package de.ree.theos.bq;

import de.ree.theos.bq.item.UpdatedSimpleItemFactory;
import de.ree.theos.bq.item.UpdatedSimpleQuestItemSerializer;
import de.ree.theos.bq.objective.ChatObjectiveFactory;
import de.ree.theos.bq.objective.PlaceBlockStoreLocationObjectiveFactory;
import de.ree.theos.bq.playtime.PlaytimeConditionFactory;
import de.ree.theos.bq.playtime.PlaytimeObjectiveFactory;
import de.ree.theos.bq.playtime.PlaytimeVariableFactory;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.common.component.BookPageWrapper;
import org.betonquest.betonquest.api.quest.QuestTypeRegistries;
import org.betonquest.betonquest.api.text.TextParser;
import org.betonquest.betonquest.item.ItemRegistry;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * TheosRee's BetonQuest AddOn.
 */
public final class TRBQAddon extends JavaPlugin {
    @Override
    public void onEnable() {
        final BetonQuest betonQuest = BetonQuest.getInstance();
        final QuestTypeRegistries questRegistries = betonQuest.getQuestRegistries();
        questRegistries.objective().register("chat", new ChatObjectiveFactory());
        questRegistries.objective().register("locStore", new PlaceBlockStoreLocationObjectiveFactory());

        questRegistries.condition().register("playtime", new PlaytimeConditionFactory(betonQuest.getPrimaryServerThreadData()));
        questRegistries.objective().register("playtime", new PlaytimeObjectiveFactory());
        questRegistries.variable().register("playtime", new PlaytimeVariableFactory());

        final ItemRegistry item = betonQuest.getFeatureRegistries().item();
        final TextParser textParser = betonQuest.getTextParser();
        final BookPageWrapper bookPageWrapper = new BookPageWrapper(betonQuest.getFontRegistry(), 114, 14);
        item.register("simple", new UpdatedSimpleItemFactory(betonQuest.getQuestPackageManager(), textParser, bookPageWrapper));
        item.registerSerializer("simple", new UpdatedSimpleQuestItemSerializer(textParser, bookPageWrapper));
    }
}
