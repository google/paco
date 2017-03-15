package com.pacoapp.paco.shared.util;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
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

public class ColumnNamesFinder implements SelectItemVisitor, SelectVisitor, ExpressionVisitor, FromItemVisitor, OrderByVisitor, ItemsListVisitor {
  private List<String> colNames = Lists.newArrayList();
  
  public List<String> getColumnList(Select select) {
    colNames = Lists.newArrayList();
    select.getSelectBody().accept(this);
    return colNames;
  }
  
  public void visit(PlainSelect plainSelect) {
    for(SelectItem sel : plainSelect.getSelectItems()){
      sel.accept(this);
    }
    
    if (plainSelect.getJoins() != null) {
      for (Iterator<Join> joinsIt = plainSelect.getJoins().iterator(); joinsIt.hasNext();) {
        Join join = joinsIt.next();
        join.getRightItem().accept(this);
        if(join.getOnExpression()!=null){
          join.getOnExpression().accept(this);
        }
      }
    }
    if (plainSelect.getGroupByColumnReferences() != null) {
      for (Iterator<Expression> gbcIt = plainSelect.getGroupByColumnReferences().iterator(); gbcIt.hasNext();) {
        Expression expr = gbcIt.next();
        expr.accept(this);
      }
    }
    if (plainSelect.getOrderByElements()!=null){
      for (Iterator<OrderByElement> obIt = plainSelect.getOrderByElements().iterator(); obIt.hasNext();) {
        OrderByElement obe = obIt.next();
        obe.accept(this);
      }
    }
    if (plainSelect.getHaving()!=null){
      Expression havingExpr = plainSelect.getHaving();
      havingExpr.accept(this);
    }
    
    if(plainSelect.getWhere()!=null){
      Expression whereExpr = plainSelect.getWhere();
      whereExpr.accept(this);
    }
  }
  
  @Override
  public void visit(AllColumns allColumns) {
    colNames.add(allColumns.toString());  
  }

  @Override
  public void visit(AllTableColumns allColumns) {
    colNames.add(allColumns.toString());
  }

  @Override
  public void visit(SelectExpressionItem selExprItem) {
    // TODO Auto-generated method stub
    selExprItem.getExpression().accept(this);
    
  }

  @Override
  public void visit(SetOperationList setOpList) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(WithItem withItem) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(NullValue nullValue) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(Function function) {
    // TODO Auto-generated method stub
    if(function.getParameters()!=null) {
      function.getParameters().accept(this);
    }
    
  }

  @Override
  public void visit(SignedExpression signedExpression) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(JdbcParameter jdbcParameter) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(JdbcNamedParameter jdbcNamedParameter) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(DoubleValue doubleValue) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(LongValue longValue) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(DateValue dateValue) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(TimeValue timeValue) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(TimestampValue timestampValue) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(Parenthesis parenthesis) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(StringValue stringValue) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(Addition addition) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(Division division) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(Multiplication multiplication) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(Subtraction subtraction) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(AndExpression andExpression) {
    andExpression.getLeftExpression().accept(this);
    andExpression.getRightExpression().accept(this); 
  }

  @Override
  public void visit(OrExpression orExpression) {
    orExpression.getLeftExpression().accept(this);
    orExpression.getRightExpression().accept(this);
    
  }

  @Override
  public void visit(Between between) {
    // TODO Auto-generated method stub
    between.getLeftExpression().accept(this);
    between.getBetweenExpressionStart().accept(this);
    between.getBetweenExpressionStart().accept(this);
  }

  @Override
  public void visit(EqualsTo equalsTo) {
    equalsTo.getLeftExpression().accept(this);
    
  }

  @Override
  public void visit(GreaterThan greaterThan) {
    greaterThan.getLeftExpression().accept(this);
  }

  @Override
  public void visit(GreaterThanEquals greaterThanEquals) {
    greaterThanEquals.getLeftExpression().accept(this);
    
  }

  @Override
  public void visit(InExpression inExpression) {
    inExpression.getLeftExpression().accept(this);
    inExpression.getRightItemsList().accept(this);
  }

  @Override
  public void visit(IsNullExpression isNullExpression) {
    isNullExpression.getLeftExpression().accept(this);
  }

  @Override
  public void visit(LikeExpression likeExpression) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(MinorThan minorThan) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(MinorThanEquals minorThanEquals) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(NotEqualsTo notEqualsTo) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(Column tableColumn) {
    colNames.add(tableColumn.getFullyQualifiedName());
    
  }

  @Override
  public void visit(SubSelect subSelect) {
   subSelect.getSelectBody().accept(this);
    
  }

  @Override
  public void visit(CaseExpression caseExpression) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(WhenClause whenClause) {
    // TODO Auto-generated method stub
    whenClause.accept(this);
    
  }

  @Override
  public void visit(ExistsExpression existsExpression) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(AllComparisonExpression allComparisonExpression) {
    // TODO Auto-generated method stub
    
    
  }

  @Override
  public void visit(AnyComparisonExpression anyComparisonExpression) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(Concat concat) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(Matches matches) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(BitwiseAnd bitwiseAnd) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(BitwiseOr bitwiseOr) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(BitwiseXor bitwiseXor) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(CastExpression cast) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(Modulo modulo) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(AnalyticExpression aexpr) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(ExtractExpression eexpr) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(IntervalExpression iexpr) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(OracleHierarchicalExpression oexpr) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(RegExpMatchOperator rexpr) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(Table tableName) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(SubJoin subjoin) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(LateralSubSelect lateralSubSelect) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(ValuesList valuesList) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(OrderByElement orderBy) {
//    orderBy.accept(this);
    orderBy.getExpression().accept(this);
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(ExpressionList expressionList) {
    // TODO Auto-generated method stub
    for( Iterator<Expression> itr = expressionList.getExpressions().iterator();itr.hasNext();){
      itr.next().accept(this);
    }
    
  }

  @Override
  public void visit(MultiExpressionList multiExprList) {
    // TODO Auto-generated method stub
    if(multiExprList.getExprList()!=null) {
      for(Iterator<ExpressionList> itr = multiExprList.getExprList().iterator(); itr.hasNext();){
        visit(itr.next());
      }
    }
  }

  @Override
  public void visit(TableFunction arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(HexValue arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(WithinGroupExpression arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(JsonExpression arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(RegExpMySQLOperator arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(UserVariable arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(NumericBind arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(KeepExpression arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(MySQLGroupConcat arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(RowConstructor arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(OracleHint arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(TimeKeyExpression arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(DateTimeLiteralExpression arg0) {
    // TODO Auto-generated method stub
    
  }
}