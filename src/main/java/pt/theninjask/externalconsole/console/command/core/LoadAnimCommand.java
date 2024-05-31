package pt.theninjask.externalconsole.console.command.core;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.console.util.LoadingProcess;

import javax.swing.text.StyledDocument;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class LoadAnimCommand implements ExternalConsoleCommand {

    private final ExternalConsole console;

    @Override
    public String getCommand() {
        return "load_anim";
    }

    @Override
    public String getDescription() {
        return "Shows an animation for a certain time (default: 5s)";
    }

    @Override
    public int executeCommand(String... args) {
        int animation = -1;
        switch (args.length) {
            default:
            case 1:
                if (StringUtils.isNumeric(args[0]))
                    animation = Integer.parseInt(args[0]);
            case 0:
                break;
        }
        try {
            Thread proc = new Thread(() -> {
                try {
                    int animTime = 5;
                    if (args.length > 1 && StringUtils.isNumeric(args[1]))
                        animTime = Integer.parseInt(args[1]);
                    Thread.sleep(animTime * 1000L);
                } catch (InterruptedException ignored) {
                }
            });
            proc.start();
            animation = animation < 0 || animation >= console._getLoadings().length
                    ? ThreadLocalRandom.current().nextInt(console._getLoadings().length)
                    : animation;
            Object[] loading = console._getLoadings()[animation];
            int i = 3;
            StyledDocument doc = console._getScreen().getStyledDocument();
            int offset = doc.getLength();
            while (proc.isAlive()) {
                String msg = String.format("Processing %s", loading[i]);
                doc.insertString(offset, msg, null);
                proc.join((int) loading[1]);
                i = ((LoadingProcess) loading[0]).nextLoading(i, loading);
                doc.remove(offset, msg.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        return switch (number) {
            case 1 -> IntStream.range(0, 15)
                    .mapToObj(Integer::toString)
                    .toArray(String[]::new);
            case 0 -> IntStream.range(0, console._getLoadings().length)
                    .mapToObj(Integer::toString)
                    .toArray(String[]::new);
            default -> null;
        };
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }

}
