package frc.robot.subsystems;

import static edu.wpi.first.units.Units.RPM;

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

public class Shooter extends SubsystemBase {
    private static final double kVelocityTolerance = 100;

    private final SparkFlex leftMotor, rightMotor;
    private final List<SparkFlex> motors;
    private final SparkFlexConfig leftConfig = new SparkFlexConfig();
    private final SparkFlexConfig rightConfig = new SparkFlexConfig();

    private double dashboardTargetRPM = 0.0;

    public Shooter() {
        leftMotor = new SparkFlex(Ports.kShooterLeft, MotorType.kBrushless);
        rightMotor = new SparkFlex(Ports.kShooterRight, MotorType.kBrushless);
        motors = List.of(leftMotor, rightMotor);
        configureMotor();
        SmartDashboard.putData(this);
    }

    private void configureMotor() {
        leftConfig.inverted(false);
        rightConfig.inverted(false);

        leftMotor.configure(leftConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        rightMotor.configure(rightConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    }

    public void setRPM(double rpm) {
        for (final SparkFlex motor : motors) {
            motor.set(rpm / KrakenX60.kFreeSpeed.in(RPM));
        }
    }

    public void setPercentOutput(double percentOutput) {
        for (final SparkFlex motor : motors) {
            motor.set(percentOutput);
        }
    }

    public void stop() {
        setPercentOutput(0.0);
    }

    public Command spinUpCommand(double rpm) {
        return runOnce(() -> setRPM(rpm))
            .andThen(Commands.waitUntil(() -> isVelocityWithinTolerance(rpm)));
    }

    public Command dashboardSpinUpCommand() {
        return defer(() -> spinUpCommand(dashboardTargetRPM));
    }

    public boolean isVelocityWithinTolerance(double targetRPM) {
        final double leftCurrentVelocity = leftMotor.getEncoder().getVelocity();
        final double rightCurrentVelocity = rightMotor.getEncoder().getVelocity();
        return (leftCurrentVelocity > targetRPM - kVelocityTolerance && leftCurrentVelocity < targetRPM + kVelocityTolerance)
            && (rightCurrentVelocity > targetRPM - kVelocityTolerance && rightCurrentVelocity < targetRPM + kVelocityTolerance);
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
    }
}
