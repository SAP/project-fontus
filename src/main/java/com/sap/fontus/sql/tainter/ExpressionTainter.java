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
	protected List<AssignmentValue> assignmentValues;

	ExpressionTainter(QueryParameters parameters, List<Expression> expressionReference) {
		this.parameters = parameters;
		this.expressionReference = expressionReference;
	}

	public void setAssignmentValues(List<AssignmentValue> assignmentValues) {
		this.assignmentValues = assignmentValues;
	}

	public List<AssignmentValue> getAssignmentValues() {
		return this.assignmentValues;
	}

	@Override
	public void visit(Column column) {
		if (column.getColumnName().compareToIgnoreCase("DEFAULT") == 0
				|| column.getColumnName().compareToIgnoreCase("FALSE") == 0
				|| column.getColumnName().compareToIgnoreCase("TRUE") == 0) {
			//Missmatched keywords, that are seen as columns, but are values
			this.addAssignmentValue(new AssignmentValue("0"));
			this.expressionReference.add(new StringValue("'0'"));
			this.addAssignmentValue(new AssignmentValue("0"));
		} else if (column.getTable() == null || column.getTable().getName() == null) {
			this.addAssignmentValue(new AssignmentValue(column.getColumnName()));
			// 'return' expression via global list
			Column newColumn = Utils.getTaintColumn(column);
			this.addAssignmentValue(new AssignmentValue(newColumn.getColumnName()));
			this.expressionReference
					.add(newColumn);
		} else {
			this.addAssignmentValue(new AssignmentValue(column.getColumnName()));
			this.expressionReference.add(Utils.getTaintColumn(column.getTable(), column)); //new Column(column.getTable() + "." + "`" + TAINT_PREFIX
					//+ column.getColumnName().replace("\"", "").replace("`", "") + "`"));
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
		this.addAssignmentValue(new AssignmentValue("?"));
		JdbcParameter newJdbcParamter = new JdbcParameter();
		newJdbcParamter.setIndex(jdbcParameter.getIndex());
		this.expressionReference.add(newJdbcParamter);
		this.addAssignmentValue(new AssignmentValue("?"));
	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {
		this.addAssignmentValue(new AssignmentValue(jdbcNamedParameter.getName()));
		JdbcNamedParameter newJdbcNamedParamter = new JdbcNamedParameter();
		newJdbcNamedParamter.setName(TAINT_PREFIX + jdbcNamedParameter.getName());
		this.expressionReference.add(newJdbcNamedParamter);
		this.addAssignmentValue(new AssignmentValue(jdbcNamedParameter.getName()));
	}

	@Override
	public void visit(StringValue stringValue) {
		this.addAssignmentValue(new AssignmentValue(stringValue.getValue()));
		// 'return' expression via global list
		StringValue newStringValue = new StringValue("'0'");
		this.expressionReference.add(newStringValue);
		this.addAssignmentValue(new AssignmentValue(newStringValue.getValue()));
	}

	@Override
	public void visit(SubSelect subSelect) {
		SelectTainter selectTainter = new SelectTainter(this.parameters);
		selectTainter.setAssignmentValues(this.assignmentValues);
		subSelect.getSelectBody().accept(selectTainter);
		if (subSelect.getWithItemsList() != null) {
			for (WithItem withItem : subSelect.getWithItemsList()) {
				SelectTainter innerSelectTainter = new SelectTainter(this.parameters);
				innerSelectTainter.setAssignmentValues(this.assignmentValues);
				withItem.accept(innerSelectTainter);
			}
		}
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
		this.addAssignmentValue(new AssignmentValue("0"));
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(DoubleValue doubleValue) {
		this.addAssignmentValue(new AssignmentValue(doubleValue.getValue()));
		StringValue newStringValue = new StringValue("'0'");
		this.expressionReference.add(newStringValue);
		this.addAssignmentValue(new AssignmentValue(newStringValue.getValue()));
	}

	@Override
	public void visit(LongValue longValue) {
		this.addAssignmentValue(new AssignmentValue(longValue.getValue()));
		StringValue newStringValue = new StringValue("'0'");
		this.expressionReference.add(newStringValue);
		this.addAssignmentValue(new AssignmentValue(newStringValue.getValue()));
	}

	@Override
	public void visit(HexValue hexValue) {

		StringValue newStringValue = new StringValue("'0'");
		this.expressionReference.add(newStringValue);
		this.addAssignmentValue(new AssignmentValue(newStringValue.getValue()));
	}

	@Override
	public void visit(DateValue dateValue) {
		StringValue newStringValue = new StringValue("'0'");
		this.expressionReference.add(newStringValue);
		this.addAssignmentValue(new AssignmentValue(newStringValue.getValue()));
	}

	@Override
	public void visit(TimeValue timeValue) {

		StringValue newStringValue = new StringValue("'0'");
		this.expressionReference.add(newStringValue);
		this.addAssignmentValue(new AssignmentValue(newStringValue.getValue()));
	}

	@Override
	public void visit(TimestampValue timestampValue) {
		StringValue newStringValue = new StringValue("'0'");
		this.expressionReference.add(newStringValue);
		this.addAssignmentValue(new AssignmentValue(newStringValue.getValue()));
	}

	@Override
	public void visit(InExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(Function arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(SignedExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(Addition arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(Division arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(Multiplication arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(Subtraction arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(AndExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(OrExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(Between arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(EqualsTo arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(GreaterThan arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(LikeExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(MinorThan arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(CaseExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(WhenClause arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(ExistsExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(Matches arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(BitwiseAnd arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(BitwiseOr arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(BitwiseXor arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(CastExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(Modulo arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(AnalyticExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(ExtractExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(IntervalExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(OracleHierarchicalExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(RegExpMatchOperator arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(JsonExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(RegExpMySQLOperator arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(UserVariable arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(NumericBind arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(KeepExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(MySQLGroupConcat arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(RowConstructor arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(OracleHint arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(TimeKeyExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	@Override
	public void visit(DateTimeLiteralExpression arg0) {
		// add '0' for correct column count
		this.expressionReference.add(new StringValue("'0'"));
		this.addAssignmentValue(new AssignmentValue("'0'"));
	}

	private void addAssignmentValue(AssignmentValue value) {
		if (this.assignmentValues != null) {
			this.assignmentValues.add(value);
		}
	}
}