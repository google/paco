{
var table = {};
table["a1"] = 5;
table["a"] = 9;
table["bb"] = "spaghetti";
table["c"] = 9;
table["ban-ana"] = 10;
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
  =  left:symbol _ "==" _ right:value  { return table[left] == right; }
  / gtstmt

gtstmt
  = left:symbol _ ">" _ right:value { return table[left] > right; }
  / gtestmt

gtestmt
  = left:symbol _ ">=" _ right:value { return table[left] >= right; }
  / ltstmt

ltstmt
  = left:symbol _ "<" _ right:value { return table[left] < right; }
  / ltestmt

ltestmt
  = left:symbol _ "<=" _ right:value { return table[left] <= right; }
  / nestmt


nestmt
  = left:symbol _ "!=" _ right:value { return table[left] != right; }
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
