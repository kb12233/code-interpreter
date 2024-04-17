package code;

import java.util.List;
import java.util.Scanner;
class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Code.runtimeError(error);
        }
    }
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case NOT:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                if (right instanceof Double) {
                    return -(double) right;
                }
                if (right instanceof Integer) {
                    return -(int) right;
                }
            case PLUS:
                if (right instanceof Double) {
                    return +(double) right;
                }
                if (right instanceof Integer) {
                    return +(int) right;
                }
        }

        // Unreachable.
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double || operand instanceof Integer) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator,
                                     Object left, Object right) {
        if ((left instanceof Double && right instanceof Integer) || (left instanceof Integer && right instanceof Double)) {
            throw new RuntimeError(operator,
                    "Number operands must be of the same data type. Ex. (INT "
                    + operator.lexeme + " INT) or " + "(FLOAT " + operator.lexeme + " FLOAT)");
        }

        if ((left instanceof Double && right instanceof Double) || (left instanceof Integer && right instanceof Integer)) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isExistingVar(Token varToken) {
        Object varObject;
        try {
            varObject = environment.get(varToken);
            return true;
        } catch (RuntimeError error) {
            return false;
        }
    }

    private boolean isTruthy(Object object) {
        // if (object == null) return false;
        // if (object instanceof Boolean) return (boolean)object;
        // return true;
        return (boolean)object;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "NIL";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        if (object instanceof Boolean) {
            return object.toString().toUpperCase();
        }

        return object.toString();
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                if (statement instanceof Stmt.Var varStmt) {
                    environment.define(varStmt.name.lexeme, varStmt.initializer != null ? evaluate(varStmt.initializer) : null, varStmt.type);
                } else {
                    execute(statement);
                }
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitScanStmt(Stmt.Scan stmt) {
        String input = new Scanner(System.in).nextLine();
        String[] values = input.split(",");
        if (values.length != stmt.variables.size()) {
            throw new RuntimeError(stmt.variables.get(0), "Number of input values does not match number of variables.");
        }
        for (int i = 0; i < values.length; i++) {
            Token variable = stmt.variables.get(i);
            Object value = values[i];
            TokenType type = environment.getType(variable.lexeme);
            switch (type) {
                case INT:
                    value = Integer.parseInt(values[i]);
                    break;
                case FLOAT:
                    value = Double.parseDouble(values[i]);
                    break;
                case BOOL:
                    value = Boolean.parseBoolean(values[i].toLowerCase());
                    break;
                case CHAR:
                    value = values[i].charAt(0);
                    break;
                case STRING:
                    value = values[i];
                    break;
                default:
                    throw new RuntimeError(variable, "Unsupported variable type.");
            }
            environment.assign(variable, value);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        if (isExistingVar(stmt.name)) {
            throw new RuntimeError(stmt.name, "Variable " + "'" + stmt.name.lexeme + "'" + " already exists!");
        }
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value, stmt.type);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case LESS_GREATER:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left > (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left > (int) right;
                }
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left >= (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left >= (int) right;
                }
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left < (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left < (int) right;
                }
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left <= (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left <= (int) right;
                }
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left - (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left - (int) right;
                }
            case PLUS:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left + (int) right;
                }
            case AMPERSAND:
                //if (left instanceof String && right instanceof String) {
                //    return (String)left + (String)right;
                //}
                String leftCopy;
                String rightCopy;
                if (left instanceof String) {
                    leftCopy = (String) left;
                } else if (left instanceof Boolean) {
                    leftCopy = left.toString().toUpperCase();
                } else {
                    leftCopy = left.toString();
                }

                if (right instanceof String) {
                    rightCopy = (String) right;
                } else if (right instanceof Boolean) {
                    rightCopy = right.toString().toUpperCase();
                } else {
                    rightCopy = right.toString();
                }

                return leftCopy + rightCopy;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double)left / (double)right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int)left / (int)right;
                }
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double)left * (double)right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int)left * (int)right;
                }
            case PERCENT:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double)left % (double)right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int)left % (int)right;
                }
        }

        // Unreachable.
        return null;
    }
}