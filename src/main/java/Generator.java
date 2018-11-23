import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Stack;

/**
 * @author Jadson Oliveira <jadsonjjmo@gmail.com>
 */
class Generator {

    private static final JSONParser jsonParser = new JSONParser();
    private static JSONObject config = new JSONObject();
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00000000");
    private static final Random random = new Random(15L);

    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("Please enter the following parameters: [CONFIG FILE]");
            System.exit(1);
        }
        final String configFile = args[0];

        try {
            config = (JSONObject) jsonParser.parse(new FileReader(configFile));
        } catch (final IOException e) {
            System.err.println("Failed to read the config file!");
            e.printStackTrace();
            System.exit(1);
        } catch (final ParseException e) {
            System.err.println("Failed to parse the config file Json!");
            e.printStackTrace();
            System.exit(1);
        }

        generateNewDataset();
    }


    private static void generateNewDataset() {
        //noinspection unchecked
        final String originDataset = (String) config.getOrDefault("origin_dataset", "origin_dataset.csv");
        //noinspection unchecked
        final String targetDataset = (String) config.getOrDefault("target_dataset", "target_dataset.csv");

        try {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(originDataset)));
            final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetDataset)));

            while (bufferedReader.ready()) {
                final String line = bufferedReader.readLine();
                final String lineWithNewAttributes = getNewAttributes(line);

                bufferedWriter.write(line + lineWithNewAttributes);
                bufferedWriter.newLine();
            }

            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (final FileNotFoundException e) {
            System.err.println("Failed to get file!");
            e.printStackTrace();
            System.exit(1);
        } catch (final IOException e) {
            System.err.println("Failed to read file!");
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static String getNewAttributes(final String line) {
        //noinspection unchecked
        final String valueSeparator = (String) config.getOrDefault("separator", "\t");
        //noinspection unchecked
        final JSONArray redundantAttributes = (JSONArray) config.getOrDefault("redundant_attributes", new JSONArray());

        final String[] attributesOnLine = line.split(valueSeparator);
        final StringBuilder newAttributes = new StringBuilder();

        for (final Object object : redundantAttributes) {
            String attributeExpression = (String) object;
            attributeExpression = normalizeExpression(attributeExpression, attributesOnLine);

            newAttributes.append(valueSeparator);
            newAttributes.append(solve(attributeExpression));
        }

        return newAttributes.toString();
    }


    public static String normalizeExpression(String expression, final String[] attributes) {
        expression = expression.replace(" ", "");

        while (expression.contains("[")) {
            final int indexStart = expression.indexOf("[");
            final int indexEnd = expression.indexOf("]");
            final String indexOfAttribute = expression.substring(indexStart + 1, indexEnd);
            final String attributeByIndex = attributes[Integer.parseInt(indexOfAttribute)];
            expression = expression.replace("[" + indexOfAttribute + "]", "{" + attributeByIndex + "}");
        }

        while (expression.contains("rand")) {
            final int indexRand = expression.indexOf("rand");
            final int indexStart = expression.indexOf("{", indexRand);
            final int indexEnd = expression.indexOf("}", indexRand);
            final int randomLimit = Integer.parseInt(expression.substring(indexStart + 1, indexEnd));
            expression = expression.replaceFirst("rand\\{" + randomLimit + "}", String.valueOf(random.nextGaussian() * randomLimit));
        }

        return expression;
    }


    private static String solveByPriority(String expression) {
        if (expression.contains("(")) {
            final int indexStart = expression.indexOf("(");
            int indexEnd = expression.indexOf(")");

            int tempIndex = expression.indexOf("(", indexStart + 1);
            while (tempIndex > 0 && tempIndex < indexEnd) {
                tempIndex = expression.indexOf("(", tempIndex + 1);
                indexEnd = expression.indexOf(")", indexEnd + 1);
            }

            final String expressionToSolve = expression.substring(indexStart + 1, indexEnd);
            final String expressionSolved = solveByPriority(expressionToSolve);
            expression = expression.replace("(" + expressionToSolve + ")", expressionSolved);
            expression = solveByPriority(expression);
        } else {

            final Stack<Double> stackNumbers = new Stack<>();
            final Stack<Character> stackOperators = new Stack<>();

            while (expression.length() > 0) {

                if (expression.charAt(0) == '{') {
                    final int indexStart = expression.indexOf("{");
                    final int indexEnd = expression.indexOf("}");
                    final String numberString = expression.substring(indexStart + 1, indexEnd);
                    final double number = Double.parseDouble(numberString);
                    expression = expression.substring(indexEnd + 1);
                    stackNumbers.push(number);
                } else {
                    final char character = expression.charAt(0);
                    expression = expression.substring(1);

                    final int indexStart;
                    final int indexEnd;
                    final String numberString;
                    final double number1;

                    switch (character) {
                        case 'l':
                        case 's':
                            indexStart = expression.indexOf("{");
                            indexEnd = expression.indexOf("}");
                            numberString = expression.substring(indexStart + 1, indexEnd);
                            number1 = Double.parseDouble(numberString);
                            expression = expression.substring(indexEnd + 1);
                            stackNumbers.push(executeOperation(number1, -1.0, character));
                            break;
                        case '^':
                            stackOperators.push(character);
                            break;
                        case '*':
                        case '/':
                        case '+':
                        case '-':
                            solveOperation(stackNumbers, stackOperators, character);
                            break;
                        default:
                            System.err.println("Operator " + character + " not found!");
                            System.exit(1);
                            break;

                    }
                }
            }

            while (!stackOperators.empty()) {
                final double number2 = stackNumbers.pop();
                final double number1 = stackNumbers.pop();
                stackNumbers.push(executeOperation(number1, number2, stackOperators.pop()));
            }

            expression = "{" + stackNumbers.pop() + "}";

        }

        return expression;
    }

    private static void solveOperation(final Stack<Double> stackNumbers, final Stack<Character> stackOperators, final char character) {
        double number2;
        double number1;
        while (!stackOperators.empty() && isPriority(character, stackOperators.peek())) {
            number2 = stackNumbers.pop();
            number1 = stackNumbers.pop();
            stackNumbers.push(executeOperation(number1, number2, stackOperators.pop()));
        }
        stackOperators.push(character);
    }

    private static boolean isPriority(final char currentOperator, final char operatorToCompare) {
        switch (currentOperator) {
            case '*':
            case '/':
                return (operatorToCompare == '^' || currentOperator == operatorToCompare);
            case '-':
            case '+':
                return (operatorToCompare == '^' || operatorToCompare == '*' ||
                        operatorToCompare == '/' || currentOperator == operatorToCompare);
            default:
                return false;
        }
    }

    private static Double executeOperation(final Double number1, final Double number2, final char operator) {
        switch (operator) {
            case '^':
                return Math.pow(number1, number2);
            case '*':
                return number1 * number2;
            case '/':
                return number1 / number2;
            case '+':
                return number1 + number2;
            case '-':
                return number1 - number2;
            case 'l':
                Double logResult = Math.log(number1);
                if (logResult.isNaN() || logResult.isInfinite()) {
                    logResult = 0.0;
                }
                return logResult;
            case 's':
                Double sinResult = Math.sin(Math.toRadians(number1));
                if (sinResult.isNaN() || sinResult.isInfinite()) {
                    sinResult = 0.0;
                }
                return sinResult;
            default:
                System.err.println("Operator not found!");
                System.exit(1);
        }
        return null;
    }

    public static String solve(final String expression) {
        final String stringValue = solveByPriority(expression);

        //Avoid double NaN or Infinity
        Double value = Double.parseDouble(stringValue.substring(1, stringValue.length() - 1));
        value = (value.isNaN() || value.isInfinite()) ? 0.0 : value;

        String newAttribute = decimalFormat.format(value);
        newAttribute = newAttribute.replace(",", ".");
        return newAttribute;
    }

}
