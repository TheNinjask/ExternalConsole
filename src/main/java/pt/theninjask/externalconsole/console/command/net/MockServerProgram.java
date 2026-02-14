package pt.theninjask.externalconsole.console.command.net;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.Builder;
import lombok.Getter;
import net.engio.mbassy.listener.Handler;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.python.google.common.net.MediaType;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.event.ExternalConsoleClosingEvent;
import pt.theninjask.externalconsole.util.KeyPressedAdapter;

import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static pt.theninjask.externalconsole.util.KeyPressedAdapter.isKeyPressed;

public class MockServerProgram implements ExternalConsoleCommand {

    private static ExternalConsole console;

    private static HintType hintType = HintType.NONE;

    private String[] contentTypes = null;

    private final Map<Option, Supplier<String[]>> optionsMap;

    private final Option helpOpt = Option.builder("?")
            .longOpt("help")
            .desc("Gets help")
            .hasArg(false)
            .build();

    private final Option urlOpt = Option.builder("u")
            .longOpt("url")
            .desc("Set url to redirect")
            .numberOfArgs(1)
            .build();


    private final Option portOpt = Option.builder("p")
            .longOpt("port")
            .desc("Set port to open")
            .numberOfArgs(1)
            .build();

    private final Option regexOpt = Option.builder("r")
            .longOpt("regex")
            .desc("Set regex to match path for mocking")
            .numberOfArgs(1)
            .build();

    private final Option literalOpt = Option.builder("l")
            .longOpt("literal")
            .desc("Set literal to match path for mocking")
            .numberOfArgs(1)
            .build();

    private final Option mockUrlOpt = Option.builder("m")
            .longOpt("mock-url")
            .desc("Set endpoint for doing mocking")
            .numberOfArgs(1)
            .build();

    private final Option overrideTokenOpt = Option.builder("o")
            .longOpt("override-token")
            .desc("Override the token used for url when redirecting")
            .numberOfArgs(1)
            .build();

    private final Option overrideCookieOpt = Option.builder("c")
            .longOpt("override-cookie")
            .desc("Override the cookie used for url when redirecting")
            .numberOfArgs(1)
            .build();
    private static HttpServer server = null;


    public MockServerProgram(ExternalConsole console) {
        ExternalConsole.registerEventListener(new Object() {
            @Handler
            public void onClose(ExternalConsoleClosingEvent event) {
                Optional.ofNullable(server)
                        .ifPresent(svr -> svr.stop(0));
            }
        });
        contentTypes = Arrays.stream(MediaType.class.getDeclaredFields())
                .filter(field -> {
                    if (!field.getDeclaringClass().isAssignableFrom(MediaType.class))
                        return false;
                    int modifiers = field.getModifiers();
                    return Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && Modifier.isPublic(modifiers);
                })
                .map(field -> {
                    try {
                        return field.get(null);
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(MediaType.class::cast)
                .map(MediaType::toString)
                .map(type -> {
                    if (type.startsWith("\"") && type.endsWith("\"")) {
                        return type.substring(1, type.length() - 1);
                    }
                    return type;
                })
                .toArray(String[]::new);

        MockServerProgram.console = console;
        this.optionsMap = Map
                .ofEntries(
                        Map.entry(
                                urlOpt,
                                () -> new String[]{"http://", "https://", "https://serebii.net", "https://github.com/TheNinjask/ExternalConsole"}
                        ),
                        Map.entry(
                                portOpt,
                                () -> IntStream.range(8080, 8089).sorted().mapToObj(Integer::toString).toArray(String[]::new)
                        ),
                        Map.entry(
                                helpOpt,
                                () -> null
                        ),
                        Map.entry(
                                regexOpt,
                                () -> null
                        ),
                        Map.entry(
                                literalOpt,
                                () -> null
                        ),
                        Map.entry(
                                mockUrlOpt,
                                () -> new String[]{"http://", "https://", "https://serebii.net", "https://github.com/TheNinjask/ExternalConsole"}
                        ),
                        Map.entry(
                                overrideTokenOpt,
                                () -> new String[]{"Bearer XYZ"}
                        ),
                        Map.entry(
                                overrideCookieOpt,
                                () -> new String[]{"cookie=pookie"}
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
        return "mockserver";
    }

    @Override
    public String getDescription() {
        return "Simple program to help mock requests to a server";
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
            int port = Optional.of(portOpt)
                    .map(Option::getOpt)
                    .map(cmd::getOptionValue)
                    .map(Integer::parseInt)
                    .orElse(8080);
            MockHandlerArgs mockHandlerArgs = getMockHandlerArgs(cmd);
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new MockHandler(mockHandlerArgs));
            server.setExecutor(null); // Default executor
            ExternalConsole.setClosable(false);
            ExternalConsole.println("Mock Server Activated (CTRL+C to stop)");
            server.start();
            while ((!KeyPressedAdapter.isKeyPressed(
                    KeyEvent.VK_CONTROL
            ) || !KeyPressedAdapter.isKeyPressed(
                    KeyEvent.VK_C
            ) && server != null)) {
                // Program Loop
            }
            if (server != null) {
                server.stop(0);
                server = null;
                ExternalConsole.println("Mock Server Deactivated");
            }
            ExternalConsole.setClosable(true);
            return 0;
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e instanceof NoSuchElementException) {
                msg = "--url is required (%s)".formatted(msg);
            }
            ExternalConsole.println(msg);
            ExternalConsole.setClosable(true);
            return -1;
        }
    }

    private MockHandlerArgs getMockHandlerArgs(CommandLine cmd) {
        String url = Optional.of(urlOpt)
                .map(Option::getOpt)
                .map(cmd::getOptionValue)
                .orElseThrow();
        String literal = Optional.of(literalOpt)
                .map(Option::getOpt)
                .map(cmd::getOptionValue)
                .orElse(null);
        String regex = Optional.of(regexOpt)
                .map(Option::getOpt)
                .map(cmd::getOptionValue)
                .orElse(null);
        String mockUrl = Optional.of(mockUrlOpt)
                .map(Option::getOpt)
                .map(cmd::getOptionValue)
                .orElse(null);
        String overrideToken = Optional.of(overrideTokenOpt)
                .map(Option::getOpt)
                .map(cmd::getOptionValue)
                .orElse(null);
        String overrideCookie = Optional.of(overrideCookieOpt)
                .map(Option::getOpt)
                .map(cmd::getOptionValue)
                .orElse(null);
        MockHandlerArgs mockHandlerArgs = MockHandlerArgs.builder()
                .url(url)
                .literal(literal)
                .regex(regex)
                .mockUrl(mockUrl)
                .overrideToken(overrideToken)
                .overrideCookie(overrideCookie)
                .build();
        return mockHandlerArgs;
    }

    @Override
    public boolean isProgram() {
        return true;
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        if (server == null) {
            if (number == 0 || currArgs.length < number) {
                return optionsMap.keySet()
                        .stream()
                        .map(o -> String.format("--%s", o.getLongOpt()))
                        .toList()
                        .toArray(new String[optionsMap.size()]);
            }
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
        } else {
            return switch (hintType) {
                case STATUS_CODE -> new String[]{
                        "200", "204", "400", "403", "404", "500", "503", "504"
                };
                case HAS_BODY -> new String[]{"true", "false"};
                case BODY -> Paths.get(".").toFile().list((dir, name) -> new File(dir, name).isFile());
                case CONTENT_TYPE -> contentTypes;
                default -> null;
            };
        }
    }

    @Override
    public String resultMessage(int result) {
        return switch (result) {
            case -1 -> "An exception has occurred!";
            default -> ExternalConsoleCommand.super.resultMessage(result);
        };
    }

    @Getter
    @Builder
    static class MockHandlerArgs {
        private String url;
        private String literal;
        private String regex;
        private String mockUrl;
        private String overrideToken;
        private String overrideCookie;
    }

    // Custom handler that catches ALL requests to any path
    static class MockHandler implements HttpHandler {

        private final String url;
        private final String literal;
        private final Pattern regex;

        private final String mockUrl;

        private final String overrideToken;

        private final String overrideCookie;

        private final static Supplier<Boolean> DEFAULT_INPUT_LOOP =
                () -> !(isKeyPressed(KeyEvent.VK_CONTROL) && isKeyPressed(KeyEvent.VK_C));

        public MockHandler(
                MockHandlerArgs args
        ) {
            this.url = args.getUrl();
            this.literal = args.getLiteral();
            this.regex = args.getRegex() == null ? null : Pattern.compile(args.getRegex());
            this.mockUrl = args.getMockUrl();
            this.overrideToken = args.getOverrideToken();
            this.overrideCookie = args.getOverrideCookie();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    handleOptions(exchange);
                    return;
                }
                if (isToMock(exchange)) {
                    if (mockUrl == null)
                        handleMock(exchange);
                    else
                        handleRedirect(exchange, mockUrl);
                } else {
                    handleRedirect(exchange, url);
                }


            } catch (IOException | URISyntaxException | InterruptedException e) {
                ExternalConsole.println(ExceptionUtils.getStackTrace(e));
                throw new IOException(e);
            }
        }

        private void handleOptions(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
            exchange.sendResponseHeaders(200, -1);
        }

        private boolean isToMock(HttpExchange exchange) {
            if (literal == null && regex == null) {
                return false;
            }
            String path = exchange.getRequestURI().getPath();
            if (literal != null && Objects.equals(literal, path)) {
                return true;
            }
            if (regex != null && regex.matcher(path).matches()) {
                return true;
            }
            return false;
        }

        private void oldHandleMock(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();

            String response = String.format(
                    "Method: %s\nPath: %s\nQuery: %s\n",
                    method,
                    path,
                    (query != null ? query : "No query parameters")
            );

            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void handleMock(HttpExchange exchange) throws IOException {

            ExternalConsole.println("<=>");
            ExternalConsole.println("Caught Request to mock: (%s) %s".formatted(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath()));

            BufferedReader read = new BufferedReader(new InputStreamReader(console.getInputStream()));

            hintType = HintType.STATUS_CODE;
            console.getOutputStream().write("Status Code: ".getBytes());
            var statusCode = Integer.parseInt(getInput(read));
            ExternalConsole.println(statusCode);

            hintType = HintType.CONTENT_TYPE;
            console.getOutputStream().write("Content-Type: ".getBytes());
            var contentType = getInput(read);
            var contentTypeParsed = console.inputToArgs(contentType);
            contentType = contentTypeParsed.length > 0 ? contentTypeParsed[0] : contentType;
            ExternalConsole.println(contentType);

            hintType = HintType.HAS_BODY;
            console.getOutputStream().write("Has Body: ".getBytes());
            var hasBody = Boolean.parseBoolean(getInput(read));
            ExternalConsole.println(hasBody);
            byte[] body = null;
            if (hasBody) {
                hintType = HintType.BODY;
                console.getOutputStream().write("Body Path: ".getBytes());
                var bodyPath = getInput(read);
                var bodyPathParsed = console.inputToArgs(bodyPath);
                bodyPath = bodyPathParsed.length > 0 ? bodyPathParsed[0] : bodyPath;
                ExternalConsole.println(bodyPath);
                body = Files.readAllBytes(
                        Paths.get(bodyPath)
                );
            }

            exchange.getResponseHeaders().put(
                    "Content-Type",
                    Collections.singletonList(contentType)
            );
            exchange.sendResponseHeaders(statusCode,
                    hasBody ? body.length : 0
            );
            OutputStream os = exchange.getResponseBody();
            if (hasBody) {
                os.write(body);
            }
            os.close();
            hintType = HintType.NONE;
            ExternalConsole.println("<=>");
            ExternalConsole.println();
        }


        private void handleRedirect(
                HttpExchange exchange,
                String targetHost
        ) throws IOException, URISyntaxException, InterruptedException {
            String targetUrl = targetHost + exchange.getRequestURI();
            ExternalConsole.println("Forwarding request to: " + targetUrl);

            URL url = new URL(targetUrl);
            var hasBody = List.of(
                    "POST",
                    "PUT",
                    "PATCH"
            ).contains(exchange.getRequestMethod().toUpperCase());

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(url.toURI())
                    .method(exchange.getRequestMethod(),
                            hasBody ? HttpRequest.BodyPublishers.ofByteArray(exchange.getRequestBody().readAllBytes())
                                    : HttpRequest.BodyPublishers.noBody()
                    );

            exchange.getRequestHeaders()
                    .forEach((key, values) -> {
                        if (List.of("connection", "host", "content-length")
                                .contains(key.toLowerCase()))
                            return;
                        if (Objects.equals(
                                "authorization",
                                key.toLowerCase()) && overrideToken != null
                        ) {
                            requestBuilder.headers(
                                    Stream.of(key, overrideToken)
                                            .toArray(String[]::new)
                            );
                        }
                        requestBuilder.headers(
                                Stream.concat(
                                                Stream.of(key),
                                                values.stream())
                                        .toArray(String[]::new)
                        );
                    });

            HttpResponse<byte[]> response = HttpClient.newHttpClient()
                    .send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());

            int responseCode = response.statusCode();

            byte[] responseBody = response.body();

            var responseHeaders = new HashMap<>(response.headers().map());
            responseHeaders.keySet()
                    .stream()
                    .filter("Access-Control-Allow-Origin"::equalsIgnoreCase)
                    .findFirst()
                    .ifPresent(responseHeaders::remove);

            exchange.getResponseHeaders().putAll(responseHeaders);
            exchange.getResponseHeaders().remove(null);
            boolean isChunked = responseHeaders.entrySet()
                    .stream()
                    .filter(entry -> "Transfer-Encoding".equalsIgnoreCase(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(Objects::nonNull)
                    .map(String::toLowerCase)
                    .anyMatch(s -> Objects.equals(s, "chunked"));

            exchange.sendResponseHeaders(responseCode,
                    responseCode == 204 ? -1 :
                            isChunked ? 0 : responseBody.length);

            OutputStream os = exchange.getResponseBody();
            if (responseBody.length > 0)
                os.write(responseBody);
            os.close();
        }

        private String getInput(BufferedReader read) throws IOException {
            return getInput(read, DEFAULT_INPUT_LOOP);
        }

        private String getInput(BufferedReader read, Supplier<Boolean> inputLoop) throws IOException {
            while (inputLoop.get()) {
                String str;
                if ((str = read.readLine()) != null)
                    return str;
            }
            throw new RuntimeException();
        }
    }

    private enum HintType {
        STATUS_CODE,
        HAS_BODY,
        BODY,
        CONTENT_TYPE,
        NONE;
    }

}
