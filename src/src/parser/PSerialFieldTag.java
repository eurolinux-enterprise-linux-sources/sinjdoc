// SerialFieldTag.java, created Wed Mar 19 13:01:22 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.ClassType;
import net.cscott.sinjdoc.SerialFieldTag;
import net.cscott.sinjdoc.SourcePosition;
import net.cscott.sinjdoc.Tag;
import net.cscott.sinjdoc.TagVisitor;
import net.cscott.sinjdoc.Type;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The <code>PSerialFieldTag</code> class documents a Serializable field
 * defined by an ObjectStreamField.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PSerialFieldTag.java,v 1.10 2003/05/08 03:54:26 cananian Exp $
 */
class PSerialFieldTag extends PTag.Trailing
    implements net.cscott.sinjdoc.SerialFieldTag {
    final String fieldName;
    final Type   fieldType;
    final String fieldTypeString;
    final List<Tag> fieldDescription;
    PSerialFieldTag(SourcePosition sp, String name, List<Tag> contents,
		    TypeContext tagContext)
	throws TagParseException {
	super(sp, name, contents);
	assert name()=="serialField";
	//  parse the tag.
	Pair<Matcher,List<Tag>> pair =
	    extractRegexpFromHead(contents, NAME_AND_TYPE,
				  "field name and type");
	// okay, assign to the fields of the object.
	this.fieldName = pair.left.group(1);
	this.fieldTypeString = pair.left.group(2);
	// XXX what if field type is array?
	this.fieldType = parseParameterizedType
	    (tagContext, fieldTypeString,
	     ((PSourcePosition)contents.get(0).position()).add
	     (pair.left.start(2)));
	this.fieldDescription = pair.right;
    }
    private static final Pattern NAME_AND_TYPE = Pattern.compile
	("(\\S+)\\s+(\\S+)");

    /** Compare based on source position. */
    public int compareTo(SerialFieldTag tag) {
	if (position().line() != tag.position().line())
	    return position().line() - tag.position().line();
	return position().column() - tag.position().column();
    }
    public List<Tag> description() { return fieldDescription; }
    public String fieldName() { return fieldName; }
    public String fieldType() { return fieldTypeString; }
    public ClassDoc fieldTypeDoc() {
	if (fieldType instanceof ClassType)
	    return ((ClassType)fieldType).asClassDoc();
	// XXX also link in the base type's doc for array and parameterized
	//     types?
	return null;
    }

    public <T> T accept(TagVisitor<T> visitor) { return visitor.visit(this); }
}
