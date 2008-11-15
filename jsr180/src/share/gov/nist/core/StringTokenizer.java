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

import java.util.*;

/**
 * Base string token splitter.
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class StringTokenizer {
    /** Current buffer to be parsed. */
    protected String buffer;
    /** Current offset int input buffer. */
    protected int ptr;
    /** Saved pointer for peek operations. */
    protected int savedPtr;
    /** Current token delimiter. */
    char delimiter;

    /**
     * Default constructor.
     * Resets the buffer offset to 0 and the default
     * newline delimiter.
     */
    public StringTokenizer() {
        this.delimiter = '\n';
        this.ptr = 0;
    }
    /**
     * Constructs a string tokenizer for input buffer.
     * @param buffer the text to be parsed
     */
    public StringTokenizer(String buffer) {
        this.buffer = buffer;
        this.ptr = 0;
        this.delimiter = '\n';

    }

    /**
     * Constructs a string tokenizer for input buffer
     * and specified field separator.
     * @param buffer the text to be parsed
     * @param delimiter the field separator character
     */
    public StringTokenizer(String buffer, char delimiter) {
        this.buffer = buffer;
        this.delimiter = delimiter;
        this.ptr = 0;
    }

    /**
     * Gets the next token.
     * @return the next token, not including the field separator
     */
    public String nextToken() {
        StringBuffer retval = new StringBuffer();

        while (ptr < buffer.length()) {
            if (buffer.charAt(ptr) == delimiter) {
                retval.append(buffer.charAt(ptr));
                ptr++;
                break;
            } else {
                retval.append(buffer.charAt(ptr));
                ptr++;
            }
        }

        return retval.toString();
    }

    /**
     * Checks if more characters are available.
     * @return true if more characters can be processed
     */
    public boolean hasMoreChars() {
        return ptr < buffer.length();
    }

    /**
     * Checks if character is part of a hexadecimal number.
     * @param ch character to be checked
     * @return true if the character is a hex digit
     */
    public static boolean isHexDigit(char ch) {
        if (isDigit(ch))
            return true;
        else {
            char ch1 = Character.toUpperCase(ch);
            return ch1 == 'A' || ch1 == 'B' || ch1 == 'C' ||
                    ch1 == 'D' || ch1 == 'E' || ch1 == 'F';
        }
    }

    /**
     * Checks if the character is an alphabetic character.
     * @param ch the character to be checked.
     * @return true if the character is alphabetic
     */
    public static boolean isAlpha(char ch) {
        boolean retval = Character.isUpperCase(ch) ||
                Character.isLowerCase(ch);
        // Debug.println("isAlpha is returning " + retval  + " for " + ch);
        return retval;
    }

    /**
     * Checks if the character is a numeric character.
     * @param ch the character to be checked.
     * @return true if the character is a deciomal digit
     */
    public static boolean isDigit(char ch) {
        boolean retval =  Character.isDigit(ch);
        // Debug.println("isDigit is returning " + retval + " for " + ch);
        return retval;
    }

    /**
     * Checks if the string contains numeric characters only.
     * @param str the string to be checked.
     * @return true if the string contains numeric characters only
     */
    public static boolean isDigitString(String str) {
        int len = str.length();
        if (len == 0) { // empty string - return false
            return false;
        } else {
            boolean retval = true;
            for (int i = 0; i < str.length(); i++) {
                if (!Character.isDigit(str.charAt(i))) {
                    retval = false;
                    break;
                }
            }
            return retval;
        }
    }

    /**
     * Checks if the given character is allowed in method/header/parameter name.
     * The character is valid if it is: (1) a digit or (2) a letter, or
     * (3) is one of the characters on the next list: -.!%*_+`'~
     * @param ch the character to check
     * @return true if the character is valid, false otherwise
     */
    public static boolean isValidChar(char ch) {
        String validChars = "-.!%*_+`'~";

        if (!((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z') ||
              (ch >= 'a' && ch <= 'z')) && (validChars.indexOf(ch) == -1)) {
            // ("Invalid character '" + ch + "' in the name.");
            return false;
        }

        return true;
    }

    /**
     * Checks if the given symbol belongs to the escaped group.
     * The character is escaped if it is satisfies the next ABNF
     * (see RFC3261 p.220): <br>
     * escaped  =  "%" HEXDIG HEXDIG
     * <br>
     * @param name the string to be parsed for escaped value
     * @param index shift inside parsed string
     * @return true if string contains escaped value, false otherwise
     */
    public static boolean isEscaped(String name, int index) {
        // RFC3261 p.220
        // escaped     =  "%" HEXDIG HEXDIG
        //
        if (name.charAt(index) != '%' ||
            (name.length() - index - 2) < 0 ||
            !isHexDigit(name.charAt(index + 1)) ||
            !isHexDigit(name.charAt(index + 2))) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the given sequence is quoted pair.
     *
     * @param name the string to be parsed for quoted pair
     * @param offset inside parsed string
     * @return true if quoted pair is placed at <code>name</code>
     * [<code>offset</code>], false otherwise
     */
    public static boolean isQuotedPair(String name, int offset) {
        // RFC3261 p.222
        // quoted-pair  =  "\" (%x00-09 / %x0B-0C
        //                / %x0E-7F)
        //
        if (name.charAt(offset) != '\\' ||
            (name.length() - offset - 1) <= 0) {
            return false;
        }

        char ch = name.charAt(offset + 1);
        if (ch == 0xA ||
            ch == 0xD ||
            ch > 0x7F) {
            return false;
        }

        return true;

    }

    /**
     * Gets the next line of text.
     * @return characters up to the next newline
     */
    public String getLine() {
        StringBuffer retval = new StringBuffer();
        while (ptr < buffer.length() && buffer.charAt(ptr) != '\n') {
            retval.append(buffer.charAt(ptr));
            ptr++;
        }
        if (ptr < buffer.length() && buffer.charAt(ptr) == '\n') {
            retval.append('\n');
            ptr++;
        }
        return retval.toString();
    }

    /**
     * Peeks at the next line without consuming the
     * characters.
     * @return the next line of text
     */
    public String peekLine() {
        int curPos = ptr;
        String retval = this.getLine();
        ptr = curPos;
        return retval;
    }

    /**
     * Looks ahead one character in the input buffer
     * without consuming the character.
     * @return the next character in the input buffer
     * @exception ParseException if a parsing error occurs
     */
    public char lookAhead() throws ParseException {
        return lookAhead(0);
    }

    /**
     * Looks ahead a specified number of characters in the input buffer
     * without consuming the character.
     * @param k the number of characters to advance the
     * current buffer offset
     * @return the requested character in the input buffer
     * @exception ParseException if a parsing error occurs
     */
    public char lookAhead(int k) throws ParseException  {
        // Debug.out.println("ptr = " + ptr);
        if (ptr+k < buffer.length())
            return buffer.charAt(ptr + k);
        else return '\0';
    }

    /**
     * Gets one character in the input buffer
     * and consumes the character.
     * @return the next character in the input buffer
     * @exception ParseException if a parsing error occurs
     */
    public char getNextChar() throws ParseException {
        if (ptr >= buffer.length())
            throw new ParseException
                    (buffer + " getNextChar: End of buffer", ptr);
        else return buffer.charAt(ptr++);
    }

    /**
     * Advances the current pointer to the saved peek pointer
     * to consume the characters that were pending parsing
     * completion.
     */
    public void  consume() {
        ptr = savedPtr;
    }

    /**
     * Consume the specified number of characters from the input
     * buffer.
     * @param k the number of characters to advance the
     * current buffer offset
     */
    public void consume(int k) {
        ptr += k;
    }

    /**
     * Gets a Vector of the buffer tokenized by lines.
     * @return vector of tokens
     */
    public Vector getLines() {
        Vector result = new Vector();
        while (hasMoreChars()) {
            String line = getLine();
            result.addElement(line);
        }
        return result;
    }


    /**
     * Gets the next token from the buffer.
     * @param delim the field separator
     * @return the next textual token
     * @exception ParseException if a parsing error occurs
     */
    public String getNextToken(char delim) throws ParseException {
        StringBuffer retval = new StringBuffer();
        while (true) {
            char la = lookAhead(0);
            // System.out.println("la = " + la);
            if (la == delim) break;
            else if (la == '\0')
                throw new ParseException("EOL reached", 0);
            retval.append(buffer.charAt(ptr));
            consume(1);
        }
        return retval.toString();
    }

    /**
     * Gets the SDP field name of the line.
     * @param line the input buffer to be parsed
     * @return the SDP field name
     */
    public static String getSDPFieldName(String line) {
        if (line == null)
            return null;
        String fieldName = null;
        try {
            int begin = line.indexOf("=");
            fieldName = line.substring(0, begin);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return fieldName;
    }

    /**
     * According to the RFC 3261, section 7.3.1:
     *
     * Header fields can be extended over multiple lines by preceding each
     * extra line with at least one SP or horizontal tab (HT).  The line
     * break and the whitespace at the beginning of the next line are
     * treated as a single SP character.
     *
     * This function converts all pairs of newline+space/tab in the
     * string 's' into signle spaces.
     *
     * @param s string to handle.
     * @return processed string.
     */
    public static String convertNewLines(String s) {
        int  i;
        char chCurr;
        String result = "";
        // new Exception("convertNewLines").printStackTrace();

        if (s.length() == 0) {
            return result;
        }

        // Eat leading spaces and carriage returns (necessary??).
        i = 0;
        i = skipWhiteSpace(s, i);

        while (i < s.length()) {
            chCurr = s.charAt(i);

            // Actually, the spec requires "<CRLF> <Space|Tab>" for multiline
            // header values, but we support also LFCR, LF and CR.
            if (chCurr == '\n' || chCurr == '\r') {
                if (i < s.length() - 1 &&
                        (s.charAt(i+1) == '\t' || s.charAt(i+1) == ' ')) {
                    // Check if the last saved symbol is CR or LF.
                    // This will be needed if we decide not to skip CRLF bellow.
                    result += ' ';
                    i++;
                } else {
                    /*
                     * RFC 3261, p. 221:
                     * A CRLF is allowed in the definition of TEXT-UTF8-TRIM
                     * only as part of a header field continuation. It is
                     * expected that the folding LWS will be replaced with
                     * a single SP before interpretation of the TEXT-UTF8-TRIM
                     * value.
                     *
                     * But it's not clearly defined what to do if CRLF or CR, or
                     * LF without following LWS is occured, so we just skip it.
                     */
                }
            } else {
                result += chCurr;
            }

            i++;
        } // end while()

        // System.out.println("@@@\nconverted from:\n<<"+s+">> " +
        //                    "into:\n<<"+result+">>");

        return result;
    }

    /**
     * Skip whitespace that starts at offset i in the string s
     * @param s string containing some text
     * @param i offset where the whitespace begins
     * @return offset of the text following the whitespace
     */
    private static int skipWhiteSpace(String s, int i) {
        int len = s.length();
        if (i >= len) {
            return i;
        }

        char chCurr;
        chCurr = s.charAt(i);

        while (chCurr == '\n' || chCurr == '\r' ||
               chCurr == '\t' || chCurr == ' ') {
            i++;
            if (i >= len) break;
            chCurr = s.charAt(i);
        }

        return i;
    }

}
