import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;

/**
 * @author Jadson Oliveira <jadsonjjmo@gmail.com>
 */
public class Generator {

    private static final JSONParser jsonParser = new JSONParser();
    private static JSONObject config = new JSONObject();
    private static DecimalFormat decimalFormat = new DecimalFormat("0.00000000");

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Please enter the following parameters: [CONFIG FILE]");
            System.exit(1);
        }
        final String configFile = args[0];

        try {
            config = (JSONObject) jsonParser.parse(new FileReader(configFile));
        } catch (IOException e) {
            System.err.println("Failed to read the config file!");
            e.printStackTrace();
            System.exit(1);
        } catch (ParseException e) {
            System.err.println("Failed to parse the config file Json!");
            e.printStackTrace();
            System.exit(1);
        }

        generateNewDataset();
    }


    private static void generateNewDataset() {
        final String originDataset = (String) config.getOrDefault("origin_dataset", "origin_dataset.csv");
        final String targetDataset = (String) config.getOrDefault("target_dataset", "target_dataset.csv");

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(originDataset)));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetDataset)));

            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                String lineWithNewAttributes = getNewAttributes(line);

                bufferedWriter.write(line + lineWithNewAttributes);
                bufferedWriter.newLine();
            }

            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (FileNotFoundException e) {
            System.err.println("Failed to get file!");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Failed to read file!");
            e.printStackTrace();
            System.exit(1);
        }

    }


    private static String getNewAttributes(String line) {

        final String valueSeparator = (String) config.getOrDefault("separator", "\t");
        final JSONArray reduntantAttributes = (JSONArray) config.getOrDefault("redundant_attributes", new JSONArray());

        String[] attributesOnLine = line.split(valueSeparator);
        String newAttributes = "";

        for (Object object : reduntantAttributes) {
            String attributeExpression = (String) object;
            attributeExpression = normalizeExpression(attributeExpression, attributesOnLine);

            newAttributes += valueSeparator;
            newAttributes += solve(attributeExpression);
        }

        return newAttributes;

    }


    public static String normalizeExpression(String expression, String[] attributes) {
        expression = expression.replace(" ", "");
        Random random = new Random();

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
            expression = expression.replaceFirst("rand\\{" + randomLimit + "}", String.valueOf(random.nextInt(randomLimit) + random.nextDouble()));
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

            String expressionToSolve = expression.substring(indexStart + 1, indexEnd);
            String expressionSolved = solveByPriority(expressionToSolve);
            expression = expression.replace("(" + expressionToSolve + ")", expressionSolved);
            expression = solveByPriority(expression);
        } else {

            Stack<Double> stackNumbers = new Stack<>();
            Stack<Character> stackOperators = new Stack<>();

            while (expression.length() > 0) {

                if (expression.charAt(0) == '{') {
                    final int indexStart = expression.indexOf("{");
                    final int indexEnd = expression.indexOf("}");
                    final String numberString = expression.substring(indexStart + 1, indexEnd);
                    double number = Double.parseDouble(numberString);
                    expression = expression.substring(indexEnd + 1);
                    stackNumbers.push(number);
                } else {
                    char character = expression.charAt(0);
                    expression = expression.substring(1);

                    int indexStart;
                    int indexEnd;
                    String numberString;
                    double number1;

                    switch (character) {
                        case 'l':
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

                    }
                }
            }

            while (!stackOperators.empty()) {
                double number2 = stackNumbers.pop();
                double number1 = stackNumbers.pop();
                stackNumbers.push(executeOperation(number1, number2, stackOperators.pop()));
            }

            expression = "{" + stackNumbers.pop() + "}";

        }

        return expression;
    }

    private static void solveOperation(Stack<Double> stackNumbers, Stack<Character> stackOperators, char character) {
        double number2;
        double number1;
        while (!stackOperators.empty() && isPriority(character, stackOperators.peek())) {
            number2 = stackNumbers.pop();
            number1 = stackNumbers.pop();
            stackNumbers.push(executeOperation(number1, number2, stackOperators.pop()));
        }
        stackOperators.push(character);
    }

    private static boolean isPriority(char atualOperator, char operatorToCompare) {
        switch (atualOperator) {
            case '*':
            case '/':
                return (operatorToCompare == '^' || atualOperator == operatorToCompare);
            case '-':
            case '+':
                return (operatorToCompare == '^' || operatorToCompare == '*' ||
                        operatorToCompare == '/' || atualOperator == operatorToCompare);
            default:
                return false;
        }
    }

    private static Double executeOperation(Double number1, Double number2, char operator) {
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
                if (logResult.isNaN()) {
                    logResult = 0.0;
                }
                if (logResult.isInfinite()) {
                    logResult = Double.MAX_VALUE;
                }
                return logResult;
            default:
                System.err.println("Operator not found!");
                System.exit(1);
        }
        return null;
    }

    public static String solve(String expression) {
        String stringValue = solveByPriority(expression);

        //Avoid double NaN or Infinity
        Double value = Double.parseDouble(stringValue.substring(1, stringValue.length() - 1);
        value = value.isNaN() ? 0.0 : value;
        value = value.isInfinite() ? Double.MAX_VALUE : value;

        String newAttribute = decimalFormat.format(value);
        newAttribute = newAttribute.replace(",", ".");
        return newAttribute;
    }

}
