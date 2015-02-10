/**
 * @author <a href="mailto:jacek.witkowski@gmail.com">Jacek Witkowski</a>
 * Created on 2015-02-2015.
 */
package eliza.data;

public class Rule {
    private final ResponseDesc responseDesc;
    private final String input;
    private String previousResponse;

    public Rule(final ResponseDesc responseDesc, final String input, final String previousResponse) {
        this.responseDesc = responseDesc;
        this.input = input;
        this.previousResponse = previousResponse;
    }

    public ResponseDesc getResponseDesc() {
        return responseDesc;
    }

    public String getInput() {
        return input;
    }

    public String getPreviousResponse() {
        return previousResponse;
    }
}
