/**
 * @author <a href="mailto:jacek.witkowski@gmail.com">Jacek Witkowski</a>
 * Created on 2015-02-2015.
 */
package eliza.data;

import com.google.common.collect.Lists;

import java.util.*;

/**
 * Class for retrieving responses learned by bot.
 */
public class ResponsesGenerator {
    private final List<Rule> rulesList;
    private final Random randomGenerator;
    private final List<ResponseDesc> defaultResponseDescriptors;
    private final List<ResponseDesc> repeatResponseDescriptors;
    private final Map<String, String> transpositions;

    private ResponseDesc previousResponse;
    private String previousInput;

    public ResponsesGenerator(final List<Rule> rulesList, final List<ResponseDesc> defaultResponseDescriptors,
                              final List<ResponseDesc> repeatResponseDescriptors,
                              final Map<String, String> transpositions) {
        this.rulesList = rulesList;
        this.defaultResponseDescriptors = defaultResponseDescriptors;
        this.repeatResponseDescriptors = repeatResponseDescriptors;
        this.transpositions = transpositions;

        this.randomGenerator = new Random();
    }

    public ResponseDesc getResponse(final String rawInput) {
        final String preprocessedInput = normalize(rawInput);
        if (preprocessedInput.length() == 0) {
            return new ResponseDesc(false, "");
        }

        final ResponseDesc result;
        if (previousInput!=null && previousInput.equals(preprocessedInput)) {
            result = oneOf(repeatResponseDescriptors);
            previousResponse = result;
        } else {
            final List<Rule> matchingRules = findMatchingRules(preprocessedInput);
            final List<Rule> mostRelevantRules = filterMostRelevant(matchingRules, preprocessedInput);
            if (matchingRules.isEmpty()) {
                result = oneOf(defaultResponseDescriptors);
                previousResponse = result;
            } else {
                final Rule rule = oneOf(mostRelevantRules);
                final String processedMessage = makeMessage(rule, preprocessedInput);
                result = new ResponseDesc(rule.getResponseDesc().isTerminal(), processedMessage);
                previousResponse = rule.getResponseDesc();
            }
        }

        previousInput = preprocessedInput;
        return result;
    }

    private List<Rule> filterMostRelevant(final List<Rule> matchingRules, final String input) {
        if (matchingRules.isEmpty()) {
            return Collections.emptyList();
        }

        final List<RuleWithDistance> rulesWithDistancesList = makeRulesWithDistancesList(matchingRules, input);
        final int minDistance = Collections.min(rulesWithDistancesList, new Comparator<RuleWithDistance>() {
            @Override
            public int compare(final RuleWithDistance o1, final RuleWithDistance o2) {
                return o1.distance - o2.distance;
            }
        }).distance;

        final List<Rule> mostRelevantRules = new ArrayList<>(matchingRules.size());
        for (final RuleWithDistance ruleWithDistance : rulesWithDistancesList) {
            if (ruleWithDistance.distance == minDistance) {
                mostRelevantRules.add(ruleWithDistance.rule);
            }
        }
        return mostRelevantRules;
    }

    private List<RuleWithDistance> makeRulesWithDistancesList(final List<Rule> matchingRules, final String input) {
        final List<RuleWithDistance> result = new ArrayList<>(matchingRules.size());
        for (final Rule matchingRule : matchingRules) {
            final int distance = calcDistance(matchingRule.getInput(), input);
            result.add(new RuleWithDistance(matchingRule, distance));
        }

        return result;
    }

    private String makeMessage(final Rule rule, final String input) {
        final int cutPosition = rule.getInput().length();
        final String message = rule.getResponseDesc().getMessage();
        final String wildcardSubstitute = input.substring(cutPosition);
        final String withoutPunctuations = wildcardSubstitute.replace("?", " ").replace(".", " ").trim();
        final String transposedSubstitute = transpose(withoutPunctuations);
        return message.replace("*", transposedSubstitute);
    }

    private String transpose(final String input) {
        StringBuilder resultBuilder = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(input);
        while (tokenizer.hasMoreTokens()) {
            final String word = tokenizer.nextToken().trim();
            final String transposedWord = transpositions.getOrDefault(word,word);
            resultBuilder.append(" ");
            resultBuilder.append(transposedWord);
        }

        return resultBuilder.toString();
    }

    private List<Rule> findMatchingRules(final String input) {
        final List<Rule> result = new ArrayList<>();

        for (final Rule rule : rulesList) {
            if(matches(rule, input)) {
                result.add(rule);
            }
        }

        return result;
    }

    private boolean matches(final Rule rule, final String input) {
        boolean result = false;
        final String rulesPattern = rule.getInput();
        if(input.startsWith(rulesPattern))
        {
            result = true;
            final String expectedPrevResponse = rule.getPreviousResponse();
            if(expectedPrevResponse != null &&
              (previousResponse == null || !expectedPrevResponse.equals(previousResponse.getMessage())))
            {
                result = false;
            }
        }

        return result;
    }

    /** Picks random item from the given list and returns it. */
    private <T> T oneOf(final List<T> itemsList) {
        final int size = itemsList.size();
        final int index = randomGenerator.nextInt(size);
        return itemsList.get(index);
    }

    /** Calculates Levenshtein distance between two strings. */
    private int calcDistance(final String s1, final String s2) {
        final int m = s1.length();
        final int n = s2.length();

        final int distance[][] = new int[m+1][n+1];
        for (int i = 0; i <= m; ++i) {
            distance[i][0] = i;
        }
        for (int i = 1; i <= n; ++i) {
            distance[0][i] = i;
        }

        for (int i=1; i<=m; ++i) {
            for (int j=1; j<=n; ++j) {
                int cost = 0;
                if (s1.charAt(i-1) != s2.charAt(j-1)) {
                    cost = 1;
                }

                distance[i][j] = Collections.min(Lists.newArrayList(
                    distance[i - 1][j] + 1,         //deletion
                    distance[i][j - 1] + 1,         //insertion
                    distance[i - 1][j - 1] + cost   //substitution
                ));
            }
        }

        return distance[m][n];
    }

    private String normalize(final String string) {
        return string.trim().toUpperCase();
    }

    private static class RuleWithDistance {
        private final Rule rule;
        private final int distance;

        public RuleWithDistance(final Rule rule, final int distance) {
            this.rule = rule;
            this.distance = distance;
        }
    }
}
