package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Volts;

// import com.revrobotics.spark.SparkFlex;
// import com.revrobotics.spark.SparkBase.ControlType;
// import com.revrobotics.spark.SparkBase.PersistMode;
// import com.revrobotics.spark.SparkBase.ResetMode;
// import com.revrobotics.spark.SparkLowLevel.MotorType;
// import com.revrobotics.spark.config.SparkFlexConfig;

import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.Command.InterruptionBehavior;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

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
    }

    // private final SparkFlex pivotMotor, rollerMotor;
    // private final SparkFlexConfig pivotConfig = new SparkFlexConfig();
    // private final SparkFlexConfig rollerConfig = new SparkFlexConfig();

    private boolean isHomed = false;

    public Intake() {
        // pivotMotor = new SparkFlex(Ports.kIntakePivot, MotorType.kBrushless);
        // rollerMotor = new SparkFlex(Ports.kIntakeRollers, MotorType.kBrushless);
        // configureMotors();
        SmartDashboard.putData(this);
    }

    public void set(Position position) {
        // pivotMotor.getClosedLoopController().setReference(position.degrees, ControlType.kPosition);
    }

    public void set(Speed speed) {
        // rollerMotor.set(speed.percentOutput);
    }

    public Command intakeCommand() {
        return Commands.none();
    }

    public Command agitateCommand() {
        return Commands.none();
    }

    public Command homingCommand() {
        return runOnce(() -> isHomed = true)
            .unless(() -> isHomed)
            .withInterruptBehavior(InterruptionBehavior.kCancelIncoming);
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addStringProperty("Command", () -> getCurrentCommand() != null ? getCurrentCommand().getName() : "null", null);
    }
}
