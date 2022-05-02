import pt.theninjask.externalconsole.console.ExternalConsole;

public class Main {

	public static void main(String[] args) {
		ExternalConsole.setViewable(true);
		ExternalConsole.setSystemStreams();
	}

	public static final void test(String[] args) {
		for (String string : args) {
			System.out.println(string);
		}
		System.out.println("//////////////");
		String cmd = "test 1 2 3\"hello world\"4 \"hi sun\" 5";

		cmd = cmd.strip();
		boolean quote = false;
		int argSize = 0;
		String arg = "";
		Object[] link = new Object[2];
		Object[] start = link;
		for (int i = 0; i < cmd.length(); i++) {
			char chary = cmd.charAt(i);
			if (chary == '\"') {
				quote = !quote;
				if (i + 1 == cmd.length()) {
					link[0] = arg;
					argSize++;
				}
			} else if (chary == ' ' && !quote) {
				Object[] tmp = new Object[2];
				link[0] = arg;
				link[1] = tmp;
				link = tmp;
				argSize++;
				arg = "";
			} else if (i + 1 == cmd.length()) {
				link[0] = arg + chary;
				argSize++;
			} else {
				arg += chary;
			}
		}
		String[] argsy = new String[argSize];
		link = start;
		for (int i = 0; i < argSize; i++) {
			argsy[i] = (String) link[0];
			link = (Object[]) link[1];
		}

		System.out.println(args.length == argsy.length);
		if (args.length == argsy.length)
			for (int i = 0; i < args.length; i++) {
				System.out.println(args[i].equals(argsy[i]));
			}

	}

}
