// ThrowsTag.java, created Wed Mar 19 13:03:46 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.DocErrorReporter;
import net.cscott.sinjdoc.SourcePosition;
import net.cscott.sinjdoc.Tag;
import net.cscott.sinjdoc.TagVisitor;
import net.cscott.sinjdoc.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * The <code>PThrowsTag</code> class represents a @throws or @exception
 * documentation tag.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PThrowsTag.java,v 1.13 2003/05/08 03:54:26 cananian Exp $
 */
class PThrowsTag extends PTag.Trailing
    implements net.cscott.sinjdoc.ThrowsTag {
    final Type exceptionType;
    final String exceptionName;
    final List<Tag> exceptionComment;
    PThrowsTag(SourcePosition sp, String name, List<Tag> contents,
	       TypeContext tagContext) throws TagParseException {
	super(sp, name, contents);
	assert name()=="throws" || name()=="exception";
	// parse the tag
	Pair<Matcher,List<Tag>> pair =
	    extractRegexpFromHead(contents, NAME, "exception name");
	// okay, assign to the fields of the object.
	this.exceptionName = pair.left.group();
	this.exceptionType = parseParameterizedType
	    (tagContext, exceptionName,
	     ((PSourcePosition)contents.get(0).position()).add
	     (pair.left.start()));
	this.exceptionComment = pair.right;
    }
    private static final Pattern NAME = Pattern.compile("\\S+");

    public Type exception() { return exceptionType; }
    public List<Tag> exceptionComment() { return exceptionComment; }
    public String exceptionName() { return exceptionName; }

    public <T> T accept(TagVisitor<T> visitor) { return visitor.visit(this); }
}
