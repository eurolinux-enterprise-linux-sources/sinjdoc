// ParamTag.java, created Wed Mar 19 12:42:42 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.DocErrorReporter;
import net.cscott.sinjdoc.SourcePosition;
import net.cscott.sinjdoc.Tag;
import net.cscott.sinjdoc.TagVisitor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * The <code>PParamTag</code> class represents a @param documentation tag.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PParamTag.java,v 1.10 2003/05/08 03:54:26 cananian Exp $
 */
class PParamTag extends PTag.Trailing
    implements net.cscott.sinjdoc.ParamTag {
    final String parameterName;
    final List<Tag> parameterComment;
    PParamTag(SourcePosition sp, String name, List<Tag> contents)
	throws TagParseException {
	super(sp, name, contents);
	assert name()=="param";
	// parse the tag.
	Pair<Matcher,List<Tag>> pair =
	    extractRegexpFromHead(contents, NAME, "parameter name");
	// assign to fields.
	this.parameterName = pair.left.group();
	this.parameterComment = pair.right;
    }
    private static final Pattern NAME = Pattern.compile("\\S+");

    public List<Tag> parameterComment() { return parameterComment; }
    public String parameterName() { return parameterName; }

    public <T> T accept(TagVisitor<T> visitor) { return visitor.visit(this); }
}
