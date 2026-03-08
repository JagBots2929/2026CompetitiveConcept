package frc.robot.subsystems;

import static edu.wpi.first.units.Units.RPM;

import com.revrobotics.spark.SparkFlex;
import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkFlexConfig;

import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.KrakenX60;
import frc.robot.Ports;

public class Feeder extends SubsystemBase {
    public enum Speed {
        FEED(5000);

        private final double rpm;

        private Speed(double rpm) {
            this.rpm = rpm;
        }

        public AngularVelocity angularVelocity() {
            return RPM.of(rpm);
        }
    }

    private final SparkFlex motor;
    private final SparkFlexConfig config = new SparkFlexConfig();

    public Feeder() {
        motor = new SparkFlex(Ports.kFeeder, MotorType.kBrushless);
        motor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        SmartDashboard.putData(this);
    }

    public void set(Speed speed) {
        motor.set(speed.rpm / KrakenX60.kFreeSpeed.in(RPM));
    }

    public void setPercentOutput(double percentOutput) {
        motor.set(percentOutput);
    }

    public Command feedCommand() {
        return startEnd(() -> set(Speed.FEED), () -> setPercentOutput(0));
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addStringProperty("Command", () -> getCurrentCommand() != null ? getCurrentCommand().getName() : "null", null);
        builder.addDoubleProperty("RPM", () -> motor.getEncoder().getVelocity(), null);
        builder.addDoubleProperty("Stator Current", () -> motor.getOutputCurrent(), null);
        builder.addDoubleProperty("Supply Current", () -> motor.getOutputCurrent(), null);
    }
}
