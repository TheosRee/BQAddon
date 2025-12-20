package de.ree.theos.bq.animation;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.variable.Variable;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Event to swing an arm of a player.
 */
public class SwingArmEvent implements OnlineEvent {
    /**
     * Hand to swing.
     */
    private final Variable<EquipmentSlot> hand;

    /**
     * Create a new Event.
     *
     * @param hand the hand to swing
     */
    public SwingArmEvent(final Variable<EquipmentSlot> hand) {
        this.hand = hand;
    }

    @Override
    public void execute(final OnlineProfile profile) throws QuestException {
        profile.getPlayer().swingHand(hand.getValue(profile));
    }
}
