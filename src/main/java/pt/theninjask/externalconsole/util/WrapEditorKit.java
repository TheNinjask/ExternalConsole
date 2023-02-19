package pt.theninjask.externalconsole.util;

import javax.swing.text.*;
import java.io.Serial;

/**
 * sauce <a href="http://www.java2s.com/Tutorials/Java/Swing_How_to/JTextPane/Wrap_long_words_in_JTextPane.htm">...</a>
 */
public class WrapEditorKit extends StyledEditorKit {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    ViewFactory defaultFactory = new WrapColumnFactory();

    public ViewFactory getViewFactory() {
        return defaultFactory;
    }

    private static class WrapColumnFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                switch (kind) {
                    case AbstractDocument.ContentElementName -> {
                        return new WrapLabelView(elem);
                    }
                    case AbstractDocument.ParagraphElementName -> {
                        return new ParagraphView(elem);
                    }
                    case AbstractDocument.SectionElementName -> {
                        return new BoxView(elem, View.Y_AXIS);
                    }
                    case StyleConstants.ComponentElementName -> {
                        return new ComponentView(elem);
                    }
                    case StyleConstants.IconElementName -> {
                        return new IconView(elem);
                    }
                }
            }
            return new LabelView(elem);
        }
    }

    private static class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }

        public float getMinimumSpan(int axis) {
            return switch (axis) {
                case View.X_AXIS -> 0;
                case View.Y_AXIS -> super.getMinimumSpan(axis);
                default -> throw new IllegalArgumentException("Invalid axis: " + axis);
            };
        }
    }
}
