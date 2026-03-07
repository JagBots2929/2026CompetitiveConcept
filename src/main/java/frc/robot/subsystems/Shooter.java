package frc.robot.subsystems;

// import java.util.List;

// import com.revrobotics.spark.SparkFlex;
// import com.revrobotics.spark.SparkBase.PersistMode;
// import com.revrobotics.spark.SparkBase.ResetMode;
// import com.revrobotics.spark.SparkLowLevel.MotorType;
// import com.revrobotics.spark.config.SparkFlexConfig;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Ports;

public class Shooter extends SubsystemBase {
    // private final SparkFlex leftMotor, rightMotor;
    // private final List<SparkFlex> motors;
    // private final SparkFlexConfig leftConfig = new SparkFlexConfig();
    // private final SparkFlexConfig rightConfig = new SparkFlexConfig();

    private double dashboardTargetRPM = 0.0;

    public Shooter() {
        // leftMotor = new SparkFlex(Ports.kShooterLeft, MotorType.kBrushless);
        // rightMotor = new SparkFlex(Ports.kShooterRight, MotorType.kBrushless);
        // motors = List.of(leftMotor, rightMotor);
        // configureMotor();
        SmartDashboard.putData(this);
    }

    private void configureMotor() {
        // leftConfig.inverted(false);
        // rightConfig.inverted(false);
        // leftMotor.configure(leftConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        // rightMotor.configure(rightConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    }

    public void setRPM(double rpm) {
        // motor.set(rpm / KrakenX60.kFreeSpeed.in(RPM));
    }

    public void setPercentOutput(double percentOutput) {
        // motor.set(percentOutput);
    }

    public void stop() {
        setPercentOutput(0.0);
    }

    public Command spinUpCommand(double rpm) {
        return Commands.none();
    }

    public Command dashboardSpinUpCommand() {
        return Commands.none();
    }

    public boolean isVelocityWithinTolerance(double targetRPM) {
        return true;
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addStringProperty("Command", () -> getCurrentCommand() != null ? getCurrentCommand().getName() : "null", null);
        builder.addDoubleProperty("Dashboard RPM", () -> dashboardTargetRPM, value -> dashboardTargetRPM = value);
    }
}
