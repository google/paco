grammar QuestionCondition;
@header {
package com.google.android.apps.paco.questioncondparser;
}
@lexer::header {
package com.google.android.apps.paco.questioncondparser;
}
@members {
   Environment environment;
   public QuestionConditionParser(TokenStream input, Environment environment) {
       this(input);
       this.environment = environment;
   }
}


comparison  returns [boolean value]
   : question_part LTE i=OBJECT { 
        if (environment.getValue($question_part.text) == null) {
          $value = false;
        } else {
          Object obj = environment.getValue($question_part.text);
          if (obj instanceof Integer) {
            $value =  ((Integer)obj) <= Integer.parseInt($i.text); 
          } else if (obj instanceof List) {
            Integer predValue = Integer.parseInt($i.text);
            List list = ((List)obj);
            if (list.size() != 1) {
              $value = false;
            } else {
              $value = ((Integer)list.get(0)) <= predValue;
            }
          } 
        }
      }
   | question_part LT i=INTEGER {         
        if (environment.getValue($question_part.text) == null) {
          $value = false;
        } else {
          Object obj = environment.getValue($question_part.text);
          if (obj instanceof Integer) {
            $value =  ((Integer)obj) < Integer.parseInt($i.text); 
          } else if (obj instanceof List) {
            Integer predValue = Integer.parseInt($i.text);
            List list = ((List)obj);
            if (list.size() != 1) {
              $value = false;
            } else {
              $value = ((Integer)list.get(0)) < predValue;
            }
          } 
        }
      }
    | question_part GTE i=INTEGER { 
        if (environment.getValue($question_part.text) == null) {
          $value = false;
        } else {
          Object obj = environment.getValue($question_part.text);
          if (obj instanceof Integer) {
            $value =  ((Integer)obj) >= Integer.parseInt($i.text); 
          } else if (obj instanceof List) {
            Integer predValue = Integer.parseInt($i.text);
            List list = ((List)obj);
            if (list.size() != 1) {
              $value = false;
            } else {
              $value = ((Integer)list.get(0)) >= predValue;
            }
          } 
        }
      }
   | question_part GT i=INTEGER { 
       if (environment.getValue($question_part.text) == null) {
         $value = false;
       } else {
         Object obj = environment.getValue($question_part.text);
         if (obj instanceof Integer) {
           $value =  ((Integer)obj) > Integer.parseInt($i.text); 
         } else if (obj instanceof List) {
           Integer predValue = Integer.parseInt($i.text);
           List list = ((List)obj);
           if (list.size() != 1) {
             $value = false;
           } else {
             $value = ((Integer)list.get(0)) > predValue;
           }
         } 
       }
     }
   | question_part EQ i=INTEGER { 
        if (environment.getValue($question_part.text) == null) {
          $value = false;
        } else {
          Object obj = environment.getValue($question_part.text);
          if (obj instanceof Integer) {
            $value =  ((Integer)obj) == Integer.parseInt($i.text); 
          } else if (obj instanceof List) {
            $value = ((List)obj).contains(Integer.parseInt($i.text));
          }
        }
      }
   | question_part NE i=INTEGER { 
        if (environment.getValue($question_part.text) == null) {
          $value = false;
        } else {
          Object obj = environment.getValue($question_part.text);
          if (obj instanceof Integer) {
            $value = ((Integer)obj) != Integer.parseInt($i.text); 
          } else if (obj instanceof List) {
            $value = !((List)obj).contains(Integer.parseInt($i.text));
          } else {
            $value = false; //default case
          }
        }
     }
   | question_part 'contains' i=INTEGER { 
        if (environment.getValue($question_part.text) == null) {
          $value = false;
        } else {
          Object obj = environment.getValue($question_part.text);
          if (obj instanceof List) {        
            $value = ((List)obj).contains(Integer.parseInt($i.text)); 
          } else {
            $value = false; // default case
          }
        }
      }
   ;

expression returns [boolean value]
   :   c=comparison {$value = $c.value;}
   ( OR c1=comparison {$value = $value || $c1.value; }
   | AND c1=comparison {$value = $value && $c1.value; }
   )*
   ;

question_part
:QUESTION_NAME { if (!environment.exists($QUESTION_NAME.text)) {
       throw new IllegalArgumentException("unknown reference: " + $QUESTION_NAME.text);
   }
  // if (!environment.correctType($QUESTION_NAME.text)) {
  //   throw new IllegalArgumentException("Does not have the proper response type: " + $QUESTION_NAME.text);
  // }
   }
       ;

OR : '||' ;
AND : '&&' ;
QUESTION_NAME
 :     ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
   ;

INTEGER :       '0'..'9'+
   ;

COMMENT
   :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
   |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
   ;

WS  :   ( ' '
       | '\t'
       | '\r'
       | '\n'
       ) {$channel=HIDDEN;}
   ;

STRING
   :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
   ;

CHAR:  '\'' ( ESC_SEQ | ~('\''|'\\') ) '\''
   ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
   :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
   |   UNICODE_ESC
   |   OCTAL_ESC
   ;

fragment
OCTAL_ESC
   :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
   |   '\\' ('0'..'7') ('0'..'7')
   |   '\\' ('0'..'7')
   ;

fragment
UNICODE_ESC
   :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
   ;



EQ      :        '=' | '==';
LT      :        '<';
LTE      :        '<=';
GT      :        '>';
GTE      :        '>=';
NE      :       '!=' ;

