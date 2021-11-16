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

	private List<Taint> taints;
	private List<Expression> expressionReference;

	ExpressionTainter(List<Taint> taints, List<Expression> expressionReference) {
		this.taints = taints;
		this.expressionReference = expressionReference;
	}

	@Override
	public void visit(Column column) {
		if(column.getColumnName().compareToIgnoreCase("DEFAULT") == 0
				|| column.getColumnName().compareToIgnoreCase("FALSE") == 0
				|| column.getColumnName().compareToIgnoreCase("TRUE") == 0)
			//Missmatched keywords, that are seen as columns, but are values
			expressionReference.add(new StringValue("'0'"));
		else if (column.getTable() == null || column.getTable().getName() == null)
			// 'return' expression via global list
			expressionReference
					.add(new Column("`" + TAINT_PREFIX + column.getColumnName().replace("\"", "").replace("`", "")  + "`"));
		else
			expressionReference.add(new Column(column.getTable() + "." + "`" + TAINT_PREFIX
					+ column.getColumnName().replace("\"", "").replace("`", "") + "`"));
	}

	@Override
	public void visit(Concat concat) {
		// 'Return' new concat with tainted left and right expression
		Concat taintConcat = new Concat();
		concat.getLeftExpression().accept(this);
		if (!expressionReference.isEmpty()) {
			taintConcat.setLeftExpression(expressionReference.get(0));
			expressionReference.clear();
		}
		concat.getRightExpression().accept(this);
		if (!expressionReference.isEmpty()) {
			taintConcat.setRightExpression(expressionReference.get(0));
			expressionReference.clear();
		}
		expressionReference.add(taintConcat);
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		JdbcParameter newJdbcParamter = new JdbcParameter();
		newJdbcParamter.setIndex(jdbcParameter.getIndex());
		expressionReference.add(newJdbcParamter);
	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {
		JdbcNamedParameter newJdbcNamedParamter = new JdbcNamedParameter();
		newJdbcNamedParamter.setName(TAINT_PREFIX + jdbcNamedParameter.getName());
		expressionReference.add(newJdbcNamedParamter);
	}

	@Override
	public void visit(StringValue stringValue) {
		// '' around new String Values needed, otherwise first and last char
		// replaced
		String taintBits = "0";
		for (Taint taint : taints) {
			if (taint.getName().compareTo(stringValue.getValue()) == 0) {
				taintBits = taint.getTaintBits();
				break;
			}
		}
		// 'return' expression via global list
		expressionReference.add(new StringValue("'" + taintBits + "'"));
	}

	@Override
	public void visit(SubSelect subSelect) {
		subSelect.getSelectBody().accept(new SelectTainter(taints));
		if (subSelect.getWithItemsList() != null) {
			for (WithItem withItem : subSelect.getWithItemsList()) {
				withItem.accept(new SelectTainter(taints));
			}
		}
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		// 'return' new Parenthsis obj with tainted expression
		parenthesis.getExpression().accept(this);
		if (!expressionReference.isEmpty()) {
			Expression expr = expressionReference.get(0);
			expressionReference.clear();
			expressionReference.add(new Parenthesis(expr));
		}
	}

	@Override
	public void visit(NullValue arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(DoubleValue doubleValue) {
		// '' around new String Values needed, otherwise first and last char
		// replaced
		String taintBits = "0";
		for (Taint taint : taints) {
			if (taint.getName().compareTo(Double.toString(doubleValue.getValue())) == 0) {
				taintBits = taint.getTaintBits();
				break;
			}
		}
		expressionReference.add(new StringValue("'" + taintBits + "'"));
	}

	@Override
	public void visit(LongValue longValue) {
		// '' around new String Values needed, otherwise first and last char
		// replaced
		String taintBits = "0";
		for (Taint taint : taints) {
			if (taint.getName().compareTo(longValue.getStringValue()) == 0) {
				taintBits = taint.getTaintBits();
				break;
			}
		}
		expressionReference.add(new StringValue("'" + taintBits + "'"));
	}

	@Override
	public void visit(HexValue hexValue) {
		// '' around new String Values needed,otherwise first and last char
		// replaced
		String taintBits = "0";
		for (Taint taint : taints) {
			if (taint.getName().compareTo(hexValue.getValue()) == 0) {
				taintBits = taint.getTaintBits();
				break;
			}
		}
		expressionReference.add(new StringValue("'" + taintBits + "'"));
	}

	@Override
	public void visit(DateValue dateValue) {
		// '' around new String Values needed,otherwise first and last char
		// replaced
		String taintBits = "0";
		for (Taint taint : taints) {
			if (taint.getName().compareTo(dateValue.getValue().toString()) == 0) {
				taintBits = taint.getTaintBits();
				break;
			}
		}
		expressionReference.add(new StringValue("'" + taintBits + "'"));
	}

	@Override
	public void visit(TimeValue timeValue) {
		// '' around new String Values needed,otherwise first and last char
		// replaced
		String taintBits = "0";
		for (Taint taint : taints) {
			if (taint.getName().compareTo(timeValue.getValue().toString()) == 0) {
				taintBits = taint.getTaintBits();
				break;
			}
		}
		expressionReference.add(new StringValue("'" + taintBits + "'"));
	}

	@Override
	public void visit(TimestampValue timestampValue) {
		// '' around new String Values needed, otherwise first and last
		// char replaced
		String taintBits = "0";
		for (Taint taint : taints) {
			if (taint.getName().compareTo(timestampValue.getValue().toString()) == 0) {
				taintBits = taint.getTaintBits();
				break;
			}
		}
		expressionReference.add(new StringValue("'" + taintBits + "'"));
	}

	@Override
	public void visit(InExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Function arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(SignedExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Addition arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Division arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Multiplication arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Subtraction arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(AndExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(OrExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Between arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(EqualsTo arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(GreaterThan arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(LikeExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(MinorThan arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(CaseExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(WhenClause arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(ExistsExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Matches arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(BitwiseAnd arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(BitwiseOr arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(BitwiseXor arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(CastExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(Modulo arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(AnalyticExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(ExtractExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(IntervalExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(OracleHierarchicalExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(RegExpMatchOperator arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(JsonExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(RegExpMySQLOperator arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(UserVariable arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(NumericBind arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(KeepExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(MySQLGroupConcat arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(RowConstructor arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(OracleHint arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(TimeKeyExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}

	@Override
	public void visit(DateTimeLiteralExpression arg0) {
		// add '0' for correct column count
		expressionReference.add(new StringValue("'0'"));
	}
}