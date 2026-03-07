package frc.robot;

import com.ctre.phoenix6.CANBus;

public final class Ports {
    // CAN Buses
    public static final CANBus kRoboRioCANBus = new CANBus("rio");
    public static final CANBus kCANivoreCANBus = new CANBus("main");

    // Talon FX IDs
    public static final int kIntakePivot = 10;
    public static final int kIntakeRollers = 11;
    public static final int kFloor = 12;
    public static final int kFeeder = 13;
    public static final int kShooterLeft = 14;
    // TODO this was removed for Bonney Lake competition // public static final int kShooterMiddle = 15;
    public static final int kShooterRight = 16;
    // TODO this doesn't exist for bonney lake // public static final int kHanger = 18;

    // PWM Ports // TODO don't have these yet for Bonney Lake
    // public static final int kHoodLeftServo = 3;
    // public static final int kHoodRightServo = 4;
}
