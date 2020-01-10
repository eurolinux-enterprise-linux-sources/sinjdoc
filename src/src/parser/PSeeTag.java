// SeeTag.java, created Wed Mar 19 12:59:15 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.ClassType;
import net.cscott.sinjdoc.FieldDoc;
import net.cscott.sinjdoc.MemberDoc;
import net.cscott.sinjdoc.MethodDoc;
import net.cscott.sinjdoc.PackageDoc;
import net.cscott.sinjdoc.SourcePosition;
import net.cscott.sinjdoc.Tag;
import net.cscott.sinjdoc.TagVisitor;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * The <code>PSeeTag</code> class represents a "see also" documentation
 * tag.  The @see tag can be plain text, or reference a class or
 * member.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PSeeTag.java,v 1.13 2003/08/01 00:01:26 cananian Exp $
 */
class PSeeTag extends PTag.NonText
    implements net.cscott.sinjdoc.SeeTag {
    final List<Tag> label;
    final String classPart;
    final String memberNamePart;
    final String memberArgsPart;
    final TypeContext tagContext;
    PSeeTag(SourcePosition sp, String name, List<Tag> contents,
	    TypeContext tagContext) throws TagParseException {
	super(sp, name, contents);
	assert name()=="see" || name()=="link" || name()=="linkplain";
	Pair<Matcher,List<Tag>> pair;
	char firstChar = extractRegexpFromHead
	    (contents, FIRSTCHAR, "non-space character")
	    .left.group().charAt(0);
	if (firstChar=='\"') { // string.
	    // strip the " characters from the start and end of tags
	    pair = extractRegexpFromHead(contents, STRING_START,
					 "start of string");
	    pair = extractRegexpFromTail(pair.right, STRING_END,
					 "end of string");
	    this.label = pair.right;
	    this.classPart = this.memberNamePart = this.memberArgsPart = null;
	    this.tagContext=null;
	} else if (firstChar=='<') { // href
	    // strip the <a href="..."> and </a> from start and end of tags
	    pair = extractRegexpFromHead(contents, HREF_START,
					 "html start tag");
	    pair = extractRegexpFromTail(pair.right, HREF_END,
					 "html end tag");
	    this.label = pair.right;
	    this.classPart = this.memberNamePart = this.memberArgsPart = null;
	    this.tagContext=null;
	} else { // java member reference.
	    pair = extractRegexpFromHead
		(contents, JREF, "java package, class, or member reference");
	    this.label = pair.right;
	    this.classPart = pair.left.group(1);
	    this.memberNamePart = pair.left.group(2);
	    this.memberArgsPart = pair.left.group(3);
	    this.tagContext=tagContext;
	}
    }
    private static final Pattern FIRSTCHAR = Pattern.compile("^\\s*(\\S)");

    private static final Pattern STRING_START = Pattern.compile("^\\s*\"");
    private static final Pattern STRING_END = Pattern.compile("\"\\s*$");

    private static final Pattern HREF_START = Pattern.compile("^\\s*<[^<>]*>");
    private static final Pattern HREF_END = Pattern.compile("<[^<>]*>\\s*$");

    // insanely complicated regex.  We anchor at the start and allow leading
    // spaces, to keep the regex from skipping malformed bits at the beginning.
    // Then we say there is *either* a class/pattern specifier *or* the
    // first character is '#'.  Then we have an optional "#member" and an
    // optional "(args)" section.  Lastly, we say there either must be trailing
    // space or it must be the end of the string.  This keeps us from skipping
    // malformed trailing bits.
    private static final Pattern JREF = Pattern.compile
	("^\\s*(?:([^#\\s\\(,\\)]+)|(?=#))(?:#([^\\(,\\)\\s\\.]+)(\\([^\\(\\)]*\\))?)?(?:\\z|\\s+)");
	
    
    public boolean isTrailing() { return name=="see"; }
    public boolean isInline() { return name=="link" || name=="linkplain"; }
    // parsed values
    public List<Tag> label() { return label; }
    public String referencedClassName() { return classPart; }
    public String referencedMemberName() {
	if (memberNamePart==null) return null;
	StringBuffer sb = new StringBuffer(memberNamePart);
	if (memberArgsPart!=null) sb.append(memberArgsPart);
	return sb.toString();
    }
    public ClassDoc referencedClass() {
	if (memberNamePart==null) {
	    if (classPart==null) return null;
	    if (referencedPackage()!=null) return null; // package ref, not cls
	}
	// use class scope if class if unspecified.
	if (classPart==null) return tagContext.classScope;
	// look up class.
	return tagContext.lookupClassTypeName(classPart,false).asClassDoc();
    }
    public MemberDoc referencedMember() {
	if (memberNamePart==null) return null;
	ClassDoc cd = referencedClass();
	if (cd==null) return null; // can't find.
	// XXX should really look through outer classes, superclasses,
	//     interfaces, etc.
	if (memberArgsPart==null) { // look for fields.
	    for (FieldDoc fd : cd.fields())
		if (fd.name().equals(memberNamePart)) return fd;
	} else { // look for methods.
	    for (MethodDoc md : cd.methods())
		if (md.name().equals(memberNamePart) &&
		    md.signature().equals(expandSig(memberArgsPart)))
		    return md;
	}
	// not found.
	return null;
    }
    // fully-qualify all the type names in the given signature.
    private String expandSig(String sig) {
	StringBuffer result = new StringBuffer();
	Matcher matcher = TYPE.matcher(sig);
	while (matcher.find()) {
	    ClassType ty =
		tagContext.lookupClassTypeName(matcher.group(),false);
	    matcher.appendReplacement(result, ty.canonicalTypeName());
	}
	matcher.appendTail(result);
	return result.toString();
    }
    private static final Pattern TYPE = Pattern.compile("[^(),]+");

    public PackageDoc referencedPackage() {
	if (classPart==null) return null;
	if (memberNamePart!=null) return null; // it's a member, not a package
	return tagContext.pc.rootDoc.packageNamed(classPart);
    }

    public <T> T accept(TagVisitor<T> visitor) { return visitor.visit(this); }
}
