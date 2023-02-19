package pt.theninjask.externalconsole.console.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class UsedCommand {
    private final String fullCommand;
    @Setter
    private UsedCommand previous;

    @Setter
    private UsedCommand next;


    public static final UsedCommand NULL_UC = new UsedCommand();

    private UsedCommand() {
        this.fullCommand = null;
        this.previous = this;
        this.next = this;
    }

}
