package de.ree.theos.bq.npc;

import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.identifier.NpcIdentifier;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.quest.action.*;
import org.betonquest.betonquest.api.service.npc.NpcManager;

public record DespawnActionFactory(NpcManager npcManager) implements PlayerActionFactory, PlayerlessActionFactory {

    @Override
    public PlayerAction parsePlayer(final Instruction instruction) throws QuestException {
        return parse(instruction);
    }

    @Override
    public PlayerlessAction parsePlayerless(final Instruction instruction) throws QuestException {
        return parse(instruction);
    }

    private NullableActionAdapter parse(final Instruction instruction) throws QuestException {
        final Argument<NpcIdentifier> identifier = instruction.identifier(NpcIdentifier.class).get();
        return new NullableActionAdapter(profile
                -> npcManager.get(profile, identifier.getValue(profile)).despawn());
    }
}
