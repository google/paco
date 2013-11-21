// $ANTLR 3.2 Sep 23, 2009 12:02:23 /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g 2013-11-04 13:57:14

package com.google.android.apps.paco.questioncondparser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class QuestionConditionParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "LTE", "INTEGER", "LT", "GTE", "GT", "EQ", "NE", "OR", "AND", "QUESTION_NAME", "COMMENT", "WS", "ESC_SEQ", "STRING", "CHAR", "HEX_DIGIT", "UNICODE_ESC", "OCTAL_ESC", "'contains'"
    };
    public static final int INTEGER=5;
    public static final int LT=6;
    public static final int T__22=22;
    public static final int UNICODE_ESC=20;
    public static final int GTE=7;
    public static final int OCTAL_ESC=21;
    public static final int QUESTION_NAME=13;
    public static final int CHAR=18;
    public static final int HEX_DIGIT=19;
    public static final int AND=12;
    public static final int EOF=-1;
    public static final int LTE=4;
    public static final int WS=15;
    public static final int ESC_SEQ=16;
    public static final int OR=11;
    public static final int GT=8;
    public static final int EQ=9;
    public static final int COMMENT=14;
    public static final int STRING=17;
    public static final int NE=10;

    // delegates
    // delegators


        public QuestionConditionParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public QuestionConditionParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return QuestionConditionParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g"; }


       Environment environment;
       public QuestionConditionParser(TokenStream input, Environment environment) {
           this(input);
           this.environment = environment;
       }



    // $ANTLR start "comparison"
    // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:17:1: comparison returns [boolean value] : ( question_part LTE i= INTEGER | question_part LT i= INTEGER | question_part GTE i= INTEGER | question_part GT i= INTEGER | question_part EQ i= INTEGER | question_part NE i= INTEGER | question_part 'contains' i= INTEGER );
    public final boolean comparison() throws RecognitionException {
        boolean value = false;

        Token i=null;
        QuestionConditionParser.question_part_return question_part1 = null;

        QuestionConditionParser.question_part_return question_part2 = null;

        QuestionConditionParser.question_part_return question_part3 = null;

        QuestionConditionParser.question_part_return question_part4 = null;

        QuestionConditionParser.question_part_return question_part5 = null;

        QuestionConditionParser.question_part_return question_part6 = null;

        QuestionConditionParser.question_part_return question_part7 = null;


        try {
            // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:18:4: ( question_part LTE i= INTEGER | question_part LT i= INTEGER | question_part GTE i= INTEGER | question_part GT i= INTEGER | question_part EQ i= INTEGER | question_part NE i= INTEGER | question_part 'contains' i= INTEGER )
            int alt1=7;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==QUESTION_NAME) ) {
                switch ( input.LA(2) ) {
                case LT:
                    {
                    alt1=2;
                    }
                    break;
                case 22:
                    {
                    alt1=7;
                    }
                    break;
                case GT:
                    {
                    alt1=4;
                    }
                    break;
                case NE:
                    {
                    alt1=6;
                    }
                    break;
                case GTE:
                    {
                    alt1=3;
                    }
                    break;
                case LTE:
                    {
                    alt1=1;
                    }
                    break;
                case EQ:
                    {
                    alt1=5;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 1, 1, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:18:6: question_part LTE i= INTEGER
                    {
                    pushFollow(FOLLOW_question_part_in_comparison37);
                    question_part1=question_part();

                    state._fsp--;

                    match(input,LTE,FOLLOW_LTE_in_comparison39); 
                    i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_comparison43); 
                     
                            if (environment.getValue((question_part1!=null?input.toString(question_part1.start,question_part1.stop):null)) == null) {
                              value = false;
                            } else {
                              Object obj = environment.getValue((question_part1!=null?input.toString(question_part1.start,question_part1.stop):null));
                              if (obj instanceof Integer) {
                                value =  ((Integer)obj) <= Integer.parseInt((i!=null?i.getText():null)); 
                              } else if (obj instanceof List) {
                                Integer predValue = Integer.parseInt((i!=null?i.getText():null));
                                List list = ((List)obj);
                                if (list.size() != 1) {
                                  value = false;
                                } else {
                                  value = ((Integer)list.get(0)) <= predValue;
                                }
                              } 
                            }
                          

                    }
                    break;
                case 2 :
                    // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:36:6: question_part LT i= INTEGER
                    {
                    pushFollow(FOLLOW_question_part_in_comparison52);
                    question_part2=question_part();

                    state._fsp--;

                    match(input,LT,FOLLOW_LT_in_comparison54); 
                    i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_comparison58); 
                             
                            if (environment.getValue((question_part2!=null?input.toString(question_part2.start,question_part2.stop):null)) == null) {
                              value = false;
                            } else {
                              Object obj = environment.getValue((question_part2!=null?input.toString(question_part2.start,question_part2.stop):null));
                              if (obj instanceof Integer) {
                                value =  ((Integer)obj) < Integer.parseInt((i!=null?i.getText():null)); 
                              } else if (obj instanceof List) {
                                Integer predValue = Integer.parseInt((i!=null?i.getText():null));
                                List list = ((List)obj);
                                if (list.size() != 1) {
                                  value = false;
                                } else {
                                  value = ((Integer)list.get(0)) < predValue;
                                }
                              } 
                            }
                          

                    }
                    break;
                case 3 :
                    // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:54:7: question_part GTE i= INTEGER
                    {
                    pushFollow(FOLLOW_question_part_in_comparison68);
                    question_part3=question_part();

                    state._fsp--;

                    match(input,GTE,FOLLOW_GTE_in_comparison70); 
                    i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_comparison74); 
                     
                            if (environment.getValue((question_part3!=null?input.toString(question_part3.start,question_part3.stop):null)) == null) {
                              value = false;
                            } else {
                              Object obj = environment.getValue((question_part3!=null?input.toString(question_part3.start,question_part3.stop):null));
                              if (obj instanceof Integer) {
                                value =  ((Integer)obj) >= Integer.parseInt((i!=null?i.getText():null)); 
                              } else if (obj instanceof List) {
                                Integer predValue = Integer.parseInt((i!=null?i.getText():null));
                                List list = ((List)obj);
                                if (list.size() != 1) {
                                  value = false;
                                } else {
                                  value = ((Integer)list.get(0)) >= predValue;
                                }
                              } 
                            }
                          

                    }
                    break;
                case 4 :
                    // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:72:6: question_part GT i= INTEGER
                    {
                    pushFollow(FOLLOW_question_part_in_comparison83);
                    question_part4=question_part();

                    state._fsp--;

                    match(input,GT,FOLLOW_GT_in_comparison85); 
                    i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_comparison89); 
                     
                           if (environment.getValue((question_part4!=null?input.toString(question_part4.start,question_part4.stop):null)) == null) {
                             value = false;
                           } else {
                             Object obj = environment.getValue((question_part4!=null?input.toString(question_part4.start,question_part4.stop):null));
                             if (obj instanceof Integer) {
                               value =  ((Integer)obj) > Integer.parseInt((i!=null?i.getText():null)); 
                             } else if (obj instanceof List) {
                               Integer predValue = Integer.parseInt((i!=null?i.getText():null));
                               List list = ((List)obj);
                               if (list.size() != 1) {
                                 value = false;
                               } else {
                                 value = ((Integer)list.get(0)) > predValue;
                               }
                             } 
                           }
                         

                    }
                    break;
                case 5 :
                    // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:90:6: question_part EQ i= INTEGER
                    {
                    pushFollow(FOLLOW_question_part_in_comparison98);
                    question_part5=question_part();

                    state._fsp--;

                    match(input,EQ,FOLLOW_EQ_in_comparison100); 
                    i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_comparison104); 
                     
                            if (environment.getValue((question_part5!=null?input.toString(question_part5.start,question_part5.stop):null)) == null) {
                              value = false;
                            } else {
                              Object obj = environment.getValue((question_part5!=null?input.toString(question_part5.start,question_part5.stop):null));
                              if (obj instanceof Integer) {
                                value =  ((Integer)obj) == Integer.parseInt((i!=null?i.getText():null)); 
                              } else if (obj instanceof List) {
                                value = ((List)obj).contains(Integer.parseInt((i!=null?i.getText():null)));
                              }
                            }
                          

                    }
                    break;
                case 6 :
                    // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:102:6: question_part NE i= INTEGER
                    {
                    pushFollow(FOLLOW_question_part_in_comparison113);
                    question_part6=question_part();

                    state._fsp--;

                    match(input,NE,FOLLOW_NE_in_comparison115); 
                    i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_comparison119); 
                     
                            if (environment.getValue((question_part6!=null?input.toString(question_part6.start,question_part6.stop):null)) == null) {
                              value = false;
                            } else {
                              Object obj = environment.getValue((question_part6!=null?input.toString(question_part6.start,question_part6.stop):null));
                              if (obj instanceof Integer) {
                                value = ((Integer)obj) != Integer.parseInt((i!=null?i.getText():null)); 
                              } else if (obj instanceof List) {
                                value = !((List)obj).contains(Integer.parseInt((i!=null?i.getText():null)));
                              } else {
                                value = false; //default case
                              }
                            }
                         

                    }
                    break;
                case 7 :
                    // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:116:6: question_part 'contains' i= INTEGER
                    {
                    pushFollow(FOLLOW_question_part_in_comparison128);
                    question_part7=question_part();

                    state._fsp--;

                    match(input,22,FOLLOW_22_in_comparison130); 
                    i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_comparison134); 
                     
                            if (environment.getValue((question_part7!=null?input.toString(question_part7.start,question_part7.stop):null)) == null) {
                              value = false;
                            } else {
                              Object obj = environment.getValue((question_part7!=null?input.toString(question_part7.start,question_part7.stop):null));
                              if (obj instanceof List) {        
                                value = ((List)obj).contains(Integer.parseInt((i!=null?i.getText():null))); 
                              } else {
                                value = false; // default case
                              }
                            }
                          

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "comparison"


    // $ANTLR start "expression"
    // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:130:1: expression returns [boolean value] : c= comparison ( OR c1= comparison | AND c1= comparison )* ;
    public final boolean expression() throws RecognitionException {
        boolean value = false;

        boolean c = false;

        boolean c1 = false;


        try {
            // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:131:4: (c= comparison ( OR c1= comparison | AND c1= comparison )* )
            // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:131:8: c= comparison ( OR c1= comparison | AND c1= comparison )*
            {
            pushFollow(FOLLOW_comparison_in_expression159);
            c=comparison();

            state._fsp--;

            value = c;
            // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:132:4: ( OR c1= comparison | AND c1= comparison )*
            loop2:
            do {
                int alt2=3;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==OR) ) {
                    alt2=1;
                }
                else if ( (LA2_0==AND) ) {
                    alt2=2;
                }


                switch (alt2) {
            	case 1 :
            	    // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:132:6: OR c1= comparison
            	    {
            	    match(input,OR,FOLLOW_OR_in_expression168); 
            	    pushFollow(FOLLOW_comparison_in_expression172);
            	    c1=comparison();

            	    state._fsp--;

            	    value = value || c1; 

            	    }
            	    break;
            	case 2 :
            	    // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:133:6: AND c1= comparison
            	    {
            	    match(input,AND,FOLLOW_AND_in_expression181); 
            	    pushFollow(FOLLOW_comparison_in_expression185);
            	    c1=comparison();

            	    state._fsp--;

            	    value = value && c1; 

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "expression"

    public static class question_part_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "question_part"
    // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:137:1: question_part : QUESTION_NAME ;
    public final QuestionConditionParser.question_part_return question_part() throws RecognitionException {
        QuestionConditionParser.question_part_return retval = new QuestionConditionParser.question_part_return();
        retval.start = input.LT(1);

        Token QUESTION_NAME8=null;

        try {
            // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:138:1: ( QUESTION_NAME )
            // /Users/bobevans/git/paco/Paco/src/com/google/android/apps/paco/questioncondparser/QuestionCondition.g:138:2: QUESTION_NAME
            {
            QUESTION_NAME8=(Token)match(input,QUESTION_NAME,FOLLOW_QUESTION_NAME_in_question_part204); 
             if (!environment.exists((QUESTION_NAME8!=null?QUESTION_NAME8.getText():null))) {
                   throw new IllegalArgumentException("unknown reference: " + (QUESTION_NAME8!=null?QUESTION_NAME8.getText():null));
               }
              // if (!environment.correctType((QUESTION_NAME8!=null?QUESTION_NAME8.getText():null))) {
              //   throw new IllegalArgumentException("Does not have the proper response type: " + (QUESTION_NAME8!=null?QUESTION_NAME8.getText():null));
              // }
               

            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "question_part"

    // Delegated rules


 

    public static final BitSet FOLLOW_question_part_in_comparison37 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_LTE_in_comparison39 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INTEGER_in_comparison43 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_question_part_in_comparison52 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LT_in_comparison54 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INTEGER_in_comparison58 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_question_part_in_comparison68 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_GTE_in_comparison70 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INTEGER_in_comparison74 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_question_part_in_comparison83 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_GT_in_comparison85 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INTEGER_in_comparison89 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_question_part_in_comparison98 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_EQ_in_comparison100 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INTEGER_in_comparison104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_question_part_in_comparison113 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_NE_in_comparison115 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INTEGER_in_comparison119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_question_part_in_comparison128 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_comparison130 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INTEGER_in_comparison134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparison_in_expression159 = new BitSet(new long[]{0x0000000000001802L});
    public static final BitSet FOLLOW_OR_in_expression168 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_comparison_in_expression172 = new BitSet(new long[]{0x0000000000001802L});
    public static final BitSet FOLLOW_AND_in_expression181 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_comparison_in_expression185 = new BitSet(new long[]{0x0000000000001802L});
    public static final BitSet FOLLOW_QUESTION_NAME_in_question_part204 = new BitSet(new long[]{0x0000000000000002L});

}