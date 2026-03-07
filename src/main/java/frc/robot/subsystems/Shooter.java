package frc.robot.subsystems;

import java.util.List;

import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkFlexConfig;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.KrakenX60;
import frc.robot.Ports;
import static edu.wpi.first.units.Units.RPM;

public class Shooter extends SubsystemBase {
    private static final double kVelocityTolerance = 100;

    private final SparkFlex leftMotor, rightMotor;
    private final List<SparkFlex> motors;
    private final SparkFlexConfig leftConfig = new SparkFlexConfig();
    private final SparkFlexConfig rightConfig = new SparkFlexConfig();

    // private final VelocityVoltage velocityRequest = new VelocityVoltage(0).withSlot(0);
    // private final VoltageOut voltageRequest = new VoltageOut(0);

    private double dashboardTargetRPM = 0.0;

    public Shooter() {
        leftMotor = new SparkFlex(Ports.kShooterLeft, MotorType.kBrushless);
        rightMotor = new SparkFlex(Ports.kShooterRight, MotorType.kBrushless);
        motors = List.of(leftMotor, rightMotor);

        leftMotor.configure(leftConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        rightMotor.configure(rightConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

        // configureMotor(leftMotor, InvertedValue.CounterClockwise_Positive);
        // configureMotor(rightMotor, InvertedValue.Clockwise_Positive);

        SmartDashboard.putData(this);
    }

    private void configureMotor() {
        /* final TalonFXConfiguration config = new TalonFXConfiguration()
            .withMotorOutput(
                new MotorOutputConfigs()
                    .withInverted(invertDirection)
                    .withNeutralMode(NeutralModeValue.Coast)
            )
            .withVoltage(
                new VoltageConfigs()
                    .withPeakReverseVoltage(Volts.of(0))
            )
            .withCurrentLimits(
                new CurrentLimitsConfigs()
                    .withStatorCurrentLimit(Amps.of(120))
                    .withStatorCurrentLimitEnable(true)
                    .withSupplyCurrentLimit(Amps.of(70))
                    .withSupplyCurrentLimitEnable(true)
            )
            .withSlot0(
                new Slot0Configs()
                    .withKP(0.5)
                    .withKI(2)
                    .withKD(0)
                    .withKV(12.0 / KrakenX60.kFreeSpeed.in(RotationsPerSecond)) // 12 volts when requesting max RPS
            ); */

        leftConfig.inverted(true);
        rightConfig.inverted(false);

        leftMotor.configure(leftConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        rightMotor.configure(rightConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    }

    public void setRPM(double rpm) {
        for (final SparkFlex motor : motors) {
            /* motor.setControl(
                velocityRequest
                    .withVelocity(RPM.of(rpm))
            ); */
            motor.set(rpm / KrakenX60.kFreeSpeed.in(RPM));
        }
    }

    public void setPercentOutput(double percentOutput) {
        for (final SparkFlex motor : motors) {
            /* motor.setControl(
                voltageRequest
                    .withOutput(Volts.of(percentOutput * 12.0))
            ); */
            motor.set(percentOutput);
        }
    }

    public void stop() {
        setPercentOutput(0.0);
    }

    public Command spinUpCommand(double rpm) {
        return runOnce(() -> setRPM(rpm))
            .andThen(Commands.waitUntil(() -> isVelocityWithinTolerance(rpm) == true));
    }

    public Command dashboardSpinUpCommand() {
        return defer(() -> spinUpCommand(dashboardTargetRPM)); 
    }

    public boolean isVelocityWithinTolerance(double targetRPM) {
            // final boolean isInVelocityMode = motor.getEncoder().getVelocity().equals();
            final double leftCurrentVelocity = leftMotor.getEncoder().getVelocity();
            final double rightCurrentVelocity = rightMotor.getEncoder().getVelocity();
            final double targetVelocity = targetRPM;
            // return currentVelocity.isNear(targetVelocity, kVelocityTolerance);
            if((leftCurrentVelocity > targetVelocity - kVelocityTolerance && leftCurrentVelocity < targetVelocity + kVelocityTolerance) && (rightCurrentVelocity > targetVelocity - kVelocityTolerance && rightCurrentVelocity < targetVelocity + kVelocityTolerance)) {
                return true;
            }
            return false;
    }

    private void initSendable(SendableBuilder builder, SparkFlex motor, String name) {
        builder.addDoubleProperty(name + " RPM", () -> motor.getEncoder().getVelocity(), null);
        builder.addDoubleProperty(name + " Stator Current", () -> motor.getOutputCurrent(), null);
        builder.addDoubleProperty(name + " Supply Current", () -> motor.getAppliedOutput(), null);
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        initSendable(builder, leftMotor, "Left");
        initSendable(builder, rightMotor, "Right");
        builder.addStringProperty("Command", () -> getCurrentCommand() != null ? getCurrentCommand().getName() : "null", null);
        builder.addDoubleProperty("Dashboard RPM", () -> dashboardTargetRPM, value -> dashboardTargetRPM = value);
        // builder.addDoubleProperty("Target RPM", () -> , null);
    }
}
