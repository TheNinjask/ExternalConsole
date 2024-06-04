package pt.theninjask.externalconsole.console.command.net;

import pt.theninjask.externalconsole.event.BasicEvent;

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

    public String getId() {
        return this.id;
    }

    public String getUrl() {
        return this.url;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getBody() {
        return this.body;
    }
}
