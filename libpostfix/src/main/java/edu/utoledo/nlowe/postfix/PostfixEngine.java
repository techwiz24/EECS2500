package edu.utoledo.nlowe.postfix;

import edu.utoledo.nlowe.CustomDataTypes.CustomStack;
import edu.utoledo.nlowe.postfix.exception.PostfixArithmeticException;
import edu.utoledo.nlowe.postfix.exception.PostfixOverflowException;
import edu.utoledo.nlowe.postfix.exception.PostfixUnderflowException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A math engine that accepts string expressions in postfix notation as an integer.
 * <br/>
 * All literals must be integers, and all operations will be rounded to an integer.
 * <br/>
 * The following binary operations are supported by default:
 * <ul>
 *     <li>'<': Addition</li>
 *     <li>'-': Subtraction</li>
 *     <li>'*' or 'x': Multiplication</li>
 *     <li>'/': Division</li>
 *     <li>'%': Modulus</li>
 *     <li>'^': Exponentiation</li>
 *     <li>'<': Left Shift</li>
 *     <li>'>': Right Shift</li>
 * </ul>
 * <br/>
 * The following unary operators are supported by default:
 * <ul>
 *     <li>'Q' Square Root</li>
 *     <li>'C' Cube Root</li>
 * </ul>
 * <br/>
 * Additional operators may be registered by calling <code>register()</code> and
 * passing a lambda for either a <code>BinaryOperator</code> or a <code>UnaryOperator</code>
 */
public class PostfixEngine
{

    /** A regular expression that matches a single integer (positive, or negative) */
    public static final String NUMERIC_REGEX = "^(-)?[0-9]+$";

    /** A regular expression that matches oen or more white-space characters. Used to split tokens in postfix notation */
    public static final String TOKEN_SEPARATOR_REGEX = "\\s+";

    /** A part of a regular expression that matches the preceding characters not immediately followed by a number, underscore, or opening parenthesis*/
    public static final String NOT_FOLLOWED_BY_NUMBER_OR_PARENTHESIS_REGEX = "(?![0-9\\(_])";
    /** A part of a regular expression that matches characters not immediately preceded by a number, closing parenthesis, or underscore */
    private static final String NOT_PRECEDED_BY_NUMBER_OR_PARENTHESIS = "(?<![0-9\\)_])";

    /** All operators registered with the engine */
    private final HashMap<String, Operator> operators = new HashMap<>();

    public PostfixEngine()
    {
        // Register default valid operators
        register("+", (a, b) -> a + b);
        register("-", (a, b) -> a - b);
        register("x", (a, b) -> a * b);
        register("*", (a, b) -> a * b);
        register("/", (a, b) -> a / b);
        register("%", (a, b) -> a % b);
        register("^", (a, b) -> (long) Math.pow(a, b));
        register("<", (a, b) -> a << b);
        register(">", (a, b) -> a >> b);
        register("Q", (a) -> (long) Math.sqrt(a));
        register("C", (a) -> (long) Math.cbrt(a));
    }

    /**
     * Converts the given expression from infix notation to postfix notation
     * <br/>
     * A valid infix expression must meet all of the following requirements:
     * <ul>
     *     <li>Is not empty</li>
     *     <li>All parenthesis must be matched</li>
     *     <li>All unary operators must have an operand or group immediately following them</li>
     *     <li>Must not contain invalid tokens (non-numeric non-parenthesis characters that are not registered operators)</li>
     * </ul>
     *
     * @param expression a valid expression in infix notation
     * @return The equivalent postfix expression
     */
    public String convertInfixExpression(String expression)
    {
        // Validate and simplify the expression
        // An exception will be thrown if the expression fails validation
        String simplifiedExpression = validateAndSimplifyInfixExpression(expression);

        // Create a buffer to hold the resulting postfix expression and a buffer for groups / operators
        StringBuilder result = new StringBuilder();
        CustomStack<String> buffer = new CustomStack<>();

        int index = 0;
        do
        {
            if (simplifiedExpression.charAt(index) == '_')
            {
                // An underscore indicates spacing for a mixed separator style
                // Skip the underscore and move on
                index++;
                continue;
            }

            // Extract the next token and increment the index by its length
            String token = extractInfixToken(simplifiedExpression, index);
            index += token.length();

            // Now that we've extracted the token, we can parse it
            if (token.length() == 1 && isValidOperator(token))
            {
                // Convert everything this operator needs
                while (buffer.size() > 0 && !buffer.peek().equals("("))
                {
                    //Operator Precedence is ignored for this project
                    result.append(buffer.pop()).append(" ");
                }
                buffer.push(token);
            }
            else if (token.equals("("))
            {
                buffer.push(token);
            }
            else if (token.equals(")"))
            {
                // End of sub group
                while (!buffer.peek().equals("("))
                {
                    result.append(buffer.pop()).append(" ");
                }
                buffer.pop();
            }
            else
            {
                // Just a literal, append it
                result.append(token).append(" ");
            }
        } while (index < simplifiedExpression.length());

        // Append any extra operators left on the expression
        while (buffer.size() > 0)
        {
            result.append(buffer.pop()).append(" ");
        }

        return result.toString().trim();
    }

    /**
     * Validates and simplifies the infix expression
     * <br/>
     * Validation Rules:
     * <ul>
     *     <li>The expression must not be null or empty or all whitespace</li>
     *     <li>All infix expressions must have matched parenthesis</li>
     *     <li>All unary operators must have an operand immediately after the operator</li>
     * </ul>
     * <br/>
     * Simplification Rules:
     * <ul>
     *     <li>Replace all whitespace characters with underscores</li>
     * </ul>
     * @param expression The expression to validate and simplify
     * @return The simplified and validated infix expression
     * @throws IllegalArgumentException if the input expression fails any validation rules
     */
    public String validateAndSimplifyInfixExpression(String expression){
        if (expression == null || expression.length() == 0)
        {
            throw new IllegalArgumentException("Nothing to convert");
        }

        // Infix expressions must have matched parenthesis
        int parenthesisCounter = 0;
        for (char c : expression.toCharArray())
        {
            // Increment the counter for opening parenthesis, decrement the counter for closing parenthesis
            parenthesisCounter += (c == '(' ? 1 : (c == ')' ? -1 : 0));
        }

        // If the counter is not at zero, then we have an unmatched parenthesis somewhere
        if (parenthesisCounter != 0)
        {
            throw new IllegalArgumentException("Malformed infix expression (unmatched parenthesis): '" + expression + "'");
        }

        // Not all infix expressions are delimited by tabs or spaces, some may even be mixed
        // Strip them out, replacing them with underscores to detect mixed separator styles
        // Then, parse the expression character by character
        String simplifiedExpression = expression.replaceAll(TOKEN_SEPARATOR_REGEX, "_");

        // Unary operators in infix notation cannot come after their operands
        // Build a regex looking for any unary operator that is not followed by a number or opening parenthesis
        StringBuilder unaryValidator = new StringBuilder().append("[");
        operators.keySet().stream().filter(o -> operators.get(o) instanceof UnaryOperator).forEach(o -> unaryValidator.append(Pattern.quote(o)));
        unaryValidator.append("]").append(NOT_FOLLOWED_BY_NUMBER_OR_PARENTHESIS_REGEX);

        // Test the simplified expression to validate unary operators
        if (Pattern.compile(unaryValidator.toString()).matcher(simplifiedExpression).find())
        {
            throw new IllegalArgumentException("Malformed infix expression (missing operand for unary operator): '" + expression + "'");
        }

        // Binary operators in infix notation cannot come before their operands
        // Build a regex looking for any binary operators that are not preceded by a number or closing parenthesis
        StringBuilder binaryValidator = new StringBuilder().append(NOT_PRECEDED_BY_NUMBER_OR_PARENTHESIS).append("[");
        operators.keySet().stream().filter((o -> operators.get(o) instanceof BinaryOperator)).forEach(o -> binaryValidator.append(Pattern.quote(o)));
        binaryValidator.append("]");

        // Test the simplified expression to validate binary operators
        if (Pattern.compile(binaryValidator.toString()).matcher(simplifiedExpression).find())
        {
            throw new IllegalArgumentException("Malformed infix expression (missing operand for binary operator): '" + expression + "'");
        }

        return simplifiedExpression;
    }

    /**
     * Extract the next token from the given infix expression starting at the specified offset
     *
     * @param expression The simplified infix expression to extract the token from
     * @param offset The offset to start searching for tokens at
     * @return The extracted token
     * @throws IllegalArgumentException if an invalid token is encountered
     */
    private String extractInfixToken(String expression, int offset){
        if (expression.charAt(offset) == '(' ||
            expression.charAt(offset) == ')' ||
            isValidOperator(String.valueOf(expression.charAt(offset)))
            )
        {
            // The token is just a parenthesis or operator
            return String.valueOf(expression.charAt(offset));
        }
        else if (String.valueOf(expression.charAt(offset)).matches(NUMERIC_REGEX))
        {
            // The token is a number or the start of a number
            StringBuilder currentToken = new StringBuilder(String.valueOf(expression.charAt(offset++)));

            // While there are more numbers remaining, append them to the currentToken String Builder
            boolean moreNumbers;
            do
            {
                if (offset < expression.length() && String.valueOf(expression.charAt(offset)).matches(NUMERIC_REGEX))
                {
                    currentToken.append(expression.charAt(offset++));
                    moreNumbers = true;
                }
                else
                {
                    moreNumbers = false;
                }
            } while (moreNumbers);

            // We've found the entire number
            return currentToken.toString();
        }
        else
        {
            throw new IllegalArgumentException("Unrecognized token '" + expression.charAt(offset) + "'");
        }
    }

    /**
     * Registers the token <code>token</code> to the specified binary operator functional interface.
     * <br/>
     * If the specified token is already assigned to an operator, it will be overwritten.
     *
     * @param token    The token that denotes the operator
     * @param operator The functional interface to apply tokens to when this operator is encountered
     */
    public void register(String token, BinaryOperator operator)
    {
        operators.put(token, operator);
    }

    /**
     * Registers the token <code>token</code> to the specified unary operator functional interface.
     * <br/>
     * If the specified token is already assigned to an operator, it will be overwritten.
     *
     * @param token    The token that denotes the operator
     * @param operator The functional interface to apply tokens to when this operator is encountered
     */
    public void register(String token, UnaryOperator operator)
    {
        operators.put(token, operator);
    }

    /**
     * Evaluates the specified expression following the rules of postfix notation.
     *
     * @param expression a valid postfix expression. Each literal and operator must be separated by one or more white-space characters
     * @return the integer value of the expression
     * @throws IllegalArgumentException if the expression is invalid in any way
     */
    public long evaluate(String expression) throws IllegalArgumentException, PostfixArithmeticException
    {
        CustomStack<Long> buffer = new CustomStack<>();

        // Split the expression into tokens by white space (one or more of either a space or a tab)
        String[] parts = expression.trim().split(TOKEN_SEPARATOR_REGEX);
        for (String token : parts)
        {
            if (isValidOperator(token))
            {
                // Try to evaluate the operator based on what is already on the stack
                Operator op = getEvaluatorFunction(token);

                if (op instanceof BinaryOperator)
                {
                    // Binary operators need at least two operands on the stack
                    if (buffer.size() < 2)
                    {
                        throw new IllegalArgumentException("Malformed postfix expression (not enough literals): '" + expression + "'");
                    }

                    // Extract the arguments backwards because we're using a stack
                    long b = buffer.pop();
                    long a = buffer.pop();

                    // Push the result onto the stack
                    buffer.push(((BinaryOperator) op).evaluate(a, b));
                }
                else if (op instanceof UnaryOperator)
                {
                    // Unary Operators need at least one operand on the stack
                    if (buffer.size() == 0)
                    {
                        throw new IllegalArgumentException("Malformed postfix expression (not enough literals): '" + expression + "'");
                    }

                    // Pop the operand off of the stack, and push the evaluated result onto the stack
                    buffer.push(((UnaryOperator) op).evaluate(buffer.pop()));
                }
            }
            else if (token.matches(NUMERIC_REGEX))
            {
                // Just an integer, push it onto the stack
                buffer.push(Long.parseLong(token));
            }
            else
            {
                // Not a valid operator or literal
                throw new IllegalArgumentException("Malformed postfix expression (unrecognized token " + token + "): '" + expression + "'");
            }
        }

        // If we're left with more than one item on the stack, then the expression has too many literals
        if (buffer.size() != 1)
        {
            throw new IllegalArgumentException("Malformed postfix expression (too many literals): '" + expression + "'");
        }

        // The result is the only thing left on the stack
        long result = buffer.pop();

        // Check for overflow / underflow
        if (result > Integer.MAX_VALUE)
        {
            throw new PostfixOverflowException(result, "Integer overflow while evaluating expression '" + expression + "'");
        }
        else if (result < Integer.MIN_VALUE)
        {
            throw new PostfixUnderflowException(result, "Integer underflow while evaluating expression '" + expression + "'");
        }
        else
        {
            return result;
        }
    }

    /**
     * Evaluates the specified infix expression by first converting to postfix and then evaluating the result
     *
     * @param expression A valid infix expression
     * @return the integer result of the evaluated expression
     */
    public long evaluateInfix(String expression)
    {
        return evaluate(convertInfixExpression(expression));
    }

    /**
     * Gets the operator assigned to the specified token
     *
     * @param operator the token representing the operator
     * @return the operator's functional interface that is registered to the token
     */
    public Operator getEvaluatorFunction(String operator)
    {
        if (!isValidOperator(operator))
        {
            throw new IllegalArgumentException("Undefined postfix operator: " + operator);
        }

        return operators.get(operator);
    }

    /**
     * @param operator the operator to check
     * @return <code>true</code> if and only if the specified operator is registered
     */
    public boolean isValidOperator(String operator)
    {
        return operators.keySet().contains(operator);
    }

    /**
     * @return The unmodifiable set of all registered operator tokens
     */
    public Set<String> getSupportedOperators()
    {
        return Collections.unmodifiableSet(operators.keySet());
    }

}
