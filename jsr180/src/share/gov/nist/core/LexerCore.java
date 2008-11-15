/*
 * Portions Copyright  2000-2008 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */
package gov.nist.core;

import java.util.Hashtable;
import java.util.Vector;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * A lexical analyzer that is used by all parsers in this implementation.
 */
public abstract class LexerCore extends StringTokenizer {


    // IMPORTANT - All keyword matches should be between START and END
    /** START token. */
    public static final int START = 2048;
    /** END token. */
    public static final int END = START + 2048;
    // IMPORTANT -- This should be < END
    /** ID token. */
    public static final int ID = END - 1;
    // Individial token classes.
    /** WHITESPACE token. */
    public static final int WHITESPACE = END + 1;
    /** DIGIT (numeric) token. */
    public static final int DIGIT = END + 2;
    /** ALPHA (alphabetic) token. */
    public static final int ALPHA = END + 3;
    /** BACKSLASH (escaping) token. */
    public static final int BACKSLASH = (int) '\\';
    /** Single QUOTE token. */
    public static final int QUOTE = (int) '\'';
    /** AT sign token. */
    public static final int AT = (int) '@';
    /** SPACE token. */
    public static final int SP = (int) ' ';
    /** HT (Horizontal tab) token. */
    public static final int HT = (int) '\t';
    /** COLON token. */
    public static final int COLON = (int) ':';
    /** STAR (asterisk) token. */
    public static final int STAR = (int) '*';
    /** DOLLAR token. */
    public static final int DOLLAR = (int) '$';
    /** PLUS token. */
    public static final int PLUS = (int) '+';
    /** POUND token. */
    public static final int POUND = (int) '#';
    /** MINUS token. */
    public static final int MINUS = (int) '-';
    /** DOUBLEQUOTE token. */
    public static final int DOUBLEQUOTE = (int) '\"';
    /** TILDE token. */
    public static final int TILDE = (int) '~';
    /** BACK_QUOTE token. */
    public static final int BACK_QUOTE = (int) '`';
    /** NULL token. */
    public static final int NULL = (int) '\0';
    /** EQUALS (equals sign) token. */
    public static final int EQUALS = (int) '=';
    /** SEMICOLON token. */
    public static final int SEMICOLON = (int) ';';
    /** Forward SLASH token. */
    public static final int SLASH = (int) '/';
    /** L_SQUARE_BRACKET (left square bracket) token. */
    public static final int L_SQUARE_BRACKET = (int) '[';
    /** R_SQUARE_BRACKET (right square bracket) token. */
    public static final int R_SQUARE_BRACKET = (int) ']';
    /** R_CURLY (right curly bracket) token. */
    public static final int R_CURLY = (int) '}';
    /** L_CURLY (left curly bracket) token. */
    public static final int L_CURLY = (int) '{';
    /** HAT (carot) token. */
    public static final int HAT = (int) '^';
    /** Veritcal BAR token. */
    public static final int BAR = (int) '|';
    /** DOT (period) token. */
    public static final int DOT = (int) '.';
    /** EXCLAMATION token. */
    public static final int EXCLAMATION = (int) '!';
    /** LPAREN (left paren) token. */
    public static final int LPAREN = (int) '(';
    /** RPAREN (right paren) token. */
    public static final int RPAREN = (int) ')';
    /** GREATER_THAN token. */
    public static final int GREATER_THAN = (int) '>';
    /** LESS_THAN token. */
    public static final int LESS_THAN = (int) '<';
    /** PERCENT token. */
    public static final int PERCENT = (int) '%';
    /** QUESTION mark token. */
    public static final int QUESTION = (int) '?';
    /** AND (ampersand)  token. */
    public static final int AND = (int) '&';
    /** UNDERSCPRE token. */
    public static final int UNDERSCORE = (int) '_';

    /** Global symbol table for intermediate elements. */
    protected static Hashtable globalSymbolTable;
    /** Lexical rules tables. */
    protected static Hashtable lexerTables;
    /** Current elements of current lexing operation. */
    protected Hashtable currentLexer;
    /** Name of the  current Lexer. */
    protected String currentLexerName;
    /** Current matched token. */
    protected Token currentMatch;

    /**
     * Initializes the hash tables on first
     * loading of the class.
     */
    static {
        globalSymbolTable = new Hashtable();
        lexerTables = new Hashtable();
    }

    /**
     * Adds a new keyword and value pair.
     * @param name the name of the keyword
     * @param value the content of the keyword
     */
    protected void addKeyword(String name, int value) {
        // System.out.println("addKeyword " + name + " value = " + value);
        // new Exception().printStackTrace();
        Integer val = new Integer(value);
        currentLexer.put(name, val);
        if (! globalSymbolTable.containsKey(val))
            globalSymbolTable.put(val, name);
    }

    /**
     * Looks up a requested token.
     * @param value the token to find
     * @return the value of the token
     */
    public String lookupToken(int value) {
        if (value > START) {
            return (String) globalSymbolTable.get(new Integer(value));
        } else {
            Character ch = new Character((char)value);
            return ch.toString();
        }
    }

    /**
     * Adds a new Lexer. If the named lexer
     * does not exist anew hashtable is allocated.
     * @param lexerName the lexer name
     * @return the current lexer Hashtable
     */
    protected Hashtable addLexer(String lexerName) {
        currentLexer = (Hashtable) lexerTables.get(lexerName);
        if (currentLexer == null) {
            currentLexer = new Hashtable();
            lexerTables.put(lexerName, currentLexer);
        }
        return currentLexer;
    }


    /**
     * Selects a specific lexer by name.
     * @param lexerName the requested lexer
     */
    public abstract void selectLexer(String lexerName);

    /**
     * Default constructor.
     * Allocates a new hashtable and labels the Lexer
     * as "charLexer".
     */
    protected LexerCore() {
        this.currentLexer = new Hashtable();
        this.currentLexerName = "charLexer";
    }


    /**
     * Constructs a new lexer by name.
     * @param lexerName the name for the lexer
     */
    public LexerCore(String lexerName) {
        selectLexer(lexerName);
    }


    /**
     * Initialize the lexer with a buffer.
     * @param lexerName the requested lexer
     * @param buffer initial buffer to process
     */
    public LexerCore(String lexerName, String buffer) {
        this(lexerName);
        this.buffer = buffer;
    }

    /**
     * Peeks at the next id, but doesn't move the buffer pointer forward.
     * @return the textual ID of the next token
     */

    public String peekNextId() {
        int oldPtr = ptr;
        String retval = ttoken();
        savedPtr = ptr;
        ptr = oldPtr;
        return retval;
    }


    /**
     * Gets the next id.
     * @return textual ID of the next token
     */
    public String getNextId() {
        return ttoken();
    }

    // call this after you call match
    /**
     * Gets the next token.
     * @return the next token
     */
    public Token getNextToken() {
        return this.currentMatch;

    }

    /**
     * Looks ahead for one token.
     * @return the next token
     * @exception ParseException if an error occurs during parsing
     */
    public Token peekNextToken() throws ParseException {
        return (Token) peekNextToken(1).elementAt(0);
    }


    /**
     * Peeks at the next token.
     * @param ntokens the number of tokens to look ahead
     * @return a list of next tokens
     * @exception ParseException if an error occurs during parsing
     */
    public Vector peekNextToken(int ntokens) throws ParseException {
        int old = ptr;
        Vector retval = new Vector();
        for (int i = 0; i < ntokens; i++) {
            Token tok = new Token();
            if (startsId()) {
                String id = ttoken();
                tok.tokenValue = id;
                if (currentLexer.containsKey(id.toUpperCase())) {
                    Integer type = (Integer) currentLexer.get(id.toUpperCase());
                    tok.tokenType = type.intValue();
                } else tok.tokenType = ID;
            } else {
                char nextChar = getNextChar();
                tok.tokenValue =
                        new StringBuffer().append(nextChar).toString();
                if (isAlpha(nextChar)) {
                    tok.tokenType = ALPHA;
                } else if (isDigit(nextChar)) {
                    tok.tokenType = DIGIT;
                } else tok.tokenType = (int) nextChar;
            }
            retval.addElement(tok);
        }
        savedPtr = ptr;
        ptr = old;
        return retval;
    }

    /**
     * Match the given token or throw an exception, if no such token
     * can be matched.
     * @param tok the token to be checked
     * @return the matched token
     * @exception ParseException if an error occurs during parsing
     */
    public Token match(int tok) throws ParseException {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "match " + tok);
        }

        if (tok > START && tok < END) {
            if (tok == ID) {
                // Generic ID sought.
                if (!startsId())
                    throw new ParseException(buffer + "\nID expected", ptr);
                String id = getNextId();
                this.currentMatch = new Token();
                this.currentMatch.tokenValue = id;
                this.currentMatch.tokenType = ID;
            } else {
                String nexttok = getNextId();
                Integer cur =
                        (Integer) currentLexer.get(nexttok.toUpperCase());

                if (cur == null || cur.intValue() != tok)
                    throw new ParseException
                            (buffer + "\nUnexpected Token : "+
                            nexttok, ptr);
                this.currentMatch = new Token();
                this.currentMatch.tokenValue = nexttok;
                this.currentMatch.tokenType = tok;
            }
        } else if (tok > END) {
            // Character classes.
            char next = lookAhead(0);
            if (tok == DIGIT) {
                if (! isDigit(next))
                    throw new
                            ParseException(buffer + "\nExpecting DIGIT", ptr);
                this.currentMatch = new Token();
                this.currentMatch.tokenValue =
                        new StringBuffer().append(next).toString();
                this.currentMatch.tokenType = tok;
                consume(1);

            } else if (tok == ALPHA) {
                if (! isAlpha(next))
                    throw new ParseException
                            (buffer + "\nExpecting ALPHA", ptr);
                this.currentMatch = new Token();
                this.currentMatch.tokenValue =
                        new StringBuffer().append(next).toString();
                this.currentMatch.tokenType = tok;
                consume(1);

            }

        } else {
            // This is a direct character spec.
            Character ch = new Character((char)tok);
            char next = lookAhead(0);
            if (next == ch.charValue()) {
                this.currentMatch = new Token();
                this.currentMatch.tokenValue =
                        new StringBuffer().append(ch.charValue()).toString();
                this.currentMatch.tokenType = tok;
                consume(1);
            } else throw new
                    ParseException(buffer + "\nExpecting " +
                                   ch.charValue(), ptr);
        }
        return this.currentMatch;
    }

    /**
     * Checks for space or horiizontal tab.
     * The tokens are consumed if present.
     * All parsing errors are ignored.(if any)
     */
    public void SPorHT() {
        try {
            while (lookAhead(0) == ' ' || lookAhead(0) == '\t')
                consume(1);
        } catch (ParseException ex) {
            // Ignore
        }
    }

    /**
     * Checks for staring IDs.
     * @return true if next char is alphanumeric or
     * begins with appropriate punctuation characters.
     */
    public boolean startsId() {
        try {
            char nextChar = lookAhead(0);
            return isValidChar(nextChar);
        } catch (ParseException ex) {
            return false;
        }
    }

    /**
     * Gets the next textual token.
     * @return the next token as a string
     */
    public String ttoken() {
        StringBuffer nextId = new StringBuffer();
        try {
            while (hasMoreChars()) {
                char nextChar = lookAhead(0);
                // println("nextChar = " + nextChar);
                if (isValidChar(nextChar)) {
                    consume(1);
                    nextId.append(nextChar);
                } else break;

            }
            return nextId.toString();
        } catch (ParseException ex) {
            return nextId.toString();
        }
    }

    /**
     * Gets the next textual token including embedded
     * white space
     * @return the next text token as a string with embedded space and
     * tab characters
     */
    public String ttokenAllowSpace() {
        StringBuffer nextId = new StringBuffer();
        try {
            while (hasMoreChars()) {
                char nextChar = lookAhead(0);
                // println("nextChar = " + nextChar);
                if (isAlpha(nextChar) ||
                        isDigit(nextChar) ||
                        nextChar == '_' ||
                        nextChar == '+' ||
                        nextChar == '-' ||
                        nextChar == '!' ||
                        nextChar == '`' ||
                        nextChar == '\'' ||
                        nextChar == '~' ||
                        nextChar == '.' ||
                        nextChar == ' ' ||
                        nextChar == '\t' ||
                        nextChar == '*') {
                    nextId.append(nextChar);
                    consume(1);
                } else break;

            }
            return nextId.toString();
        } catch (ParseException ex) {
            return nextId.toString();
        }
    }


    // Assume the cursor is at a quote.
    /**
     * Gets a quoted string.
     * Read all the characters between double
     * quotes into the next textual token.
     * Preserve all back slash escaped characters.
     * @return the contents of the quoted string, both
     * starting and ending double quote characters
     * are consumed.
     * @exception ParseException if any parsing errors occur
     */
    public String quotedString() throws ParseException {
        StringBuffer retval = new StringBuffer();
        if (lookAhead(0) != '\"')
            return null;
        consume(1);
        while (true) {
            char next = getNextChar();
            if (next == '\"')
                break;
            else if (next == '\\') {
                retval.append(next);
                next = getNextChar();
                retval.append(next);
            } else {
                retval.append(next);
            }
        }
        return retval.toString();
    }

    // Assume the cursor is at a "("
    /**
     * Gets a comment string.
     * Consumes all characters between left and right
     * parens. Back slashed escaped characters are preserved.
     * @return the comment string, both starting and ending parens are
     * consumed.
     * @exception  ParseException if any parsing errors occur, or if the
     * comment is not properly closed
     */
    public String comment() throws ParseException {
        StringBuffer retval = new StringBuffer();
        if (lookAhead(0) != '(')
            return null;
        consume(1);
        while (true) {
            char next = getNextChar();
            if (next == ')') {
                break;
            } else if (next == '\0') {
                throw new ParseException(this.buffer + " :unexpected EOL",
                        this.ptr);
            } else if (next == '\\') {
                retval.append(next);
                next = getNextChar();
                if (next == '\0')
                    throw new ParseException(this.buffer +
                            " : unexpected EOL", this.ptr);
                retval.append(next);
            } else {
                retval.append(next);
            }
        }
        return retval.toString();
    }


    /**
     * Gets a token up to the next semicolon or end of line.
     * The end of line or terminating semicolon are not
     * consumed. If a parsing exception occurs, the consumed
     * characters are returned.
     * @return the next token without embedded semicolons
     */
    public String byteStringNoSemicolon() {
        StringBuffer retval = new StringBuffer();
        try {
            char next;
            while ((next = lookAhead(0)) != '\0') {
                if (next == '\n' || next == ';') {
                    break;
                } else {
                    consume(1);
                    retval.append(next);
                }
            }
        } catch (ParseException ex) {
            return retval.toString();
        }
        return retval.toString();
    }

    /**
     * Gets a token up to the next comma or end of line.
     * The end of line or terminating comma are not
     * consumed. If a parsing exception occurs, the consumed
     * characters are returned.
     * @return the next token without embedded commas
     */
    public String byteStringNoComma() {
        StringBuffer retval = new StringBuffer();
        try {
            char next;
            while ((next = lookAhead(0)) != '\0') {
                if (next == '\n' || next == ',') {
                    break;
                } else {
                    consume(1);
                    retval.append(next);
                }
            }
        } catch (ParseException ex) {
        }
        return retval.toString();
    }


    /**
     * Converts a character to a string.
     * @param ch the character to enclose
     * @return a string containing the single character
     */
    public static String charAsString(char ch) {
        return new Character(ch).toString();
    }

    /**
     * Lookahead in the inputBuffer for n chars and return as a string.
     * Do not consume the input. In the event of a parsing
     * error return the characters that could be consumed.
     * @param nchars the number of characters to look ahead
     * @return a string containing the designated characters
     */
    public String charAsString(int nchars) {

        StringBuffer retval = new StringBuffer();
        try {
            for (int i = 0; i < nchars; i++) {
                retval.append(lookAhead(i));
            }
            return retval.toString();
        } catch (ParseException ex) {
            return retval.toString();

        }
    }

    /**
     * Gets and consumes the next number.
     * Only digits are included in the returned string.
     * @return the parsed number as a string
     * @exception ParseException if any parsing errors occur
     */
    public String number() throws ParseException {

        StringBuffer retval = new StringBuffer();
        if (! isDigit(lookAhead(0))) {
            throw new ParseException
                    (buffer + ": Unexpected token at " +lookAhead(0), ptr);
        }
        retval.append(lookAhead(0));
        consume(1);
        while (true) {
            char next = lookAhead(0);
            if (isDigit(next)) {
                retval.append(next);
                consume(1);
            } else
                break;
        }
        return retval.toString();
    }

    /**
     * Mark the position for backtracking.
     * @return the current pointer in the parsed content
     */
    public int markInputPosition() {
        return ptr;
    }

    /**
     * Rewinds the input pointer to the marked position.
     * @param position the desired parsing location
     */
    public void rewindInputPosition(int position) {
        this.ptr = position;
    }

    /**
     * Gets the rest of the string buffer.
     * @return the remaining text in the buffer, or null if the
     * buffer has been consumed.
     */
    public String getRest() {
        if (ptr >= buffer.length())
            return null;
        else
            return buffer.substring(ptr);
    }

    /**
     * Gets the sub-String until the requested character is
     * encountered.
     * @param  c the character to match
     * @return the string up til the separator caharacter
     * @exception ParseException if a parsing error occurs
     */
    public String getString(char c) throws ParseException {
        int savedPtr = ptr;
        StringBuffer retval = new StringBuffer();
        while (true) {
            char next = lookAhead(0);

            if (next == '\0') {
                ParseException exception = new ParseException
                        (this.buffer +
                        "unexpected EOL", this.ptr);
                ptr = savedPtr;
                throw exception;
            } else if (next == c) {
                consume(1);
                break;
            } else if (next == '\\') {
                consume(1);
                char nextchar = lookAhead(0);
                if (nextchar == '\0') {
                    ParseException exception =
                            new ParseException(this.buffer +
                            "unexpected EOL", this.ptr);
                    ptr = savedPtr;
                    throw exception;
                } else {
                    consume(1);
                    retval.append(nextchar);
                }
            } else {
                consume(1);
                retval.append(next);
            }
        }
        return retval.toString();
    }






    /**
     * Gets the read pointer.
     * @return offset in the buffer
     */
    public int getPtr() { return this.ptr; }

    /**
     * Gets the buffer.
     * @return the parsing buffer
     */
    public String getBuffer() { return this.buffer; }

    /**
     * Creates a parse exception.
     * @return an exception with the current buffer and offset
     * in the exception contents
     */
    public ParseException createParseException() {
        return new ParseException(this.buffer, this.ptr);
    }
}
