/*
* Copyright 2011 Google Inc. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.  
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
// $ANTLR 3.2 Sep 23, 2009 12:02:23 /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g 2010-10-13 17:58:12

package com.google.android.apps.paco.questioncondparser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class QuestionConditionLexer extends Lexer {
    public static final int UNICODE_ESC=18;
    public static final int WS=13;
    public static final int CHAR=16;
    public static final int EQ=7;
    public static final int STRING=15;
    public static final int LT=4;
    public static final int GT=6;
    public static final int NE=8;
    public static final int COMMENT=12;
    public static final int ESC_SEQ=14;
    public static final int OR=9;
    public static final int QUESTION_NAME=11;
    public static final int INTEGER=5;
    public static final int AND=10;
    public static final int EOF=-1;
    public static final int HEX_DIGIT=17;
    public static final int OCTAL_ESC=19;

    // delegates
    // delegators

    public QuestionConditionLexer() {;} 
    public QuestionConditionLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public QuestionConditionLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g"; }

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:40:4: ( '||' )
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:40:6: '||'
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:41:5: ( '&&' )
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:41:7: '&&'
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:43:2: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:43:8: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:43:32: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:46:9: ( ( '0' .. '9' )+ )
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:46:17: ( '0' .. '9' )+
            {
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:46:17: ( '0' .. '9' )+
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
            	    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:46:17: '0' .. '9'
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:50:4: ( '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' | '/*' ( options {greedy=false; } : . )* '*/' )
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
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:50:8: '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
                    {
                    match("//"); 

                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:50:13: (~ ( '\\n' | '\\r' ) )*
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( ((LA3_0>='\u0000' && LA3_0<='\t')||(LA3_0>='\u000B' && LA3_0<='\f')||(LA3_0>='\u000E' && LA3_0<='\uFFFF')) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:50:13: ~ ( '\\n' | '\\r' )
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

                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:50:27: ( '\\r' )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0=='\r') ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:50:27: '\\r'
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
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:51:8: '/*' ( options {greedy=false; } : . )* '*/'
                    {
                    match("/*"); 

                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:51:13: ( options {greedy=false; } : . )*
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
                    	    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:51:41: .
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:54:5: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:54:9: ( ' ' | '\\t' | '\\r' | '\\n' )
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:62:4: ( '\"' ( ESC_SEQ | ~ ( '\\\\' | '\"' ) )* '\"' )
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:62:7: '\"' ( ESC_SEQ | ~ ( '\\\\' | '\"' ) )* '\"'
            {
            match('\"'); 
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:62:11: ( ESC_SEQ | ~ ( '\\\\' | '\"' ) )*
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
            	    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:62:13: ESC_SEQ
            	    {
            	    mESC_SEQ(); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:62:23: ~ ( '\\\\' | '\"' )
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:65:5: ( '\\'' ( ESC_SEQ | ~ ( '\\'' | '\\\\' ) ) '\\'' )
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:65:8: '\\'' ( ESC_SEQ | ~ ( '\\'' | '\\\\' ) ) '\\''
            {
            match('\''); 
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:65:13: ( ESC_SEQ | ~ ( '\\'' | '\\\\' ) )
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
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:65:15: ESC_SEQ
                    {
                    mESC_SEQ(); 

                    }
                    break;
                case 2 :
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:65:25: ~ ( '\\'' | '\\\\' )
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:69:11: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:69:13: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:73:4: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UNICODE_ESC | OCTAL_ESC )
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
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:73:8: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
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
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:74:8: UNICODE_ESC
                    {
                    mUNICODE_ESC(); 

                    }
                    break;
                case 3 :
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:75:8: OCTAL_ESC
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:80:4: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
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
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:80:8: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:80:13: ( '0' .. '3' )
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:80:14: '0' .. '3'
                    {
                    matchRange('0','3'); 

                    }

                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:80:24: ( '0' .. '7' )
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:80:25: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:80:35: ( '0' .. '7' )
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:80:36: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 2 :
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:81:8: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:81:13: ( '0' .. '7' )
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:81:14: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:81:24: ( '0' .. '7' )
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:81:25: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 3 :
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:82:8: '\\\\' ( '0' .. '7' )
                    {
                    match('\\'); 
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:82:13: ( '0' .. '7' )
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:82:14: '0' .. '7'
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:87:4: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:87:8: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:92:9: ( '=' | '==' )
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
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:92:18: '='
                    {
                    match('='); 

                    }
                    break;
                case 2 :
                    // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:92:24: '=='
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:93:9: ( '<' )
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:93:18: '<'
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:94:9: ( '>' )
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:94:18: '>'
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
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:95:9: ( '!=' )
            // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:95:17: '!='
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
        // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:1:8: ( OR | AND | QUESTION_NAME | INTEGER | COMMENT | WS | STRING | CHAR | EQ | LT | GT | NE )
        int alt12=12;
        switch ( input.LA(1) ) {
        case '|':
            {
            alt12=1;
            }
            break;
        case '&':
            {
            alt12=2;
            }
            break;
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
        case 'G':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'S':
        case 'T':
        case 'U':
        case 'V':
        case 'W':
        case 'X':
        case 'Y':
        case 'Z':
        case '_':
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'n':
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
            {
            alt12=3;
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
        case '8':
        case '9':
            {
            alt12=4;
            }
            break;
        case '/':
            {
            alt12=5;
            }
            break;
        case '\t':
        case '\n':
        case '\r':
        case ' ':
            {
            alt12=6;
            }
            break;
        case '\"':
            {
            alt12=7;
            }
            break;
        case '\'':
            {
            alt12=8;
            }
            break;
        case '=':
            {
            alt12=9;
            }
            break;
        case '<':
            {
            alt12=10;
            }
            break;
        case '>':
            {
            alt12=11;
            }
            break;
        case '!':
            {
            alt12=12;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("", 12, 0, input);

            throw nvae;
        }

        switch (alt12) {
            case 1 :
                // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:1:10: OR
                {
                mOR(); 

                }
                break;
            case 2 :
                // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:1:13: AND
                {
                mAND(); 

                }
                break;
            case 3 :
                // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:1:17: QUESTION_NAME
                {
                mQUESTION_NAME(); 

                }
                break;
            case 4 :
                // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:1:31: INTEGER
                {
                mINTEGER(); 

                }
                break;
            case 5 :
                // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:1:39: COMMENT
                {
                mCOMMENT(); 

                }
                break;
            case 6 :
                // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:1:47: WS
                {
                mWS(); 

                }
                break;
            case 7 :
                // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:1:50: STRING
                {
                mSTRING(); 

                }
                break;
            case 8 :
                // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:1:57: CHAR
                {
                mCHAR(); 

                }
                break;
            case 9 :
                // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:1:62: EQ
                {
                mEQ(); 

                }
                break;
            case 10 :
                // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:1:65: LT
                {
                mLT(); 

                }
                break;
            case 11 :
                // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:1:68: GT
                {
                mGT(); 

                }
                break;
            case 12 :
                // /Users/bobevans/Documents/projects/pacoandroid2/android2_workspace/Paco/src/com/google/sampling/experiential/questioncondparser/QuestionCondition.g:1:71: NE
                {
                mNE(); 

                }
                break;

        }

    }


 

}