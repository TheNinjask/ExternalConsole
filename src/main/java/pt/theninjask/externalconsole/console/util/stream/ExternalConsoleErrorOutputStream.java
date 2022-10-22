package pt.theninjask.externalconsole.console.util.stream;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import pt.theninjask.externalconsole.console.ExternalConsole;

public class ExternalConsoleErrorOutputStream extends OutputStream {

	private SimpleAttributeSet errorSet;
	private ExternalConsole console;

	public ExternalConsoleErrorOutputStream(ExternalConsole console) {
		this.console = console;
		errorSet = new SimpleAttributeSet();
		StyleConstants.setForeground(errorSet, Color.RED);
	}

	@Override
	public void write(int b) throws IOException {
		try {
			StyledDocument doc = console._getScreen().getStyledDocument();
			doc.insertString(doc.getLength(), Character.toString(b), errorSet);
			if (console._getAutoScroll())
				console._getScreen().setCaretPosition(doc.getLength());
			console._getScroll().repaint();
			console._getScroll().revalidate();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// continue
		}
		/*
		 * console.append(Character.toString(b)); if (autoScroll) try {
		 * console.setCaretPosition(console.getLineStartOffset(console.getLineCount() -
		 * 1)); } catch (BadLocationException e) { } scroll.repaint();
		 * scroll.revalidate();
		 */
	}

}
