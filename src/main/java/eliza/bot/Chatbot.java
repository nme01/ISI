/**
 * @author <a href="mailto:jacek.witkowski@gmail.com">Jacek Witkowski</a>
 * Created on 2015-02-2015.
 */
package eliza.bot;

import eliza.data.ResponsesGenerator;
import eliza.data.ResponseDesc;
import eliza.io.Communicator;
import eliza.io.DataLoader;
import eliza.io.DatabaseReadException;

import java.io.IOException;

public class Chatbot {

    public static void main(final String[] args)
    {
        try {
            final DataLoader dataLoader = new DataLoader("src/main/resources/eliza/data/rules.json");
            final ResponsesGenerator responsesData = dataLoader.loadResponsesGenerator();
            final Communicator communicator = new Communicator();

            String input;
            ResponseDesc responseDesc;
            do
            {
                input = communicator.read();
                responseDesc = responsesData.getResponse(input);
                communicator.print(responseDesc.getMessage());
            }
            while(!responseDesc.isTerminal());
        } catch (DatabaseReadException|IOException e) {
            e.printStackTrace();
        }
    }
}
