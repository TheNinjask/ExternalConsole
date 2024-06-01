package pt.theninjask.externalconsole.console.command.net;

import org.apache.commons.cli.*;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class CurlyCommand implements ExternalConsoleCommand {

    private final ExternalConsole console;

    private final Map<Option, Supplier<String[]>> optionsMap;

    private final Option urlOpt = Option.builder("u")
            .longOpt("url")
            .desc("Set request's url")
            .required()
            .numberOfArgs(1)
            .build();
    private final Option methodOpt = Option.builder("m")
            .longOpt("method")
            .desc("Set request's method")
            .required()
            .numberOfArgs(1)
            .build();

    private final Option helpOpt = Option.builder("?")
            .longOpt("help")
            .desc("Gets help")
            .hasArg(false)
            .build();

    private final Option idOpt = Option.builder("i")
            .longOpt("id")
            .desc("Associates an ID to the request")
            .numberOfArgs(1)
            .build();

    private final Option bodyOpt = Option.builder("b")
            .longOpt("body")
            .desc("Set request's body path")
            .numberOfArgs(1)
            .build();

    private final Option headersOpt = Option.builder("h")
            .longOpt("headers")
            .desc("Set request's headers (comes in pairs)")
            .hasArgs()
            .build();

    public CurlyCommand(ExternalConsole console) {
        this.console = console;
        this.optionsMap = Map
                .ofEntries(
                        Map.entry(
                                urlOpt,
                                () -> new String[]{"http://", "https://"}
                        ),
                        Map.entry(
                                methodOpt,
                                () -> new String[]{"GET", "PUT", "POST", "DELETE", "PATCH"}
                        ),
                        Map.entry(
                                helpOpt,
                                () -> null
                        ),
                        Map.entry(
                                idOpt,
                                () -> new String[]{UUID.randomUUID().toString()}
                        ),
                        Map.entry(
                                bodyOpt,
                                () -> null
                        ),
                        Map.entry(
                                headersOpt,
                                () -> new String[]{"key", "value"}
                        )
                );
    }

    private void printHelp() {
        Options options = new Options();
        optionsMap.keySet().forEach(options::addOption);
        new HelpFormatter().printHelp(new PrintWriter(console.getOutputStream(), true), HelpFormatter.DEFAULT_WIDTH,
                this.getCommand(), null, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null,
                true);
    }

    @Override
    public String getCommand() {
        return "curly";
    }

    @Override
    public String getDescription() {
        return "External Console version of curl";
    }

    @Override
    public int executeCommand(String... args) {
        Options options = new Options();
        optionsMap.keySet().forEach(options::addOption);
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption(helpOpt.getOpt())) {
                printHelp();
                return 0;
            }

            var preRequest = HttpRequest.newBuilder()
                    .uri(new URI(cmd.getOptionValue(urlOpt.getOpt())))
                    .method(cmd.getOptionValue(methodOpt.getOpt()),
                            cmd.hasOption(bodyOpt.getOpt()) ?
                                    HttpRequest.BodyPublishers.ofString(
                                            new String(
                                                    Files.readAllBytes(
                                                            Paths.get(
                                                                    cmd.getOptionValue(bodyOpt.getOpt())
                                                            )))
                                    ) :
                                    HttpRequest.BodyPublishers.noBody());
            if (cmd.hasOption(headersOpt.getOpt())) {
                preRequest = preRequest.headers(cmd.getOptionValues(headersOpt.getOpt()));
            }
            var request = preRequest.build();
            var response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            ExternalConsole.println(response.body());
            ExternalConsole.triggerEvent(
                    new CurlyResponseEvent(
                            cmd.getOptionValue(idOpt.getOpt()),
                            cmd.getOptionValue(urlOpt.getOpt()),
                            cmd.getOptionValue(methodOpt.getOpt()),
                            response.statusCode(),
                            response.body()
                    )
            );
        } catch (ParseException e) {
            ExternalConsole.println(e.getMessage());
            printHelp();
            return -1;
        } catch (Exception e) {
            ExternalConsole.println(e.getMessage());
            return -2;
        }
        return 0;
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        if (number == 0 || currArgs.length < number)
            return optionsMap.keySet()
                    .stream()
                    .map(o -> String.format("--%s", o.getLongOpt()))
                    .toList()
                    .toArray(new String[optionsMap.size()]);
        var lastArg = currArgs[number - 1];
        var finalLastArg = lastArg.replaceAll("^--", "");
        return optionsMap.entrySet()
                .stream()
                .filter(e -> e.getKey().getOpt().equals(finalLastArg) || e.getKey().getLongOpt().equals(finalLastArg))
                .findFirst()
                .map(e -> e.getValue().get())
                .orElse(optionsMap.keySet()
                        .stream()
                        .map(o -> String.format("--%s", o.getLongOpt()))
                        .toList()
                        .toArray(new String[optionsMap.size()]));
    }

}
