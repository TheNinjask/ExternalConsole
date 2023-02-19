package pt.theninjask.externalconsole.console.util.stream;

import pt.theninjask.externalconsole.console.ExternalConsole;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.io.OutputStream;

public class ExternalConsoleOutputStream extends OutputStream {

    private final ExternalConsole console;

    public ExternalConsoleOutputStream(ExternalConsole console) {
        this.console = console;
    }

    @Override
    public void write(int b) {
        try {
            StyledDocument doc = console._getScreen().getStyledDocument();
            doc.insertString(doc.getLength(), Character.toString(b), null);
            if (console._getAutoScroll())
                console._getScreen().setCaretPosition(doc.getLength());
            if (b == '\n')
                console._clearExtraLines();
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
