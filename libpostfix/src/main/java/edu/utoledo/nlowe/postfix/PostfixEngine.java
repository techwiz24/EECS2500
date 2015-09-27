package edu.utoledo.nlowe.postfix;

import edu.utoledo.nlowe.CustomDataTypes.CustomStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * A math engine that accepts string expressions in postfix notation as an integer.
 *
 * All literals must be integers, and all operations will be rounded to an integer.
 *
 * The following binary operations are supported by default:
 * * '+'        Addition
 * * '-'        Subtraction
 * * 'x' or '*' Multiplication
 * * '/'        Division
 * * '%'        Modulus
 * * '^'        Exponentiation
 * * '<'        Left Shift
 * * '>'        Right Shift
 *
 * The following unary operators are supported by default:
 * * 'Q'        Square Root
 * * 'C'        Cube Root
 *
 * Additional operators may be registered by calling <code>register()</code> and
 * passing a lambda for either a <code>BinaryOperator</code> or a <code>UnaryOperator</code>
 */
public class PostfixEngine {

    /** A regular expression that matches a single integer (positive, or negative) */
    public static final String NUMERIC_REGEX = "^(-)?[0-9]+$";

    /** All operators registered with the engine */
    private final HashMap<String, Operator> operators = new HashMap<>();

    public PostfixEngine(){
        // Register default valid operators
        register("+", (a, b) -> a + b);
        register("-", (a, b) -> a - b);
        register("x", (a, b) -> a * b);
        register("*", (a, b) -> a * b);
        register("/", (a, b) -> a / b);
        register("%", (a, b) -> a % b);
        register("^", (a, b) -> (int) Math.pow(a, b));
        register("<", (a, b) -> a << b);
        register(">", (a, b) -> a >> b);
        register("Q", (a) -> (int) Math.sqrt(a));
        register("C", (a) -> (int) Math.cbrt(a));
    }

    /**
     * Converts the given expression from infix notation to postfix notation
     *
     * @param expression a valid expression in infix notation
     * @return The equivalent postfix expression
     */
    public String convertInfixExpression(String expression){
        StringBuilder result = new StringBuilder();
        CustomStack<String> buffer = new CustomStack<>();

        String[] parts = expression.trim().split(" +");
        for(String token : parts){
            if(token.length() == 1 && isValidOperator(token)){
                while(buffer.size() > 0 && !buffer.peek().equals("(")){
                    //Operator Precedence is ignored for this project
                    buffer.pop();
                    result.append(buffer.peek()).append(" ");
                }
                buffer.push(token);
            }else if(token.equals("(")){
                buffer.push(token);
            }else if(token.equals(")")){
                while(!buffer.peek().equals("(")){
                    result.append(buffer.pop()).append(" ");
                }
                buffer.pop();
            }else{
                result.append(token).append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Registers the token <code>token</code> to the specified binary operator functional interface.
     *
     * If the specified token is already assigned to an operator, it will be overwritten.
     *
     * @param token The token that denotes the operator
     * @param operator The functional interface to apply tokens to when this operator is encountered
     */
    public void register(String token, BinaryOperator operator){
        operators.put(token, operator);
    }

    /**
     * Registers the token <code>token</code> to the specified unary operator functional interface.
     *
     * If the specified token is already assigned to an operator, it will be overwritten.
     *
     * @param token The token that denotes the operator
     * @param operator The functional interface to apply tokens to when this operator is encountered
     */
    public void register(String token, UnaryOperator operator){
        operators.put(token, operator);
    }

    /**
     * Evaluates the specified expression following the rules of postfix notation.
     *
     * @param expression a valid postfix expression
     * @return the integer value of the expression
     * @throws IllegalArgumentException if the expression is invalid in any way
     */
    public int evaluate(String expression) throws IllegalArgumentException {
        CustomStack<Integer> buffer = new CustomStack<>();

        // Split the expression into tokens by white space (one or more of either a space or a tab)
        String[] parts = expression.trim().split("[ \\t]+");
        for(String token : parts){
            if(isValidOperator(token)){
                // Try to evaluate the operator based on what is already on the stack
                Operator op = getEvaluatorFunction(token);

                if(op instanceof BinaryOperator){
                    // Binary operators need at least two operands on the stack
                    if(buffer.size() < 2){
                        throw new IllegalArgumentException("Malformed postfix expression (not enough literals): '" + expression + "'");
                    }

                    // Extract the arguments backwards because we're using a stack
                    int b = buffer.pop();
                    int a = buffer.pop();

                    // Push the result onto the stack
                    buffer.push(((BinaryOperator) op).evaluate(a, b));
                }else if(op instanceof UnaryOperator){
                    // Unary Operators need at least one operand on the stack
                    if(buffer.size() == 0){
                        throw new IllegalArgumentException("Malformed postfix expression (not enough literals): '" + expression + "'");
                    }

                    // Pop the operand off of the stack, and push the evaluated result onto the stack
                    buffer.push(((UnaryOperator)op).evaluate(buffer.pop()));
                }else{
                    throw new IllegalStateException("The operator type " + op.getClass() + " is unsupported at this time");
                }
            }else if(token.matches(NUMERIC_REGEX)){
                // Just an integer, push it onto the stack
                buffer.push(Integer.parseInt(token));
            }else{
                // Not a valid operator or literal
                throw new IllegalArgumentException("Malformed postfix expression (unrecognized token " + token + "): '" + expression + "'");
            }
        }

        // If we're left with more than one item on the stack, then the expression has too many literals
        if(buffer.size() != 1){
            throw new IllegalArgumentException("Malformed postfix expression (too many literals): '" + expression + "'");
        }

        // The result is the only thing left on the stack
        return buffer.pop();
    }

    /**
     * Evalueates the specified infix expression by first converting to postfix and then evaluating the result.
     *
     * @param expression A valid invix expression
     * @return the integer result of the evaluated expression
     */
    public int evaluateInfix(String expression){
        return evaluate(convertInfixExpression(expression));
    }

    /**
     * Gets the operator assigned to the specified token
     *
     * @param operator the token representing the operator
     * @return the operator's functional interface that is registered to the token
     */
    public Operator getEvaluatorFunction(String operator){
        if(!isValidOperator(operator)){
            throw new IllegalArgumentException("Undefined postfix operator: " + operator);
        }

        return operators.get(operator);
    }

    /**
     * @param operator the operator to check
     * @return <code>true</code> if and only if the specified operator is registered
     */
    public boolean isValidOperator(String operator){
        return operators.keySet().contains(operator);
    }

    /**
     * @return The unmodifiable set of all registered operator tokens
     */
    public Set<String> getSupportedOperators(){
        return Collections.unmodifiableSet(operators.keySet());
    }

}
