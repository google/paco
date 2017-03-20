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
package com.google.android.apps.paco.questioncondparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;


public class ExpressionEvaluator {
  
      private Environment environment;

      public ExpressionEvaluator(Environment environment) {
        this.environment = environment;
      }

	public static void main(String[] args) throws IOException {
	    Environment interpreter = new Environment();
	    ExpressionEvaluator m = new ExpressionEvaluator(interpreter);
		
		addTestFieldsToInterpreter(interpreter);

		System.out.print("first:" + m.parse(args[0]));
		
		
		interpreter.addInput(new Binding("goo", Integer.class, 100));
		System.out.print("second:" + 
		    m.parse("baz < 12 && goo > 100 && goo > 1000 && baz = 8"));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter an Expression:");
		String line;
		while ((line = br.readLine()) != null) {
		  
		  try {
			System.out.println(m.parse(line));			
		  } catch (Exception e) {
		    System.err.println(e.getMessage());
		  }
		  System.out.println("Enter an Expression:");
		}
		
	}

	public boolean parse(String expr) throws IllegalArgumentException {
		CharStream input = null;
		//		if (args.length > 0) input = new ANTLRFileStream(args[0]);
		//		else 
		try {
          input = new ANTLRInputStream(new StringBufferInputStream(expr));
        } catch (IOException e1) {
          System.err.println(e1.getMessage());
          return false;
        }
		QuestionConditionLexer lex = new QuestionConditionLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lex);
		QuestionConditionParser parser = new QuestionConditionParser(tokens, environment);
		try {
			try {
				return parser.expression();				
			} catch (IllegalArgumentException ie) {
				String message = "Invalid Expression: " +ie.getMessage();
                //System.err.println(message);
                throw new IllegalArgumentException(message, ie);				
			}
		} catch (RecognitionException e) {			
			String message = "Invalid Expression: " + e.getMessage();
          //System.err.println(message);
          throw new IllegalArgumentException(message, e);
		}
	}

	private static void addTestFieldsToInterpreter(Environment interpreter) {
		interpreter.addInput(new Binding("foo", Integer.class, 4));
		interpreter.addInput(new Binding("bar", String.class, "spaghetti"));
		interpreter.addInput(new Binding("baz", Integer.class, 8));		
	}
}
