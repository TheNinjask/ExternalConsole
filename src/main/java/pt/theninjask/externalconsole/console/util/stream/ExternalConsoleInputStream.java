package pt.theninjask.externalconsole.console.util.stream;

import java.io.InputStream;

public class ExternalConsoleInputStream extends InputStream {

    private Object[] contents = {new byte[0], null};

    private Object[] tail = contents;

    private int pointer = 0;

    @Override
    public int read() {
        if (pointer >= ((byte[]) contents[0]).length)
            if (contents[1] == null)
                return -1;
            else {
                contents = (Object[]) contents[1];
                pointer = 0;
            }
        return ((byte[]) contents[0])[pointer++];
    }

    public void insertData(byte[] data) {
        Object[] newTail = new Object[]{data, null};
        tail[1] = newTail;
        tail = newTail;
    }

    public void consumeAll() {
        contents = new Object[]{new byte[0], null};
        tail = contents;
        pointer = 0;
    }

}
