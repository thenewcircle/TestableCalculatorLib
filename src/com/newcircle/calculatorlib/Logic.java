package com.newcircle.calculatorlib;

import java.util.Locale;

import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;

import android.annotation.SuppressLint;
import android.util.Log;

public class Logic {
	private static String TAG = "Logic"; 

	private static Symbols mSymbols = new Symbols();

	private static final String INFINITY_UNICODE = "\u221e";

	public static final String MARKER_EVALUATE_ON_RESUME = "?";

	// the two strings below are the result of Double.toString() for Infinity & NaN
	// they are not output to the user and don't require internationalization
	private static final String INFINITY = "Infinity";
	private static final String NAN      = "NaN";
 
	public static final char MINUS = '\u2212';
	public final static int DELETE_MODE_BACKSPACE = 0;
	public final static int DELETE_MODE_CLEAR = 1;

	public final static int LINE_LENGTH = 20;
	private static int mLineLength = LINE_LENGTH;

	private final static String ERROR_STRING = "ERR";

	private static final Double ERROR_VAL = Double.MAX_VALUE;

	public enum OP {
		PLUS,
		MINUS,
		MUL,
		DIV
	}

	public static Double Calc(Double x, Double y, OP op) {
		switch(op) {
		case PLUS:
			return x+y;
		case DIV:
			return x/y;
		case MINUS:
			return x-y;
		case MUL:
			return x*y;
		default:
			return ERROR_VAL;
		}
	}

	@SuppressLint("DefaultLocale")
	public static Double Calc(Double x, Double y, String op) {
		Log.d(TAG, "Calc("+x+","+y+"," + op + ")");
		if(op.equals("+"))
			return Calc(x,y, OP.PLUS);
		else if(op.equals("/") || op.equals("\u00f7"))
			return Calc(x,y, OP.DIV);
		else if(op.equals("*") || op.equals("\u00d7") || op.equalsIgnoreCase("x"))
			return Calc(x,y, OP.MUL);
		else if(op.equals("-") || op.equals("\u2212"))
			return Calc(x,y, OP.MINUS);

		return ERROR_VAL;
	}

	public static Double Calc(String x, String y, String op) {
		try {
			return Calc(Double.parseDouble(x), Double.parseDouble(y), op);
		} catch(Exception e) {
			return ERROR_VAL;
		}
	}

	public static String evaluate(String input) throws SyntaxException {
		mLineLength = LINE_LENGTH;

		if (input.trim().equals("")) {
			return "";
		}

		// drop final infix operators (they can only result in error)
		int size = input.length();
		while (size > 0 && isOperator(input.charAt(size - 1))) {
			input = input.substring(0, size - 1);
			--size;
		}
		// Find and replace any translated mathematical functions.
		double value = mSymbols.eval(input);
		Log.d(TAG, input + "=" + value);
		
		String result = "";
		for (int precision = mLineLength; precision > 6; precision--) {
			result = tryFormattingWithPrecision(value, precision);
			if (result.length() <= mLineLength) {
				break;
			}
		}
		return result.replace('-', MINUS).replace(INFINITY, INFINITY_UNICODE);
	}


	private static String tryFormattingWithPrecision(double value, int precision) {
		// The standard scientific formatter is basically what we need. We will
		// start with what it produces and then massage it a bit.
		String result = String.format(Locale.US, "%" + mLineLength + "." + precision + "g", value);
		if (result.equals(NAN)) { // treat NaN as Error
			return ERROR_STRING;
		}
		String mantissa = result;
		String exponent = null;
		int e = result.indexOf('e');
		if (e != -1) {
			mantissa = result.substring(0, e);

			// Strip "+" and unnecessary 0's from the exponent
			exponent = result.substring(e + 1);
			if (exponent.startsWith("+")) {
				exponent = exponent.substring(1);
			}
			exponent = String.valueOf(Integer.parseInt(exponent));
		} else {
			mantissa = result;
		}

		int period = mantissa.indexOf('.');
		if (period == -1) {
			period = mantissa.indexOf(',');
		}
		if (period != -1) {
			// Strip trailing 0's
			while (mantissa.length() > 0 && mantissa.endsWith("0")) {
				mantissa = mantissa.substring(0, mantissa.length() - 1);
			}
			if (mantissa.length() == period + 1) {
				mantissa = mantissa.substring(0, mantissa.length() - 1);
			}
		}

		if (exponent != null) {
			result = mantissa + 'e' + exponent;
		} else {
			result = mantissa;
		}
		return result;
	}

	static boolean isOperator(String text) {
		return text.length() == 1 && isOperator(text.charAt(0));
	}

	static boolean isOperator(char c) {
		//plus minus times div
		return "+\u2212\u00d7\u00f7/*".indexOf(c) != -1;

	}

}
