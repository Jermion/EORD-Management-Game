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

    public EventManager(Creature creature, Runnable updateStatusLabels, Consumer<String> addDialogue) {

        this.creature = creature;
        this.updateStatusLabels = updateStatusLabels;
        this.addDialogue = addDialogue;

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

            case NONE, CHAT, NO_ACTION -> {
                // No Effect
            }
        }
    }

    public void coolantUsed() {
        switch (currentEvent) {
            case HEAT -> {
                usedCoolant = true;
                checkHeatEvent();
            }

            case AGITATION, CHAT, NONE, NO_ACTION -> {
                // No Effect
            }
        }
    }

    private void startHeatEvent() {
        currentEvent = EventType.HEAT;

        usedSuppress = false;
        usedCoolant = false;

        creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 20);
        creature.getBiologicalSystem().changeSin(Sin.WRATH, 15);
        creature.getBiologicalSystem().changeSin(Sin.SLOTH, -10);
        updateStatusLabels.run();

        addDialogue.accept("It is too hot in here. Let me out.");

        eventTimer = new PauseTransition(Duration.seconds(10));
        eventTimer.setOnFinished(event -> {
            if (currentEvent == EventType.HEAT) {
                creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 20);
                creature.getBiologicalSystem().changeSin(Sin.WRATH, 30);
                creature.getBiologicalSystem().changeSin(Sin.SLOTH, -20);

                updateStatusLabels.run();
                addDialogue.accept("My flesh melts...");

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

            addDialogue.accept("This is better...");

            scheduleNextEvent();
        }
    }

    private void startAgitationEvent() {
        currentEvent = EventType.AGITATION;

        creature.getBiologicalSystem().changeSin(Sin.WRATH, 20);
        creature.getBiologicalSystem().changeSin(Sin.SLOTH, -15);

        updateStatusLabels.run();

        addDialogue.accept("My thoughts are moving too quickly.");

        eventTimer = new PauseTransition(Duration.seconds(10));

        eventTimer.setOnFinished(event -> {
            if (currentEvent == EventType.AGITATION) {
                creature.getBiologicalSystem().changeSin(Sin.WRATH, 20);
                creature.getBiologicalSystem().changeSin(Sin.SLOTH, -15);

                updateStatusLabels.run();
                addDialogue.accept("I cannot silence my own thoughts");

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
                addDialogue.accept("Do you think your superior just because your watching me from this screen?");

                creature.getBiologicalSystem().changeSin(Sin.WRATH, 10);
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
            int eventChoice = (int) (Math.random() * 4);
            switch (eventChoice) {
                case 0 -> startHeatEvent();
                case 1 -> startAgitationEvent();
                case 2 -> startChatEvent();
                case 3 -> startNoActionEvent();
            }
        });
        nextEventTimer.play();
    }

}
