// $ANTLR 3.2 Sep 23, 2009 12:02:23 /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g 2012-04-02 16:32:36

package com.google.android.apps.paco.questioncondparser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class QuestionConditionLexer extends Lexer {
    public static final int INTEGER=5;
    public static final int LT=4;
    public static final int UNICODE_ESC=18;
    public static final int T__20=20;
    public static final int OCTAL_ESC=19;
    public static final int QUESTION_NAME=11;
    public static final int CHAR=16;
    public static final int HEX_DIGIT=17;
    public static final int AND=10;
    public static final int EOF=-1;
    public static final int WS=13;
    public static final int ESC_SEQ=14;
    public static final int OR=9;
    public static final int GT=6;
    public static final int EQ=7;
    public static final int COMMENT=12;
    public static final int STRING=15;
    public static final int NE=8;

    // delegates
    // delegators

    public QuestionConditionLexer() {;} 
    public QuestionConditionLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public QuestionConditionLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g"; }

    // $ANTLR start "T__20"
    public final void mT__20() throws RecognitionException {
        try {
            int _type = T__20;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:7:7: ( 'contains' )
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:7:9: 'contains'
            {
            match("contains"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__20"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:77:4: ( '||' )
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:77:6: '||'
            {
            match("||"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:78:5: ( '&&' )
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:78:7: '&&'
            {
            match("&&"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "QUESTION_NAME"
    public final void mQUESTION_NAME() throws RecognitionException {
        try {
            int _type = QUESTION_NAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:80:2: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:80:8: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:80:32: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUESTION_NAME"

    // $ANTLR start "INTEGER"
    public final void mINTEGER() throws RecognitionException {
        try {
            int _type = INTEGER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:83:9: ( ( '0' .. '9' )+ )
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:83:17: ( '0' .. '9' )+
            {
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:83:17: ( '0' .. '9' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:83:17: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INTEGER"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:87:4: ( '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' | '/*' ( options {greedy=false; } : . )* '*/' )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='/') ) {
                int LA6_1 = input.LA(2);

                if ( (LA6_1=='/') ) {
                    alt6=1;
                }
                else if ( (LA6_1=='*') ) {
                    alt6=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:87:8: '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
                    {
                    match("//"); 

                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:87:13: (~ ( '\\n' | '\\r' ) )*
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( ((LA3_0>='\u0000' && LA3_0<='\t')||(LA3_0>='\u000B' && LA3_0<='\f')||(LA3_0>='\u000E' && LA3_0<='\uFFFF')) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:87:13: ~ ( '\\n' | '\\r' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop3;
                        }
                    } while (true);

                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:87:27: ( '\\r' )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0=='\r') ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:87:27: '\\r'
                            {
                            match('\r'); 

                            }
                            break;

                    }

                    match('\n'); 
                    _channel=HIDDEN;

                    }
                    break;
                case 2 :
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:88:8: '/*' ( options {greedy=false; } : . )* '*/'
                    {
                    match("/*"); 

                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:88:13: ( options {greedy=false; } : . )*
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0=='*') ) {
                            int LA5_1 = input.LA(2);

                            if ( (LA5_1=='/') ) {
                                alt5=2;
                            }
                            else if ( ((LA5_1>='\u0000' && LA5_1<='.')||(LA5_1>='0' && LA5_1<='\uFFFF')) ) {
                                alt5=1;
                            }


                        }
                        else if ( ((LA5_0>='\u0000' && LA5_0<=')')||(LA5_0>='+' && LA5_0<='\uFFFF')) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:88:41: .
                    	    {
                    	    matchAny(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);

                    match("*/"); 

                    _channel=HIDDEN;

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:91:5: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:91:9: ( ' ' | '\\t' | '\\r' | '\\n' )
            {
            if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:99:4: ( '\"' ( ESC_SEQ | ~ ( '\\\\' | '\"' ) )* '\"' )
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:99:7: '\"' ( ESC_SEQ | ~ ( '\\\\' | '\"' ) )* '\"'
            {
            match('\"'); 
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:99:11: ( ESC_SEQ | ~ ( '\\\\' | '\"' ) )*
            loop7:
            do {
                int alt7=3;
                int LA7_0 = input.LA(1);

                if ( (LA7_0=='\\') ) {
                    alt7=1;
                }
                else if ( ((LA7_0>='\u0000' && LA7_0<='!')||(LA7_0>='#' && LA7_0<='[')||(LA7_0>=']' && LA7_0<='\uFFFF')) ) {
                    alt7=2;
                }


                switch (alt7) {
            	case 1 :
            	    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:99:13: ESC_SEQ
            	    {
            	    mESC_SEQ(); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:99:23: ~ ( '\\\\' | '\"' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "CHAR"
    public final void mCHAR() throws RecognitionException {
        try {
            int _type = CHAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:102:5: ( '\\'' ( ESC_SEQ | ~ ( '\\'' | '\\\\' ) ) '\\'' )
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:102:8: '\\'' ( ESC_SEQ | ~ ( '\\'' | '\\\\' ) ) '\\''
            {
            match('\''); 
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:102:13: ( ESC_SEQ | ~ ( '\\'' | '\\\\' ) )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='\\') ) {
                alt8=1;
            }
            else if ( ((LA8_0>='\u0000' && LA8_0<='&')||(LA8_0>='(' && LA8_0<='[')||(LA8_0>=']' && LA8_0<='\uFFFF')) ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:102:15: ESC_SEQ
                    {
                    mESC_SEQ(); 

                    }
                    break;
                case 2 :
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:102:25: ~ ( '\\'' | '\\\\' )
                    {
                    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CHAR"

    // $ANTLR start "HEX_DIGIT"
    public final void mHEX_DIGIT() throws RecognitionException {
        try {
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:106:11: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:106:13: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "HEX_DIGIT"

    // $ANTLR start "ESC_SEQ"
    public final void mESC_SEQ() throws RecognitionException {
        try {
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:110:4: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UNICODE_ESC | OCTAL_ESC )
            int alt9=3;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='\\') ) {
                switch ( input.LA(2) ) {
                case '\"':
                case '\'':
                case '\\':
                case 'b':
                case 'f':
                case 'n':
                case 'r':
                case 't':
                    {
                    alt9=1;
                    }
                    break;
                case 'u':
                    {
                    alt9=2;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                    {
                    alt9=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 1, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:110:8: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
                    {
                    match('\\'); 
                    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:111:8: UNICODE_ESC
                    {
                    mUNICODE_ESC(); 

                    }
                    break;
                case 3 :
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:112:8: OCTAL_ESC
                    {
                    mOCTAL_ESC(); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "ESC_SEQ"

    // $ANTLR start "OCTAL_ESC"
    public final void mOCTAL_ESC() throws RecognitionException {
        try {
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:117:4: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
            int alt10=3;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='\\') ) {
                int LA10_1 = input.LA(2);

                if ( ((LA10_1>='0' && LA10_1<='3')) ) {
                    int LA10_2 = input.LA(3);

                    if ( ((LA10_2>='0' && LA10_2<='7')) ) {
                        int LA10_4 = input.LA(4);

                        if ( ((LA10_4>='0' && LA10_4<='7')) ) {
                            alt10=1;
                        }
                        else {
                            alt10=2;}
                    }
                    else {
                        alt10=3;}
                }
                else if ( ((LA10_1>='4' && LA10_1<='7')) ) {
                    int LA10_3 = input.LA(3);

                    if ( ((LA10_3>='0' && LA10_3<='7')) ) {
                        alt10=2;
                    }
                    else {
                        alt10=3;}
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 10, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:117:8: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:117:13: ( '0' .. '3' )
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:117:14: '0' .. '3'
                    {
                    matchRange('0','3'); 

                    }

                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:117:24: ( '0' .. '7' )
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:117:25: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:117:35: ( '0' .. '7' )
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:117:36: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 2 :
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:118:8: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:118:13: ( '0' .. '7' )
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:118:14: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:118:24: ( '0' .. '7' )
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:118:25: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 3 :
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:119:8: '\\\\' ( '0' .. '7' )
                    {
                    match('\\'); 
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:119:13: ( '0' .. '7' )
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:119:14: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "OCTAL_ESC"

    // $ANTLR start "UNICODE_ESC"
    public final void mUNICODE_ESC() throws RecognitionException {
        try {
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:124:4: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:124:8: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
            {
            match('\\'); 
            match('u'); 
            mHEX_DIGIT(); 
            mHEX_DIGIT(); 
            mHEX_DIGIT(); 
            mHEX_DIGIT(); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "UNICODE_ESC"

    // $ANTLR start "EQ"
    public final void mEQ() throws RecognitionException {
        try {
            int _type = EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:129:9: ( '=' | '==' )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='=') ) {
                int LA11_1 = input.LA(2);

                if ( (LA11_1=='=') ) {
                    alt11=2;
                }
                else {
                    alt11=1;}
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:129:18: '='
                    {
                    match('='); 

                    }
                    break;
                case 2 :
                    // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:129:24: '=='
                    {
                    match("=="); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQ"

    // $ANTLR start "LT"
    public final void mLT() throws RecognitionException {
        try {
            int _type = LT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:130:9: ( '<' )
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:130:18: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LT"

    // $ANTLR start "GT"
    public final void mGT() throws RecognitionException {
        try {
            int _type = GT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:131:9: ( '>' )
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:131:18: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GT"

    // $ANTLR start "NE"
    public final void mNE() throws RecognitionException {
        try {
            int _type = NE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:132:9: ( '!=' )
            // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:132:17: '!='
            {
            match("!="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NE"

    public void mTokens() throws RecognitionException {
        // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:8: ( T__20 | OR | AND | QUESTION_NAME | INTEGER | COMMENT | WS | STRING | CHAR | EQ | LT | GT | NE )
        int alt12=13;
        alt12 = dfa12.predict(input);
        switch (alt12) {
            case 1 :
                // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:10: T__20
                {
                mT__20(); 

                }
                break;
            case 2 :
                // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:16: OR
                {
                mOR(); 

                }
                break;
            case 3 :
                // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:19: AND
                {
                mAND(); 

                }
                break;
            case 4 :
                // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:23: QUESTION_NAME
                {
                mQUESTION_NAME(); 

                }
                break;
            case 5 :
                // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:37: INTEGER
                {
                mINTEGER(); 

                }
                break;
            case 6 :
                // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:45: COMMENT
                {
                mCOMMENT(); 

                }
                break;
            case 7 :
                // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:53: WS
                {
                mWS(); 

                }
                break;
            case 8 :
                // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:56: STRING
                {
                mSTRING(); 

                }
                break;
            case 9 :
                // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:63: CHAR
                {
                mCHAR(); 

                }
                break;
            case 10 :
                // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:68: EQ
                {
                mEQ(); 

                }
                break;
            case 11 :
                // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:71: LT
                {
                mLT(); 

                }
                break;
            case 12 :
                // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:74: GT
                {
                mGT(); 

                }
                break;
            case 13 :
                // /Users/bobevans/projects/questioncond_incodenow/QuestionCondition.g:1:77: NE
                {
                mNE(); 

                }
                break;

        }

    }


    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA12_eotS =
        "\1\uffff\1\4\14\uffff\6\4\1\25\1\uffff";
    static final String DFA12_eofS =
        "\26\uffff";
    static final String DFA12_minS =
        "\1\11\1\157\14\uffff\1\156\1\164\1\141\1\151\1\156\1\163\1\60\1"+
        "\uffff";
    static final String DFA12_maxS =
        "\1\174\1\157\14\uffff\1\156\1\164\1\141\1\151\1\156\1\163\1\172"+
        "\1\uffff";
    static final String DFA12_acceptS =
        "\2\uffff\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\7"+
        "\uffff\1\1";
    static final String DFA12_specialS =
        "\26\uffff}>";
    static final String[] DFA12_transitionS = {
            "\2\7\2\uffff\1\7\22\uffff\1\7\1\15\1\10\3\uffff\1\3\1\11\7\uffff"+
            "\1\6\12\5\2\uffff\1\13\1\12\1\14\2\uffff\32\4\4\uffff\1\4\1"+
            "\uffff\2\4\1\1\27\4\1\uffff\1\2",
            "\1\16",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\17",
            "\1\20",
            "\1\21",
            "\1\22",
            "\1\23",
            "\1\24",
            "\12\4\7\uffff\32\4\4\uffff\1\4\1\uffff\32\4",
            ""
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__20 | OR | AND | QUESTION_NAME | INTEGER | COMMENT | WS | STRING | CHAR | EQ | LT | GT | NE );";
        }
    }
 

}