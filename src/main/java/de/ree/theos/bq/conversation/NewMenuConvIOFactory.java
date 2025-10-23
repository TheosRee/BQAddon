package de.ree.theos.bq.conversation;

import org.betonquest.betonquest.api.common.component.FixedComponentLineWrapper;
import org.betonquest.betonquest.api.common.component.font.FontRegistry;
import org.betonquest.betonquest.api.config.ConfigAccessor;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.text.TextParser;
import org.betonquest.betonquest.compatibility.protocollib.conversation.MenuConvIO.ACTION;
import org.betonquest.betonquest.compatibility.protocollib.conversation.MenuConvIO.CONTROL;
import org.betonquest.betonquest.compatibility.protocollib.conversation.MenuConvIOSettings;
import org.betonquest.betonquest.conversation.Conversation;
import org.betonquest.betonquest.conversation.ConversationColors;
import org.betonquest.betonquest.conversation.ConversationIO;
import org.betonquest.betonquest.conversation.ConversationIOFactory;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Menu conversation output.
 */
public class NewMenuConvIOFactory implements ConversationIOFactory {
    /**
     * Plugin instance to run tasks.
     */
    private final Plugin plugin;

    /**
     * the text parser to parse the configuration text.
     */
    private final TextParser textParser;

    /**
     * The font registry to use in APIs that work with {@link net.kyori.adventure.text.Component}.
     */
    private final FontRegistry fontRegistry;

    /**
     * The colors used for the conversation.
     */
    private final ConversationColors colors;

    /**
     * The config accessor to the plugin's configuration.
     */
    private final ConfigAccessor config;

    /**
     * Create a new Menu conversation IO factory.
     *
     * @param plugin       the plugin instance to run tasks
     * @param textParser   the text parser to parse the configuration text
     * @param fontRegistry the font registry used for the conversation
     * @param config       the config accessor to the plugin's configuration
     * @param colors       the colors used for the conversation
     */
    public NewMenuConvIOFactory(final Plugin plugin, final TextParser textParser, final FontRegistry fontRegistry,
            final ConfigAccessor config, final ConversationColors colors) {
        this.plugin = plugin;
        this.textParser = textParser;
        this.fontRegistry = fontRegistry;
        this.config = config;
        this.colors = colors;
    }

    @Override
    public ConversationIO parse(final Conversation conversation, final OnlineProfile onlineProfile) throws QuestException {
        final MenuConvIOSettings settings = MenuConvIOSettings.fromConfigurationSection(
                textParser, config.getConfigurationSection("conversation.io.menu"));
        final FixedComponentLineWrapper componentLineWrapper = new FixedComponentLineWrapper(fontRegistry, settings.lineLength());
        return new NewMenuConvIO(conversation, onlineProfile, colors,
                settings, componentLineWrapper, plugin, getControls(settings));
    }

    private Map<CONTROL, ACTION> getControls(final MenuConvIOSettings settings) throws QuestException {
        final Map<CONTROL, ACTION> controls = new EnumMap<>(CONTROL.class);
        for (final CONTROL control : controls(settings.controlCancel(), "control_cancel")) {
            if (!controls.containsKey(control)) {
                controls.put(control, ACTION.CANCEL);
            }
        }
        for (final CONTROL control : controls(settings.controlSelect(), "control_select")) {
            if (!controls.containsKey(control)) {
                controls.put(control, ACTION.SELECT);
            }
        }
        for (final CONTROL control : controls(settings.controlMove(), "control_move")) {
            if (!controls.containsKey(control)) {
                controls.put(control, ACTION.MOVE);
            }
        }
        return controls;
    }

    private List<CONTROL> controls(final String string, final String name) throws QuestException {
        try {
            return Arrays.stream(string.split(","))
                    .map(s -> s.toUpperCase(Locale.ROOT))
                    .map(CONTROL::valueOf).toList();
        } catch (final IllegalArgumentException e) {
            throw new QuestException("Invalid data for '" + name + "': " + string, e);
        }
    }
}
