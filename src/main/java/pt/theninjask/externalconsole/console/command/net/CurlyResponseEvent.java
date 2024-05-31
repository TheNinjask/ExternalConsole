package pt.theninjask.externalconsole.console.command.net;

import lombok.Getter;
import pt.theninjask.externalconsole.event.BasicEvent;

@Getter
public class CurlyResponseEvent extends BasicEvent {

    private final String id;
    private final String url;
    private final String httpMethod;
    private final int statusCode;
    private final String body;

    public CurlyResponseEvent(String id, String url, String httpMethod, int statusCode, String body) {
        super(CurlyResponseEvent.class.getSimpleName(), false);
        this.id = id;
        this.url = url;
        this.httpMethod = httpMethod;
        this.statusCode = statusCode;
        this.body = body;
    }

}
