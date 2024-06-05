package pt.theninjask.externalconsole.console.component;

import pt.theninjask.externalconsole.util.WrapEditorKit;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyledDocument;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.Serial;

public class ScreenConsole extends JScrollPane {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int LINE_LIMIT = 493;
    private final JTextPane screen;
    private boolean autoScroll;

    public ScreenConsole() {
        super();
        this.autoScroll = true;
        screen = new JTextPane();
        screen.setEditorKit(new WrapEditorKit());
        screen.setEditable(false);
        DefaultCaret caret = (DefaultCaret) screen.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        this.setViewportView(screen);
        this.setFocusable(false);
        this.setEnabled(false);
        this.setBorder(null);
        this.setWheelScrollingEnabled(true);
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        this.setOpaque(false);
        this.getViewport().setOpaque(false);
        screen.setOpaque(false);

        JScrollPane scroll = this;
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
            }
        });
    }

    public JTextPane getScreen() {
        return screen;
    }

    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    public boolean getAutoScroll() {
        return autoScroll;
    }

    public void println(String msg) {
        try {
            StyledDocument doc = screen.getStyledDocument();
            doc.insertString(doc.getLength(), String.format("%s\n", msg), null);
            if (autoScroll)
                screen.setCaretPosition(doc.getLength());
            // singleton.console.append(String.format("%s\n", msg));
            _clearExtraLines();
            this.repaint();
            this.revalidate();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void _clearExtraLines() throws BadLocationException {
        StyledDocument doc = screen.getStyledDocument();
        String text = doc.getText(0, doc.getLength());
        long lines = text.chars().filter(c -> c == '\n').count();
        for (; lines > LINE_LIMIT; lines--) {
            doc.remove(0, doc.getText(0, doc.getLength()).indexOf('\n') + 1);
        }
    }

}
