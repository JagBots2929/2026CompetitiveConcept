package frc.robot.subsystems;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class HoodStub extends SubsystemBase {

    public HoodStub() {
        
    }

    /** Expects a position between 0.0 and 1.0 */
    public void setPosition(double position) {
        
    }

    /** Expects a position between 0.0 and 1.0 */
    public Command positionCommand(double position) {
        return Commands.none();
    }

    public boolean isPositionWithinTolerance() {
        return true;
    }

    private void updateCurrentPosition() {

    }

    @Override
    public void periodic() {
        updateCurrentPosition();
    }

    @Override
    public void initSendable(SendableBuilder builder) {

    }
}
