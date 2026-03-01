package de.ree.theos.bq.playtime;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.DefaultObjective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.common.function.QuestFunction;
import org.betonquest.betonquest.api.identifier.ObjectiveIdentifier;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.objective.ObjectiveData;
import org.betonquest.betonquest.api.quest.objective.service.ObjectiveProperties;
import org.betonquest.betonquest.api.quest.objective.service.ObjectiveService;
import org.betonquest.betonquest.config.PluginMessage;
import org.betonquest.betonquest.quest.action.folder.TimeUnit;
import org.bukkit.Statistic;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

/**
 * Player has to play specified amount of time.
 */
public class PlaytimeObjective extends DefaultObjective {

    /**
     * The required play time.
     */
    private final Argument<Number> timePlayed;

    /**
     * Starting mode to eventually offset from the already present value at objective start.
     */
    private final Argument<CountingMode> mode;

    /**
     * The time unit used for starting the objective.
     */
    private final Argument<TimeUnit> timeUnit;

    /**
     * The runnable task that checks the progress.
     */
    private final BukkitTask runnable;

    /**
     * Constructor for the DelayObjective.
     *
     * @param service    the {@link ObjectiveService} for this objective
     * @param mode       the starting mode to eventually offset from the already present value at start
     * @param timeUnit   the unit of time the player has to play
     * @param interval   the interval in ticks at which the objective checks if the time played
     * @param timePlayed the time in
     * @throws QuestException if there is an error in the instruction
     */
    public PlaytimeObjective(final ObjectiveService service, final Argument<Number> timePlayed, final Argument<CountingMode> mode,
            final Argument<TimeUnit> timeUnit,
            final Argument<Number> interval) throws QuestException {
        super(service);
        this.timePlayed = timePlayed;
        this.mode = mode;
        this.timeUnit = timeUnit;
        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                final List<Profile> players = new LinkedList<>();
                final ObjectiveService service = getService();
                for (final Entry<Profile, String> entry : service.getData().entrySet()) {
                    final Profile profile = entry.getKey();
                    final PlaytimeData playerData = new PlaytimeData(entry.getValue(), profile, getObjectiveID());
                    profile.getOnlineProfile().ifPresent(onlineProfile -> {
                        if (onlineProfile.getPlayer().getStatistic(Statistic.TOTAL_WORLD_TIME) >= playerData.getPlaytime()) {
                            service.getExceptionHandler().handle(() -> {
                                if (service.checkConditions(profile)) {
                                    players.add(profile);
                                }
                            });
                        }
                    });
                }
                for (final Profile profile : players) {
                    service.complete(profile);
                }
            }
        }.runTaskTimer(BetonQuest.getInstance(), 1, interval.getValue(null).longValue());
        service.setDefaultData(this::getDefaultDataInstruction);
        service.getProperties().setParentProperties(new ObjectiveProperties() {
            @Override
            public String getProperty(final String name, final Profile profile) throws QuestException {
                return PlaytimeObjective.this.getProperty(name, profile);
            }

            @Override
            public void setProperty(final String name, final QuestFunction<Profile, String> property) {
                throw new UnsupportedOperationException("Cannot set property for PlaytimeObjective");
            }

            @Override
            public void setParentProperties(final ObjectiveProperties properties) {
                throw new UnsupportedOperationException("Cannot set parent properties for PlaytimeObjective");
            }
        });
    }

    @Override
    public void close() {
        runnable.cancel();
        super.close();
    }

    private String getDefaultDataInstruction(final Profile profile) throws QuestException {
        final long targetValue = timeUnit.getValue(profile).getTicks(timePlayed.getValue(profile).longValue());
        return switch (mode.getValue(profile)) {
            case TOTAL -> String.valueOf(targetValue);
            case RELATIVE -> String.valueOf(targetValue + profile.getPlayer().getStatistic(Statistic.TOTAL_WORLD_TIME));
        };
    }

    private String getProperty(final String name, final Profile profile) throws QuestException {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "left" -> LegacyComponentSerializer.legacySection().serialize(parseVariableLeft(profile));
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
     */
    private PlaytimeData getPlaytimeData(final Profile profile) {
        final String data = getService().getData().get(profile);
        return new PlaytimeData(data, profile, getObjectiveID());
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
        public PlaytimeData(final String instruction, final Profile profile, final ObjectiveIdentifier objID) {
            super(instruction, profile, objID);
            timestamp = Long.parseLong(instruction);
        }

        private long getPlaytime() {
            return timestamp;
        }
    }
}
