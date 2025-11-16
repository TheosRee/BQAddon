package de.ree.theos.bq.conversation;

import org.betonquest.betonquest.compatibility.packetevents.conversation.input.ConversationAction;
import org.betonquest.betonquest.compatibility.packetevents.conversation.input.ConversationSession;
import org.bukkit.Bukkit;
import org.bukkit.Input;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Conversation Session that works on a {@link PlayerInputEvent}.
 */
public class InputEventSession implements ConversationSession, Listener {
    /**
     * Key for modifiers while in the conversation.
     */
    private static final NamespacedKey ATTRIBUTE_KEY = new NamespacedKey("betonquest", "menu_conv_io");

    /**
     * Attributes to set to 0 while in the conversation.
     */
    private static final Set<Attribute> ATTRIBUTES = new HashSet<>(Arrays.asList(Attribute.MOVEMENT_SPEED, Attribute.JUMP_STRENGTH));

    /**
     * Plugin instance to run tasks.
     */
    private final Plugin plugin;

    /**
     * Player of the session.
     */
    private final Player player;

    /**
     * The conversation action to call.
     */
    private final ConversationAction action;

    /**
     * Creates a new Conversation Input Session based on the {@link PlayerInputEvent}.
     *
     * @param plugin the plugin to start tasks
     * @param player the player of the session
     * @param action the conversation action to call on input
     */
    public InputEventSession(final Plugin plugin, final Player player, final ConversationAction action) {
        this.plugin = plugin;
        this.player = player;
        this.action = action;
    }

    @Override
    public void begin() {
        final AttributeModifier attributeModifier = new AttributeModifier(ATTRIBUTE_KEY,
                -1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        for (final Attribute attribute : ATTRIBUTES) {
            final AttributeInstance attributeInstance = player.getAttribute(attribute);
            if (attributeInstance != null) {
                attributeInstance.addTransientModifier(attributeModifier);
            }
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void end() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (final Attribute attribute : ATTRIBUTES) {
                final AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance != null) {
                    attributeInstance.removeModifier(ATTRIBUTE_KEY);
                }
            }
        });
    }

    /**
     * Processes the conversation on movement input.
     *
     * @param event the input event
     */
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void onInput(final PlayerInputEvent event) {
        if (!event.getPlayer().equals(player)) {
            return;
        }
        final Input input = event.getInput();
        if (input.isSneak()) {
            action.unmount();
        }
        if (input.isJump()) {
            action.jump();
        }
        if (input.isForward()) {
            action.forward();
        }
        if (input.isBackward()) {
            action.back();
        }
        if (input.isLeft()) {
            action.left();
        }
        if (input.isRight()) {
            action.right();
        }
    }
}
