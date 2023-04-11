package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.List;

import static com.sap.fontus.Constants.TAINT_PREFIX;


public class ExpressionTainter extends ExpressionVisitorAdapter {

	protected final QueryParameters parameters;
	protected final List<Expression> expressionReference;

	ExpressionTainter(QueryParameters parameters, List<Expression> expressionReference) {
		super();
		this.parameters = parameters;
		this.expressionReference = expressionReference;
	}

	private void checkBinaryExpressionForParameters(BinaryExpression exp) {
		if(exp.getLeftExpression() instanceof JdbcParameter) {
			this.parameters.addParameter(ParameterType.ASSIGNMENT_UNTAINTED);
		}
		if(exp.getRightExpression() instanceof JdbcParameter) {
			this.parameters.addParameter(ParameterType.ASSIGNMENT_UNTAINTED);
		}
	}

	@Override
	public void visit(Column column) {
		if (column.getColumnName().compareToIgnoreCase("DEFAULT") == 0
				|| column.getColumnName().compareToIgnoreCase("FALSE") == 0
				|| column.getColumnName().compareToIgnoreCase("TRUE") == 0) {
			//Missmatched keywords, that are seen as columns, but are values
			this.expressionReference.add(new StringValue("'0'"));
		} else if (column.getTable() == null || column.getTable().getName() == null) {
			// 'return' expression via global list
			Column newColumn = Utils.getTaintColumn(column);
			this.expressionReference
					.add(newColumn);
		} else {
			this.expressionReference.add(Utils.getTaintColumn(column.getTable(), column));
		}
	}

	@Override
	public void visit(Concat concat) {
		// 'Return' new concat with tainted left and right expression
		Concat taintConcat = new Concat();
		concat.getLeftExpression().accept(this);
		if (!this.expressionReference.isEmpty()) {
			taintConcat.setLeftExpression(this.expressionReference.get(0));
			this.expressionReference.clear();
		}
		concat.getRightExpression().accept(this);
		if (!this.expressionReference.isEmpty()) {
			taintConcat.setRightExpression(this.expressionReference.get(0));
			this.expressionReference.clear();
		}
		this.expressionReference.add(taintConcat);
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		this.parameters.addParameter(ParameterType.ASSIGNMENT);
		JdbcParameter taintedParameter = new JdbcParameter();
		taintedParameter.setIndex(jdbcParameter.getIndex());
		this.expressionReference.add(taintedParameter);
	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {
		JdbcNamedParameter taintedParameter = new JdbcNamedParameter();
		taintedParameter.setName(TAINT_PREFIX + jdbcNamedParameter.getName());
		this.expressionReference.add(taintedParameter);
	}

	@Override
	public void visit(StringValue stringValue) {
		// 'return' expression via global list
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(SubSelect subSelect) {
		this.parameters.begin(StatementType.SUB_SELECT);
		SelectTainter selectTainter = new SelectTainter(this.parameters);
		subSelect.getSelectBody().accept(selectTainter);
		if (subSelect.getWithItemsList() != null) {
			for (WithItem withItem : subSelect.getWithItemsList()) {
				SelectTainter innerSelectTainter = new SelectTainter(this.parameters);
				withItem.accept(innerSelectTainter);
			}
		}
		this.parameters.end(StatementType.SUB_SELECT);
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		// 'return' new Parenthsis obj with tainted expression
		parenthesis.getExpression().accept(this);
		if (!this.expressionReference.isEmpty()) {
			Expression expr = this.expressionReference.get(0);
			this.expressionReference.clear();
			this.expressionReference.add(new Parenthesis(expr));
		}
	}

	@Override
	public void visit(NullValue arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(DoubleValue doubleValue) {
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(LongValue longValue) {
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(HexValue hexValue) {
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(DateValue dateValue) {
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(TimeValue timeValue) {
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(TimestampValue timestampValue) {
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(InExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Function arg0) {
		// add '0' for correct column count
		ExpressionList parameters = arg0.getParameters();
		if(parameters != null && parameters.getExpressions() != null) {
			List<Expression> expressions = parameters.getExpressions();
			QueryParameters params = this.parameters;
			List<Expression> expressionRefs = this.expressionReference;

			for(Expression expr: expressions) {
				expr.accept(new ExpressionVisitorAdapter() {
					@Override
					public void visit(JdbcParameter jdbcParameter) {
						params.addParameter(ParameterType.ASSIGNMENT);
						JdbcParameter taintedParameter = new JdbcParameter();
						taintedParameter.setIndex(jdbcParameter.getIndex());
						expressionRefs.add(taintedParameter);
					}
				});
			}
		}
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(SignedExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Addition addition) {
		this.checkBinaryExpressionForParameters(addition);
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Division arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Multiplication arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Subtraction expr) {
		this.checkBinaryExpressionForParameters(expr);
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(AndExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(OrExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Between arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(EqualsTo arg0) {
		this.checkBinaryExpressionForParameters(arg0);
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(GreaterThan arg0) {
		this.checkBinaryExpressionForParameters(arg0);
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(LikeExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(MinorThan arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(CaseExpression caseExpression) {
		for (WhenClause when : caseExpression.getWhenClauses()) {
			when.accept(this);
		}
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(WhenClause whenClause) {
		whenClause.getWhenExpression().accept(this);
		whenClause.getThenExpression().accept(this);
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(ExistsExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Matches arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(BitwiseAnd arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(BitwiseOr arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(BitwiseXor arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(CastExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Modulo arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(AnalyticExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(ExtractExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(IntervalExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(OracleHierarchicalExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(RegExpMatchOperator arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(JsonExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(RegExpMySQLOperator arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(UserVariable arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(NumericBind arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(KeepExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(MySQLGroupConcat arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(RowConstructor arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(OracleHint arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(TimeKeyExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(DateTimeLiteralExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
	}
}