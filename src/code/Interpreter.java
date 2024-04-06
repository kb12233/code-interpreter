package code;

import java.util.List;
class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

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
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
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

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
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
        }

        // Unreachable.
        return null;
    }
}