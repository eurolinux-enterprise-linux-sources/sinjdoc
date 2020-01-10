// TagEmitter.java, created Fri Apr  4 18:52:33 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cscott@cscott.net>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.html;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.DocErrorReporter;
import net.cscott.sinjdoc.MemberDoc;
import net.cscott.sinjdoc.PackageDoc;
import net.cscott.sinjdoc.ParamTag;
import net.cscott.sinjdoc.SeeTag;
import net.cscott.sinjdoc.SerialFieldTag;
import net.cscott.sinjdoc.SourcePosition;
import net.cscott.sinjdoc.ThrowsTag;
import net.cscott.sinjdoc.Tag;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * The <code>TagEmitter</code> class encapsulates the code required to
 * emit a list of <code>Tag</code> objects.
 * 
 * @author  C. Scott Ananian <cscott@cscott.net>
 * @version $Id: TagEmitter.java,v 1.8 2003/08/01 00:01:16 cananian Exp $
 */
abstract class TagEmitter {
    /** The <code>TagInfo</code> class represents a particular tag type, and
     *  stores information about how to process it, including its order in
     *  the output and a <code>BlockTagAction</code> which can be used to
     *  emit it. */
    private static class TagInfo {
	final String kind;
	final Class tagClass;
	final BlockTagAction action;
	final int order;
	TagInfo(String kind, Class tagClass, BlockTagAction action) {
	    this.kind = kind;
	    this.tagClass = tagClass;
	    this.action = action;
	    this.order = counter++;
	}
	private static int counter=0;
    }
    /** This field enumerates all the tag types we know about.  Because
     *  of how the <code>TagInfo.order</code> field is assigned, the
     *  order of this array should be the same as the order in which
     *  the tags should be emitted.  (Explicitly specifying order
     *  integers would eliminate this restriction.) */
    private final static TagInfo[] tagInfo = new TagInfo[] {
	new TagInfo("deprecated", null, new SimpleBlockAction("Deprecated")),
	new TagInfo(".text", null, new InlineAction()),
	new TagInfo("serialData", null, new SimpleBlockAction("Serial Data")),
	new TagInfo("serialField", SerialFieldTag.class,
		    new SimpleBlockAction("Serialized Fields")/*XXX*/),
	new TagInfo("serial", null, new SimpleBlockAction("Serial")/*XXX*/),
	new TagInfo(".specified", SpecifiedTag.class,
		    new SimpleBlockAction("Specified by")/*XXX*/),
	new TagInfo(".overrides", OverrideTag.class,
		    new SimpleBlockAction("Overrides")/*XXX*/),
	new TagInfo("param", ParamTag.class,
		    new SimpleBlockAction("Parameters")/*XXX*/),
	new TagInfo("return", null, new SimpleBlockAction("Returns")),
	new TagInfo("throws", ThrowsTag.class,
		    new SimpleBlockAction("Throws")/*XXX*/),
	new TagInfo("since", null, new SimpleBlockAction("Since") {
		void emit(PrintWriter pw, String kind, List<Tag> tags,
			  TemplateContext context) {
		    if (context.options.emitSinceTag)
			super.emit(pw, kind, tags, context);
		}
	    }),
	new TagInfo("version", null, new SimpleBlockAction("Version") {
		void emit(PrintWriter pw, String kind, List<Tag> tags,
			  TemplateContext context) {
		    if (context.options.emitVersionTag)
			super.emit(pw, kind, tags, context);
		}
	    }),
	new TagInfo("author", null, new SimpleBlockAction("Author") {
		void emit(PrintWriter pw, String kind, List<Tag> tags,
			  TemplateContext context) {
		    if (context.options.emitAuthorTag)
			super.emit(pw, kind, tags, context);
		}
	    }),
	new TagInfo("see", SeeTag.class, new SimpleBlockAction("See Also") {
		void emitInner(PrintWriter pw, Tag t, TemplateContext context){
		    emitSeeTag(pw, (SeeTag) t, context, true);
		}
	    }),
	new TagInfo(".unknown", null, new SimpleBlockAction("UNKNOWN")),
    };

    /** A <code>BlockTagAction</code> allows you to emit a specific type
     *  of tag.  All tags of that type are grouped together and passed as
     *  a list (in order by source position) to the emit method. */
    static abstract class BlockTagAction {
	/** Emit the specified homogeneous group of tags to the specified
	 *  <code>PrintWriter</code>.  Errors are directed to a
	 *  <code>DocErrorReporter/code> in the specified context. */
	abstract void emit(PrintWriter pw, String kind, List<Tag> tags,
			   TemplateContext context);
    }
    /** A <code>SimpleBlockAction</code> is a common
     *  <code>BlockTagAction</code> for tags with no additional structure
     *  to their contents. */
    static class SimpleBlockAction extends BlockTagAction {
	/** The human-readable description used for this tag. */
	final String tagDescription;
	SimpleBlockAction(String desc) { this.tagDescription = desc; }
	void emit(PrintWriter pw, String kind, List<Tag> tags,
		  TemplateContext context) {
	    pw.print("<p class=\"tag tag_"+kind+"\">");
	    String desc = tagDescription; boolean first=true;
	    for (Tag t : tags) {
		assert t.isTrailing();
		if (desc==null) desc = t.name();
		if (first) {
		    pw.print("<span class=\"tagName\">"+desc+":</span> ");
		    first=false;
		}
		pw.print("<span class=\"tagContents\">");
		emitInner(pw, t, context);
		pw.print("</span> ");
	    }
	    pw.println("</p>");
	}
	// subclasses can override:
	void emitInner(PrintWriter pw, Tag t, TemplateContext context) {
	    emitInline(pw, t.contents(), context);
	}
    }
    /** An <code>InlineAction</code> handles text and inline tags by
     *  invoking <code>emitInline()</code>. */
    static class InlineAction extends BlockTagAction {
	void emit(PrintWriter pw, String kind, List<Tag> tags,
		  TemplateContext context) {
	    emitInline(pw, tags, context);
	}
    }
    /** The <code>SpecifiedTag</code> is a synthetic tag used for
     *  "Specified By" notations. */
    static interface SpecifiedTag extends Tag { }
    /** The <code>OverrideTag</code> is a synthetic tag used for
     *  "Overrides" notations. */
    static interface OverrideTag extends Tag { }

    /** Returns canonical string naming this tag.  Text and inline tags
     *  return ".text" and the "exception" tag is canonicalized to
     *  "throws". */
    static String tagKind(Tag t) {
	if (t.isText() || t.isInline()) return ".text";
	if (t.name().equals("exception")) return "throws";
	return t.name();
    }
    /** Lookup the <code>TagInfo</code> corresponding to the type of
     *  this <code>Tag</code>. */
    static TagInfo lookup(Tag t) {
	String kind = tagKind(t);
	// brain-dead method for now.
	while (true) {
	    for (int i=0; i<tagInfo.length; i++) {
		if (tagInfo[i].kind.equals(kind) &&
		    (tagInfo[i].tagClass==null ||
		     tagInfo[i].tagClass.isInstance(t)))
		    return tagInfo[i];
	    }
	    // not found; do again with kind=".unknown"
	    assert !kind.equals(".unknown"); // protect against infinite loop
	    kind=".unknown";
	}
    }
    
    /** Compares two SourcePositions. */
    private static final Comparator<SourcePosition> POSITION_COMPARATOR =
	new Comparator<SourcePosition>() {
	    public int compare(SourcePosition sp1, SourcePosition sp2) {
		int c = sp1.file().toString().compareTo(sp2.file().toString());
		if (c!=0) return c;
		c = sp1.line() - sp2.line();
		if (c!=0) return c;
		c = sp1.column() - sp2.column();
		return c;
	    }
	};
    /** Compares two Tags for canonical order. */
    private static final Comparator<Tag> TAG_COMPARATOR =
	new Comparator<Tag>() {
	    public int compare(Tag t1, Tag t2) {
		// use tagSort as primary key.
		TagInfo ti1 = lookup(t1), ti2 = lookup(t2);
		int c = ti1.order - ti2.order;
		if (c!=0) return c;
		// use source location as secondary key.
		return POSITION_COMPARATOR.compare
		(t1.position(), t2.position());
	    }
	};
    
    /** An <code>InlineTagAction</code> processes a text or inline tag. */
    static abstract class InlineTagAction {
	abstract void emit(PrintWriter pw, Tag t, TemplateContext context);
    }
    /** A map relating inline tag kinds to the proper actions to emit them. */
    static final Map<String,InlineTagAction> inlineActions =
	new HashMap<String,InlineTagAction>();
    static { // initialize the map.
	inlineActions.put(".text", new InlineTagAction() {
		void emit(PrintWriter pw, Tag t, TemplateContext context) {
		    assert t.isText();
		    pw.print(t.text());
		}
	    });
	inlineActions.put("docRoot", new InlineTagAction() {
		void emit(PrintWriter pw, Tag t, TemplateContext context) {
		    String docRoot=context.curURL.makeRelative("");
		    if (docRoot.length()==0) docRoot="./";
		    pw.print(docRoot);
		}
	    });
	inlineActions.put("link", new InlineTagAction() {
		void emit(PrintWriter pw, Tag t, TemplateContext context) {
		    emitSeeTag(pw, (SeeTag) t, context, true);
		}
	    });
	inlineActions.put("linkplain", new InlineTagAction() {
		void emit(PrintWriter pw, Tag t, TemplateContext context) {
		    emitSeeTag(pw, (SeeTag) t, context, false);
		}
	    });
	inlineActions.put(".unknown", new InlineTagAction() {
		void emit(PrintWriter pw, Tag t, TemplateContext context) {
		    assert t.isInline();
		    context.root.printWarning(t.position(),
					      "Unknown inline tag: "+t.name());
		    // emit as unprocessed.
		    pw.print("{@"); pw.print(t.name()); pw.print(" ");
		    // (recurse on contents)
		    emitInline(pw, t.contents(), context);
		    // close tag.
		    pw.print("}");
		}
	    });
    }
    /** Emit a see tag. */
    static void emitSeeTag(PrintWriter pw, SeeTag t, TemplateContext context,
			   boolean isCodeFont) {
	// XXX the no-label output is not exactly according to the javadoc
	// specs; should be fixed.
	List<Tag> label = t.label();
	MemberDoc md = t.referencedMember();
	ClassDoc cd = t.referencedClass();
	PackageDoc pd = t.referencedPackage();

	if (isCodeFont && label.size()>0) pw.print("<code>");

	if (md!=null) { // reference to a member
	    if (label.size()>0) {
		pw.print("<a href=\"");
		pw.print(context.curURL.makeRelative(HTMLUtil.toURL(md)));
		pw.print("\">");
		emitInline(pw, label, context);
		pw.print("</a>");
		if (isCodeFont) pw.print("</code>");
	    } else
		pw.print(HTMLUtil.toLink(context.curURL, md));
	} else if (cd!=null) { // reference to a class
	    if (label.size()>0) {
		pw.print("<a href=\"");
		pw.print(context.curURL.makeRelative(HTMLUtil.toURL(cd)));
		pw.print("\">");
		emitInline(pw, label, context);
		pw.print("</a>");
		if (isCodeFont) pw.print("</code>");
	    } else
		pw.print(HTMLUtil.toLink(context.curURL, cd, cd.name()));
	} else if (pd!=null) { // reference to a package
	    String page = "package-summary.html";
	    if (label.size()>0) {
		pw.print("<a href=\"");
		pw.print(context.curURL.makeRelative(HTMLUtil.toURL(pd,page)));
		pw.print("\">");
		emitInline(pw, label, context);
		pw.print("</a>");
		if (isCodeFont) pw.print("</code>");
	    } else
		pw.print(HTMLUtil.toLink(context.curURL, pd, page));
	} else { // well, just use the raw contents; no linking
	    emitInline(pw, t.contents(), context);
	}

	if (isCodeFont && label.size()>0) pw.print("</code>");
    }
    /** Canonicalize the kind of this inline tag. */
    static String inlineTagKind(Tag t) {
	assert !t.isTrailing();
	if (t.isText()) return ".text";
	if (inlineActions.containsKey(t.name())) return t.name();
	return ".unknown";
    }
    /** Emit the given list of inline and text tags to the specified
     *  <code>PrintWriter</code>. */
    private static void emitInline(PrintWriter pw, List<Tag> tags,
				   TemplateContext context) {
	// no tag in this list should be trailing.
	for (Tag t : tags) {
	    assert !t.isTrailing();
	    inlineActions.get(inlineTagKind(t)).emit(pw, t, context);
	}
    }
    /** Emit a top-level list of tags. */
    public static void emit(PrintWriter pw, List<Tag> tags,
			    TemplateContext context) {
	// XXX add synthetic 'overrides' tags using the TemplateContext.
	// sort tags.
	List<Tag> sorted = new ArrayList<Tag>(tags);
	Collections.sort(sorted, TAG_COMPARATOR);
	// now group trailing tags & invoke actions.
	List<Tag> group = new ArrayList<Tag>();
	TagInfo groupType=null;
	for (Tag t : sorted) {
	    TagInfo ti = lookup(t);
	    if (groupType!=ti) { // this is a new group!
		if (groupType!=null) // emit old group
		    groupType.action.emit(pw, groupType.kind, group, context);
		group.clear(); // start new group.
	    }
	    // add tag to group.
	    group.add(t);
	    groupType=ti;
	}
	// emit any unfinished group.
	if (groupType!=null)
	    groupType.action.emit(pw, groupType.kind, group, context);
	// done!
    }
}
