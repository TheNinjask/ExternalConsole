package pt.theninjask.externalconsole.console.util.stream;

import pt.theninjask.externalconsole.console.ExternalConsole;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ExternalConsoleOutputStream extends OutputStream {

    private Optional<Integer> offset;

    private final ExternalConsole console;

    private final Map<Character, Rule> characterRuleMap;

    private final Runnable defaultPreInsertPhaseLogic;

    private final Runnable defaultPostInsertPhaseLogic;

    private record Rule(
            ExternalConsoleOutputStream parent,
            boolean allowInsertion,
            Consumer<ExternalConsoleOutputStream> preInsertPhaseLogic,
            Consumer<ExternalConsoleOutputStream> postInsertPhaseLogic
    ) {

        public void preInsertPhase() {
            preInsertPhaseLogic.accept(parent);
        }

        public boolean allowInsertion() {
            return allowInsertion;
        }

        public void postInsertPhase() {
            postInsertPhaseLogic.accept(parent);
        }
    }

    public ExternalConsoleOutputStream(ExternalConsole console) {
        this.console = console;
        this.offset = Optional.empty();
        this.defaultPreInsertPhaseLogic = () -> {
            try {
                StyledDocument doc = console._getScreen().getStyledDocument();
                if (offset.isPresent())
                    doc.remove(offset.get(), 1);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        };
        this.defaultPostInsertPhaseLogic = () -> {
        };
        this.characterRuleMap = Map.ofEntries(
                Map.entry(
                        '\r',
                        new Rule(
                                this,
                                false,
                                (ignore) -> {
                                },
                                (parent) -> {
                                    try {
                                        StyledDocument doc = parent.console._getScreen().getStyledDocument();
                                        String text = doc.getText(0, doc.getLength());
                                        parent.offset = Optional.of(text.lastIndexOf('\n') + 1);
                                    } catch (BadLocationException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        )
                ),
                Map.entry(
                        '\n',
                        new Rule(
                                this,
                                true,
                                (parent) -> parent.offset = Optional.empty(),
                                (parent) -> {
                                    try {
                                        parent.console._clearExtraLines();
                                        parent.offset = Optional.empty();
                                    } catch (BadLocationException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        )
                )
        );
    }

    @Override
    public void write(int b) {
        try {
            Optional<Rule> charRuleOpt = Optional.ofNullable(characterRuleMap.get((char) b));

            StyledDocument doc = console._getScreen().getStyledDocument();

            charRuleOpt.ifPresentOrElse(Rule::preInsertPhase, defaultPreInsertPhaseLogic);

            boolean allowInsertion = charRuleOpt.map(Rule::allowInsertion)
                    .orElse(true);
            if (allowInsertion)
                doc.insertString(offset.orElse(doc.getLength()), Character.toString(b), null);
            offset = offset.map(v -> v + 1);

            charRuleOpt.ifPresentOrElse(Rule::postInsertPhase, defaultPostInsertPhaseLogic);

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
