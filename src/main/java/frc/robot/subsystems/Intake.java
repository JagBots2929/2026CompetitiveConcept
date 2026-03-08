package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Volts;

import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkFlexConfig;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Command.InterruptionBehavior;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Ports;

public class Intake extends SubsystemBase {
    public enum Speed {
        STOP(0),
        INTAKE(0.8);

        private final double percentOutput;

        private Speed(double percentOutput) {
            this.percentOutput = percentOutput;
        }

        public Voltage voltage() {
            return Volts.of(percentOutput * 12.0);
        }
    }

    // MUST TUNE
    public enum Position {
        HOMED(110),
        STOWED(100),
        INTAKE(-4),
        AGITATE(20);

        private final double degrees;

        private Position(double degrees) {
            this.degrees = degrees;
        }

        public Angle angle() {
            return Degrees.of(degrees);
        }
    }

    private static final double kPivotReduction = 50.0;
    private static final double kPositionTolerance = 5;

    private final SparkFlex pivotMotor, rollerMotor;
    private final SparkFlexConfig pivotConfig = new SparkFlexConfig();
    private final SparkFlexConfig rollerConfig = new SparkFlexConfig();

    private boolean isHomed = false;

    public Intake() {
        pivotMotor = new SparkFlex(Ports.kIntakePivot, MotorType.kBrushless);
        rollerMotor = new SparkFlex(Ports.kIntakeRollers, MotorType.kBrushless);
        configureMotors();
        SmartDashboard.putData(this);
    }

    private void configureMotors() {
        pivotConfig.encoder.positionConversionFactor(360.0 / kPivotReduction);
        pivotConfig.closedLoop.p(0.1);

        rollerMotor.configure(rollerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        pivotMotor.configure(pivotConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    }

    private boolean isPositionWithinTolerance(double targetPose) {
        final double currentPosition = pivotMotor.getEncoder().getPosition();
        if(currentPosition < targetPose + kPositionTolerance && currentPosition > targetPose - kPositionTolerance) {
            return true;
        }
        return false;
    }

    private void setPivotPercentOutput(double percentOutput) {
        pivotMotor.set(percentOutput);
    }

    public void set(Position position) {
        pivotMotor.getClosedLoopController().setSetpoint(position.degrees, ControlType.kPosition);
    }

    public void set(Speed speed) {
        rollerMotor.set(speed.percentOutput);
    }

    public Command intakeCommand() {
        return startEnd(
            () -> {
                set(Position.INTAKE);
                set(Speed.INTAKE);
            },
            () -> set(Speed.STOP)
        );
    }

    public Command agitateCommand() {
        return runOnce(() -> set(Speed.INTAKE))
            .andThen(
                Commands.sequence(
                    runOnce(() -> set(Position.AGITATE)),
                    Commands.waitUntil(() ->isPositionWithinTolerance(Position.AGITATE.degrees) == true),
                    runOnce(() -> set(Position.INTAKE)),
                    Commands.waitUntil(() ->isPositionWithinTolerance(Position.INTAKE.degrees) == true)
                )
                .repeatedly()
            )
            .handleInterrupt(() -> {
                set(Position.INTAKE);
                set(Speed.STOP);
            });
    }

    public Command homingCommand() {
        return Commands.sequence(
            runOnce(() -> setPivotPercentOutput(0.1)),
            Commands.waitUntil(() -> pivotMotor.getOutputCurrent() > 6),
            runOnce(() -> {
                pivotMotor.getEncoder().setPosition(Position.HOMED.degrees);
                isHomed = true;
                set(Position.STOWED);
            })
        )
        .unless(() -> isHomed)
        .withInterruptBehavior(InterruptionBehavior.kCancelIncoming);
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addStringProperty("Command", () -> getCurrentCommand() != null ? getCurrentCommand().getName() : "null", null);
        builder.addDoubleProperty("Angle (degrees)", () -> pivotMotor.getEncoder().getPosition(), null);
        builder.addDoubleProperty("RPM", () -> rollerMotor.getEncoder().getVelocity(), null);
        builder.addDoubleProperty("Pivot Supply Current", () -> pivotMotor.getAppliedOutput(), null);
        builder.addDoubleProperty("Roller Supply Current", () -> rollerMotor.getAppliedOutput(), null);
    }
}
