package frc.robot;

import com.ctre.phoenix6.CANBus;

public final class Ports {
    // CAN Buses
    public static final CANBus kRoboRioCANBus = new CANBus("rio");
    public static final CANBus kCANivoreCANBus = new CANBus("main");

    // Talon FX IDs
    public static final int kIntakePivot = 15;
    public static final int kIntakeRollers = 16;
    public static final int kFloor = 17;
    public static final int kFeeder = 18;
    public static final int kShooterLeft = 19;
    // TODO this was removed for Bonney Lake competition // public static final int kShooterMiddle = 15;
    public static final int kShooterRight = 20;
    // TODO this doesn't exist for bonney lake // public static final int kHanger = 18;

    // PWM Ports // TODO don't have these yet for Bonney Lake
    // public static final int kHoodLeftServo = 3;
    // public static final int kHoodRightServo = 4;
}
