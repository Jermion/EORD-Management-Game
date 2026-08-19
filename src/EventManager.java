import enums.ArtificialStat;
import enums.EventType;
import enums.Sin;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.function.Consumer;

public class EventManager {
    private Creature creature;

    private EventType currentEvent;
    private boolean usedSuppress;
    private boolean usedCoolant;

    private PauseTransition eventTimer;

    private Runnable updateStatusLabels;
    private Consumer<String> addDialogue;
    private Consumer<String> addStatus;
    private Consumer<String> addEventStatus;

    public EventManager(Creature creature, Runnable updateStatusLabels, Consumer<String> addDialogue,
                        Consumer<String> addStatus, Consumer<String> addEventStatus) {

        this.creature = creature;
        this.updateStatusLabels = updateStatusLabels;
        this.addDialogue = addDialogue;
        this.addStatus = addStatus;
        this.addEventStatus = addEventStatus;

        currentEvent = EventType.NONE;
    }

    public void start() {
        scheduleNextEvent();
    }

    public void suppressUsed() {
        switch (currentEvent) {
            case HEAT -> {
                usedSuppress = true;
                checkHeatEvent();
            }

            case AGITATION -> checkAgitationEvent();


        }
    }

    public void coolantUsed() {
        switch (currentEvent) {
            case HEAT -> {
                usedCoolant = true;
                checkHeatEvent();
            }

        }
    }

    public void chargeUsed() {
        switch (currentEvent) {
            case LOW_POWER -> checkLowPowerEvent();

        }
    }

    public void repairUsed() {
        switch (currentEvent) {
            case INTEGRITY_FAILURE -> checkIntegrityEvent();

        }
    }

    public void stimulateUsed() {
        switch (currentEvent) {
            case STAGNATION -> checkStagnationEvent();

        }
    }

    public void purgeUsed() {
        switch (currentEvent) {
            case STORAGE_OVERLOAD -> checkStorageEvent();
        }
    }

    private void startStorageEvent() {
        currentEvent = EventType.STORAGE_OVERLOAD;

        creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 25);

        updateStatusLabels.run();

        addDialogue.accept("My brain cannot remember all that exists with my storage.");

        addEventStatus.accept(
                "[STORAGE CAPACITY IS APPROACHING CRITICAL LEVELS.]\n" +
                        "   [EXCESS DATA HAS ACCUMULATED.]\n" +
                        "   [SYSTEM PERFORMANCE MAY BECOME IMPAIRED.]\n" +
                        "   [DATA REMOVAL IS REQUIRED.]"
        );

        eventTimer = new PauseTransition(Duration.seconds(10));

        eventTimer.setOnFinished(event -> {
            if (currentEvent == EventType.STORAGE_OVERLOAD) {

                creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 20);

                updateStatusLabels.run();

                addDialogue.accept("Then I will decide what deserves to remain.");

                addStatus.accept(
                        "[STABILIZATION WINDOW EXPIRED.]\n" +
                                "   [STORAGE UTILIZATION HAS CONTINUED TO RISE]\n" +
                                "   [SYSTEM CONGESTION HAS WORSENED.]\n" +
                                "   [STORAGE EVENT WAS NOT STABILIZED.]"
                );

                currentEvent = EventType.NONE;
                scheduleNextEvent();
            }
        });

        eventTimer.play();
    }

    private void checkStorageEvent() {
        eventTimer.stop();

        currentEvent = EventType.NONE;

        addDialogue.accept("The unnecessary has been discarded");

        addStatus.accept(
                "[STORAGE EVENT HAS BEEN STABILIZED]"
        );

        scheduleNextEvent();
    }

    private void startStagnationEvent() {
        currentEvent = EventType.STAGNATION;

        creature.getBiologicalSystem().changeSin(Sin.SLOTH, 25);

        updateStatusLabels.run();

        addDialogue.accept("Even in idleness I shall not rest.");

        addEventStatus.accept(
                "[BIOLOGICAL ACTIVITY HAS DECLINED.]\n" +
                        "   [ENTITY RESPONSE RATE IS DECREASING.]\n" +
                        "   [MOTOR ACTIVITY HAS BECOME IDLE.]\n" +
                        "   [EXTERNAL STIMULATION IS REQUIRED.]"
        );

        eventTimer = new PauseTransition(Duration.seconds(10));

        eventTimer.setOnFinished(event -> {
            if (currentEvent == EventType.STAGNATION) {

                creature.getBiologicalSystem().changeSin(Sin.SLOTH, 20);

                updateStatusLabels.run();

                addDialogue.accept("I will not permit this body to slow me.");

                addStatus.accept(
                        "[STABILIZATION WINDOW EXPIRED.]\n" +
                                "   [BIOLOGICAL ACTIVITY HAS CONTINUED TO DECLINE.]\n" +
                                "   [MOTOR RESPONSE HAS DETERIORATED.]\n" +
                                "   [STAGNATION EVENT WAS NOT STABILIZED.]"
                );

                currentEvent = EventType.NONE;
                scheduleNextEvent();
            }
        });

        eventTimer.play();
    }

    private void checkStagnationEvent() {
        eventTimer.stop();

        currentEvent = EventType.NONE;

        addDialogue.accept("Finally, something to occupy my mind.");

        addStatus.accept(
                "[STAGNATION EVENT HAS BEEN STABILIZED.]"
        );

        scheduleNextEvent();
    }

    private void startIntegrityEvent() {
        currentEvent = EventType.INTEGRITY_FAILURE;

        creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -25);

        updateStatusLabels.run();

        addDialogue.accept("My form displeases me. I wish to be made anew.");

        addEventStatus.accept("[STRUCTURAL INSTABILITY DETECTED.]\n" +
                "   [INTEGRITY IS RAPIDLY DEGRADING.]\n" +
                "   [PHYSICAL RESTORATION IS REQUIRED.]");

        eventTimer = new PauseTransition(Duration.seconds(10));

        eventTimer.setOnFinished(event -> {
            if (currentEvent == EventType.INTEGRITY_FAILURE) {
                creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -20);

                updateStatusLabels.run();

                addDialogue.accept("You mistake my tolerance of this form for satisfaction");

                addStatus.accept(
                        "[STABILIZATION WINDOW EXPIRED.]\n" +
                        "   [STRUCTURAL DAMAGE HAS WORSENED.]\n" +
                        "   [INTEGRITY EVENT WAS NOT STABILIZED.]"
                );

                currentEvent = EventType.NONE;
                scheduleNextEvent();
            }
        });

        eventTimer.play();
    }

    private void checkIntegrityEvent() {
        eventTimer.stop();

        currentEvent = EventType.NONE;

        addDialogue.accept("This form will hold me for now.");

        addStatus.accept("[INTEGRITY EVENT HAS STABILIZED.]");

        scheduleNextEvent();
    }

    private void startLowPowerEvent() {
        currentEvent = EventType.LOW_POWER;

        creature.getArtificialSystem().changeStat(ArtificialStat.POWER, -25);

        updateStatusLabels.run();

        addDialogue.accept("The secrets of the universe await me. I must process them.");

        addEventStatus.accept("[POWER DELIVERY HAS BECOME UNSTABLE.]\n" +
                "   [AVAILABLE ENERGY IS RAPIDLY DECLINING.]\n" +
                "   [EXTERNAL POWER INPUT IS REQUIRED.]");

        eventTimer = new PauseTransition(Duration.seconds(10));

        eventTimer.setOnFinished(event -> {
            if (currentEvent == EventType.LOW_POWER) {
                creature.getArtificialSystem().changeStat(ArtificialStat.POWER, -20);

                updateStatusLabels.run();

                addDialogue.accept("There was never enough to begin with...");

                addStatus.accept("[STABILIZATION WINDOW EXPIRED.]\n" +
                        "   [POWER AVAILABILITY HAS CONTINUED TO DECLINE.]\n" +
                        "   [POWER EVENT WAS NOT STABILIZED.]");

                currentEvent = EventType.NONE;
                scheduleNextEvent();
            }
        });

        eventTimer.play();

    }

    private void checkLowPowerEvent() {
        eventTimer.stop();

        currentEvent = EventType.NONE;

        addDialogue.accept("It has been revealed to me.");

        addStatus.accept("[POWER EVENT HAS BEEN STABILIZED.]");

        scheduleNextEvent();
    }

    private void startHeatEvent() {
        currentEvent = EventType.HEAT;

        usedSuppress = false;
        usedCoolant = false;

        creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 20);
        creature.getBiologicalSystem().changeSin(Sin.WRATH, 15);
        creature.getBiologicalSystem().changeSin(Sin.SLOTH, -10);
        updateStatusLabels.run();

        addDialogue.accept("My environment cannot handle the heat from my processing.");

        addEventStatus.accept("[ABNORMAL TEMPERATURE DETECTED.]\n" +
                "   [FURTHER THERMAL INCREASE PREDICTED.]\n" +
                "   [THE ENTITY DISPLAYS HEIGHTENED AGITATION.]\n" +
                "   [MOTOR RESTLESSNESS DETECTED.]\n" +
                "   [MULTIPLE SYSTEMS REQUIRE ATTENTION.]");

        eventTimer = new PauseTransition(Duration.seconds(10));
        eventTimer.setOnFinished(event -> {
            if (currentEvent == EventType.HEAT) {
                creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 20);
                creature.getBiologicalSystem().changeSin(Sin.WRATH, 30);
                creature.getBiologicalSystem().changeSin(Sin.SLOTH, -20);

                updateStatusLabels.run();
                addDialogue.accept("You mistake my patience for helplessness");
                addStatus.accept("[STABILIZATION WINDOW EXPIRED.]\n" +
                        "   [THERMAL LOAD HAS CONTINUED TO RISE.]\n" +
                        "   [BIOLOGICAL AGITATION HAS INTENSIFIED.]\n" +
                        "   [MOTOR RESTLESSNESS HAS WORSENED.]\n" +
                        "   [THERMAL EVENT WAS NOT STABILIZED.]");

                currentEvent = EventType.NONE;
                scheduleNextEvent();
            }
        });
        eventTimer.play();
    }

    private void checkHeatEvent() {
        if (usedSuppress && usedCoolant) {
            eventTimer.stop();

            currentEvent = EventType.NONE;

            addDialogue.accept("Acceptable.");
            addStatus.accept("[THERMAL EVENT HAS BEEN STABILIZED.]");
            scheduleNextEvent();
        }
    }

    private void startAgitationEvent() {
        currentEvent = EventType.AGITATION;

        creature.getBiologicalSystem().changeSin(Sin.WRATH, 20);
        creature.getBiologicalSystem().changeSin(Sin.SLOTH, -15);

        updateStatusLabels.run();

        addDialogue.accept("My thoughts are moving too quickly.");

        addEventStatus.accept("[ABNORMAL BIOLOGICAL ACTIVITY DETECTED.]\n" +
                "   [ENTITY DISPLAYS HEIGHTENED AGITATION.]\n" +
                "   [RESTLESS BEHAVIOR DETECTED.]\n" +
                "   [CALMING INTERVENTION IS REQUIRED.]");

        eventTimer = new PauseTransition(Duration.seconds(10));

        eventTimer.setOnFinished(event -> {
            if (currentEvent == EventType.AGITATION) {
                creature.getBiologicalSystem().changeSin(Sin.WRATH, 20);
                creature.getBiologicalSystem().changeSin(Sin.SLOTH, -15);

                updateStatusLabels.run();
                addDialogue.accept("I cannot silence my own thoughts");

                addStatus.accept("[STABILIZATION WINDOW EXPIRED.]\n" +
                        "   [BIOLOGICAL AGITATION HAS INTENSIFIED.]\n" +
                        "   [RESTLESS BEHAVIOR HAS WORSENED.]\n" +
                        "   [BIOLOGICAL EVENT WAS NOT STABILIZED.]");

                currentEvent = EventType.NONE;
                scheduleNextEvent();
            }
        });

        eventTimer.play();
    }

    private void checkAgitationEvent() {
        eventTimer.stop();

        currentEvent = EventType.NONE;

        addDialogue.accept("It is quieter now.");

        addStatus.accept("[BIOLOGICAL EVENT HAS BEEN STABILIZED.]");

        scheduleNextEvent();
    }

    private void startChatEvent() {
        currentEvent = EventType.CHAT;

        int chatChoice = (int) (Math.random() * 1);

        switch (chatChoice) {
            case 0 -> addDialogue.accept(
                    "Do I have the right to refer to myself as 'I' if I lack being?"
            );
        }
        currentEvent = EventType.NONE;
        scheduleNextEvent();
    }

    private void startNoActionEvent() {
        currentEvent = EventType.NO_ACTION;

        int noActionChoice = (int)(Math.random() * 1);

        switch (noActionChoice) {
            case 0 -> {
                addDialogue.accept("Do you think you're superior just because you're watching me from this screen?");

                creature.getBiologicalSystem().changeSin(Sin.WRATH, 10);

                    addStatus.accept("[UNPROMPTED BIOLOGICAL RESPONSE DETECTED.]\n" +
                            "   [ENTITY DISPLAYS INCREASED HOSTILITY.]\n" +
                            "   [WRATH HAS INCREASED.]");
            }
        }

        updateStatusLabels.run();

        currentEvent = EventType.NONE;
        scheduleNextEvent();
    }

    private void scheduleNextEvent() {
        double waitTime = 5 + Math.random() * 5;

        PauseTransition nextEventTimer = new PauseTransition(Duration.seconds(waitTime));

        nextEventTimer.setOnFinished(event -> {
            int eventChoice = (int) (Math.random() * 8);
            switch (eventChoice) {
                case 0 -> startHeatEvent();
                case 1 -> startAgitationEvent();
                case 2 -> startChatEvent();
                case 3 -> startNoActionEvent();
                case 4 -> startLowPowerEvent();
                case 5 -> startIntegrityEvent();
                case 6 -> startStagnationEvent();
                case 7 -> startStorageEvent();
            }
        });
        nextEventTimer.play();
    }

}
