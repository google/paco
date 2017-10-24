package com.pacoapp.paco.shared.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.WithinGroupExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

public class QueryPreprocessor implements SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor,
                               SelectItemVisitor, OrderByVisitor {

  private Map<String, Class> validColumnNamesDataTypeInDb;
  private Map<String, Long> dateParamWithLong = Maps.newHashMap();
  private List<String> requestedDateColumns;
  private String invalidColumnName;
  private String invalidDataType;
  private boolean containsExperimentIdClause;
  private boolean containsWhoClause;
  private static List<Class> allPossibleConstantExpTypes = Lists.newArrayList();
  private Set<String> whoClauseValues = Sets.newHashSet();
  private Set<Long> expIdClauseValues = Sets.newHashSet();
  private boolean webRequest = false;
  private boolean isOutputColumnsPresent;
  private String probableSqlInjectionClause;

  static {
    allPossibleConstantExpTypes.add(StringValue.class);
    allPossibleConstantExpTypes.add(DoubleValue.class);
    allPossibleConstantExpTypes.add(DateValue.class);
    allPossibleConstantExpTypes.add(LongValue.class);
    allPossibleConstantExpTypes.add(TimestampValue.class);
    allPossibleConstantExpTypes.add(TimeValue.class);
  }

  public QueryPreprocessor(Select select, Map<String, Class> validColumnNames, boolean webRequest,
                           List<String> reqDateColNames) {
    requestedDateColumns = reqDateColNames;
    validColumnNamesDataTypeInDb = validColumnNames;
    this.webRequest = webRequest;
    select.getSelectBody().accept(this);

  }

  public String probableSqlInjection() {
    return probableSqlInjectionClause;
  }

  public String getInvalidDataType() {
    return invalidDataType;
  }

  public String getInvalidColumnName() {
    return invalidColumnName;
  }

  public void setInvalidColumnName(String invalidColumnName) {
    this.invalidColumnName = invalidColumnName;
  }

  public boolean containExpIdClause() {
    return containsExperimentIdClause;
  }

  public boolean containWhoClause() {
    return containsWhoClause;
  }

  public Set<Long> getExpIdValues() {
    return expIdClauseValues;
  }

  public Set<String> getWhoClause() {
    return whoClauseValues;
  }

  public boolean isOutputColumnsPresent() {
    return isOutputColumnsPresent;
  }
  
  public boolean isExpIdOrExpServerIdPresent(String colName) {
    boolean isPresent = false;
    if (validColumnNamesDataTypeInDb.containsKey(Constants.EXPERIMENT_SERVER_ID) && colName.equalsIgnoreCase(Constants.EXPERIMENT_SERVER_ID)) {
        isPresent = true;
    } else if (validColumnNamesDataTypeInDb.containsKey(EventBaseColumns.EXPERIMENT_ID) && colName.equalsIgnoreCase(EventBaseColumns.EXPERIMENT_ID)) {
      isPresent = true;
    } 
    return isPresent;
  }

  public void visit(PlainSelect plainSelect) {
    plainSelect.getFromItem().accept(this);

    if (plainSelect.getJoins() != null) {
      for (Iterator<Join> joinsIt = plainSelect.getJoins().iterator(); joinsIt.hasNext();) {
        Join join = joinsIt.next();
        join.getRightItem().accept(this);
      }
    }
    if (plainSelect.getWhere() != null) {
      plainSelect.getWhere().accept(this);
    }
    if (plainSelect.getSelectItems() != null) {
      List<SelectItem> sItems = plainSelect.getSelectItems();
      for (SelectItem si : sItems) {
        si.accept(this);
      }
    }
    if (plainSelect.getOrderByElements() != null) {
      List<OrderByElement> orderElements = plainSelect.getOrderByElements();
      for (OrderByElement oBy : orderElements) {
        oBy.accept(this);
      }
    }

    if (plainSelect.getGroupByColumnReferences() != null) {
      List<Expression> grBys = plainSelect.getGroupByColumnReferences();
      for (Expression gBy : grBys) {
        gBy.accept(this);
      }
    }

  }

  // public void visit(Union union) {
  // for (Iterator iter = union.getPlainSelects().iterator(); iter.hasNext();) {
  // PlainSelect plainSelect = (PlainSelect) iter.next();
  // visit(plainSelect);
  // }
  // }

  public void visit(Table tableName) {

  }

  public void visit(SubSelect subSelect) {
    subSelect.getSelectBody().accept(this);
  }

  public void visit(Addition addition) {
    visitBinaryExpression(addition);
  }

  public void visit(AndExpression andExpression) {
    visitBinaryExpression(andExpression);
  }

  public void visit(Between between) {
    between.getLeftExpression().accept(this);
    between.getBetweenExpressionStart().accept(this);
    between.getBetweenExpressionEnd().accept(this);
  }

  public void visit(Column tableColumn) {
    String colName = tableColumn.getColumnName().toLowerCase();
    if ((validColumnNamesDataTypeInDb.get(colName) == null)) {
      invalidColumnName = colName;
    } else if (colName.equalsIgnoreCase(Constants.WHO)) {
      containsWhoClause = true;
    } else if (colName.equalsIgnoreCase(Constants.WHEN)) {
      tableColumn.setColumnName(Constants.WHEN_WITH_BACKTICK);
    } else if (colName.equalsIgnoreCase(OutputBaseColumns.NAME)) {
      isOutputColumnsPresent = true;
    } else if (colName.equalsIgnoreCase(OutputBaseColumns.ANSWER)) {
      isOutputColumnsPresent = true;
    }
  }

  public void visit(Division division) {
    visitBinaryExpression(division);
  }

  public void visit(DoubleValue doubleValue) {
  }

  public void visit(EqualsTo equalsTo) {
    visitBinaryExpression(equalsTo);
  }

  public void visit(Function function) {
  }

  public void visit(GreaterThan greaterThan) {
    visitBinaryExpression(greaterThan);
  }

  public void visit(GreaterThanEquals greaterThanEquals) {
    visitBinaryExpression(greaterThanEquals);
  }

  public void visit(InExpression inExpression) {
    ItemsList ril = inExpression.getRightItemsList();
    Expression le = inExpression.getLeftExpression();
    if (le != null) {
      if (le instanceof Column) {
        Column leftColumn = (Column) le;
        String leftColName = leftColumn.getColumnName().toLowerCase();
        if (requestedDateColumns != null && requestedDateColumns.contains(leftColName)) {
          if (ril instanceof ItemsList) {
            ExpressionList expList = (ExpressionList) ril;
            List<Expression> elList = expList.getExpressions();
            List<Expression> newUtcList = Lists.newArrayList();
            
            for (Expression expr : elList) {
              if (expr instanceof StringValue) {
                if (webRequest) {
                  newUtcList.add(expr);
                } else {
                  LongValue lgVal = new LongValue(TimeUtil.convertDateToLong(expr.toString()));
                  dateParamWithLong.put(expr.toString(), lgVal.getValue());
                }
              } else {
                invalidDataType = expr.toString();
              }
            }
            expList.setExpressions(newUtcList);
          } else {
            invalidDataType = ril.toString();
          }
        } else if (isExpIdOrExpServerIdPresent(leftColName)) {
          containsExperimentIdClause = true;
          if (ril instanceof ItemsList) {
            ExpressionList expList = (ExpressionList) ril;
            List<Expression> elList = expList.getExpressions();
            for (Expression expr : elList) {
              if (expr instanceof LongValue) {
                expIdClauseValues.add(((LongValue) expr).getValue());
              } else {
                invalidDataType = expr.toString();
              }
            }
          }
        } else if (validColumnNamesDataTypeInDb.containsKey(Constants.WHO) && leftColName.equalsIgnoreCase(Constants.WHO)) {
          if (ril instanceof ItemsList) {
            ExpressionList expList = (ExpressionList) ril;
            List<Expression> elList = expList.getExpressions();
            for (Expression expr : elList) {
              if (expr instanceof StringValue) {
                whoClauseValues.add(((StringValue) expr).getValue());
              } else {
                invalidDataType = expr.toString();
              }
            }
          }
        } else if ((validColumnNamesDataTypeInDb.get(leftColName) != null)) {
          Class dataType = validColumnNamesDataTypeInDb.get(leftColName);
          if (ril instanceof ExpressionList) {
            ExpressionList expList = (ExpressionList) ril;
            List<Expression> elList = expList.getExpressions();
            for (Expression expr : elList) {
              if (!(expr.getClass().equals(dataType))) {
                invalidDataType = expr.toString();
              }
            }
          }
        }
        
      } else if (allPossibleConstantExpTypes.contains(le.getClass())){//left expr can never be a constant(string, number, char...) value. this for preventing sql injection
        probableSqlInjectionClause = inExpression.toString();
      }
    }

    inExpression.getLeftExpression().accept(this);
    inExpression.getRightItemsList().accept(this);
  }

  // public void visit(InverseExpression inverseExpression) {
  // inverseExpression.getExpression().accept(this);
  // }

  public void visit(IsNullExpression isNullExpression) {
  }

  public void visit(JdbcParameter jdbcParameter) {
  }

  public void visit(LikeExpression likeExpression) {
    visitBinaryExpression(likeExpression);
  }

  public void visit(ExistsExpression existsExpression) {
    existsExpression.getRightExpression().accept(this);
  }

  public void visit(LongValue longValue) {
  }

  public void visit(MinorThan minorThan) {
    visitBinaryExpression(minorThan);
  }

  public void visit(MinorThanEquals minorThanEquals) {
    visitBinaryExpression(minorThanEquals);
  }

  public void visit(Multiplication multiplication) {
    visitBinaryExpression(multiplication);
  }

  public void visit(NotEqualsTo notEqualsTo) {
    visitBinaryExpression(notEqualsTo);
  }

  public void visit(NullValue nullValue) {
  }

  public void visit(OrExpression orExpression) {
    visitBinaryExpression(orExpression);
  }

  public void visit(Parenthesis parenthesis) {
    parenthesis.getExpression().accept(this);
  }

  public void visit(StringValue stringValue) {
  }

  public void visit(Subtraction subtraction) {
    visitBinaryExpression(subtraction);
  }

  public void visitBinaryExpression(BinaryExpression binaryExpression) {
    Expression re = binaryExpression.getRightExpression();
    Expression le = binaryExpression.getLeftExpression();
    if (le != null) {
      if (le instanceof Column) {
        String leftColName = ((Column) le).getColumnName().toLowerCase();
        if (requestedDateColumns != null && requestedDateColumns.contains(leftColName)) {
          if (re instanceof StringValue) {
              if (!webRequest) {
                LongValue lgVal = new LongValue(TimeUtil.convertDateToLong(re.toString().substring(1,re.toString().length()-1)));
                dateParamWithLong.put(re.toString(), lgVal.getValue());
              }
          } else {
            invalidDataType = re.toString();
          }
        } else if (validColumnNamesDataTypeInDb.containsKey(Constants.WHO) && leftColName.equalsIgnoreCase(Constants.WHO)) {
          Class dataType = validColumnNamesDataTypeInDb.get(leftColName);
          if (re.getClass().equals(dataType)) {
            whoClauseValues.add(re.toString());
          } else {
            invalidDataType = re.toString();
          }
        } else if (isExpIdOrExpServerIdPresent(leftColName)) {
           Class dataType = validColumnNamesDataTypeInDb.get(leftColName);
           // should be changed here and not in visit of Column because, the parser does not go to the visit column method before visit of binary expression.
           // We also need to know, if the clause has exp id or exp server id. So, we do the check and also change the containsExperimentIdClause value
           // and then perform the manipulation.
           containsExperimentIdClause = true;
          if (re.getClass().equals(dataType)) {
            expIdClauseValues.add(Long.parseLong(re.toString()));
          } else {
            invalidDataType = re.toString();
          }
        } else if ((validColumnNamesDataTypeInDb.get(leftColName) != null)) {
          Class dataType = validColumnNamesDataTypeInDb.get(leftColName);
          if (!(re.getClass().equals(dataType))) {
            invalidDataType = re.toString();
          }
        }
      } else if (allPossibleConstantExpTypes.contains(le.getClass())) {
        probableSqlInjectionClause = binaryExpression.toString();
      }
    }

    binaryExpression.getLeftExpression().accept(this);
    binaryExpression.getRightExpression().accept(this);
  }

  public void visit(ExpressionList expressionList) {
    for (Iterator iter = expressionList.getExpressions().iterator(); iter.hasNext();) {
      Expression expression = (Expression) iter.next();
      expression.accept(this);
    }

  }

  public void visit(DateValue dateValue) {
  }

  public void visit(TimestampValue timestampValue) {
  }

  public void visit(TimeValue timeValue) {
  }

  public void visit(CaseExpression caseExpression) {
  }

  public void visit(WhenClause whenClause) {
  }

  public void visit(AllComparisonExpression allComparisonExpression) {
    allComparisonExpression.getSubSelect().getSelectBody().accept(this);
  }

  public void visit(AnyComparisonExpression anyComparisonExpression) {
    anyComparisonExpression.getSubSelect().getSelectBody().accept(this);
  }

  public void visit(SubJoin subjoin) {
    subjoin.getLeft().accept(this);
    subjoin.getJoin().getRightItem().accept(this);
  }

  @Override
  public void visit(MultiExpressionList arg0) {

  }

  @Override
  public void visit(SignedExpression arg0) {

  }

  @Override
  public void visit(JdbcNamedParameter arg0) {

  }

  @Override
  public void visit(Concat arg0) {

  }

  @Override
  public void visit(Matches arg0) {

  }

  @Override
  public void visit(BitwiseAnd arg0) {

  }

  @Override
  public void visit(BitwiseOr arg0) {

  }

  @Override
  public void visit(BitwiseXor arg0) {

  }

  @Override
  public void visit(CastExpression arg0) {

  }

  @Override
  public void visit(Modulo arg0) {

  }

  @Override
  public void visit(AnalyticExpression arg0) {

  }

  @Override
  public void visit(ExtractExpression arg0) {

  }

  @Override
  public void visit(IntervalExpression arg0) {

  }

  @Override
  public void visit(OracleHierarchicalExpression arg0) {

  }

  @Override
  public void visit(RegExpMatchOperator arg0) {

  }

  @Override
  public void visit(LateralSubSelect arg0) {

  }

  @Override
  public void visit(ValuesList arg0) {

  }

  @Override
  public void visit(SetOperationList arg0) {

  }

  @Override
  public void visit(WithItem arg0) {

  }

  @Override
  public void visit(HexValue arg0) {

  }

  @Override
  public void visit(WithinGroupExpression arg0) {

  }

  @Override
  public void visit(JsonExpression arg0) {

  }

  @Override
  public void visit(RegExpMySQLOperator arg0) {

  }

  @Override
  public void visit(UserVariable arg0) {

  }

  @Override
  public void visit(NumericBind arg0) {

  }

  @Override
  public void visit(KeepExpression arg0) {

  }

  @Override
  public void visit(MySQLGroupConcat arg0) {

  }

  @Override
  public void visit(RowConstructor arg0) {

  }

  @Override
  public void visit(OracleHint arg0) {

  }

  @Override
  public void visit(TimeKeyExpression arg0) {

  }

  @Override
  public void visit(DateTimeLiteralExpression arg0) {

  }

  @Override
  public void visit(TableFunction arg0) {

  }

  @Override
  public void visit(AllColumns allColumns) {

  }

  @Override
  public void visit(AllTableColumns allTableColumns) {

  }

  @Override
  public void visit(SelectExpressionItem selectExpressionItem) {
    selectExpressionItem.getExpression().accept(this);
  }

  @Override
  public void visit(OrderByElement orderBy) {
    orderBy.getExpression().accept(this);
  }

  public Map<String, Long> getDateParamWithLong() {
    return dateParamWithLong;
  }

  public void setDateParamWithLong(Map<String, Long> dateParamWithLong) {
    this.dateParamWithLong = dateParamWithLong;
  }
}
