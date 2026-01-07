package de.ree.theos.bq.animation;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.action.online.OnlineAction;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Event to swing an arm of a player.
 */
public class SwingArmEvent implements OnlineAction {
    /**
     * Hand to swing.
     */
    private final Argument<EquipmentSlot> hand;

    /**
     * Create a new Event.
     *
     * @param hand the hand to swing
     */
    public SwingArmEvent(final Argument<EquipmentSlot> hand) {
        this.hand = hand;
    }

    @Override
    public void execute(final OnlineProfile profile) throws QuestException {
        profile.getPlayer().swingHand(hand.getValue(profile));
    }
}
