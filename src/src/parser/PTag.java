// Tag.java, created Wed Mar 19 12:45:35 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ClassType;
import net.cscott.sinjdoc.DocErrorReporter;
import net.cscott.sinjdoc.SourcePosition;
import net.cscott.sinjdoc.Tag;
import net.cscott.sinjdoc.TagVisitor;
import net.cscott.sinjdoc.Type;
import net.cscott.sinjdoc.TypeArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * The <code>PTag</code> class represents a documentation tag.  The
 * tag name and tag text are available for queries; tags with structure
 * or which require special processing are handled by subclasses.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PTag.java,v 1.21 2003/08/01 00:01:26 cananian Exp $
 */
abstract class PTag 
    implements net.cscott.sinjdoc.Tag {
    final SourcePosition sp;
    PTag(SourcePosition sp) { this.sp = sp; }
    public boolean isInline() { return false; }
    public boolean isText() { return false; }
    public boolean isTrailing() { return false; }

    public String name() { return null; }
    public String text() { return null; }
    public SourcePosition position() { return sp; }
    public List<Tag> contents() { return null; }
    public <T> T accept(TagVisitor<T> visitor) { return visitor.visit(this); }

    public String toString() {
	if (isText()) return text();
	StringBuffer sb=new StringBuffer();
	if (isInline()) sb.append("{");
	sb.append("@"); sb.append(name()); sb.append(" ");
	for (Tag t : contents())
	    sb.append(t.toString());
	if (isInline()) sb.append("}");
	return sb.toString();
    }

    // utility functions.
    // XXX make handle nested parameterized types
    // XXX make handle wildcards
    static Type parseParameterizedType(TypeContext tagContext, String typeName,
				       PSourcePosition pos)
	throws TagParseException {
	Stack<List<TypeArgument>> typeStack = new Stack<List<TypeArgument>>();
	typeStack.push(new ArrayList<TypeArgument>(2));
	Matcher matcher = PIECE.matcher(typeName);
	int lastMatch = 0;
	while (matcher.find()) {
	    lastMatch=matcher.end();
	    typeStack.peek().add(new PTypeArgument
				 (tagContext.lookupTypeName(matcher.group(1),
							    true/*lazy*/),
				  false, false));
	    if ("<".equals(matcher.group(2))) { // start new parameterized type
		if (!(typeStack.peek().get(typeStack.peek().size()-1).getType()
		      instanceof ClassType))
		    throw new TagParseException
			(pos.add(matcher.start(1)),
			 "can't parameterize a type variable");
		typeStack.push(new ArrayList<TypeArgument>(2));
	    } else if (">".equals(matcher.group(2))) {// end parameterized type
		if (typeStack.size()==1)
		    throw new TagParseException
			(pos.add(matcher.start(2)), "unmatched > in type");
		List<TypeArgument> typeArgs = typeStack.pop();
		int last = typeStack.peek().size()-1;
		assert last>=0; // regexp doesn't match otherwise
		ClassType base=(ClassType)typeStack.peek().get(last).getType();
		// XXX doesn't handle parameterized inner classes.
		Type pt = new PParameterizedType(base,null,typeArgs);
		typeStack.peek().set(last,
				     new PTypeArgument(pt, false, false));
	    }
	}
	if (lastMatch!=typeName.length())
	    throw new TagParseException
		(pos.add(lastMatch), "Trailing garbage");
	if (typeStack.size()!=1)
	    throw new TagParseException
		(pos.add(lastMatch-1), "unmatched < in type");
	if (typeStack.peek().size()<1)
	    throw new TagParseException(pos, "no type found");
	if (typeStack.peek().size()>1)
	    throw new TagParseException(pos, "too many types found");
	return typeStack.peek().get(0).getType();
    }
    private static final Pattern PIECE=Pattern.compile
	("\\G\\s*([^<>,]+)\\s*(\\z|[<>,])");

    static class Text extends PTag {
	final String text;
	public boolean isText() { return true; }
	public String text() { return text; }
	Text(SourcePosition sp, String text) {
	    super(sp);
	    this.text = text;
	}
    }
    static abstract class NonText extends PTag {
	final String name;
	final List<Tag> contents;
	public final String name() { return name; }
	public final List<Tag> contents() { return contents; }
	NonText(SourcePosition sp, String name, List<Tag> contents) {
	    super(sp);
	    this.name = name.intern();
	    this.contents = contents;
	}
	// a generic parsing function using a regexp.
	protected Pair<Matcher,List<Tag>> extractRegexpFromHead
	    (List<Tag> contents, Pattern pat, String patternDescription)
	    throws TagParseException {
	    return extractRegexp(contents, pat, patternDescription, true);
	}
	protected Pair<Matcher,List<Tag>> extractRegexpFromTail
	    (List<Tag> contents, Pattern pat, String patternDescription)
	    throws TagParseException {
	    return extractRegexp(contents, pat, patternDescription, false);
	}
	private Pair<Matcher,List<Tag>> extractRegexp
	    (List<Tag> contents, Pattern pat, String patternDescription,
	     boolean fromHead) throws TagParseException {
	    if (contents.size()==0)
		throw new TagParseException
		    (position(), "Empty @"+name()+" tag");
	    Tag tag = contents.get(fromHead?0:(contents.size()-1));
	    if (!tag.isText())
		throw new TagParseException
		    (tag.position(), "Illegal inline tag in @"+name());

	    Matcher matcher = pat.matcher(tag.text());
	    if (!matcher.find())
		throw new TagParseException
		    (tag.position(), "Invalid or missing "+patternDescription+" in "+
		     "@"+name()+" tag");
	    ArrayList<Tag> nContents = new ArrayList<Tag>(contents.size());
	    if (fromHead) { // trim first element
		if (matcher.end()!=tag.text().length())
		    nContents.add
			(PTag.newTextTag
			 (tag.text().substring(matcher.end()),
			  ((PSourcePosition)tag.position()).add(matcher.end())));
		nContents.addAll(contents.subList(1, contents.size()));
	    } else { // trim last element
		nContents.addAll(contents.subList(0, contents.size()-1));
		if (matcher.start()>0)
		    nContents.add
			(PTag.newTextTag
			 (tag.text().substring(0, matcher.start()),
			  tag.position()));
	    }
	    nContents.trimToSize();
	    // okay!
	    return new Pair<Matcher,List<Tag>>
		(matcher, Collections.unmodifiableList(nContents));
	}
    }
    static class Trailing extends NonText {
	public boolean isTrailing() { return true; }
	Trailing(SourcePosition sp, String name, List<Tag> contents) {
	    super(sp, name, contents);
	}
    }
    static class Inline extends NonText {
	public boolean isInline() { return true; }
	Inline(SourcePosition sp, String name, List<Tag> contents) {
	    super(sp, name, contents);
	}
    }
    /** Convenience method to create a new 'Tag' representing plain-text;
     *  i.e. it has kind()=="Text". */
    static Tag newTextTag(final String text, final SourcePosition pos) {
	return new Text(pos, text);
    }
    /** Select a new non-inline Tag object to create based on the tagname. */
    static Tag newTag(String tagname, List<Tag> contents,
		      SourcePosition pos, TypeContext tagContext) {
	tagname=tagname.intern();
	try {
	    if (tagname=="param")
		return new PParamTag(pos, tagname, contents);
	    if (tagname=="see")
		return new PSeeTag(pos, tagname, contents, tagContext);
	    if (tagname=="serialField")
		return new PSerialFieldTag(pos, tagname, contents, tagContext);
	    if (tagname=="throws" || tagname=="exception")
		return new PThrowsTag(pos, tagname, contents, tagContext);
	} catch (TagParseException tpe) {
	    tagContext.pc.reporter.printError
		(tpe.getPosition(), tpe.getMessage());
	}
	// opaque tag, no internal structure.
	return new Trailing(pos, tagname, contents);
    }
    /** Select a new inline Tag object to create based on the tagname. */
    // sp is position of first char of 'tagname'
    static Tag newInlineTag(String tagname, List<Tag> contents,
			    SourcePosition pos, TypeContext tagContext) {
	DocErrorReporter reporter = tagContext.pc.reporter;
	tagname=tagname.intern();
	try {
	    if (tagname=="link" || tagname=="linkplain")
		return new PSeeTag(pos, tagname, contents, tagContext);
	} catch (TagParseException tpe) {
	    tagContext.pc.reporter.printError
		(tpe.getPosition(), tpe.getMessage());
	}
	return new Inline(pos, tagname, contents);
    }

    static protected class TagParseException extends Exception {
	private final SourcePosition position;
	TagParseException(SourcePosition position, String message) {
	    super(message);
	    this.position = position;
	}
	public SourcePosition getPosition() { return position; }
    }
}
