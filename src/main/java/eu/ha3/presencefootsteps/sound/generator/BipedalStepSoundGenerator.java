package eu.ha3.presencefootsteps.sound.generator;

import eu.ha3.presencefootsteps.config.Variator;
//import eu.ha3.presencefootsteps.mixins.ILivingEntity;
import eu.ha3.presencefootsteps.sound.Isolator;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.util.PlayerUtil;
import eu.ha3.presencefootsteps.world.Association;
import eu.ha3.presencefootsteps.world.Solver;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

class BipedalStepSoundGenerator implements StepSoundGenerator {

    private double lastX;
    private double lastY;
    private double lastZ;

    protected double motionX;
    protected double motionY;
    protected double motionZ;

    // Construct
    protected Solver solver;
    protected AcousticLibrary acoustics;
    protected Variator variator;

    // Footsteps
    protected float dmwBase;
    protected float dwmYChange;
    protected double yPosition;

    // Airborne
    protected boolean isAirborne;
    protected float fallDistance;

    protected float lastReference;
    protected boolean isImmobile;
    protected long timeImmobile;

    protected long immobilePlayback;
    protected int immobileInterval;

    protected boolean isRightFoot;

    protected double xMovec;
    protected double zMovec;
    protected boolean scalStat;

    private boolean stepThisFrame;

    private boolean isMessyFoliage;
    private long brushesTime;

    @Override
    public void setIsolator(Isolator isolator) {
        solver = isolator.getSolver();
        acoustics = isolator.getAcoustics();
        variator = isolator.getVariator();
    }

    @Override
    public boolean generateFootsteps(LivingEntity ply) {
        simulateMotionData(ply);
        simulateFootsteps(ply);
        simulateAirborne(ply);
        simulateBrushes(ply);
        simulateStationary(ply);
        return true;
    }

    protected void simulateStationary(LivingEntity ply) {
        if (isImmobile && (ply.isOnGround() || !ply.isUnderWater()) && playbackImmobile()) {
            Association assos = solver.findAssociation(ply, 0d, isRightFoot);

            if (assos.hasAssociation() || !isImmobile) {
                solver.playAssociation(ply, assos, State.STAND);
            }
        }
    }

    protected boolean playbackImmobile() {
        long now = System.currentTimeMillis();
        if (now - immobilePlayback > immobileInterval) {
            immobilePlayback = now;
            immobileInterval = (int) Math.floor(
                    (Math.random() * (variator.IMOBILE_INTERVAL_MAX - variator.IMOBILE_INTERVAL_MIN)) + variator.IMOBILE_INTERVAL_MIN);
            return true;
        }
        return false;
    }

    /**
     * Fills in the blanks that aren't present on the client when playing on a
     * remote server.
     */
    protected void simulateMotionData(LivingEntity ply) {
        if (PlayerUtil.isClientPlayer(ply)) {
            motionX = ply.getDeltaMovement().x;
            motionY = ply.getDeltaMovement().y;
            motionZ = ply.getDeltaMovement().z;
        } else {
            // Other players don't send their motion data so we have to make our own
            // approximations.
            motionX = (ply.getX() - lastX);
            lastX = ply.getX();
            motionY = (ply.getY() - lastY);

            if (ply.isOnGround()) {
                motionY += 0.0784000015258789d;
            }

            lastY = ply.getY();

            motionZ = (ply.getZ() - lastZ);
            lastZ = ply.getZ();
        }

        if (ply instanceof RemotePlayer) {
            if (ply.level.getGameTime() % 1 == 0) {

                if (motionX != 0 || motionZ != 0) {
                    ply.moveDist += Mth.sqrt((float) (Math.pow(motionX, 2) + Math.pow(motionY, 2) + Math.pow(motionZ, 2))) * 0.8;
                } else {
                    ply.moveDist += Mth.sqrt((float) (Math.pow(motionX, 2) + Math.pow(motionZ, 2))) * 0.8;
                }

                if (ply.isOnGround()) {
                    ply.fallDistance = 0;
                } else if (motionY < 0) {
                    ply.fallDistance -= motionY * 200;
                }
            }
        }
    }

    protected boolean stoppedImmobile(float reference) {
        float diff = lastReference - reference;
        lastReference = reference;
        if (!isImmobile && diff == 0f) {
            timeImmobile = System.currentTimeMillis();
            isImmobile = true;
        } else if (isImmobile && diff != 0f) {
            isImmobile = false;
            return System.currentTimeMillis() - timeImmobile > variator.IMMOBILE_DURATION;
        }

        return false;
    }

    protected void simulateFootsteps(LivingEntity ply) {
        final float distanceReference = ply.moveDist;

        stepThisFrame = false;

        if (dmwBase > distanceReference) {
            dmwBase = 0;
            dwmYChange = 0;
        }

        double movX = motionX;
        double movZ = motionZ;

        double scal = movX * xMovec + movZ * zMovec;
        if (scalStat != scal < 0.001f) {
            scalStat = !scalStat;

            if (scalStat && variator.PLAY_WANDER && !solver.hasStoppingConditions(ply)) {
                solver.playAssociation(ply, solver.findAssociation(ply, 0, isRightFoot),
                        State.WANDER);
            }
        }
        xMovec = movX;
        zMovec = movZ;

        if (ply.isOnGround() || ply.isUnderWater() || ply.onClimbable()) {
            State event = null;

            float dwm = distanceReference - dmwBase;
            boolean immobile = stoppedImmobile(distanceReference);
            if (immobile && !ply.onClimbable()) {
                dwm = 0;
                dmwBase = distanceReference;
            }

            float distance = 0f;
            double verticalOffsetAsMinus = 0f;

            if (ply.onClimbable() && !ply.isOnGround()) {
                distance = variator.DISTANCE_LADDER;
            } else if (!ply.isUnderWater() && Math.abs(yPosition - ply.getY()) > 0.4) {
                // This ensures this does not get recorded as landing, but as a step
                if (yPosition < ply.getY()) { // Going upstairs
                    distance = variator.DISTANCE_STAIR;
                    event = speedDisambiguator(ply, State.UP, State.UP_RUN);
                } else if (!ply.isShiftKeyDown()) { // Going downstairs
                    distance = -1f;
                    verticalOffsetAsMinus = 0f;
                    event = speedDisambiguator(ply, State.DOWN, State.DOWN_RUN);
                }

                dwmYChange = distanceReference;

            } else {
                distance = variator.DISTANCE_HUMAN;
            }

            if (event == null) {
                event = speedDisambiguator(ply, State.WALK, State.RUN);
            }
            distance = reevaluateDistance(event, distance);

            if (dwm > distance) {
                produceStep(ply, event, verticalOffsetAsMinus);
                stepped(ply, event);
                dmwBase = distanceReference;
            }
        }

        if (ply.isOnGround()) {
            // This fixes an issue where the value is evaluated while the player is between
            // two steps in the air while descending stairs
            yPosition = ply.getY();
        }
    }

    protected void produceStep(LivingEntity ply, State event) {
        produceStep(ply, event, 0d);
    }

    protected void produceStep(LivingEntity ply, @Nullable State event, double verticalOffsetAsMinus) {
        if (!solver.playStoppingConditions(ply)) {
            if (event == null) {
                event = speedDisambiguator(ply, State.WALK, State.RUN);
            }

            solver.playAssociation(ply, solver.findAssociation(ply, verticalOffsetAsMinus, isRightFoot), event);
            isRightFoot = !isRightFoot;
        }

        stepThisFrame = true;
    }

    protected void stepped(LivingEntity ply, State event) {

    }

    protected float reevaluateDistance(State event, float distance) {
        return distance;
    }

    protected void simulateAirborne(LivingEntity ply) {
        if ((ply.isOnGround() || ply.onClimbable()) == isAirborne) {
            isAirborne = !isAirborne;
            simulateJumpingLanding(ply);
        }
        if (isAirborne) {
            fallDistance = ply.fallDistance;
        }
    }

    /*protected boolean isJumping(LivingEntity ply) {
        return ((ILivingEntity) ply).isJumping();
    }*/

    protected double getOffsetMinus(LivingEntity ply) {
        if (ply instanceof RemotePlayer) {
            return 1;
        }
        return 0;
    }

    protected void simulateJumpingLanding(LivingEntity ply) {
        if (solver.hasStoppingConditions(ply)) {
            return;
        }

        if (isAirborne) {
            simulateJumping(ply);
        } else {
            simulateLanding(ply);
        }
    }

    protected void simulateJumping(LivingEntity ply) {
        if (variator.EVENT_ON_JUMP) {
            double speed = motionX * motionX + motionZ * motionZ;
            if (speed < variator.SPEED_TO_JUMP_AS_MULTIFOOT) {
                // STILL JUMP
                playMultifoot(ply, getOffsetMinus(ply) + 0.4d, State.JUMP);
                // 2 - 0.7531999805212d (magic number for vertical offset?)
            } else {
                playSinglefoot(ply, getOffsetMinus(ply) + 0.4d, State.JUMP, isRightFoot);
                // RUNNING JUMP
                // Do not toggle foot:
                // After landing sounds, the first foot will be same as the one used to jump.
            }
        }
    }

    protected void simulateLanding(LivingEntity ply) {
        if (fallDistance > variator.LAND_HARD_DISTANCE_MIN) {
            playMultifoot(ply, getOffsetMinus(ply), State.LAND);
            // Always assume the player lands on their two feet
            // Do not toggle foot:
            // After landing sounds, the first foot will be same as the one used to jump.
        } else if (/* !this.stepThisFrame && */!ply.isShiftKeyDown()) {
            playSinglefoot(ply, getOffsetMinus(ply), speedDisambiguator(ply, State.CLIMB, State.CLIMB_RUN),
                    isRightFoot);
            if (!this.stepThisFrame)
                isRightFoot = !isRightFoot;
        }
    }

    protected State speedDisambiguator(LivingEntity ply, State walk, State run) {
        if (!PlayerUtil.isClientPlayer(ply)) { // Other players don't send motion data, so have to decide some other way
            if (ply.isSprinting()) {
                return run;
            }
            return walk;
        }

        double velocity = motionX * motionX + motionZ * motionZ;
        return velocity > variator.SPEED_TO_RUN ? run : walk;
    }

    private void simulateBrushes(LivingEntity ply) {
        if (brushesTime > System.currentTimeMillis()) {
            return;
        }

        brushesTime = System.currentTimeMillis() + 100;

        if ((motionX == 0 && motionZ == 0) || ply.isShiftKeyDown()) {
            return;
        }

        Association assos = solver.findAssociation(ply.level, new BlockPos(
            ply.getZ(),
            ply.getY() - 0.1D - ply.getMyRidingOffset() - (ply.isOnGround() ? 0 : 0.25D),
            ply.getZ()
        ), Solver.MESSY_FOLIAGE_STRATEGY);

        if (!assos.isNull()) {
            if (!isMessyFoliage) {
                isMessyFoliage = true;
                solver.playAssociation(ply, assos, State.WALK);
            }
        } else if (isMessyFoliage) {
            isMessyFoliage = false;
        }
    }

    protected void playSinglefoot(LivingEntity ply, double verticalOffsetAsMinus, State eventType, boolean foot) {
        Association assos = solver.findAssociation(ply, verticalOffsetAsMinus, isRightFoot);

        if (assos.isNotEmitter()) {
            assos = solver.findAssociation(ply, verticalOffsetAsMinus + 1, isRightFoot);
        }

        solver.playAssociation(ply, assos, eventType);
    }

    protected void playMultifoot(LivingEntity ply, double verticalOffsetAsMinus, State eventType) {
        // STILL JUMP
        Association leftFoot = solver.findAssociation(ply, verticalOffsetAsMinus, false);
        Association rightFoot = solver.findAssociation(ply, verticalOffsetAsMinus, true);

        if (leftFoot.hasAssociation() && leftFoot.equals(rightFoot)) {
            // If the two feet solve to the same sound, except NO_ASSOCIATION, only play the sound once
            rightFoot = Association.NOT_EMITTER;
        }

        solver.playAssociation(ply, leftFoot, eventType);
        solver.playAssociation(ply, rightFoot, eventType);
    }
}
