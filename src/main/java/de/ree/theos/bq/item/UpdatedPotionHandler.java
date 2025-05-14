package de.ree.theos.bq.item;

import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.item.typehandler.Existence;
import org.betonquest.betonquest.item.typehandler.HandlerUtil;
import org.betonquest.betonquest.item.typehandler.ItemMetaHandler;
import org.betonquest.betonquest.item.typehandler.Number;
import org.betonquest.betonquest.util.Utils;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Handles de-/serialization of Potions.
 */
public class UpdatedPotionHandler implements ItemMetaHandler<PotionMeta> {

    /**
     * The 'extended' string.
     */
    public static final String EXTENDED = "extended";

    /**
     * The 'upgraded' string.
     */
    public static final String UPGRADED = "upgraded";

    /**
     * Prefix indicating 'extended' potion types.
     */
    private static final String LONG_PREFIX = "long_";

    /**
     * Prefix indicating 'upgraded' potion types.
     */
    private static final String STRONG_PREFIX = "strong_";

    /**
     * The potion type, defaulting to water.
     */
    private PotionType type = PotionType.WATER;

    /**
     * The required potion type existence.
     */
    private Existence typeE = Existence.WHATEVER;

    /**
     * If the potion is extended.
     */
    private boolean extended;

    /**
     * The required extended existence.
     */
    private Existence extendedE = Existence.WHATEVER;

    /**
     * If the potion is upgraded.
     */
    private boolean upgraded;

    /**
     * The required upgraded existence.
     */
    private Existence upgradedE = Existence.WHATEVER;

    /**
     * The custom potion effects.
     */
    private List<CustomEffectHandler> custom = new ArrayList<>();

    /**
     * The required custom potion effects existence.
     */
    private Existence customE = Existence.WHATEVER;

    /**
     * If the Potions need to be exact the same or just contain all specified effects.
     */
    private boolean exact = true;

    /**
     * The empty default Constructor.
     */
    public UpdatedPotionHandler() {
    }

    @Nullable
    private static String addCustomEffects(final PotionMeta potionMeta, @Nullable final String effects) {
        final List<PotionEffect> customEffects = potionMeta.getCustomEffects();
        if (customEffects.isEmpty()) {
            return effects;
        }
        final StringBuilder string = new StringBuilder();
        for (final PotionEffect effect : customEffects) {
            final int power = effect.getAmplifier() + 1;
            final int duration = (effect.getDuration() - (effect.getDuration() % 20)) / 20;
            string.append(effect.getType().getKey().asMinimalString())
                    .append(':').append(power).append(':').append(duration).append(',');
        }
        return (effects == null ? "" : effects) + " effects:" + string.substring(0, string.length() - 1);
    }

    @Override
    public Class<PotionMeta> metaClass() {
        return PotionMeta.class;
    }

    @Override
    public Set<String> keys() {
        return Set.of("type", EXTENDED, UPGRADED, "effects", "effects-containing");
    }

    @Override
    @Nullable
    public String serializeToString(final PotionMeta potionMeta) {
        return addCustomEffects(potionMeta, getBasePotionEffects(potionMeta));
    }

    @Override
    public void set(final String key, final String data) throws QuestException {
        switch (key) {
            case "type" -> setType(data);
            case EXTENDED -> {
                if (EXTENDED.equals(data)) {
                    extendedE = Existence.REQUIRED;
                    this.extended = true;
                } else {
                    extendedE = Existence.REQUIRED;
                    this.extended = Boolean.parseBoolean(data);
                }
                typeSet(LONG_PREFIX);
            }
            case UPGRADED -> {
                if (UPGRADED.equals(data)) {
                    upgradedE = Existence.REQUIRED;
                    this.upgraded = true;
                } else {
                    upgradedE = Existence.REQUIRED;
                    this.upgraded = Boolean.parseBoolean(data);
                }
                typeSet(STRONG_PREFIX);
            }
            case "effects" -> setCustom(data);
            case "effects-containing" -> exact = false;
            default -> throw new QuestException("Unknown potion key: " + key);
        }
    }

    @Override
    public void populate(final PotionMeta potionMeta) {
        potionMeta.setBasePotionType(type);
        for (final PotionEffect effect : getCustom()) {
            potionMeta.addCustomEffect(effect, true);
        }
    }

    @Override
    public boolean check(final PotionMeta meta) {
        return checkBase(meta.getBasePotionType()) && checkCustom(meta.getCustomEffects());
    }

    private void typeSet(final String prefix) throws QuestException {
        final String potionType = prefix + type.getKey().asMinimalString();
        try {
            type = PotionType.valueOf(potionType.toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException e) {
            throw new QuestException("Invalid potion type: " + prefix + type.getKey().asMinimalString());
        }
    }

    @Nullable
    private String getBasePotionEffects(final PotionMeta potionMeta) {
        final Keyed type = potionMeta.getBasePotionType();
        if (type == null) {
            return null;
        }
        final String minimalString = type.getKey().asMinimalString();
        final String effects;
        if (minimalString.startsWith(LONG_PREFIX)) {
            effects = minimalString.substring(LONG_PREFIX.length()) + " extended";
        } else if (minimalString.startsWith(STRONG_PREFIX)) {
            effects = minimalString.substring(STRONG_PREFIX.length()) + " upgraded";
        } else {
            effects = minimalString;
        }
        return "type:" + effects;
    }

    private boolean checkBase(@Nullable final PotionType base) {
        switch (typeE) {
            case WHATEVER:
                return true;
            case REQUIRED:
                if (base == null || base != type) {
                    return false;
                }
                final String key = base.getKey().getKey();
                if (extendedE == Existence.REQUIRED && key.startsWith(LONG_PREFIX) == extended) {
                    return false;
                }
                return upgradedE != Existence.REQUIRED || key.startsWith(STRONG_PREFIX) == upgraded;
            default:
                return false;
        }
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private boolean checkCustom(final List<PotionEffect> custom) {
        if (customE == Existence.WHATEVER) {
            return true;
        }
        if (custom.isEmpty()) {
            return customE == Existence.FORBIDDEN;
        }
        if (exact && custom.size() != this.custom.size()) {
            return false;
        }
        for (final CustomEffectHandler checker : this.custom) {
            PotionEffect effect = null;
            for (final PotionEffect e : custom) {
                if (e.getType().equals(checker.customType)) {
                    effect = e;
                    break;
                }
            }
            if (!checker.check(effect)) {
                return false;
            }
        }
        return true;
    }

    private void setType(final String type) throws QuestException {
        typeE = Existence.REQUIRED;
        try {
            this.type = PotionType.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException e) {
            throw new QuestException("No such potion type: " + type, e);
        }
    }

    private List<PotionEffect> getCustom() {
        final List<PotionEffect> effects = new LinkedList<>();
        if (customE == Existence.FORBIDDEN) {
            return effects;
        }
        for (final CustomEffectHandler checker : custom) {
            if (checker.customTypeE != Existence.FORBIDDEN) {
                effects.add(new PotionEffect(checker.customType, checker.duration, checker.power));
            }
        }
        return effects;
    }

    private void setCustom(final String custom) throws QuestException {
        final String[] parts = HandlerUtil.getNNSplit(custom, "Potion is null!", ",");
        if (Existence.NONE_KEY.equalsIgnoreCase(parts[0])) {
            customE = Existence.FORBIDDEN;
            return;
        }
        this.custom = new ArrayList<>(parts.length);
        for (final String part : parts) {
            final CustomEffectHandler checker = new CustomEffectHandler(part);
            this.custom.add(checker);
        }
        customE = Existence.REQUIRED;
    }

    /**
     * Handles additional potion effects.
     */
    private static class CustomEffectHandler {
        /**
         * The expected argument count of the formatted effect.
         */
        private static final int INSTRUCTION_FORMAT_LENGTH = 3;

        /**
         * The custom potion type.
         */
        private final PotionEffectType customType;

        /**
         * The required existence.
         */
        private final Existence customTypeE;

        /**
         * The number compare state for the duration.
         */
        private final Number durationE;

        /**
         * The effect duration, in ticks.
         */
        private final int duration;

        /**
         * The effect amplifier, starting at 0.
         */
        private final int power;

        /**
         * The number compare state for the power.
         */
        private final Number powerE;

        /**
         * Create a new Custom Potion data from serialized string.
         *
         * @param custom the serialized potion data
         * @throws QuestException if the data is malformed
         */
        public CustomEffectHandler(final String custom) throws QuestException {
            final String[] parts = HandlerUtil.getNNSplit(custom, "Potion is null!", ":");
            if (parts[0].startsWith("none-")) {
                customTypeE = Existence.FORBIDDEN;
                customType = getType(parts[0].substring("none-".length()));
                powerE = Number.WHATEVER;
                power = 1;
                durationE = Number.WHATEVER;
                duration = 60 * 20;
                return;
            }
            customType = getType(parts[0]);
            customTypeE = Existence.REQUIRED;
            if (parts.length != INSTRUCTION_FORMAT_LENGTH) {
                throw new QuestException("Wrong effect format");
            }
            final Map.Entry<Number, Integer> effectPower = HandlerUtil.getNumberValue(parts[1], "effect power");
            powerE = effectPower.getKey();
            power = effectPower.getValue() - 1;
            if (power < 0) {
                throw new QuestException("Effect power must be a positive integer");
            }
            final Map.Entry<Number, Integer> effectDuration = HandlerUtil.getNumberValue(parts[2], "effect duration");
            durationE = effectDuration.getKey();
            duration = effectDuration.getValue() * 20;
        }

        private PotionEffectType getType(final String name) throws QuestException {
            final NamespacedKey potionKey = NamespacedKey.fromString(name.toLowerCase(Locale.ROOT));
            if (potionKey == null) {
                throw new QuestException("Invalid potion key: " + name);
            }
            return Utils.getNN(Registry.MOB_EFFECT.get(potionKey), "Unknown effect type: " + potionKey);
        }

        private boolean check(@Nullable final PotionEffect effect) {
            return switch (customTypeE) {
                case WHATEVER -> true;
                case REQUIRED -> effect != null && effect.getType().equals(customType)
                        && durationE.isValid(effect.getDuration(), duration)
                        && powerE.isValid(effect.getAmplifier(), power);
                case FORBIDDEN -> effect == null;
            };
        }
    }
}
