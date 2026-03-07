package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Second;
import static edu.wpi.first.units.Units.Volts;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.FeedbackConfigs;
import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkFlexConfig;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Command.InterruptionBehavior;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.KrakenX60;
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
    private static final AngularVelocity kMaxPivotSpeed = KrakenX60.kFreeSpeed.div(kPivotReduction);
    private static final double kPositionTolerance = 5;

    private final SparkFlex pivotMotor, rollerMotor;
    private final SparkFlexConfig pivotConfig = new SparkFlexConfig();
    private final SparkFlexConfig rollerConfig = new SparkFlexConfig();
    private final VoltageOut pivotVoltageRequest = new VoltageOut(0);
    private final MotionMagicVoltage pivotMotionMagicRequest = new MotionMagicVoltage(0).withSlot(0);
    private final VoltageOut rollerVoltageRequest = new VoltageOut(0);

    private boolean isHomed = false;

    public Intake() {
        pivotMotor = new SparkFlex(Ports.kIntakePivot, MotorType.kBrushless);
        rollerMotor = new SparkFlex(Ports.kIntakeRollers, MotorType.kBrushless);
        configureMotors();
        SmartDashboard.putData(this);
    }

    private void configureMotors() {
        /* final TalonFXConfiguration config = new TalonFXConfiguration()
            .withMotorOutput(
                new MotorOutputConfigs()
                    .withInverted(InvertedValue.CounterClockwise_Positive)
                    .withNeutralMode(NeutralModeValue.Brake)
            )
            .withCurrentLimits(
                new CurrentLimitsConfigs()
                    .withStatorCurrentLimit(Amps.of(120))
                    .withStatorCurrentLimitEnable(true)
                    .withSupplyCurrentLimit(Amps.of(70))
                    .withSupplyCurrentLimitEnable(true)
            )
            .withFeedback(
                new FeedbackConfigs()
                    .withFeedbackSensorSource(FeedbackSensorSourceValue.RotorSensor)
                    .withSensorToMechanismRatio(kPivotReduction)
            )
            .withMotionMagic(
                new MotionMagicConfigs()
                    .withMotionMagicCruiseVelocity(kMaxPivotSpeed)
                    .withMotionMagicAcceleration(kMaxPivotSpeed.per(Second))
            )
            .withSlot0(
                new Slot0Configs()
                    .withKP(300)
                    .withKI(0)
                    .withKD(0)
                    .withKV(12.0 / kMaxPivotSpeed.in(RotationsPerSecond)) // 12 volts when requesting max RPS
            ); */

        rollerMotor.configure(rollerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        pivotMotor.configure(pivotConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    }

    // private void configureRollerMotor() {
    //     final TalonFXConfiguration config = new TalonFXConfiguration()
    //         .withMotorOutput(
    //             new MotorOutputConfigs()
    //                 .withInverted(InvertedValue.Clockwise_Positive)
    //                 .withNeutralMode(NeutralModeValue.Brake)
    //         )
    //         .withCurrentLimits(
    //             new CurrentLimitsConfigs()
    //                 .withStatorCurrentLimit(Amps.of(120))
    //                 .withStatorCurrentLimitEnable(true)
    //                 .withSupplyCurrentLimit(Amps.of(70))
    //                 .withSupplyCurrentLimitEnable(true)
    //         );
    //     rollerMotor.getConfigurator().apply(config);
    // }

    private boolean isPositionWithinTolerance(double targetPose) {
        final double currentPosition = pivotMotor.getEncoder().getPosition();
        final double targetPosition = targetPose;
        if(currentPosition < targetPosition + kPositionTolerance && currentPosition > targetPosition - kPositionTolerance) {
            return true;
        }
        return false;
    }

    private void setPivotPercentOutput(double percentOutput) {
        /* pivotMotor.setControl(
            pivotVoltageRequest
                .withOutput(Volts.of(percentOutput * 12.0))
        ); */

        pivotMotor.set(percentOutput);
    }

    public void set(Position position) {
        // pivotMotor.setControl(
        //     pivotMotionMagicRequest
        //         .withPosition(position.angle())
        // );

        pivotMotor.getEncoder().setPosition(position.degrees);
    }

    public void set(Speed speed) {
        // rollerMotor.setControl(
        //     rollerVoltageRequest
        //         .withOutput(speed.voltage())
        // );

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
                pivotMotor.set(Position.HOMED.degrees);
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
