package de.ree.theos.bq.conversation;

import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.api.common.component.FixedComponentLineWrapper;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.compatibility.protocollib.conversation.MenuConvIO.ACTION;
import org.betonquest.betonquest.compatibility.protocollib.conversation.MenuConvIO.CONTROL;
import org.betonquest.betonquest.compatibility.protocollib.conversation.MenuConvIOSettings;
import org.betonquest.betonquest.compatibility.protocollib.conversation.display.Display;
import org.betonquest.betonquest.compatibility.protocollib.conversation.display.Scroll;
import org.betonquest.betonquest.conversation.ChatConvIO;
import org.betonquest.betonquest.conversation.Conversation;
import org.betonquest.betonquest.conversation.ConversationColors;
import org.betonquest.betonquest.conversation.ConversationState;
import org.bukkit.Bukkit;
import org.bukkit.Input;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An {@link ChatConvIO} implementation that use player ingame movements to control the conversation.
 */
public class NewMenuConvIO extends ChatConvIO {
    /**
     * Key for modifiers while in the conversation.
     */
    private static final NamespacedKey ATTRIBUTE_KEY = new NamespacedKey("betonquest", "menu_conv_io");

    /**
     * Attributes to set to 0 while in the conversation.
     */
    private static final Set<Attribute> ATTRIBUTES = new HashSet<>(Arrays.asList(Attribute.MOVEMENT_SPEED, Attribute.JUMP_STRENGTH));

    /**
     * The controls that are used in the conversation.
     */
    protected final Map<CONTROL, ACTION> controls;

    /**
     * Thread safety.
     */
    private final Lock lock = new ReentrantLock();

    /**
     * Plugin instance to schedule tasks.
     */
    private final Plugin plugin;

    /**
     * All players that are currently on cooldown are in this list.
     * The cooldown is used to prevent players from spamming through the conversation or skipping through it by accident.
     */
    private final List<Player> selectionCooldowns = new ArrayList<>();

    /**
     * The settings for this conversation IO.
     */
    private final MenuConvIOSettings settings;

    /**
     * The component line wrapper to use for the conversation.
     */
    private final FixedComponentLineWrapper componentLineWrapper;

    /**
     * The current state of the conversation.
     */
    @SuppressWarnings("PMD.AvoidUsingVolatile")
    protected volatile ConversationState state = ConversationState.CREATED;

    /**
     * The runnable that updates the display.
     */
    @Nullable
    protected BukkitRunnable displayRunnable;

    /**
     * The display used to show the conversation.
     */
    @Nullable
    protected Display chatDisplay;

    /**
     * Creates a new MenuConvIO instance.
     *
     * @param conv                 the conversation this IO is part of
     * @param onlineProfile        the online profile of the player participating in the conversation
     * @param colors               the colors used in the conversation
     * @param settings             the settings for the conversation IO
     * @param componentLineWrapper the component line wrapper to use for the conversation
     * @param plugin               the plugin instance to run tasks
     * @param controls             the used controls
     */
    public NewMenuConvIO(final Conversation conv, final OnlineProfile onlineProfile, final ConversationColors colors,
            final MenuConvIOSettings settings, final FixedComponentLineWrapper componentLineWrapper,
            final Plugin plugin, final Map<CONTROL, ACTION> controls) {
        super(conv, onlineProfile, colors);
        this.plugin = plugin;
        this.settings = settings;
        this.componentLineWrapper = componentLineWrapper;
        this.controls = controls;
    }

    private void start() {
        if (state.isStarted()) {
            return;
        }

        lock.lock();
        try {
            if (state.isStarted()) {
                return;
            }
            state = ConversationState.ACTIVE;

            final Player player = onlineProfile.getPlayer();

            final AttributeModifier attributeModifier = new AttributeModifier(ATTRIBUTE_KEY,
                    -1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
            for (final Attribute attribute : ATTRIBUTES) {
                final AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance != null) {
                    attributeInstance.addTransientModifier(attributeModifier);
                }
            }

            Bukkit.getPluginManager().registerEvents(this, plugin);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Displays all data to the player. Should be called after setting all
     * options.
     */
    @Override
    public void display() {
        if (Component.empty().equals(npcText) && options.isEmpty()) {
            end(() -> {
            });
            return;
        }

        if (!options.isEmpty()) {
            start();
        }

        updateDisplay();
        if (settings.refreshDelay() > 0) {
            displayRunnable = new BukkitRunnable() {

                @Override
                public void run() {
                    updateDisplay();

                    if (state.isEnded()) {
                        this.cancel();
                    }
                }
            };
            displayRunnable.runTaskTimerAsynchronously(plugin, settings.refreshDelay(), settings.refreshDelay());
        }
    }

    // Override this event from our parent
    @SuppressWarnings("deprecation")
    @Override
    @EventHandler(ignoreCancelled = true)
    public void onReply(final AsyncPlayerChatEvent event) {
        // Empty
    }

    /**
     * Clears the data. Should be called before the cycle begins to ensure
     * nothing is left from previous one.
     */
    @Override
    public void clear() {
        if (displayRunnable != null) {
            displayRunnable.cancel();
            displayRunnable = null;
        }

        chatDisplay = null;

        super.clear();
    }

    /**
     * Ends the work of this conversation IO. Should be called when the
     * conversation ends.
     */
    @Override
    public void end(final Runnable callback) {
        if (state.isEnded()) {
            return;
        }
        lock.lock();
        try {
            if (state.isEnded()) {
                return;
            }
            state = ConversationState.ENDED;

            Bukkit.getScheduler().runTask(plugin, () -> {
                final Player player = onlineProfile.getPlayer();
                for (final Attribute attribute : ATTRIBUTES) {
                    final AttributeInstance attributeInstance = player.getAttribute(attribute);
                    if (attributeInstance != null) {
                        attributeInstance.removeModifier(ATTRIBUTE_KEY);
                    }
                }
            });

            // Stop updating display
            if (displayRunnable != null) {
                displayRunnable.cancel();
                displayRunnable = null;
            }

            super.end(callback);
        } finally {
            lock.unlock();
        }
    }

    private void passPlayerAnswer() {
        if (chatDisplay == null || isOnCooldown()) {
            return;
        }
        chatDisplay.getSelection().ifPresent(index -> conv.passPlayerAnswer(index + 1));
    }

    /**
     * Handles the player interact event.
     *
     * @param event the event
     */
    @SuppressWarnings("PMD.CollapsibleIfStatements")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerInteractEvent(final PlayerInteractEvent event) {
        if (state.isInactive()) {
            return;
        }

        lock.lock();
        try {
            if (state.isInactive()) {
                return;
            }

            if (!event.getPlayer().equals(onlineProfile.getPlayer())) {
                return;
            }

            event.setCancelled(true);

            final Action action = event.getAction();
            if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                if (controls.containsKey(CONTROL.LEFT_CLICK)) {
                    handleSteering(controls.get(CONTROL.LEFT_CLICK));
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Handles the player interact entity event.
     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerInteractEntityEvent(final PlayerInteractEntityEvent event) {
        if (state.isInactive()) {
            return;
        }

        lock.lock();
        try {
            if (state.isInactive()) {
                return;
            }

            if (!event.getPlayer().equals(onlineProfile.getPlayer())) {
                return;
            }

            event.setCancelled(true);

            if (controls.containsKey(CONTROL.LEFT_CLICK)) {
                handleSteering(controls.get(CONTROL.LEFT_CLICK));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Handles the entity damage by entity event.
     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityDamageByEntityEvent(final EntityDamageByEntityEvent event) {
        if (state.isInactive()) {
            return;
        }

        lock.lock();
        try {
            if (state.isInactive()) {
                return;
            }

            if (!event.getDamager().equals(onlineProfile.getPlayer())) {
                return;
            }

            event.setCancelled(true);

            if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) && controls.containsKey(CONTROL.LEFT_CLICK)) {
                handleSteering(controls.get(CONTROL.LEFT_CLICK));
            }
        } finally {
            lock.unlock();
        }
    }

    private void handleSteering(final ACTION action) {
        switch (action) {
            case CANCEL -> {
                if (!conv.isMovementBlock()) {
                    conv.endConversation();
                }
            }
            case SELECT -> {
                if (!isOnCooldown()) {
                    passPlayerAnswer();
                }
            }
            default -> {
            }
        }
    }

    /**
     * Handles the player item held event.
     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerItemHeldEvent(final PlayerItemHeldEvent event) {
        if (state.isInactive()) {
            return;
        }

        lock.lock();
        try {
            if (state.isInactive()) {
                return;
            }

            if (!event.getPlayer().equals(onlineProfile.getPlayer())) {
                return;
            }

            if (!controls.containsKey(CONTROL.SCROLL)) {
                return;
            }

            event.setCancelled(true);

            updateDisplay(getScrollDirection(event.getPreviousSlot(), event.getNewSlot()));
        } finally {
            lock.unlock();
        }
    }

    private void updateDisplay() {
        updateDisplay(Scroll.NONE);
    }

    private void updateDisplay(final Scroll scroll) {
        if (chatDisplay == null) {
            chatDisplay = new Display(settings, componentLineWrapper, npcName, npcText, new ArrayList<>(options.values()));
        }
        conv.sendMessage(chatDisplay.getDisplay(scroll));
    }

    private Scroll getScrollDirection(final int start, final int end) {
        for (int offset = 1; offset <= 4; offset++) {
            if ((start + offset) % 9 == end) {
                return Scroll.DOWN;
            }
        }
        return Scroll.UP;
    }

    /**
     * Processes the conversation on movement input.
     *
     * @param event the input event
     */
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void onInput(final PlayerInputEvent event) {
        if (!event.getPlayer().equals(onlineProfile.getPlayer()) || options.isEmpty()) {
            return;
        }
        final Input input = event.getInput();
        if (input.isJump() && controls.containsKey(CONTROL.JUMP)) {
            switch (controls.get(CONTROL.JUMP)) {
                case CANCEL:
                    if (!conv.isMovementBlock()) {
                        conv.endConversation();
                    }
                    break;
                case SELECT:
                    lock.lock();
                    try {
                        passPlayerAnswer();
                    } finally {
                        lock.unlock();
                    }
                    break;
                case MOVE:
                    break;
            }
        } else if (input.isBackward() && controls.containsKey(CONTROL.MOVE)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> NewMenuConvIO.this.updateDisplay(Scroll.DOWN));
        } else if (input.isForward() && controls.containsKey(CONTROL.MOVE)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> NewMenuConvIO.this.updateDisplay(Scroll.UP));
        } else if (input.isSneak() && controls.containsKey(CONTROL.SNEAK)) {
            switch (controls.get(CONTROL.SNEAK)) {
                case CANCEL -> {
                    if (!conv.isMovementBlock()) {
                        conv.endConversation();
                    }
                }
                case SELECT -> {
                    lock.lock();
                    try {
                        if (!isOnCooldown()) {
                            passPlayerAnswer();
                        }
                    } finally {
                        lock.unlock();
                    }
                }
                case MOVE -> {
                }
            }
        }
    }

    private boolean isOnCooldown() {
        final Player player = onlineProfile.getPlayer();
        if (selectionCooldowns.contains(player)) {
            return true;
        } else {
            selectionCooldowns.add(player);
            Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, () -> selectionCooldowns.remove(player), settings.rateLimit());
        }
        return false;
    }
}
