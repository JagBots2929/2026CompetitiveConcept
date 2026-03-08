package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Volts;

import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkFlexConfig;

import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Ports;

public class Floor extends SubsystemBase {
    public enum Speed {
        STOP(0),
        FEED(0.83);

        private final double percentOutput;

        private Speed(double percentOutput) {
            this.percentOutput = percentOutput;
        }

        public Voltage voltage() {
            return Volts.of(percentOutput * 12.0);
        }
    }

    private final SparkFlex motor;
    private final SparkFlexConfig config = new SparkFlexConfig();

    public Floor() {
        motor = new SparkFlex(Ports.kFloor, MotorType.kBrushless);
        motor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        SmartDashboard.putData(this);
    }

    public void set(Speed speed) {
        motor.set(speed.percentOutput);
    }

    public Command feedCommand() {
        return startEnd(() -> set(Speed.FEED), () -> set(Speed.STOP));
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addStringProperty("Command", () -> getCurrentCommand() != null ? getCurrentCommand().getName() : "null", null);
        builder.addDoubleProperty("RPM", () -> motor.getEncoder().getVelocity(), null);
        builder.addDoubleProperty("Stator Current", () -> motor.getOutputCurrent(), null);
        builder.addDoubleProperty("Supply Current", () -> motor.getAppliedOutput(), null);
    }
}
