// Doc.java, created Wed Mar 19 12:04:15 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.Doc;
import net.cscott.sinjdoc.ProgramElementDoc;
import net.cscott.sinjdoc.SeeTag;
import net.cscott.sinjdoc.SourcePosition;
import net.cscott.sinjdoc.Tag;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The <code>PDoc</code> class is the abstract base class representing
 * all java language constructs (classes, packages, methods, etc) which
 * have comments and have been processed by this run of SinjDoc.  All
 * <code>PDoc</code> items are <code>ReferenceUnique</code>, that is, they
 * are == comparable.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PDoc.java,v 1.26 2003/08/01 00:01:26 cananian Exp $
 */
abstract class PDoc implements net.cscott.sinjdoc.Doc {
    final ParseControl pc;
    PDoc(ParseControl pc) {
	this.pc = pc;
    }
    public abstract String getRawCommentText();
    /** Return a <code>PSourcePosition</code> object corresponding to the
     *  position of the raw comment text. */
    public abstract PSourcePosition getRawCommentPosition();
    /** Return a <code>TypeContext</code> that should be used for @see
     *  tags on this doc item. */
    public abstract TypeContext getCommentContext();
    /** Return true if leading stars should be stripped from the raw
     *  comment text for this doc item.
     * @return true
     */
    public boolean shouldStripStars() { return true; }
    public boolean isClass() { return false; }
    public boolean isConstructor() { return false; }
    public boolean isError() { return false; }
    public boolean isException() { return false; }
    public boolean isField() { return false; }
    public abstract boolean isIncluded();
    public boolean isInterface() { return false; }
    public boolean isMethod() { return false; }
    public boolean isOrdinaryClass() { return false; }
    public abstract String name();
    public SourcePosition position() { return PSourcePosition.NO_INFO; }

    // parse raw comment text into tags using regexp.
    public List<Tag> tags() {
	if (tagCache!=null) return tagCache;
	List<Tag> result = new ArrayList<Tag>();
	String raw = getRawCommentText();
	PSourcePosition sp = getRawCommentPosition();
	boolean stripStars = shouldStripStars();
	TypeContext tagContext = getCommentContext();
	// pull out all the non-inline tags.
	Pattern tagPattern = (stripStars?TAGPATSS:TAGPAT);
	Matcher tagMatcher = tagPattern.matcher(raw);
	int start = tagMatcher.find() ? tagMatcher.start() : raw.length();
	String firstPart = raw.substring(0,start); // doc comment.
	String lastPart = raw.substring(start); // tag portion.
	// create the initial 'text' section.
	result.addAll(parseInline(firstPart, sp));
	// now the tags.
	sp = sp.add(start);
	int lastTagStart=0, lastTagEnd=0;  String lastTagName=null;
	tagMatcher = tagPattern.matcher(lastPart);
	while (tagMatcher.find()) {
	    // last tag went from lastTagStart to tagMatcher.start()
	    if (lastTagName!=null) {
		List<Tag> contents =
		    parseInline(lastPart.substring
				(lastTagEnd, tagMatcher.start()),
				sp.add(lastTagEnd));
		result.add(PTag.newTag(lastTagName, contents,
				       sp.add(lastTagStart), tagContext));
	    }
	    lastTagStart= tagMatcher.start();
	    lastTagEnd  = tagMatcher.end();
	    lastTagName = tagMatcher.group(1);
	}
	// last tag went from lastTagStart to lastPart.length()
	if (lastTagName!=null) {
	    List<Tag> contents =
		parseInline(lastPart.substring(lastTagEnd),sp.add(lastTagEnd));
	    result.add(PTag.newTag(lastTagName, contents,
				   sp.add(lastTagStart), tagContext));
	}
	// done!
	tagCache = shrinkList(result);
	return tagCache;
    }
    private transient List<Tag> tagCache;
    private static final Pattern TAGPAT = Pattern.compile
	("^\\p{Blank}*@(\\S+)\\p{Blank}*", Pattern.MULTILINE);
    private static final Pattern TAGPATSS = Pattern.compile
	("^(?:\\p{Blank}*[*]+)?\\p{Blank}*@(\\S+)\\p{Blank}*", Pattern.MULTILINE);

    private String trimTrailingWS(String rawText) {
	Pattern pat = shouldStripStars() ? TRAILPATSS : TRAILPAT;
	return pat.matcher(rawText).replaceFirst("");
    }
    private static final Pattern TRAILPAT = Pattern.compile("\\p{Space}+\\z");
    private static final Pattern TRAILPATSS = Pattern.compile
	("\\p{Space}*(?:^\\p{Blank}*[*]*\\p{Blank}*$)*\\p{Space}*\\z");
	
    /** Parse the raw text into a series of 'Text' and 'inline' tags. */
    private List<Tag> parseInline(String rawText, PSourcePosition sp) {
	boolean stripStars = shouldStripStars();
	TypeContext tagContext = getCommentContext();
	class TagInfo {
	    public final String name;
	    public final PSourcePosition pos;
	    public final List<Tag> tags = new ArrayList<Tag>();
	    TagInfo(String name, PSourcePosition pos) {
		this.name = name; this.pos = pos;
	    }
	}
	Stack<TagInfo> tagStack = new Stack<TagInfo>();
	tagStack.push(new TagInfo(null, sp));
	rawText = trimTrailingWS(rawText);
	Matcher tagMatcher = INLINE.matcher(rawText);
	int pos=0;
	// find a tag start or end point.
	while (tagMatcher.find()) {
	    int start = tagMatcher.start();
	    // quick bypass of mismatched end braces.
	    if (tagMatcher.group(1)==null && tagStack.size()==1) {
		// don't allow mismatched ends.
		if (false) // suppress this warning; it's not useful.
		    pc.reporter.printWarning(sp.add(start),
					   "End brace without inline tag.");
		continue; // keep looking.
	    }
	    // add text from last pos to start pos to currently-active tag.
	    if (pos<start) {
		String text = rawText.substring(pos, start);
		if (stripStars) text = removeLeadingStars(text);
		tagStack.peek().tags.add(PTag.newTextTag(text, sp.add(pos)));
	    }
	    // for start tag, add new pair to stack; for end, pop a pair.
	    String tagName = tagMatcher.group(1);
	    if (tagName!=null) { // start tag.
		tagStack.push(new TagInfo(tagName, sp.add(start)));
	    } else { // end tag.
		assert tagStack.size() > 1;
		TagInfo ti = tagStack.pop();
		tagStack.peek().tags.add
		    (PTag.newInlineTag
		     (ti.name, shrinkList(ti.tags), ti.pos, tagContext));
	    }
	    pos = tagMatcher.end();
	}
	// deal with trailing text.
	if (pos<rawText.length()) {
	    String text = rawText.substring(pos);
	    if (stripStars) text = removeLeadingStars(text);
	    tagStack.peek().tags.add(PTag.newTextTag(text, sp.add(pos)));
	}
	// now deal with unmatched start tags.
	while (tagStack.size()>1) {
	    TagInfo ti = tagStack.pop();
	    pc.reporter.printError(ti.pos, "Inline tag without end brace.");
	    tagStack.peek().tags.add
		(PTag.newInlineTag
		 (ti.name, shrinkList(ti.tags), ti.pos, tagContext));
	}
	// done!
	assert tagStack.size()==1;
	assert tagStack.peek().name==null;
	return shrinkList(tagStack.pop().tags);
    }
    private static final Pattern INLINE = Pattern.compile
	("[{]@([^\\s}]+)\\s*|[}]");

    // parse inlineTags() list into first sentence tags using breakiterator.
    // note that we look for the sentence boundary by throwing away all
    // inline tag text, which means that the boundary can never fall in
    // the middle of an inline tag.  This is probably not an issue.
    public List<Tag> firstSentenceTags() {
	List<Tag> itags = inlineTags();
	// create a plain-text version of these tags.
	StringBuffer sb = new StringBuffer();
	for (Tag tag : itags)
	    if (tag.isText())
		sb.append(tag.text());
	// now create a break iterator...
	BreakIterator boundary = BreakIterator.getSentenceInstance(pc.locale);
	// ...and identify the start end of the first sentence.
	boundary.setText(sb.toString());
	int start = boundary.first();
	int end = boundary.next();
	// now translate this into tags.
	List<Tag> result = new ArrayList<Tag>(itags.size());
	Iterator<Tag> it=itags.iterator();
	Tag lastTag = null; int pos=0, lastPos=0;
	//  ...find start position.
	while (it.hasNext() && start < pos) {
	    Tag curTag = it.next();
	    if (!curTag.isText()) continue;// ignore leading non-text tags
	    lastTag=curTag;
	    lastPos=pos;
	    pos += curTag.text().length();
	}
	if (lastTag!=null) // shorten front of tag.
	    lastTag = PTag.newTextTag(lastTag.text().substring(start-lastPos),
				      ((PSourcePosition)lastTag.position())
				      .add(start-lastPos));
	// ...now find end position.
	while (it.hasNext() && pos < end) {
	    Tag curTag = it.next();
	    if (lastTag!=null) result.add(lastTag);
	    lastTag=curTag;
	    lastPos=pos;
	    if (curTag.isText())
		pos += curTag.text().length();
	}
	// shorten end of tag.
	if (lastTag!=null)
	    lastTag = PTag.newTextTag(lastTag.text().substring(0, end-lastPos),
				      lastTag.position());
	// add last tag to result.
	if (lastTag!=null) result.add(lastTag);
	// and we're done!
	return shrinkList(result);
    }
    public List<Tag> inlineTags() {
	List<Tag> result = new ArrayList<Tag>();
	for (Tag tag : tags()) {
	    if (tag.isTrailing())
		return result; // done!
	    result.add(tag);
	}
	return shrinkList(result);
    }
    public final List<Tag> tags(String tagname) {
	List<Tag> result = new ArrayList<Tag>();
	for (Tag tag : tags())
	    if ((!tag.isText()) && tag.name().equals(tagname))
		result.add(tag);
	return shrinkList(result);
    }
    public final String commentText() {
	// strip out all tags not of kind 'Text'.  append the rest.
	StringBuffer sb = new StringBuffer();
	for (Tag tag : tags())
	    if (tag.isText())
		sb.append(tag.text());
	return sb.toString();
    }
    /**
     * Compare based first on short name, then on canonical name,
     * using the <code>java.text.Collator</code> appropriate for the locale.
     */
    public final int compareTo(Doc d) {
	String q1 = this.name(), q2 = d.name();
	// name of a ClassDoc object should include its containing class.
	if (this instanceof ClassDoc)
	    for (ClassDoc p=((ClassDoc)this).containingClass(); p!=null;
		 p=p.containingClass())
		q1 = p.name()+"."+q1;
	if (d instanceof ClassDoc)
	    for (ClassDoc p=((ClassDoc)d).containingClass(); p!=null;
		 p=p.containingClass())
		q2 = p.name()+"."+q2;
	// okay.  now compare.
	int c = pc.collator.compare(q1, q2);// primary key.
	if (c!=0) return c;
	// try canonical name.  The canonical name for a Doc
	// which is not a ProgramElementDoc is just the name.
	if (this instanceof ProgramElementDoc)
	    q1 = ((ProgramElementDoc)this).canonicalName();
	if (d instanceof ProgramElementDoc)
	    q2 = ((ProgramElementDoc)this).canonicalName();
	return pc.collator.compare(q1, q2); // secondary key.
    }
    /** Convenience method: remove leading stars, as from comment text. */
    static String removeLeadingStars(String str) {
	return LEADSTAR.matcher(str).replaceAll("");
    }
    /** Pattern used by <code>removeLeadingStars()</code> method. */
    private static final Pattern LEADSTAR = Pattern.compile
	("^[\\p{Blank}]*[*]+", Pattern.MULTILINE);
    /** Convenience method to reduce memory requirements of list & make
     *  immutable. */
    private static <T> List<T> shrinkList(List<T> list) {
	if (list instanceof ArrayList)
	    ((ArrayList)list).trimToSize();
	return Collections.unmodifiableList(list);
    }
}
