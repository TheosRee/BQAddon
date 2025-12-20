package de.ree.theos.bq.playtime;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.instruction.variable.Variable;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.objective.ObjectiveData;
import org.betonquest.betonquest.api.quest.objective.ObjectiveDataFactory;
import org.betonquest.betonquest.api.quest.objective.ObjectiveID;
import org.betonquest.betonquest.config.PluginMessage;
import org.betonquest.betonquest.quest.event.folder.TimeUnit;
import org.bukkit.Statistic;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Player has to play specified amount of time.
 */
public class PlaytimeObjective extends Objective {
    /**
     * The Factory for the Playtime Data.
     */
    private static final ObjectiveDataFactory PLAYTIME_FACTORY = PlaytimeData::new;

    /**
     * The required play time.
     */
    private final Variable<Number> timePlayed;

    /**
     * Starting mode to eventually offset from the already present value at objective start.
     */
    private final Variable<CountingMode> mode;

    /**
     * The time unit used for starting the objective.
     */
    private final Variable<TimeUnit> timeUnit;

    /**
     * The runnable task that checks the progress.
     */
    private final BukkitTask runnable;

    private final BetonQuestLogger logger;

    /**
     * Constructor for the DelayObjective.
     *
     * @param instruction the instruction that created this objective
     * @param mode        the starting mode to eventually offset from the already present value at start
     * @param timeUnit    the unit of time the player has to play
     * @param interval    the interval in ticks at which the objective checks if the time played
     * @param timePlayed  the time in
     * @throws QuestException if there is an error in the instruction
     */
    public PlaytimeObjective(final Instruction instruction, final Variable<Number> timePlayed, final Variable<CountingMode> mode,
            final Variable<TimeUnit> timeUnit,
            final Variable<Number> interval) throws QuestException {
        super(instruction, PLAYTIME_FACTORY);
        this.timePlayed = timePlayed;
        this.mode = mode;
        this.timeUnit = timeUnit;
        this.logger = BetonQuest.getInstance().getLoggerFactory().create(getClass());
        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                logger.debug(instruction.getPackage(), instruction.getID() + " Running core loop..");
                final List<Profile> players = new LinkedList<>();
                for (final Entry<Profile, ObjectiveData> entry : dataMap.entrySet()) {
                    final Profile profile = entry.getKey();
                    logger.debug(instruction.getPackage(), "  Checking profile " + profile);
                    final PlaytimeData playerData = (PlaytimeData) entry.getValue();
                    profile.getOnlineProfile().ifPresent(onlineProfile -> {
                        logger.debug(instruction.getPackage(), "    Profile is online; Playtime: "
                                + onlineProfile.getPlayer().getStatistic(Statistic.TOTAL_WORLD_TIME)
                                + "; Saved Time: " + playerData.getPlaytime());
                        logger.debug(instruction.getPackage(), "    Conditions are met: " + checkConditions(profile));
                        if (onlineProfile.getPlayer().getStatistic(Statistic.TOTAL_WORLD_TIME) >= playerData.getPlaytime()
                                && checkConditions(profile)) {
                            logger.debug(instruction.getPackage(), "    Time and conditions are met");
                            players.add(profile);
                        }
                    });
                }
                for (final Profile profile : players) {
                    logger.debug(instruction.getPackage(), "  Completing for profile " + profile);
                    completeObjective(profile);
                    logger.debug(instruction.getPackage(), "  Completed");
                }
                logger.debug(instruction.getPackage(), instruction.getID() + " Ending core loop..");
            }
        }.runTaskTimer(BetonQuest.getInstance(), 1, interval.getValue(null).longValue());
    }

    @Override
    public void close() {
        runnable.cancel();
        super.close();
    }

    @Override
    public String getDefaultDataInstruction(final Profile profile) {
        return qeHandler.handle(() -> {
            final long targetValue = timeUnit.getValue(profile).getTicks(timePlayed.getValue(profile).longValue());
            return switch (mode.getValue(profile)) {
                case TOTAL -> String.valueOf(targetValue);
                case RELATIVE -> String.valueOf(targetValue + profile.getPlayer().getStatistic(Statistic.TOTAL_WORLD_TIME));
            };
        }, "");
    }

    @Override
    public String getProperty(final String name, final Profile profile) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "left" -> qeHandler.handle(() -> LegacyComponentSerializer.legacySection().serialize(parseVariableLeft(profile)), "");
            case "rawseconds" -> String.valueOf(secondsLeft(profile));
            default -> "";
        };
    }

    private long secondsLeft(final Profile profile) {
        final long wantedTickPlaytime = getPlaytimeData(profile).getPlaytime();
        final int actualTickPlaytime = profile.getPlayer().getStatistic(Statistic.TOTAL_WORLD_TIME);
        return (wantedTickPlaytime - actualTickPlaytime) / 20;
    }

    private Component parseVariableLeft(final Profile profile) throws QuestException {
        final PluginMessage pluginMessage = BetonQuest.getInstance().getPluginMessage();
        final Component daysWord = pluginMessage.getMessage(profile, "days");
        final Component daysWordSingular = pluginMessage.getMessage(profile, "days_singular");
        final Component hoursWord = pluginMessage.getMessage(profile, "hours");
        final Component hoursWordSingular = pluginMessage.getMessage(profile, "hours_singular");
        final Component minutesWord = pluginMessage.getMessage(profile, "minutes");
        final Component minutesWordSingular = pluginMessage.getMessage(profile, "minutes_singular");
        final Component secondsWord = pluginMessage.getMessage(profile, "seconds");
        final Component secondsWordSingular = pluginMessage.getMessage(profile, "seconds_singular");

        final Duration duration = Duration.ofSeconds(secondsLeft(profile));

        final TextComponent.Builder builder = Component.text();
        buildTimeDescription(builder, daysWord, daysWordSingular, duration.toDaysPart());
        buildTimeDescription(builder, hoursWord, hoursWordSingular, duration.toHoursPart());
        buildTimeDescription(builder, minutesWord, minutesWordSingular, duration.toMinutesPart());
        buildTimeDescription(builder, secondsWord, secondsWordSingular, duration.toSecondsPart());

        return builder.build();
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private void buildTimeDescription(final TextComponent.Builder builder, final Component timeUnitWord,
            final Component timeUnitSingularWord, final long timeAmount) {
        if (!builder.children().isEmpty()) {
            builder.append(Component.space());
        }
        if (timeAmount > 1) {
            builder.append(Component.text(timeAmount)).append(Component.space()).append(timeUnitWord);
        } else if (timeAmount == 1) {
            builder.append(Component.text(timeAmount)).append(Component.space()).append(timeUnitSingularWord);
        }
    }

    /**
     * Get the delay data for a profile.
     *
     * @throws NullPointerException when {@link #containsPlayer(Profile)} is false
     */
    private PlaytimeData getPlaytimeData(final Profile profile) {
        return Objects.requireNonNull((PlaytimeData) dataMap.get(profile));
    }

    /**
     * Data class for the PlaytimeObjective.
     */
    public static class PlaytimeData extends ObjectiveData {
        /**
         * The required playtime.
         */
        private final long timestamp;

        /**
         * Constructor for the PlaytimeData.
         *
         * @param instruction the data of the objective
         * @param profile     the profile associated with this objective
         * @param objID       the ID of the objective
         */
        public PlaytimeData(final String instruction, final Profile profile, final ObjectiveID objID) {
            super(instruction, profile, objID);
            timestamp = Long.parseLong(instruction);
        }

        private long getPlaytime() {
            return timestamp;
        }
    }
}
