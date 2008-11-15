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
package gov.nist.siplite.parser;

import gov.nist.core.*;
/**
 * Token types.
 */
public interface TokenTypes {
    /** Start token type. */
    public static final int START = LexerCore.START;
    // Everything under this is reserved
    /** End token type. */
    public static final int END = LexerCore.END;
    // End markder.
    /** SIP protocol scheme token type. */
    public static final int SIP = START + 3;
    /** Registration method token type. */
    public static final int REGISTER = START + 4;
    /** Invitation method token type. */
    public static final int INVITE = START + 5;
    /** Acknowledgement response token type. */
    public static final int ACK = START + 6;
    /** Termination response token type. */
    public static final int BYE = START + 7;
    /** Options request token type. */
    public static final int OPTIONS = START + 8;
    /** Cancelation response token type. */
    public static final int CANCEL = START + 9;
    /** Error information response token type. */
    public static final int ERROR_INFO = START + 10;
    /** Reply message response token type. */
    public static final int IN_REPLY_TO = START + 11;
    /** MIME message version token type. */
    public static final int MIME_VERSION = START + 12;
    /** Alert information token type. */
    public static final int ALERT_INFO = START + 13;
    /** From message header token type. */
    public static final int FROM = START + 14;
    /** To message header token type. */
    public static final int TO = START + 15;
    /** Via message header token type. */
    public static final int VIA = START + 16;
    /** User agent message header token type. */
    public static final int USER_AGENT = START + 17;
    /** Server message header token type. */
    public static final int SERVER = START + 18;
    /** Accept encoding message header token type. */
    public static final int ACCEPT_ENCODING = START + 19;
    /** Accept types message header token type. */
    public static final int ACCEPT = START + 20;
    /** Allow message header token type. */
    public static final int ALLOW = START + 21;
    /** Message route header token type. */
    public static final int ROUTE = START + 22;
    /** Authorization header token type. */
    public static final int AUTHORIZATION = START + 23;
    /** Proxy authorization header token type. */
    public static final int PROXY_AUTHORIZATION = START + 24;
    /** Retry transmission header token type. */
    public static final int RETRY_AFTER = START + 25;
    /** Proxy required response token type. */
    public static final int PROXY_REQUIRE = START + 26;
    /** Content language header token type. */
    public static final int CONTENT_LANGUAGE = START + 27;
    /** Unsupported message token type. */
    public static final int UNSUPPORTED = START + 28;
    /** Supported message token type. */
    public static final int SUPPORTED = START + 29;
    /** Warning message token type. */
    public static final int WARNING = START + 30;
    /** Maximum forwards header token type. */
    public static final int MAX_FORWARDS = START + 31;
    /** Date header token type. */
    public static final int DATE = START + 32;
    /** Priority header token type. */
    public static final int PRIORITY = START + 33;
    /** Proxy authenticate header token type. */
    public static final int PROXY_AUTHENTICATE = START + 34;
    /** Content encoding header token type. */
    public static final int CONTENT_ENCODING = START + 35;
    /** Content length header token type. */
    public static final int CONTENT_LENGTH = START + 36;
    /** Subject header token type. */
    public static final int SUBJECT = START + 37;
    /** Content type header token type. */
    public static final int CONTENT_TYPE = START + 38;
    /** Contact header token type. */
    public static final int CONTACT = START + 39;
    /** Caller identification header token type. */
    public static final int CALL_ID = START + 40;
    /** Required header token type. */
    public static final int REQUIRE = START + 41;
    /** Expires header token type. */
    public static final int EXPIRES = START + 42;
    /** Encryption header token type. */
    public static final int ENCRYPTION = START + 43;
    /** Record routing header token type. */
    public static final int RECORD_ROUTE = START + 44;
    /** Organization header token type. */
    public static final int ORGANIZATION = START + 45;
    /** C-Sequence header token type. */
    public static final int CSEQ = START + 46;
    /** Accept language header token type. */
    public static final int ACCEPT_LANGUAGE = START + 47;
    /** WWW Authenticate header token type. */
    public static final int WWW_AUTHENTICATE = START + 48;
    /** Response key header token type. */
    public static final int RESPONSE_KEY = START + 49;
    /** Hide header token type. */
    public static final int HIDE = START + 50;
    /** Caller information header token type. */
    public static final int CALL_INFO = START + 51;
    /** Content disposition header token type. */
    public static final int CONTENT_DISPOSITION = START + 52;
    /** Subscription method token type. */
    public static final int SUBSCRIBE = START + 53;
    /** Notification method token type. */
    public static final int NOTIFY = START + 54;
    /** Timestamp header token type. */
    public static final int TIMESTAMP = START + 55;
    /** Subscription state header token type. */
    public static final int SUBSCRIPTION_STATE = START + 56;
    /** Telephone protocol scheme token type. */
    public static final int TEL = START + 57;
    /** Reply to message header token type. */
    public static final int REPLY_TO = START + 58;
    /** Reason header token type. */
    public static final int REASON = START + 59;
    /** R-sequence header token type. */
    public static final int RSEQ = START + 60;
    /** R-acknowledgement header token type. */
    public static final int RACK = START + 61;
    /** Minutes til expiration header token type. */
    public static final int MIN_EXPIRES = START + 62;
    /** Event header token type. */
    public static final int EVENT = START + 63;
    /** Authentication information token type. */
    public static final int AUTHENTICATION_INFO = START + 64;
    /** Allow events token type. */
    public static final int ALLOW_EVENTS = START + 65;
    /** Refer-To token type. */
    public static final int REFER_TO = START + 66;
    /** SIPS protocol scheme token type. */
    public static final int SIPS = START + 67;
    /** MESSAGE method token type. */
    public static final int MESSAGE = START + 68;
    /** REFER method token type. */
    public static final int REFER = START + 69;
    /** PRACK method token type. */
    public static final int PRACK = START + 70;
    /** INFO method token type. */
    public static final int INFO = START + 71;
    /** UPDATE method token type. */
    public static final int UPDATE = START + 72;
    /** PUBLISH method token type. */
    public static final int PUBLISH = START + 73;
    /** ACCEPT_CONTACT method token type. */
    public static final int ACCEPT_CONTACT = START + 74;
    /** Alphabetic token type. */
    public static final int ALPHA = LexerCore.ALPHA;
    /** Decimal digit token type. */
    public static final int DIGIT = LexerCore.DIGIT;
    /** Identifier token type. */
    public static final int ID = LexerCore.ID;
    /** Whitespace token type. */
    public static final int WHITESPACE = LexerCore.WHITESPACE;
    /** Escaped character (backslash) token type. */
    public static final int BACKSLASH = LexerCore.BACKSLASH;
    /** Quote character token type. */
    public static final int QUOTE = LexerCore.QUOTE;
    /** At sign token type. */
    public static final int AT = LexerCore.AT;
    /** Space character (' ') token type. */
    public static final int SP = LexerCore.SP;
    /** Horizontal tab token type. */
    public static final int HT = LexerCore.HT;
    /** Colon character token type. */
    public static final int COLON = LexerCore.COLON;
    /** Asterisk character token type. */
    public static final int STAR = LexerCore.STAR;
    /** Dollar sign character token type. */
    public static final int DOLLAR = LexerCore.DOLLAR;
    /** Plus sign character token type. */
    public static final int PLUS = LexerCore.PLUS;
    /** Hash mark character token type. */
    public static final int POUND = LexerCore.POUND;
    /** Minus sign character token type. */
    public static final int MINUS = LexerCore.MINUS;
    /** Double quote character token type. */
    public static final int DOUBLEQUOTE = LexerCore.DOUBLEQUOTE;
    /** Tilde character token type. */
    public static final int TILDE = LexerCore.TILDE;
    /** Back quote character token type. */
    public static final int BACK_QUOTE = LexerCore.BACK_QUOTE;
    /** Null token type. */
    public static final int NULL = LexerCore.NULL;
    /** Equals sign  character token type. */
    public static final int EQUALS = (int) '=';
    /** Semicolon character token type. */
    public static final int SEMICOLON = (int) ';';
    /** Forward slash character token type. */
    public static final int SLASH = (int) '/';
    /** Left square bracket character token type. */
    public static final int L_SQUARE_BRACKET = (int) '[';
    /** Right square bracket character token type. */
    public static final int R_SQUARE_BRACKET = (int) ']';
    /** Right curly bracket character token type. */
    public static final int R_CURLY = (int) '}';
    /** Left curly bracket character token type. */
    public static final int L_CURLY = (int) '{';
    /** Carret character token type. */
    public static final int HAT = (int) '^';
    /** Vertical bar character token type. */
    public static final int BAR = (int) '|';
    /** Period character token type. */
    public static final int DOT = (int) '.';
    /** Exclamation character token type. */
    public static final int EXCLAMATION = (int) '!';
    /** Left paren character token type. */
    public static final int LPAREN = (int) '(';
    /** Right paren character token type. */
    public static final int RPAREN = (int) ')';
    /** Right angle bracket character token type. */
    public static final int GREATER_THAN = (int) '>';
    /** Left angle bracket character token type. */
    public static final int LESS_THAN = (int) '<';
    /** Percentage character token type. */
    public static final int PERCENT = (int) '%';
    /** Question mark character token type. */
    public static final int QUESTION = (int) '?';
    /** Ampersand character token type. */
    public static final int AND = (int) '&';
    /** Underscore character token type. */
    public static final int UNDERSCORE = (int) '_';

}
