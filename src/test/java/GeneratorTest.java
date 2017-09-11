import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jadson Oliveira <jadsonjjmo@gmail.com>
 */
public class GeneratorTest {

    @Test
    public void linearOperationsTest01() {
        String expression = "{10}*{1}+{20}";
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(30.0, result, 0);
    }

    @Test
    public void linearOperationsTest02() {
        String expression = "{10}*{2.0}+{20}";
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(40.0, result, 0);
    }

    @Test
    public void linearOperationsTest03() {
        String expression = "{10}*({1.0}+{2.0})+{20}";
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(50.0, result, 0);
    }

    @Test
    public void quadraticOperationsTest01() {
        String expression = "{10}*{1.0}^{2}+{20}";
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(30.0, result, 0);
    }

    @Test
    public void quadraticOperationsTest02() {
        String expression = "{10}*{2.0}^{2}+{20}";
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(60.0, result, 0);
    }

    @Test
    public void quadraticOperationsTest03() {
        String expression = "{10}*({1.0}+{2.0})^{2}+{20}";
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(110.0, result, 0);
    }

    @Test
    public void logOperationsTest01() {
        String expression = "{10}*l{1.0}+{20}";
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(20.0, result, 0);
    }

    @Test
    public void logOperationsTest02() {
        String expression = "{10}*l{2.0}+{20}";
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(30.0, result, 0);
    }

    @Test
    public void logOperationsTest03() {
        String expression = "{10}*l({1.0}+{2.0})+{20}";
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(35.8496, result, 0.0001);
    }

    @Test
    public void exponentialOperationsTest01() {
        String expression = "{10}^{1.0}+{20}";
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(30.0, result, 0);
    }

    @Test
    public void exponentialOperationsTest02() {
        String expression = "{10}^{2.0}+{20}";
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(120.0, result, 0);
    }

    @Test
    public void exponentialOperationsTest03() {
        String expression = "{10}^({1.0}+{2.0})+{20}";
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(1020.0, result, 0);
    }

    @Test
    public void approximatedOperationsTest01() {
        String expression = "{10}*{1.0}+{rand}";
        expression = Generator.normalizeExpression(expression, new String[]{"1.0", "2.0"});
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(10.0, result, 10);
    }

    @Test
    public void approximatedOperationsTest02() {
        String expression = "{10}*{2.0}+{rand}";
        expression = Generator.normalizeExpression(expression, new String[]{"1.0", "2.0"});
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(20.0, result, 10);
    }

    @Test
    public void approximatedOperationsTest03() {
        String expression = "{10}*([0]+[1])+{rand}";
        expression = Generator.normalizeExpression(expression, new String[]{"1.0", "2.0"});
        double result = Double.parseDouble(Generator.solve(expression));

        Assert.assertEquals(30.0, result, 10);
    }


}
