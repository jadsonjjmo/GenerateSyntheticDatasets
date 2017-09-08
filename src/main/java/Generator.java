import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Stack;

/**
 * @author Jadson Oliveira <jadsonjjmo@gmail.com>
 */
public class Generator {

    private static JSONParser jsonParser = new JSONParser();
    private static JSONObject config;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Please enter the following parameters: [CONFIG FILE]");
            System.exit(1);
        }
        final String configFile = args[0];
//        final String configFile = "config.json";

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


    public static void generateNewDataset() {
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


    public static String getNewAttributes(String line) {

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

        while (expression.contains("[")) {
            final int indexStart = expression.indexOf("[");
            final int indexEnd = expression.indexOf("]");
            final String indexOfAttribute = expression.substring(indexStart + 1, indexEnd);
            final String attributeByIndex = attributes[Integer.parseInt(indexOfAttribute)];
            expression = expression.replace("[" + indexOfAttribute + "]", "{" + attributeByIndex + "}");
        }

        return expression;
    }


    public static String solveByPriority(String expression) {
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
                    double number2;

                    switch (character) {

                        case 'l':
                            indexStart = expression.indexOf("{");
                            indexEnd = expression.indexOf("}");
                            numberString = expression.substring(indexStart + 1, indexEnd);
                            number1 = Double.parseDouble(numberString);
                            expression = expression.substring(indexEnd + 1);
                            stackNumbers.push(number1);
                            break;
                        case '^':
                            if (!expression.contains("l")) {
                                number1 = stackNumbers.pop();

                                indexStart = expression.indexOf("{");
                                indexEnd = expression.indexOf("}");
                                numberString = expression.substring(indexStart + 1, indexEnd);
                                number2 = Double.parseDouble(numberString);
                                expression = expression.substring(indexEnd + 1);

                                stackNumbers.push(Math.pow(number1, number2));
                            }
                            break;
                        case '*':
                            if (!expression.contains("l") && !expression.contains("^")) {
                                number1 = stackNumbers.pop();

                                indexStart = expression.indexOf("{");
                                indexEnd = expression.indexOf("}");
                                numberString = expression.substring(indexStart + 1, indexEnd);
                                number2 = Double.parseDouble(numberString);
                                expression = expression.substring(indexEnd + 1);

                                stackNumbers.push(number1 * number2);
                            }
                            break;
                        case '/':
                            if (!expression.contains("l") && !expression.contains("^")) {
                                number1 = stackNumbers.pop();

                                indexStart = expression.indexOf("{");
                                indexEnd = expression.indexOf("}");
                                numberString = expression.substring(indexStart + 1, indexEnd);
                                number2 = Double.parseDouble(numberString);
                                expression = expression.substring(indexEnd + 1);

                                stackNumbers.push(number1 / number2);
                            }
                            break;
                        case '+':
                            if (!containsPriorityOper(expression)) {
                                number1 = stackNumbers.pop();

                                indexStart = expression.indexOf("{");
                                indexEnd = expression.indexOf("}");
                                numberString = expression.substring(indexStart + 1, indexEnd);
                                number2 = Double.parseDouble(numberString);
                                expression = expression.substring(indexEnd + 1);

                                stackNumbers.push(number1 + number2);
                            }
                            break;
                        case '-':
                            if (!containsPriorityOper(expression)) {
                                number1 = stackNumbers.pop();

                                indexStart = expression.indexOf("{");
                                indexEnd = expression.indexOf("}");
                                numberString = expression.substring(indexStart + 1, indexEnd);
                                number2 = Double.parseDouble(numberString);
                                expression = expression.substring(indexEnd + 1);

                                stackNumbers.push(number1 - number2);
                            }
                            break;
                        default:
                            System.err.println("Operator " + character + " not found!");
                            System.exit(1);

                    }
                }
            }

            expression = "{" + stackNumbers.pop() + "}";

        }

        return expression;
    }

    public static boolean containsPriorityOper(String expression) {
        return (expression.contains("l") || expression.contains("^") ||
                expression.contains("*") || expression.contains("/"));
    }

    public static String solve(String expression) {
        String stringValue = solveByPriority(expression);
        return stringValue.substring(1, stringValue.length() - 1);
    }

}
