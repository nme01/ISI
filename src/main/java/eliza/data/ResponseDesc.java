/**
 * @author <a href="mailto:jacek.witkowski@gmail.com">Jacek Witkowski</a>
 * Created on 2015-02-2015.
 */
package eliza.data;

/**
 * TODO komentarz
 */
public class ResponseDesc {
    private final boolean terminal;
    private String message;

    public ResponseDesc(final boolean terminal, final String message) {
        this.terminal = terminal;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isTerminal() {
        return terminal;
    }
}
