package ua.goit.projectmanagementsystem.controller;

import ua.goit.projectmanagementsystem.controller.command.Command;
import ua.goit.projectmanagementsystem.controller.command.Exit;
import ua.goit.projectmanagementsystem.controller.command.Help;
import ua.goit.projectmanagementsystem.exception.ExitException;
import ua.goit.projectmanagementsystem.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class pmsController {
    private final View view;
    private final List<Command> commands;

    public pmsController(View view, List<Command> commands) {
        this.view = view;
        this.commands = new ArrayList<>(Arrays.asList(
                new Exit(view),
                new Help(view)
        ));
    }

    public void run() {
        view.write("Welcome to project management system");
        executeCommand();
    }

    private void executeCommand() {
        try {
            while (true) {
                view.write("Please, enter help to see available commands");
                String input = view.read();
                boolean isIncorrectCommand = true;
                for (Command command : commands) {
                    if (command.canProccess(input)) {
                        command.process();
                        isIncorrectCommand = false;
                    }
                }
                if (isIncorrectCommand) {
                    view.write("Incorrect command. Please, try again");
                }
            }
        } catch (ExitException e) {
            view.write("Good bye!");
        }
    }
}
