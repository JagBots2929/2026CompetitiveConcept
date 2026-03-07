// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import static frc.robot.generated.ChoreoTraj.OutpostAndDepotTrajectory$0;
import static frc.robot.generated.ChoreoTraj.OutpostAndDepotTrajectory$1;
import static frc.robot.generated.ChoreoTraj.OutpostAndDepotTrajectory$2;
import static frc.robot.generated.ChoreoTraj.OutpostAndDepotTrajectory$3;

import choreo.auto.AutoChooser;
import choreo.auto.AutoFactory;
import choreo.auto.AutoRoutine;
import choreo.auto.AutoTrajectory;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;
import frc.robot.subsystems.Feeder;
import frc.robot.subsystems.Floor;
import frc.robot.subsystems.HoodStub;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Limelight;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Swerve;

public final class AutoRoutines {
    private final Swerve swerve;
    private final Intake intake;
    private final Floor floor;
    private final Feeder feeder;
    private final Shooter shooter;
    private final HoodStub hood;
    private final Limelight limelight;

    private final SubsystemCommands subsystemCommands;

    private final AutoFactory autoFactory;
    private final AutoChooser autoChooser;

    public AutoRoutines(
        Swerve swerve,
        Intake intake,
        Floor floor,
        Feeder feeder,
        Shooter shooter,
        HoodStub hood,
        Limelight limelight
    ) {
        this.swerve = swerve;
        this.intake = intake;
        this.floor = floor;
        this.feeder = feeder;
        this.shooter = shooter;
        this.hood = hood;
        this.limelight = limelight;

        this.subsystemCommands = new SubsystemCommands(swerve, intake, floor, feeder, shooter, hood);

        this.autoFactory = swerve.createAutoFactory();
        this.autoChooser = new AutoChooser();
    }

    public void configure() {
        autoChooser.addRoutine("Outpost and Depot", this::outpostAndDepotRoutine);
        autoChooser.addRoutine("None", () -> autoFactory.newRoutine("None"));
        autoChooser.addRoutine("Shoot In Place", this::shootInPlace);
        SmartDashboard.putData("Auto Chooser", autoChooser);
        RobotModeTriggers.autonomous().whileTrue(autoChooser.selectedCommandScheduler());
    }

    private AutoRoutine outpostAndDepotRoutine() {
        final AutoRoutine routine = autoFactory.newRoutine("Outpost and Depot");
        final AutoTrajectory startToOutpost = OutpostAndDepotTrajectory$0.asAutoTraj(routine);
        final AutoTrajectory outpostToDepot = OutpostAndDepotTrajectory$1.asAutoTraj(routine);
        final AutoTrajectory depotToShootingPose = OutpostAndDepotTrajectory$2.asAutoTraj(routine);

        routine.active().onTrue(
            Commands.sequence(
                startToOutpost.resetOdometry(),
                startToOutpost.cmd()
            )
        );

        startToOutpost.doneDelayed(1).onTrue(outpostToDepot.cmd());

        outpostToDepot.atTimeBeforeEnd(1).onTrue(intake.intakeCommand());
        outpostToDepot.doneDelayed(0.1).onTrue(depotToShootingPose.cmd());

        depotToShootingPose.active().whileTrue(limelight.idle());
        depotToShootingPose.atTime(0.5).onTrue(
            Commands.parallel(
                shooter.spinUpCommand(2600), // using this as the setpoint for shooter commands
                hood.positionCommand(0.32)
            )
        );
        depotToShootingPose.done().onTrue(
            Commands.sequence(
                subsystemCommands.aimAndShoot(2600)
                    .withTimeout(5)
            )
        );

        return routine;
    }

    private AutoRoutine shootInPlace() {
        final AutoRoutine routine = autoFactory.newRoutine("Shoot In Place");

        Commands.runOnce(() -> subsystemCommands.aimAndShoot().withTimeout(5), shooter, floor, feeder);

        return routine;
    }
}
