{
var table = {};
table["a1"] = 5;
table["a"] = 9;
table["bb"] = "spaghetti";
table["c"] = 9;
table["ban-ana"] = 10;
table["ls"] = "1,2,3";

function operate(operator, left, right) {

  if (operator == "eq") {
    return eq(left, right);
  } else if (operator == "ge") {
    return ge(left, right);
  } else if (operator == "gt") {
    return gt(left, right);
  } else if (operator == "le") {
    return le(left, right);
  } else if (operator == "lt") {
    return lt(left, right);
  } else if (operator == "ne") {
    return ne(left, right);
  } else {
    return false;
  }
}

function eq(left, right) {
  return left == right;
}

function ge(left, right) {
  return left >= right;
}

function gt(left, right) {
  return left > right;
}

function le(left, right) {
  return left <= right;
}

function lt(left, right) {
  return left < right;
}

function ne(left, right) {
  return left != right;
}


function test(operator, table, left, right) {
  var lhs = table[left];
  if (lhs && lhs.length > 0 && lhs.indexOf(",") != -1) { 
    var l = lhs.split(",");  
    for(var i in l) { 
      if (operate(operator, l[i], right)) { 
        return true; 
      } 
    } 
    return false; 
  } else { 
    return operate(operator, lhs, right); 
  }
}

}



start
  = orstmt
  

orstmt
  = eqstmt3:andstmt _ "||" _ eqstmt4:orstmt { return eqstmt3 || eqstmt4; }
  / andstmt

andstmt
  = eqstmt3:eqstmt _ "&&" _ eqstmt4:andstmt { return eqstmt3 && eqstmt4; }
  / eqstmt

eqstmt
  =  left:symbol _ "==" _ right:value { return test("eq", table, left, right); } 
  / containsstmt

containsstmt
  =  left:symbol _ "contains" _ right:value { return test("eq", table, left, right); } 
  / gtstmt

gtstmt
  = left:symbol _ ">" _ right:value { return test("gt", table, left, right);  }
  / gtestmt

gtestmt
  = left:symbol _ ">=" _ right:value { return test("ge", table, left, right);  }
  / ltstmt

ltstmt
  = left:symbol _ "<" _ right:value { return test("lt", table, left, right);  }
  / ltestmt

ltestmt
  = left:symbol _ "<=" _ right:value { return test("le", table, left, right);  }
  / nestmt


nestmt
  = left:symbol _ "!=" _ right:value { return test("ne", table, left, right); }
  / "(" _ orstmt:orstmt _ ")" { return orstmt; }



value 
  = integer
  / string

integer "integer"
  = digits:[0-9]+ { return parseInt(digits.join(""), 10); }

symbol "symbol"
  = first:[A-Za-z] rest:[A-Za-z0-9-_]*  { return first + rest.join(""); }

string
  = "\"" alpha:[A-Za-z_0-9]+ "\"" { return alpha.join(""); }

_  = [ \r\n\t]*
