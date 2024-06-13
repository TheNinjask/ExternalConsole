package pt.theninjask.externalconsole.console.command.core;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static pt.theninjask.externalconsole.util.KeyPressedAdapter.isKeyPressed;

public class SSHProgram implements ExternalConsoleCommand {

    private int hintType = 0;

    private final ExternalConsole console;

    public SSHProgram(ExternalConsole console) {
        this.console = console;
    }

    @Override
    public String getCommand() {
        return "ssh";
    }

    @Override
    public int executeCommand(String... args) {
        Session session = null;
        ChannelShell channel = null;
        try {
            ExternalConsole.executeCommand("cls");
            BufferedReader read = new BufferedReader(new InputStreamReader(console.getInputStream()));
            ExternalConsole.println("EC's SSH 2.7.3 (CTRL+D to exit)");
            console.getOutputStream().write("Username: ".getBytes());
            hintType = 1;
            var username = getInput(read);
            ExternalConsole.println(username);
            console.getOutputStream().write("Password: ".getBytes());
            hintType = 2;
            var password = getInput(read);
            ExternalConsole.println(password.replaceAll(".", "*"));
            console.getOutputStream().write("Host: ".getBytes());
            hintType = 3;
            var host = getInput(read);
            ExternalConsole.println(host);

            console.getOutputStream().write("Port: ".getBytes());
            hintType = 4;
            var port = Integer.parseInt(getInput(read));
            ExternalConsole.println(port);

            hintType = 0;

            session = new JSch().getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelShell) session.openChannel("shell");
            channel.setOutputStream(console.getOutputStream(), true);
            var pipe = new PipedOutputStream();
            channel.setInputStream(new PipedInputStream(pipe));
            channel.connect();
            String input;
            while ((input = getInput(read)) != null) {
                pipe.write("%s\n".formatted(input)
                        .getBytes());
            }

        } catch (HaltException e) {
            // skip
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
            hintType = 0;
        }
        ExternalConsole.executeCommand("cls");
        ExternalConsole.println("Leaving EC's SSH ...");
        return 0;
    }

    private String getInput(BufferedReader read) throws IOException, HaltException {
        while (!(isKeyPressed(KeyEvent.VK_CONTROL) && isKeyPressed(KeyEvent.VK_D))) {
            String str;
            if ((str = read.readLine()) != null)
                return str;
        }
        throw new HaltException();
    }

    private static class HaltException extends Exception {

    }

    public boolean isProgram() {
        return true;
    }

    public boolean isDemo() {
        return true;
    }

    public String[] getParamOptions(int number, String[] currArgs) {
        return switch (hintType) {
            default -> null;
            case 1, 2 -> new String[]{UUID.randomUUID().toString()};
            case 3 -> new String[]{"%s.%s.%s.%s".formatted(
                    ThreadLocalRandom.current().nextInt(0, 256),
                    ThreadLocalRandom.current().nextInt(0, 256),
                    ThreadLocalRandom.current().nextInt(0, 256),
                    ThreadLocalRandom.current().nextInt(0, 256)
            )};
            case 4 -> new String[]{"22"};
        };
    }

    @Override
    public String resultMessage(int result) {
        return switch (result) {
            case -1 -> "An exception has occurred!";
            default -> ExternalConsoleCommand.super.resultMessage(result);
        };
    }

}
