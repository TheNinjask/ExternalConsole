package pt.theninjask.externalconsole.console.command.util;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleAllCommandConsumerCommand;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.console.command.core.AndCommand;
import pt.theninjask.externalconsole.event.EventHandler;
import pt.theninjask.externalconsole.event.ExternalConsoleClosingEvent;
import pt.theninjask.externalconsole.event.InputCommandExternalConsoleEvent;
import pt.theninjask.externalconsole.util.KeyPressedAdapter;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class MultipleExternalConsoleManagerCommand implements ExternalConsoleAllCommandConsumerCommand {

    private Map<String, ExternalConsoleCommand> allCommands;

    private final ExternalConsole console;

    @Override
    public String getCommand() {
        return "multiple-ec-manager";
    }

    @Override
    public String getDescription() {
        return "This command allows you to manage multiple External Consoles at the same time.";
    }


    @Override
    public int executeCommand(String... args) {
        boolean funMode = args.length > 0 &&
                (args[0].equalsIgnoreCase("--fun") || args[0].equalsIgnoreCase("--fun-circle"));
        boolean circleMode = args.length > 0 &&
                (args[0].equalsIgnoreCase("--circle") || args[0].equalsIgnoreCase("--fun-circle"));
        var it = !funMode && !circleMode ? Arrays.stream(args).iterator() :
                Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).iterator();
        List<Pair<ExternalConsole, InputCommandExternalConsoleEvent>> otherConsoles = new ArrayList<>();
        while (it.hasNext()) {
            var other = console.generateAnotherConsole();
            var cmd = it.next();
            var argSize = Integer.parseInt(it.next());
            var eventArgs = new String[argSize + 1];
            eventArgs[0] = cmd;
            IntStream.range(1, argSize + 1)
                    .forEachOrdered(i -> eventArgs[i] = other.parseArgsVars(it.next())[0]);
            otherConsoles.add(Pair.of(
                    other,
                    new InputCommandExternalConsoleEvent(
                            other,
                            eventArgs
                    )
            ));
        }
        EventHandler.getInstance()
                .registerListener(
                        ExternalConsoleClosingEvent.class,
                        this,
                        e -> {
                            if (e.getOwner() != console)
                                return;
                            otherConsoles.stream()
                                    .map(Pair::getKey)
                                    .forEach(Window::dispose);
                        },
                        0
                );
        console.executeCommand("top", "--true");
        otherConsoles.stream().forEach(p -> {
            var c = p.getKey();
            var cmd = p.getValue();
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            if (funMode && !circleMode) {
                c.setLocationRelativeToCenter(
                        ThreadLocalRandom.current().nextInt(-dim.width / 2, dim.width / 2),
                        ThreadLocalRandom.current().nextInt(-dim.height / 2, dim.height / 2)
                );
            } else if (circleMode) {
                int radius = Math.min(dim.width, dim.height) / 4;
                double angle = 2 * Math.PI * otherConsoles.indexOf(p) / otherConsoles.size();
                c.setLocationRelativeToCenter(
                        (int) (radius * Math.cos(angle)),
                        (int) (radius * Math.sin(angle))
                );

            }
            c.setClosable(true);
            c.setViewable(true);
            c.onCommand(cmd);
        });
        while (true) {
            if (KeyPressedAdapter.isCTRLAndCPressedNative())
                break;
            if (otherConsoles.stream().map(Pair::getKey).noneMatch(ExternalConsole::isDisplayable))
                break;
            // loop until all consoles are closed


            if (circleMode && funMode) {
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                int radius = Math.min(dim.width, dim.height) / 4;
                double time = System.currentTimeMillis() / 1000.0; // Time in seconds

                for (int i = 0; i < otherConsoles.size(); i++) {
                    Pair<ExternalConsole, InputCommandExternalConsoleEvent> p = otherConsoles.get(i);
                    ExternalConsole c = p.getKey();
                    double angle = 2 * Math.PI * i / otherConsoles.size() + time; // Dynamic angle
                    c.setLocationRelativeToCenter(
                            (int) (radius * Math.cos(angle)),
                            (int) (radius * Math.sin(angle))
                    );
                }
            }
        }
        otherConsoles.stream().map(Pair::getKey).forEach(Window::dispose);
        EventHandler.getInstance()
                .unregisterListener(
                        ExternalConsoleClosingEvent.class,
                        this
                );
        return 0;
    }

    @Override
    public String resultMessage(int result) {
        return ExternalConsoleAllCommandConsumerCommand.super.resultMessage(result);
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        if (currArgs.length > 0) {
            if (Set.of("--fun", "--circle", "--fun-circle").stream().anyMatch(currArgs[0]::equals))
                currArgs = Arrays.copyOfRange(currArgs, 1, currArgs.length);
        }
        return AndCommand.getParamOptions(getAllCommands(), number, currArgs);
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }

    @Override
    public boolean isDemo() {
        return true;
    }

    @Override
    public void consumeCommandMapReference(Map<String, ExternalConsoleCommand> allCommands) {
        this.allCommands = allCommands;
    }

    private Map<String, ExternalConsoleCommand> getAllCommands() {
        return Optional.ofNullable(allCommands)
                .orElseGet(Collections::emptyMap);
    }
}
