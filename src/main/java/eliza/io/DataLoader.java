/**
 * @author <a href="mailto:jacek.witkowski@gmail.com">Jacek Witkowski</a>
 * Created on 2015-02-2015.
 */
package eliza.io;

import eliza.data.ResponsesGenerator;
import eliza.data.ResponseDesc;
import eliza.data.Rule;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for loading rules needed for generating responses.
 */
public class DataLoader {
    private final String filePath;

    public DataLoader(final String pathToJSONDatabase){
        filePath = pathToJSONDatabase;
    }

    public ResponsesGenerator loadResponsesGenerator() throws DatabaseReadException {
        final JSONParser parser = new JSONParser();

        try (final FileReader reader = new FileReader(filePath)) {

            final Object obj = parser.parse(reader);
            final JSONObject jsonObj = (JSONObject) obj;

            final List<Rule> rulesList = makeRulesList(jsonObj);
            final List<ResponseDesc> defaultResponses = makeDefaultResponsesList(jsonObj);
            final List<ResponseDesc> repeatResponses = makeRepeatResponsesList(jsonObj);
            final Map<String,String> transpositions = makeTranspositionsMap(jsonObj);

            return new ResponsesGenerator(rulesList, defaultResponses, repeatResponses, transpositions);
        } catch (IOException|ParseException e) {
            throw new DatabaseReadException(e);
        }
    }

    private Map<String, String> makeTranspositionsMap(final JSONObject jsonObj) {
        final JSONArray transpositions = (JSONArray) jsonObj.get("transpositions");
        final Map<String, String> transpositionsMap = new HashMap<>(transpositions.size());
        for (Object transpObj : transpositions) {
            JSONObject transpositionJSON = (JSONObject)transpObj;
            final String input = (String)transpositionJSON.get("input");
            final String output = (String)transpositionJSON.get("response");
            transpositionsMap.put(input, output);
        }

        return transpositionsMap;
    }

    private List<ResponseDesc> makeRepeatResponsesList(final JSONObject jsonObj) {
        final List<ResponseDesc> result = new ArrayList<>();
        final JSONArray repeatResponses = (JSONArray) jsonObj.get("repeatResponses");
        for (Object repeatResponse : repeatResponses) {
            result.add(new ResponseDesc(false, (String) repeatResponse));
        }

        return result;
    }

    private List<ResponseDesc> makeDefaultResponsesList(final JSONObject jsonObj) {
        final JSONArray defaultResponsesArray = (JSONArray)jsonObj.get("defaultResponses");
        final List<ResponseDesc> defaultResponses = new ArrayList<>();
        for (Object o : defaultResponsesArray) {
            JSONObject responseDescJson = (JSONObject)o;
            String message = (String)responseDescJson.get("response");
            Boolean terminal = (Boolean)responseDescJson.getOrDefault("terminal",false);
            defaultResponses.add(new ResponseDesc(terminal, message));
        }
        return defaultResponses;
    }

    private List<Rule> makeRulesList(final JSONObject jsonObj) {
        final JSONArray rulesArray = (JSONArray)jsonObj.get("knowledge");
        final List<Rule> rulesList = new ArrayList<>();
        for (Object o : rulesArray) {
            final JSONObject combinedRule = (JSONObject)o;
            final JSONArray inputs = (JSONArray)combinedRule.get("inputs");
            final JSONArray outputs = (JSONArray)combinedRule.get("responses");
            final JSONArray previousResponses = (JSONArray)combinedRule.get("previousResponses");
            final Boolean terminal = (Boolean)combinedRule.getOrDefault("terminal", false);

            if (previousResponses != null && !previousResponses.isEmpty()) {
                for (Object pO : previousResponses) {
                    final String previousResponse = (String)pO;
                    extractSimpleRules(rulesList, inputs, outputs, previousResponse, terminal);
                }
            } else {
                extractSimpleRules(rulesList, inputs, outputs, null, terminal);
            }
        }
        return rulesList;
    }

    private void extractSimpleRules(final List<Rule> rulesList, final JSONArray inputs, final JSONArray outputs,
                                    final String previousResponse, final boolean terminal) {
        for (final Object inp : inputs) {
            final String input = (String)inp;
            for (Object op : outputs) {
                final String output = (String)op;
                final ResponseDesc responseDesc = new ResponseDesc(terminal,output);
                rulesList.add(new Rule(responseDesc, input, previousResponse));
            }
        }
    }
}
