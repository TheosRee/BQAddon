package de.ree.theos.bq.npc;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.PlayerFilter;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.npc.Npc;
import org.betonquest.betonquest.api.quest.npc.NpcWrapper;
import org.betonquest.betonquest.compatibility.npc.citizens.CitizensAdapter;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCitizensWrapper implements NpcWrapper<NPC> {

    /**
     * Already generated npcs for uuid and original id.
     */
    private final Table<UUID, Integer, WeakReference<NPC>> npcs;

    /**
     * Source Registry of NPCs to use.
     */
    private final NPCRegistry registry;

    /**
     * Id of the Npc.
     */
    private final Argument<Number> npcId;

    /**
     * Create a new Citizens Npc Wrapper.
     *
     * @param registry the registry of NPCs to use
     * @param npcId    the id of the Npc, greater or equals to zero
     */
    public PlayerCitizensWrapper(final NPCRegistry registry, final Argument<Number> npcId) {
        this.registry = registry;
        this.npcId = npcId;
        this.npcs = Tables.newCustomTable(new ConcurrentHashMap<>(), ConcurrentHashMap::new);
    }

    @Override
    public Npc<NPC> getNpc(@Nullable final Profile profile) throws QuestException {
        if (profile == null) {
            throw new QuestException("Player citizens must have a to get npcs for.");
        }
        final int npcId = this.npcId.getValue(profile).intValue();
        final NPC npc = registry.getById(npcId);
        if (npc == null) {
            throw new QuestException("Original Citizens NPC with ID '%d' not found".formatted(npcId));
        }
        final UUID uuid = profile.getPlayerUUID();
        final WeakReference<NPC> npcWeakReference = npcs.get(uuid, npcId);
        NPC relevant;

        if (npcWeakReference == null || (relevant = npcWeakReference.get()) == null) {
            relevant = createNPC(npc, uuid);
            npcs.put(uuid, npcId, new WeakReference<>(relevant));
        }
        return new CitizensAdapter(relevant);
    }

    private NPC createNPC(final NPC original, final UUID uuid) {
        final NPC copy = registry.createNPC(original.getOrAddTrait(MobType.class).getType(), original.getRawName());
        registry.deregister(copy);
        copy.spawn(original.getStoredLocation());
        for (final Trait trait : original.getTraits()) {
            copy.addTrait(trait);
        }
        copy.removeTrait(PlayerFilter.class);
        final PlayerFilter filter = copy.getOrAddTrait(PlayerFilter.class);
        filter.setAllowlist();
        filter.addPlayer(uuid);
        return copy;
    }

    @Override
    public Set<Npc<NPC>> getNpcs(@Nullable final Profile profile) throws QuestException {
        return Set.of(getNpc(profile));
    }
}
