package pt.theninjask.externalconsole.console.command;

import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

public interface LoadingExternalConsoleCommand {

    ExternalConsoleCommand getCommand(Class<? extends ExternalConsoleCommand> clazz);

}
