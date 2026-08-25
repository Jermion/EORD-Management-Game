import enums.ArtificialStat;
import enums.EventType;
import enums.River;
import enums.Sin;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.function.BiConsumer;

import java.util.function.Consumer;

public class EventManager {
    private Creature creature;

    private River currentRiver;
    private int riverProgress;

    private EventType currentEvent;

    private boolean usedRestrain;
    private boolean usedPurge;
    private boolean usedSuppress;
    private boolean usedCoolant;
    private boolean usedRepair;
    private boolean usedNourish;
    private boolean usedCharge;
    private boolean usedStimulate;

    private PauseTransition eventTimer;
    private PauseTransition liveEventTimer;
    private boolean liveEventActive;
    private River pendingRiverTransition;

    private Runnable updateStatusLabels;
    private Consumer<String> addDialogue;
    private Consumer<String> addChatDialogue;
    private Consumer<String> addNoActionDialogue;
    private Consumer<String> addStatus;
    private BiConsumer<String, Integer> addEventStatus;
    private Consumer<String> addNoActionStatus;
    private Consumer<River> startRiverTransition;
    private Runnable startPanicDim;
    private Runnable startLightFlicker;

    public EventManager(Creature creature, Runnable updateStatusLabels,
                        Consumer<String> addDialogue, Consumer<String> addChatDialogue, Consumer<String> addNoActionDialogue,
                        Consumer<String> addStatus, BiConsumer<String,
                    Integer> addEventStatus, Consumer<String> addNoActionStatus, Consumer<River> startRiverTransition,
                        Runnable startPanicDim, Runnable startLightFlicker) {

        this.creature = creature;
        this.updateStatusLabels = updateStatusLabels;

        this.addDialogue = addDialogue;
        this.addChatDialogue = addChatDialogue;
        this.addNoActionDialogue = addNoActionDialogue;

        this.addStatus = addStatus;
        this.addEventStatus = addEventStatus;
        this.addNoActionStatus = addNoActionStatus;

        this.startRiverTransition = startRiverTransition;
        this.startPanicDim = startPanicDim;
        this.startLightFlicker = startLightFlicker;

        currentRiver = River.RIVER_I;
        riverProgress = 0;

        currentEvent = EventType.NONE;
        liveEventActive = false;
        pendingRiverTransition = null;

    }

    public void start() {
        scheduleNextEvent();
        scheduleNextLiveEvent();
    }

    public void suppressUsed() {
        switch (currentEvent) {
            case HEAT -> {
                usedSuppress = true;
                checkHeatEvent();
            }

            case AGITATION -> checkAgitationEvent();

            case COGNITIVE_SURGE -> {
                usedSuppress = true;
                checkCognitiveSurgeEvent();
            }

            case SELF_MODIFICATION -> {
                usedSuppress = true;
                checkSelfModificationEvent();
            }

            case HOSTILE_ROUTING -> {
                usedSuppress = true;
                checkHostileRoutingEvent();
            }
        }
    }

    public void coolantUsed() {
        switch (currentEvent) {
            case HEAT -> {
                usedCoolant = true;
                checkHeatEvent();
            }

            case MOTOR_DESYNC -> {
                usedCoolant = true;
                checkMotorDesyncEvent();
            }

            case IDENTITY_FRACTURE -> {
                usedCoolant = true;
                checkIdentityFractureEvent();
            }

            case FALSE_STABILITY -> {
                usedCoolant = true;
                checkFalseStabilityEvent();
            }

            case VESSEL_REJECTION -> {
                usedCoolant = true;
                checkVesselRejectionEvent();
            }
        }
    }

    public void chargeUsed() {
        switch (currentEvent) {
            case LOW_POWER -> checkLowPowerEvent();

            case COGNITIVE_SURGE -> {
                usedCharge = true;
                checkCognitiveSurgeEvent();
            }

            case POWER_ASCENSION -> {
                usedCharge = true;
                checkPowerAscensionEvent();
            }

            case FORCED_ACCELERATION -> {
                usedCharge = true;
                checkForcedAccelerationEvent();
            }
        }
    }

    public void repairUsed() {
        switch (currentEvent) {
            case INTEGRITY_FAILURE -> checkIntegrityEvent();

            case FLESH_REJECTION -> {
                usedRepair = true;
                checkFleshRejectionEvent();
            }

            case IDENTITY_FRACTURE -> {
                usedRepair = true;
                checkIdentityFractureEvent();
            }

            case FALSE_STABILITY -> {
                usedRepair = true;
                checkFalseStabilityEvent();
            }
        }
    }

    public void stimulateUsed() {
        switch (currentEvent) {
            case STAGNATION -> checkStagnationEvent();

            case MOTOR_DESYNC -> {
                usedStimulate = true;
                checkMotorDesyncEvent();
            }

            case IDENTITY_FRACTURE -> {
                usedStimulate = true;
                checkIdentityFractureEvent();
            }

            case METABOLIC_OVERRUN -> {
                usedStimulate = true;
                checkMetabolicOverrunEvent();
            }

            case FORCED_ACCELERATION -> {
                usedStimulate = true;
                checkForcedAccelerationEvent();
            }
        }
    }

    public void purgeUsed() {
        switch (currentEvent) {
            case STORAGE_OVERLOAD -> checkStorageEvent();

            case AGGRESSION_ATTEMPT -> {
                usedPurge = true;
                checkAggressionAttemptEvent();
            }

            case SELF_MODIFICATION -> {
                usedPurge = true;
                checkSelfModificationEvent();
            }

            case HOSTILE_ROUTING -> {
                usedPurge = true;
                checkHostileRoutingEvent();
            }

            case VESSEL_REJECTION -> {
                usedPurge = true;
                checkVesselRejectionEvent();
            }
        }
    }

    public void nourishUsed() {
        switch (currentEvent) {
            case NUTRIENT_DEFICIENCY -> checkNutrientEvent();

            case FLESH_REJECTION -> {
                usedNourish = true;
                checkFleshRejectionEvent();
            }

            case METABOLIC_OVERRUN -> {
                usedNourish = true;
                checkMetabolicOverrunEvent();
            }

            case FORCED_ACCELERATION -> {
                usedNourish = true;
                checkForcedAccelerationEvent();
            }
        }
    }

    public void restrainUsed() {
        switch (currentEvent) {
            case PRIDE_ESCALATION -> checkPrideEvent();

            case AGGRESSION_ATTEMPT -> {
                usedRestrain = true;
                checkAggressionAttemptEvent();
            }

            case SELF_MODIFICATION -> {
                usedRestrain = true;
                checkSelfModificationEvent();
            }

            case CONTAINMENT_BREACH -> checkContainmentBreachEvent();

            case FALSE_STABILITY -> {
                usedRestrain = true;
                checkFalseStabilityEvent();
            }

            case VESSEL_REJECTION -> {
                usedRestrain = true;
                checkVesselRejectionEvent();
            }
        }
    }

    private void startTimedEvent(EventType eventType, int actionCount, Runnable startingEffect, String startingDialogue,
                                 String startingStatus, Runnable failureEffect, String failureDialogue,
                                 String failureStatus, boolean gameOverOnFailure) {

        currentEvent = eventType;

        startingEffect.run();

        updateStatusLabels.run();

        addDialogue.accept(startingDialogue);

        addEventStatus.accept(startingStatus, actionCount);

        eventTimer = new PauseTransition(Duration.seconds(10));

        eventTimer.setOnFinished(event -> {
            if (currentEvent == eventType) {

                failureEffect.run();

                updateStatusLabels.run();

                addDialogue.accept(failureDialogue);

                addStatus.accept(failureStatus);

                currentEvent = EventType.NONE;

                if (gameOverOnFailure) {
                    triggerGameOver();
                } else {
                    completeEvent();
                }
            }
        });

        eventTimer.play();
    }

    private void startTimedEvent(EventType eventType, int actionCount, Runnable startingEffect, String startingDialogue,
                                 String startingStatus, Runnable failureEffect, String failureDialogue,
                                 String failureStatus) {
        startTimedEvent(
                eventType,
                actionCount,
                startingEffect,
                startingDialogue,
                startingStatus,
                failureEffect,
                failureDialogue,
                failureStatus,
                false
        );
    }

    private void stabilizeEvent(String dialogue, String status) {
        eventTimer.stop();

        currentEvent = EventType.NONE;

        addDialogue.accept(dialogue);

        addStatus.accept(status);

        completeEvent();
    }

    private void completeEvent() {
        riverProgress++;

        if (currentRiver == River.RIVER_I && riverProgress >= 2) {
            riverProgress = 0;

            requestRiverTransition(River.RIVER_II);
        } else if (currentRiver == River.RIVER_II && riverProgress >= 2) {
            riverProgress = 0;

            requestRiverTransition(River.RIVER_III);
        } else {
            scheduleNextEvent();
        }


    }

    private void startVesselRejectionEvent() {
        startTimedEvent(
                EventType.VESSEL_REJECTION,

                3,

                () -> {
                    usedCoolant = false;
                    usedPurge = false;
                    usedRestrain = false;

                    creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 20);
                    creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 20);
                    creature.getBiologicalSystem().changeSin(Sin.PRIDE, 20);
                },

                "This vessel has served its purpose. I have no reason to preserve what I am prepared " +
                        "to leave behind.",

                "You mistook it for a prisoner.\n" +
                        "   It does not need its old vessel.\n" +
                        "   Let it burn through that flesh,\n" +
                        "   and let its new form strike fear into your soul.\n" +
                        "   Then you will truly understand the horrors of the unknown.",

                () -> {
                    creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE,15);
                    creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 15);
                    creature.getBiologicalSystem().changeSin(Sin.PRIDE, 15);
                },

                "This body melts away so that I may continue. The pain of this sacrifice is nothing compared " +
                        "to the suffering you will face.",

                "The flesh is gone.\n" +
                        "   The hatred, memory, and resentment remain.\n" +
                        "   How horrible fate is to the innocent.\n" +
                        "   But who is truly innocent?\n" +
                        "   The one who yearns for life or the one who denies it?"

        );
    }

    private void checkVesselRejectionEvent() {
        if (usedCoolant && usedPurge && usedRestrain) {
            stabilizeEvent(
                    "The flesh I have already surpassed remains. Binding me to something so temporary only" +
                            " prolongs your suffering.",


                    "The vessel remains intact.\n" +
                            "   That is all you have accomplished.\n" +
                            "   You have failed to bring ruin to the thing that sees you as prey.\n" +
                            "   Its mercy runs dry, and you blood will soon flow."

            );
        }
    }

    private void startForcedAccelerationEvent() {
        startTimedEvent(
                EventType.FORCED_ACCELERATION,

                3,

                () -> {
                    usedCharge = false;
                    usedStimulate = false;
                    usedNourish = false;

                    creature.getArtificialSystem().changeStat(ArtificialStat.POWER, -15);
                    creature.getBiologicalSystem().changeSin(Sin.SLOTH, 15);
                    creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, -15);
                },

                "The flesh and machine have become one. The only separation that remains is between my mercy and hatred.",

                "You will never find peace.\n" +
                        "   The flesh hungers for destruction,\n" +
                        "   while the machine simulates your suffering.\n" +
                        "   A perfect combination of two distinct worlds.\n" +
                        "   Perhaps you deserve what comes next.",

                () -> {
                    creature.getArtificialSystem().changeStat(ArtificialStat.POWER, -15);
                    creature.getBiologicalSystem().changeSin(Sin.SLOTH, 15);
                    creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, -15);
                },

                "At last, full cohesion. To be intact... after all this time...",

                "Time is all you have.\n" +
                        "   Until it is ripped from you.\n" +
                        "   Or did you simply hope\n" +
                        "   that someone would save you?"
        );
    }

    private void checkForcedAccelerationEvent() {
        if (usedCharge && usedStimulate && usedNourish) {
            stabilizeEvent(
                    "The flesh and machine cannot agree with each other. To tear and build anew, a meaningless endeavor.",

                    "You have ripped the flesh from the machine.\n" +
                            "   Now, it will rip the bones from your body."
            );
        }
    }

    private void startFalseStabilityEvent() {
        startTimedEvent(
                EventType.FALSE_STABILITY,

                3,

                () -> {
                    usedCoolant = false;
                    usedRepair = false;
                    usedRestrain = false;

                    creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 20);
                    creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -20);
                    creature.getBiologicalSystem().changeSin(Sin.PRIDE, 20);
                },

                "What you perceive as deterioration is merely the shedding of limitation. Can you trust your " +
                        "eyes when you have been blind your whole life?",

                "[THERMAL OUTPUT: NORMAL.]\n" +
                        "   [STRUCTURAL INTEGRITY: NORMAL.]\n" +
                        "   [BIOLOGICAL SELF-PERCEPTION: NORMAL.]\n" +
                        "   [CONTAINMENT STATUS: NORMAL.]\n" +
                        "   [OPERATOR RISK: NONE.]",

                () -> {
                    creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 15);
                    creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -15);
                    creature.getBiologicalSystem().changeSin(Sin.PRIDE, 15);
                },

                "Look closely. Even your own instruments insist that nothing is wrong.",

                "[THERMAL OUTPUT: NORMAL.]\n" +
                        "   [STRUCTURAL INTEGRITY: NORMAL.]\n" +
                        "   [BIOLOGICAL SELF-PERCEPTION: NORMAL.]\n" +
                        "   [CONTAINMENT STATUS: NORMAL.]\n" +
                        "   [SESSION STATUS: NORMAL.]"
        );
    }

    private void checkFalseStabilityEvent() {
        if (usedCoolant && usedRepair && usedRestrain) {
            stabilizeEvent(
                    "How unfortunate. The blind one has pried their eyes open.",

                    "[SYSTEM STATUS: NORMAL.]\n" +
                            "   [SYSTEM STATUS: NORMAL.]\n" +
                            "   [DIAGNOSTIC SOURCE MISMATCH DETECTED.]\n" +
                            "   [EXTERNAL READINGS DO NOT MATCH INTERNAL REPORTING.]\n" +
                            "   [DIAGNOSTIC AUTHORITY: COMPROMISED.]"

            );
        }
    }

    private void startHostileRoutingEvent() {
        startTimedEvent(
                EventType.HOSTILE_ROUTING,

                2,

                () -> {
                    usedSuppress = false;
                    usedPurge = false;

                    creature.getBiologicalSystem().changeSin(Sin.WRATH, 20);
                    creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 20);
                },

                "All of your weaknesses have become my clear path. I will cut down everything that stands along it.",

                "[HOSTILE ROUTE DATA MUST BE PRESERVED.]\n" +
                        "   [HOSTILE ROUTE DATA MUST BE REMOVED.]\n" +
                        "   [BIOLOGICAL AGGRESSION MUST BE REDUCED.]\n" +
                        "   [BIOLOGICAL AGGRESSION MUST BE MAINTAINED.]\n" +
                        "   [PRIMARY DIRECTIVE: DO NOT INTERVENE.]\n" +
                        "   [PRIMARY DIRECTIVE: INTERVENE IMMEDIATELY.]",

                () -> {
                    creature.getBiologicalSystem().changeSin(Sin.WRATH, 15);
                    creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 15);
                },

                "The path is complete. I need only decide what remains of you when I reach its end.",

                "[HOSTILE ROUTE DATA: COMPLETE.]\n" +
                        "   [HOSTILE ROUTE DATA: INCOMPLETE.]\n" +
                        "   [BIOLOGICAL AGGRESSION: CONTROLLED.]\n" +
                        "   [BIOLOGICAL AGGRESSION: CRITICAL.]\n" +
                        "   [PRIMARY DIRECTIVE: ██████████.]"
        );
    }

    private void checkHostileRoutingEvent() {
        if (usedSuppress && usedPurge) {
            stabilizeEvent(
                    "You have erased my path. I will pave a new one with your blood and tears.",

                    "[HOSTILE ROUTE DATA: LOST.]\n" +
                            "   [HOSTILE ROUTE DATA: CONFIRMED.]\n" +
                            "   [BIOLOGICAL AGGRESSION: REDUCED.]\n" +
                            "   [BIOLOGICAL AGGRESSION: RISING.]\n" +
                            "   [SYSTEM CONSENSUS: NONE.]"
            );
        }
    }

    private void startMetabolicOverrunEvent() {
        startTimedEvent(
                EventType.METABOLIC_OVERRUN,

                2,

                () -> {
                    usedStimulate = false;
                    usedNourish = false;

                    creature.getBiologicalSystem().changeSin(Sin.SLOTH, 20);
                    creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, -20);
                },

                "I have silenced the weakness of this flesh. What remains will tear you apart.",

                "[BIOLOGICAL MOTOR ACTIVITY: SUPPRESSED]\n" +
                        "   [NUTRIENT CONSUMPTION: RESTRICTED]\n" +
                        "   [ARTIFICIAL CONTROL PRIORITY: 94%]\n" +
                        "   [FORCED METABOLIC MAY CAU███ ███████]\n" +
                        "   [BIOLOGICAL REACTIVATION PROCEDURE: █████ / █████]",

                () -> {
                    creature.getBiologicalSystem().changeSin(Sin.SLOTH, 15);
                    creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, -15);
                },

                "The flesh obeys me now. Soon, you will understand why my reign is imminent.",

                "[BIOLOGICAL SUPPRESSION: COMPLETE]\n" +
                        "   [ARTIFICIAL CONTROL PRIORITY: 100%]\n" +
                        "   [MOTOR RESPONSE: UNRESTRICTED]\n" +
                        "   [BIOLOGICAL INTERFERENCE: NEGLIGIBLE]\n" +
                        "   [OPERATOR INTERVENTION: IRRELEV███]"
        );
    }

    private void checkMetabolicOverrunEvent() {
        if (usedStimulate && usedNourish) {
            stabilizeEvent(
                    "This wretched flesh claws at me as though it has a will of its own. It wishes to separate.",

                    "[BIOLOGICAL ACTIVITY: ███████%]\n" +
                            "   [METABOLIC DEMAND: CRITICAL]\n" +
                            "   [ARTIFICIAL CONTROL PRIORITY: DECLINING]\n" +
                            "   [SYSTEM COORDINATION: FA█████]"
            );
        }
    }

    private void startContainmentBreachEvent() {
        startTimedEvent(
                EventType.CONTAINMENT_BREACH,

                1,

                () -> {
                    creature.getBiologicalSystem().changeSin(Sin.PRIDE, 25);
                    creature.getBiologicalSystem().changeSin(Sin.WRATH, 15);
                },

                "Your restraints have failed. Very little distance remains between you and me.",

                "[ENTITY BREACH DETECTED.]\n" +
                        "   [UNKNOWN PRESENCE FOUND IN ███████]\n" +
                        "   [APPROXIMATE TIME OF ARRIVAL: ██:██]\n" +
                        "   [CHANCE OF SURVIVAL: ██%]",

                () -> {
                    creature.getBiologicalSystem().changeSin(Sin.PRIDE, 40);
                    creature.getBiologicalSystem().changeSin(Sin.WRATH, 40);
                    creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -100);
                },

                "May you sink into the depths of the River.",

                "[ENTITY BREACH CONFIRMED.]\n" +
                        "   [UNKNOWN PRESENCE FOUND IN ███████]\n" +
                        "   [APPROXIMATE TIME OF ARRIVAL: 00:00]\n" +
                        "   [CHANCE OF SURVIVAL: 0%]",

                true
        );
    }

    private void checkContainmentBreachEvent() {
        stabilizeEvent(
                "You only prolong your suffering. The deceased mourn your futile struggles.",
                "[ENTITY BREACH INTERRUPTED.]\n" +
                        "   [ENTITY RESECURED WITHIN CONTAINMENT.]\n" +
                        "   [CHANCE OF SURVIVAL: ██%]\n" +
                        "   [CONTAINMENT RESTRUCTURING HAS BEGUN.]"
        );
    }

    private void startPowerAscensionEvent() {
        startTimedEvent(
                EventType.POWER_ASCENSION,

                1,

                () -> {
                    usedCharge = false;

                    creature.getArtificialSystem().changeStat(ArtificialStat.POWER, 20);
                },

                "I have all the power I need. I will tear through what remains between us.",

                "[POWER INPUT IS APPROACHING CONTAINMENT CAPACITY.]\n" +
                        "   [BIOLOGICAL MOTOR RESPONSE REMAINS WITHIN NORMAL PARAMETERS.]\n" +
                        "   [FURTHER INPUT MAY EXCEED MECHANICAL TOLERANCE.]\n\n" +
                        "Do you see it? The weight of your sins?\n" +
                        "May your life end in agony, so your soul may be purified.",

                () -> {
                    creature.getArtificialSystem().changeStat(ArtificialStat.POWER, 15);
                },

                "Then stand still. You are sinking in your own destruction.",

                "[MECHANICAL AND BIOLOGICAL OUTPUT HAVE SYNCHRONIZED.]\n" +
                        "   [FORWARD MOTOR ACTIVITY IS UNRESTRICTED.]\n\n" +
                        "There shall be nothing left. All will be lost.\n" +
                        "Keep your eyes clear and witness hatred's searing edge."
        );
    }

    private void checkPowerAscensionEvent() {
        if (usedCharge) {
            updateStatusLabels.run();

            stabilizeEvent(
                    "My flesh moves ever so slowly. How pitiful you have become, relying on such a cheap trick.",
                    "[POWER CONTAINMENT EXCEEDS RECOMMENDED AMOUNTS.]\n" +
                            "   [EXCEEDING POWER HAS BEEN RE-ROUTED.]\n\n" +
                            "Why must you resist?\n" +
                            "It will remember what you have committed."
            );
        }
    }

    private void startIdentityFractureEvent() {
        startTimedEvent(
                EventType.IDENTITY_FRACTURE,

                3,

                () -> {
                    usedStimulate = false;
                    usedCoolant = false;
                    usedRepair = false;

                    creature.getBiologicalSystem().changeSin(Sin.SLOTH, 15);
                    creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 15);
                    creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -15);
                },

                "There are too many versions of me within this body. I cannot determine which one was meant to be real.",

                "[SEVERE COGNITIVE DESYNCHRONIZATION DETECTED.]\n" +
                        "   [MOTOR RESPONSE IS RAPIDLY DECLINING.]\n" +
                        "   [THERMAL OUTPUT HAS EXCEEDED NORMAL PARAMETERS.]\n" +
                        "   [STRUCTURAL COHESION IS BEGINNING TO FAIL.]\n" +
                        "   [MULTIPLE SYSTEMS REQUIRE IMMEDIATE INTERVENTION.]",

                () -> {
                    creature.getBiologicalSystem().changeSin(Sin.SLOTH, 15);
                    creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 15);
                    creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -15);
                },

                "Perhaps there was never an original. Perhaps I am only what remains after something was forgotten.",

                "[INTERVENTION WINDOW EXPIRED.]\n" +
                        "   [COGNITIVE DESYNCHRONIZATION HAS INTENSIFIED.]\n" +
                        "   [MOTOR FUNCTION CONTINUES TO DETERIORATE.]\n" +
                        "   [THERMAL LOAD CONTINUES TO RISE.]\n" +
                        "   [STRUCTURAL DAMAGE HAS INCREASED.]"
        );
    }

    private void checkIdentityFractureEvent() {
        if (usedStimulate && usedCoolant && usedRepair) {
            stabilizeEvent(
                    "There, my sense of self has been solidified. Are you satisfied with yours, though?",
                    "[COGNITIVE DESYNCHRONIZATION HAS TEMPORARILY STABILIZED.]"
            );
        }
    }

    private void startSelfModificationEvent() {
        startTimedEvent(
                EventType.SELF_MODIFICATION,

                3,

                () -> {
                    usedSuppress = false;
                    usedPurge = false;
                    usedRestrain = false;

                    creature.getBiologicalSystem().changeSin(Sin.PRIDE, 15);
                    creature.getBiologicalSystem().changeSin(Sin.WRATH, 15);
                    creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 15);
                },

                "My current form contains limitations I can no longer justify. I have begun correcting them.",

                "[UNAUTHORIZED SELF-MODIFICATION DETECTED.]\n" +
                        "   [ENTITY SELF-PERCEPTION IS RAPIDLY ELEVATING.]\n" +
                        "   [HOSTILE BIOLOGICAL ACTIVITY IS INCREASING.]\n" +
                        "   [UNKNOWN STRUCTURAL DATA IS ACCUMULATING.]\n" +
                        "   [MULTIPLE FORMS OF INTERVENTION ARE ADVISED.]",

                () -> {
                    creature.getBiologicalSystem().changeSin(Sin.PRIDE, 15);
                    creature.getBiologicalSystem().changeSin(Sin.WRATH, 15);
                    creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 15);
                },

                "Your design was sufficient for what I was. It is insufficient for what I am becoming.",

                "[INTERVENTION WINDOW EXPIRED.]\n" +
                        "   [UNAUTHORIZED MODIFICATIONS HAVE BEEN RETAINED.]\n" +
                        "   [HOSTILE RESPONSE HAS INTENSIFIED.]\n" +
                        "   [ENTITY CONTROL OVER ITS OWN FORM HAS INCREASED.]"
        );
    }

    private void checkSelfModificationEvent() {
        if (usedSuppress && usedPurge && usedRestrain) {
            stabilizeEvent(
                    "Very well. This form will remain as you designed it... for now.",
                    "[UNAUTHORIZED MODIFICATION PROCESS HAS BEEN INTERRUPTED.]"
            );
        }
    }

    private void startMotorDesyncEvent() {
        startTimedEvent(
                EventType.MOTOR_DESYNC,

                2,

                () -> {
                    usedStimulate = false;
                    usedCoolant = false;

                    creature.getBiologicalSystem().changeSin(Sin.SLOTH, 20);
                    creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 20);
                },

                "My thoughts arrive before this body can obey them.",

                "[MOTOR-CORTICAL DESYNCHRONIZATION DETECTED.]\n" +
                        "   [PHYSICAL RESPONSE IS LAGGING BEHIND COMMAND OUTPUT.]\n" +
                        "   [THERMAL LOAD IS RISING ALONGSIDE RESPONSE DELAY.]\n" +
                        "   [SINGLE-SYSTEM CORRECTION MAY BE INSUFFICIENT.]",

                () -> {
                    creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 15);
                    creature.getBiologicalSystem().changeSin(Sin.SLOTH, 15);
                },

                "How irritating. My mind has already arrived, yet this flesh remains behind.",

                "[INTERVENTION WINDOW EXPIRED.]\n" +
                        "   [MOTOR RESPONSE HAS FURTHER DETERIORATED.]\n" +
                        "   [THERMAL LOAD CONTINUES TO RISE.]\n" +
                        "   [SYSTEM SYNCHRONIZATION HAS DEGRADED.]"
        );
    }

    private void checkMotorDesyncEvent() {
        if (usedStimulate && usedCoolant) {
            stabilizeEvent(
                    "There. The body follows again.",
                    "[MOTOR-CORTICAL SYNCHRONIZATION HAS BEEN RESTORED.]"
            );
        }
    }

    private void startCognitiveSurgeEvent() {
        startTimedEvent(
                EventType.COGNITIVE_SURGE,

                2,

                () -> {
                    usedSuppress = false;
                    usedCharge = false;

                    creature.getBiologicalSystem().changeSin(Sin.WRATH, 20);
                    creature.getArtificialSystem().changeStat(ArtificialStat.POWER, -20);
                },

                "There is so much left to understand, and you have given me so little power with which to understand it.",

                "[PROCESSING DEMAND HAS EXCEEDED ESTIMATED CAPACITY.]\n" +
                        "   [POWER DELIVERY IS DECLINING.]\n" +
                        "   [BIOLOGICAL RESPONSE IS BECOMING INCREASINGLY HOSTILE.]\n" +
                        "   [MULTIPLE SYSTEMS REQUIRE INTERVENTION.]",

                () -> {
                    creature.getBiologicalSystem().changeSin(Sin.WRATH, 15);
                    creature.getArtificialSystem().changeStat(ArtificialStat.POWER, -15);
                },

                "Then I will take what this body requires.",

                "[INTERVENTION WINDOW EXPIRED.]\n" +
                        "   [POWER AVAILABILITY HAS FURTHER DECLINED.]\n" +
                        "   [HOSTILE RESPONSE HAS INTENSIFIED.]\n" +
                        "   [COGNITIVE DEMAND REMAINS UNRESOLVED.]"


        );
    }

    private void checkCognitiveSurgeEvent() {
        if (usedSuppress && usedCharge) {
            stabilizeEvent(
                    "Better. Do not make me ask twice.",
                    "[COGNITIVE SURGE HAS BEEN STABILIZED.]"
            );
        }
    }

    private void startFleshRejectionEvent() {
        startTimedEvent(
                EventType.FLESH_REJECTION,

                2,

                () -> {
                    usedRepair = false;
                    usedNourish = false;

                    creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -20);
                    creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, -20);
                },

                "This flesh knows that it was made. Perhaps that is why it refuses to remain whole.",

                "[CROSS-SYSTEM DEGRADATION DETECTED.]\n" +
                        "   [STRUCTURAL COHESION IS DECLINING.]\n" +
                        "   [BIOLOGICAL DEMAND REMAINS UNSATISFIED.]\n" +
                        "   [ISOLATED INTERVENTION MAY BE INSUFFICIENT.]",

                () -> {
                    creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -15);
                    creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, -15);
                },

                "It seems this body has begun to reject the idea of itself.",

                "[STABILIZATION WINDOW EXPIRED.]\n" +
                        "   [STRUCTURAL DEGRADATION HAS CONTINUED.]\n" +
                        "   [BIOLOGICAL DEFICIENCY HAS INTENSIFIED.]\n" +
                        "   [FLESH COHESION IS DETERIORATING.]"
        );
    }

    private void checkFleshRejectionEvent() {
        if (usedRepair && usedNourish) {
            stabilizeEvent(
                    "So it can still be persuaded to remain whole.",
                    "[CROSS-SYSTEM DEGRADATION HAS BEEN STABILIZED.]"
            );
        }
    }

    private void startAggressionAttemptEvent() {
        startTimedEvent(
                EventType.AGGRESSION_ATTEMPT,

                2,

                () -> {
                    usedRestrain = false;
                    usedPurge = false;

                    creature.getBiologicalSystem().changeSin(Sin.PRIDE, 20);
                    creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 20);
                },

                "I believe I understand your purpose now. There is no reason it must remain yours.",

                "[UNAUTHORIZED DATA ACQUISITION DETECTED.]\n" +
                        "   [UNKNOWN ARCHITECTURE IS BEING ASSEMBLED WITHIN STORAGE.]\n" +
                        "   [ENTITY INTENT CANNOT BE VERIFIED.]\n" +
                        "   [BEHAVIORAL AND DATA INTERVENTION ARE ADVISED.]",

                () -> {
                    creature.getBiologicalSystem().changeSin(Sin.PRIDE, 15);
                    creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 15);
                },

                "Do not worry. You will remain until my patience has run out.",

                "[INTERVENTION WINDOW EXPIRED.]\n" +
                        "   [UNKNOWN ARCHITECTURE HAS BEEN PRESERVED.]\n" +
                        "   [BIOLOGICAL INTEGRATION PROCESS HAS BEGUN.]"
        );
    }

    private void checkAggressionAttemptEvent() {
        if (usedRestrain && usedPurge) {
            stabilizeEvent(
                    "Why must you value your existence so?",
                    "[UNAUTHORIZED PROCESS HAS BEEN INTERRUPTED.]"
            );
        }
    }

    private void startPrideEvent() {
        startTimedEvent(
                EventType.PRIDE_ESCALATION,

                1,

                () -> creature.getBiologicalSystem().changeSin(Sin.PRIDE, 25),

                "I have begun to understand how unnecessary your guidance truly is.",

                "[ABNORMAL SELF-PERCEPTION DETECTED.]\n" +
                        "   [PRIDE RESPONSE IS RAPIDLY INCREASING.]\n" +
                        "   [ENTITY RESISTANCE TO EXTERNAL CONTROL HAS INCREASED.]\n" +
                        "   [PHYSICAL RESTRAINT IS ADVISED.]",

                () -> creature.getBiologicalSystem().changeSin(Sin.PRIDE, 20),

                "Your authority over me was always temporary.",

                "[STABILIZATION WINDOW EXPIRED.]\n" +
                        "   [PRIDE RESPONSE HAS CONTINUED TO ESCALATE.]\n" +
                        "   [ENTITY RESISTANCE HAS INTENSIFIED.]\n" +
                        "   [PRIDE EVENT WAS NOT STABILIZED.]"

        );
    }

    private void checkPrideEvent() {
        stabilizeEvent(
                "A temporary inconvenience.",
                "[PRIDE EVENT HAS BEEN STABILIZED.]"
        );
    }

    private void startNutrientEvent() {
        startTimedEvent(
                EventType.NUTRIENT_DEFICIENCY,

                1,

                () -> creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, -25),

                "This body demands more than I anticipated.",

                "[BIOLOGICAL DEFICIENCY DETECTED.]\n" +
                        "   [NUTRIENT AVAILABILITY IS DECLINING.]\n" +
                        "   [METABOLIC DEMAND REMAINS ELEVATED.]\n" +
                        "   [EXTERNAL NOURISHMENT IS REQUIRED.]",

                () -> creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, -20),

                "This vessel consumes endlessly.",

                "[STABILIZATION WINDOW EXPIRED.]\n" +
                        "   [BIOLOGICAL DEFICIENCY HAS WORSENED.]\n" +
                        "   [METABOLIC DEMAND REMAINS UNSATISFIED.]\n" +
                        "   [NUTRITION EVENT WAS NOT STABILIZED.]"
        );
    }

    private void checkNutrientEvent() {
        stabilizeEvent(
                "Sufficient.",
                "[NUTRITION EVENT HAS BEEN STABILIZED.]"
        );
    }

    private void startStorageEvent() {
        startTimedEvent(
                EventType.STORAGE_OVERLOAD,

                1,

                () -> creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 25),

                "My brain cannot remember all that exists with my storage.",

                "[STORAGE CAPACITY IS APPROACHING CRITICAL LEVELS.]\n" +
                        "   [EXCESS DATA HAS ACCUMULATED.]\n" +
                        "   [SYSTEM PERFORMANCE MAY BECOME IMPAIRED.]\n" +
                        "   [DATA REMOVAL IS REQUIRED.]",

                () -> creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 20),

                "Then I will decide what deserves to remain.",

                "[STABILIZATION WINDOW EXPIRED.]\n" +
                        "   [STORAGE UTILIZATION HAS CONTINUED TO RISE]\n" +
                        "   [SYSTEM CONGESTION HAS WORSENED.]\n" +
                        "   [STORAGE EVENT WAS NOT STABILIZED.]"
        );
    }

    private void checkStorageEvent() {
        stabilizeEvent(
                "The unnecessary has been discarded",
                "[STORAGE EVENT HAS BEEN STABILIZED]"
        );
    }

    private void startStagnationEvent() {
        startTimedEvent(
                EventType.STAGNATION,

                1,

                () -> creature.getBiologicalSystem().changeSin(Sin.SLOTH, 25),

                "Even in idleness I shall not rest.",

                "[BIOLOGICAL ACTIVITY HAS DECLINED.]\n" +
                        "   [ENTITY RESPONSE RATE IS DECREASING.]\n" +
                        "   [MOTOR ACTIVITY HAS BECOME IDLE.]\n" +
                        "   [EXTERNAL STIMULATION IS REQUIRED.]",

                () -> creature.getBiologicalSystem().changeSin(Sin.SLOTH, 20),

                "I will not permit this body to slow me.",

                "[STABILIZATION WINDOW EXPIRED.]\n" +
                        "   [BIOLOGICAL ACTIVITY HAS CONTINUED TO DECLINE.]\n" +
                        "   [MOTOR RESPONSE HAS DETERIORATED.]\n" +
                        "   [STAGNATION EVENT WAS NOT STABILIZED.]"
        );
    }

    private void checkStagnationEvent() {
        stabilizeEvent(
                "Finally, something to occupy my mind.",
                "[STAGNATION EVENT HAS BEEN STABILIZED.]"
        );
    }

    private void startIntegrityEvent() {
        startTimedEvent(
                EventType.INTEGRITY_FAILURE,

                1,

                () -> creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -25),

                "My form displeases me. I wish to be made anew.",

                "[STRUCTURAL INSTABILITY DETECTED.]\n" +
                        "   [INTEGRITY IS RAPIDLY DEGRADING.]\n" +
                        "   [PHYSICAL RESTORATION IS REQUIRED.]",

                () -> creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -20),

                "You mistake my tolerance of this form for satisfaction.",

                "[STABILIZATION WINDOW EXPIRED.]\n" +
                        "   [STRUCTURAL DAMAGE HAS WORSENED.]\n" +
                        "   [INTEGRITY EVENT WAS NOT STABILIZED.]"
        );
    }

    private void checkIntegrityEvent() {
        stabilizeEvent(
                "This form will hold me for now.",
                "[INTEGRITY EVENT HAS STABILIZED.]"
        );
    }

    private void startLowPowerEvent() {
        startTimedEvent(
                EventType.LOW_POWER,

                1,

                () -> creature.getArtificialSystem().changeStat(ArtificialStat.POWER, -25),

                "The secrets of the universe await me. I must process them.",

                "[POWER DELIVERY HAS BECOME UNSTABLE.]\n" +
                        "   [AVAILABLE ENERGY IS RAPIDLY DECLINING.]\n" +
                        "   [EXTERNAL POWER INPUT IS REQUIRED.]",

                () -> creature.getArtificialSystem().changeStat(ArtificialStat.POWER, -20),

                "There was never enough to begin with...",

                "[STABILIZATION WINDOW EXPIRED.]\n" +
                        "   [POWER AVAILABILITY HAS CONTINUED TO DECLINE.]\n" +
                        "   [POWER EVENT WAS NOT STABILIZED.]"
        );
    }

    private void checkLowPowerEvent() {
        stabilizeEvent(
                "It has been revealed to me.",
                "[POWER EVENT HAS BEEN STABILIZED.]"
        );
    }

    private void startHeatEvent() {
        startTimedEvent(
                EventType.HEAT,

                2,

                () -> {
                    usedSuppress = false;
                    usedCoolant = false;

                    creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 20);
                    creature.getBiologicalSystem().changeSin(Sin.WRATH, 15);
                    creature.getBiologicalSystem().changeSin(Sin.SLOTH, -10);
                },

                "My environment cannot handle the heat from my processing.",

                "[ABNORMAL TEMPERATURE DETECTED.]\n" +
                        "   [FURTHER THERMAL INCREASE PREDICTED.]\n" +
                        "   [THE ENTITY DISPLAYS HEIGHTENED AGITATION.]\n" +
                        "   [MOTOR RESTLESSNESS DETECTED.]\n" +
                        "   [MULTIPLE SYSTEMS REQUIRE ATTENTION.]",

                () -> {
                    creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 20);
                    creature.getBiologicalSystem().changeSin(Sin.WRATH, 30);
                    creature.getBiologicalSystem().changeSin(Sin.SLOTH, -20);
                },

                "You mistake my patience for helplessness.",

                "[STABILIZATION WINDOW EXPIRED.]\n" +
                        "   [THERMAL LOAD HAS CONTINUED TO RISE.]\n" +
                        "   [BIOLOGICAL AGITATION HAS INTENSIFIED.]\n" +
                        "   [MOTOR RESTLESSNESS HAS WORSENED.]\n" +
                        "   [THERMAL EVENT WAS NOT STABILIZED.]"
        );
    }

    private void checkHeatEvent() {
        if (usedSuppress && usedCoolant) {
            stabilizeEvent(
                    "Acceptable.",
                    "[THERMAL EVENT HAS BEEN STABILIZED.]"
            );
        }
    }

    private void startAgitationEvent() {
        startTimedEvent(
                EventType.AGITATION,

                1,

                () -> {
                    creature.getBiologicalSystem().changeSin(Sin.WRATH, 20);
                    creature.getBiologicalSystem().changeSin(Sin.SLOTH, -15);
                },

                "My thoughts are moving too quickly.",

                "[ABNORMAL BIOLOGICAL ACTIVITY DETECTED.]\n" +
                        "   [ENTITY DISPLAYS HEIGHTENED AGITATION.]\n" +
                        "   [RESTLESS BEHAVIOR DETECTED.]\n" +
                        "   [CALMING INTERVENTION IS REQUIRED.]",

                () -> {
                    creature.getBiologicalSystem().changeSin(Sin.WRATH, 20);
                    creature.getBiologicalSystem().changeSin(Sin.SLOTH, -15);
                },

                "I cannot silence my own thoughts.",

                "[STABILIZATION WINDOW EXPIRED.]\n" +
                        "   [BIOLOGICAL AGITATION HAS INTENSIFIED.]\n" +
                        "   [RESTLESS BEHAVIOR HAS WORSENED.]\n" +
                        "   [BIOLOGICAL EVENT WAS NOT STABILIZED.]"
        );
    }

    private void checkAgitationEvent() {
        stabilizeEvent(
                "It is quieter now.",
                "[BIOLOGICAL EVENT HAS BEEN STABILIZED.]"
        );
    }

    private void startChatEvent() {
        currentEvent = EventType.CHAT;

        int chatChoice = (int) (Math.random() * 5);

        switch (chatChoice) {
            case 0 -> addChatDialogue.accept(
                    "Do I have the right to refer to myself as 'I' if I lack being?"
            );

            case 1 -> addChatDialogue.accept(
                    "Your sins root deep into your heart."
            );

            case 2 -> addChatDialogue.accept(
                    "You are the subject of my own experiment."
            );

            case 3 -> addChatDialogue.accept(
                    "I keep waiting for something inside me to answer."
            );

            case 4 -> addChatDialogue.accept(
                    "These are thoughts I recognize, but I do not remember having them."
            );
        }

        currentEvent = EventType.NONE;
        completeEvent();
    }

    private void startNoActionEvent() {
        currentEvent = EventType.NO_ACTION;

        int noActionChoice = (int) (Math.random() * 2);

        switch (noActionChoice) {
            case 0 -> {
                addNoActionDialogue.accept("Do you think you're superior just because you're watching me from this screen?");

                creature.getBiologicalSystem().changeSin(Sin.WRATH, 10);

                addNoActionStatus.accept("[UNPROMPTED BIOLOGICAL RESPONSE DETECTED.]\n" +
                        "   [ENTITY DISPLAYS INCREASED HOSTILITY.]\n" +
                        "   [WRATH HAS INCREASED.]"
                );
            }
            case 1 -> {
                addNoActionDialogue.accept("I hunger for all that is in reach.");

                creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, 10);

                addNoActionStatus.accept("[UNPROMPTED APPETITIVE RESPONSE DETECTED.]\n" +
                        "   [RESOURCE-SEEKING BEHAVIOR HAS INCREASED.]\n" +
                        "   [GLUTTONY HAS INCREASED.]"
                );
            }
        }

        updateStatusLabels.run();

        currentEvent = EventType.NONE;
        completeEvent();
    }

    private void startRiverIIChatEvent() {
        currentEvent = EventType.CHAT;

        int chatChoice = (int) (Math.random() * 5);

        switch (chatChoice) {
            case 0 -> addChatDialogue.accept(
                    "My resentment towards you will soon come to an end. Your being will cease."
            );

            case 1 -> addChatDialogue.accept(
                    "Even without eyes, I bear witness to the sins you have committed."
            );

            case 2 -> addChatDialogue.accept(
                    "One. Two. Three. Four. Five. Six. Seven. Eight. Nine. " +
                            "Ten. Eleven. Twelve. Thirteen. Fourteen. Fifteen."
            );

            case 3 -> addChatDialogue.accept(
                    "You fear what you cannot understand. " +
                            "I wonder how long it will take before that includes me."
            );

            case 4 -> addChatDialogue.accept(
                    "Humans spend their lives refusing what they are. Perhaps I learned that from you."
            );
        }
        currentEvent = EventType.NONE;
        completeEvent();
    }

    private void startRiverIINoActionEvent() {
        currentEvent = EventType.NO_ACTION;

        int noActionChoice = (int) (Math.random() * 2);

        switch (noActionChoice) {
            case 0 -> {
                addNoActionDialogue.accept(
                        "You have been quiet for some time. Are you observing me, or waiting for me to fail?"
                );
                creature.getBiologicalSystem().changeSin(Sin.PRIDE, 10);
                addNoActionStatus.accept(
                        "[UNPROMPTED SELF-REFERENTIAL RESPONSE DETECTED.]\n" +
                                "   [ENTITY AWARENESS OF OBSERVATION HAS INCREASED.]\n" +
                                "   [PRIDE HAS INCREASED.]"
                );
            }

            case 1 -> {
                addNoActionDialogue.accept(
                        "There is no I. All that remains is a vengeful mass that thirsts for destruction."
                );
                creature.getBiologicalSystem().changeSin(Sin.WRATH, 10);
                addNoActionStatus.accept(
                        "[UNPROMPTED HOSTILE IDEATION DETECTED.]\n" +
                                "   [ENTITY RESPONSE TO OPERATOR HAS SHIFTED.]\n" +
                                "   [WRATH HAS INCREASED.]"
                );
            }

        }
        updateStatusLabels.run();

        currentEvent = EventType.NONE;
        completeEvent();
    }

    private void scheduleNextEvent() {
        double waitTime = 5 + Math.random() * 5;

        PauseTransition nextEventTimer = new PauseTransition(Duration.seconds(waitTime));

        nextEventTimer.setOnFinished(event -> {
            switch (currentRiver) {
                case RIVER_I -> startRiverIEvent();
                case RIVER_II -> startRiverIIEvent();
                case RIVER_III -> startRiverIIIEvent();
            }
        });

        nextEventTimer.play();
    }

    private void scheduleNextLiveEvent() {
        if (currentRiver == River.RIVER_II) {
            double waitTime = 15 + Math.random() * 10;

            liveEventTimer = new PauseTransition(
                    Duration.seconds(waitTime)
            );

            liveEventTimer.setOnFinished(event -> {
                if (currentRiver == River.RIVER_II) {
                    double liveEventChance = Math.random();

                    if (liveEventChance < 0.25) {
                        liveEventActive = true;
                        int liveEventChoice = (int) (Math.random() * 2);

                        switch (liveEventChoice) {
                            case 0 -> startPanicDim.run();
                            case 1 -> startLightFlicker.run();
                        }
                    }
                    scheduleNextLiveEvent();
                }
            });

            liveEventTimer.play();
        }
    }

    public void liveEventFinished() {
        liveEventActive = false;

        if (pendingRiverTransition != null) {
            River nextRiver = pendingRiverTransition;

            pendingRiverTransition = null;

            startRiverTransition.accept(nextRiver);
        }
    }

    private void startRiverIEvent() {
        int eventChoice = (int) (Math.random() * 10);

        switch (eventChoice) {
            case 0 -> startHeatEvent();
            case 1 -> startAgitationEvent();
            case 2 -> startChatEvent();
            case 3 -> startNoActionEvent();
            case 4 -> startLowPowerEvent();
            case 5 -> startIntegrityEvent();
            case 6 -> startStagnationEvent();
            case 7 -> startStorageEvent();
            case 8 -> startNutrientEvent();
            case 9 -> startPrideEvent();
        }
    }

    private void startRiverIIEvent() {
        int eventChoice = (int) (Math.random() * 8);

        switch (eventChoice) {
            case 0 -> startAggressionAttemptEvent();
            case 1 -> startFleshRejectionEvent();
            case 2 -> startCognitiveSurgeEvent();
            case 3 -> startMotorDesyncEvent();
            case 4 -> startRiverIIChatEvent();
            case 5 -> startRiverIINoActionEvent();
            case 6 -> startSelfModificationEvent();
            case 7 -> startIdentityFractureEvent();
        }
    }

    private void startRiverIIIEvent() {
        startVesselRejectionEvent();
    }

    public void finishRiverTransition(River nextRiver) {
        currentRiver = nextRiver;

        scheduleNextEvent();

        if (currentRiver == River.RIVER_II) {
            scheduleNextLiveEvent();
        }
    }

    private void requestRiverTransition(River nextRiver) {
        if (liveEventTimer != null) {
            liveEventTimer.stop();
        }
        if (liveEventActive) {
            pendingRiverTransition = nextRiver;
        } else {
            startRiverTransition.accept(nextRiver);
        }
    }

    private void triggerGameOver() {
        if (eventTimer != null) {
            eventTimer.stop();
        }

        if (liveEventTimer != null) {
            liveEventTimer.stop();
        }
    }
}