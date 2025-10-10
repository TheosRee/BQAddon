package de.ree.theos.bq.animation;

import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.instruction.argument.types.EnumParser;
import org.betonquest.betonquest.api.instruction.variable.Variable;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.event.PlayerEvent;
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory;
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Factory to create {@link SwingArmEvent}s from {@link Instruction}s.
 */
public class SwingArmEventFactory implements PlayerEventFactory {
    /**
     * Logger factory to create new class specific logger.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Create a new Factory.
     *
     * @param loggerFactory the logger factory to create new class specific logger
     */
    public SwingArmEventFactory(final BetonQuestLoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<EquipmentSlot> hand = instruction.get(HandParser.HAND);
        return new OnlineEventAdapter(new SwingArmEvent(hand), loggerFactory.create(SwingArmEvent.class), instruction.getPackage());
    }

    /**
     * Parses a string to Equipment Hands.
     */
    private static class HandParser extends EnumParser<EquipmentSlot> {
        /**
         * The default instance of {@link HandParser}.
         */
        private static final HandParser HAND = new HandParser();

        public HandParser() {
            super(EquipmentSlot.class);
        }

        @Override
        public EquipmentSlot apply(final String string) throws QuestException {
            final EquipmentSlot slot = super.apply(string);
            if (slot.isHand()) {
                return slot;
            }
            throw new QuestException("Equipment slot " + slot + " is not hand");
        }
    }
}
