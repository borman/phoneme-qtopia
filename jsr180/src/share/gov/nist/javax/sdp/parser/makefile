#
# Portions Copyright  2000-2008 Sun Microsystems, Inc. All Rights
# Reserved.  Use is subject to license terms.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License version
# 2 only, as published by the Free Software Foundation.
# 
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License version 2 for more details (a copy is
# included at /legal/license.txt).
# 
# You should have received a copy of the GNU General Public License
# version 2 along with this work; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA
# 
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
# Clara, CA 95054 or visit www.sun.com if you need additional
# information or have any questions.
#
# this works with the gnu make tool. If you are working with windows, please install cygwin to get
# gnumake (goto http://www.sourceware.cygnus.com).

ROOT=../../../../../../

include  $(ROOT)/build-config

all: 
	$(JAVAC) *.java

clean:  emacsclean
	$(RM) *.class
	$(RM) $(DESTINATION)/gov/nist/javax/sip/parser/*.class


origin: OriginFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.OriginFieldParser

attribparse: AttributeFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.AttributeFieldParser

bwparse: BandwidthFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.BandwidthFieldParser

cfparse: ConnectionFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.ConnectionFieldParser

emparse: EmailFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.EmailFieldParser

iparse: InformationFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.InformationFieldParser

keyparse: KeyFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.KeyFieldParser

mparse: MediaFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.MediaFieldParser

phoneparse: PhoneFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.PhoneFieldParser

protoparse: ProtoVersionFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.ProtoVersionFieldParser

repeat: RepeatFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.RepeatFieldParser

uri: URIFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.URIFieldParser

timeparse: TimeFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.TimeFieldParser
