// TemplateWriter.java, created Mon Mar 31 19:14:35 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cscott@cscott.net>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.html;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.ClassTypeVariable;
import net.cscott.sinjdoc.ConstructorDoc;
import net.cscott.sinjdoc.Doc;
import net.cscott.sinjdoc.DocErrorReporter;
import net.cscott.sinjdoc.ExecutableMemberDoc;
import net.cscott.sinjdoc.FieldDoc;
import net.cscott.sinjdoc.MemberDoc;
import net.cscott.sinjdoc.MethodDoc;
import net.cscott.sinjdoc.PackageDoc;
import net.cscott.sinjdoc.Parameter;
import net.cscott.sinjdoc.Tag;
import net.cscott.sinjdoc.Type;
import net.cscott.sinjdoc.TypeVariable;
import net.cscott.sinjdoc.html.ReplayReader.Mark;

import java.lang.reflect.Modifier;
import java.io.*;
import java.util.*;
/**
 * The <code>TemplateWriter</code> class emits chunks of template
 * interspersed with customized chunks.  It performs macro substitution
 * of the template as well.
 * 
 * @author  C. Scott Ananian <cscott@cscott.net>
 * @version $Id: TemplateWriter.java,v 1.35 2003/07/29 16:02:47 cananian Exp $
 */
class TemplateWriter extends PrintWriter  {
    final Stack<ExtendedContext> contextStack = new Stack<ExtendedContext>();
    final ReplayReader templateReader;

    /** Creates a <code>TemplateWriter</code> which uses the specified
     *  <code>Reader</code> as a template and writes to the provides
     *  <code>Writer</code>.  Macros are expanded using the specified
     *  <code>TemplateContext</code>. */
    TemplateWriter(Writer delegate, Reader templateReader,
		   TemplateContext context) {
        super(delegate);
	this.templateReader = new ReplayReader(templateReader);
	contextStack.push(new ExtendedContext
			  (Collections.singletonList(context),
			   this.templateReader));
    }
    /** Creates a <code>TemplateWriter</code> which uses the reader provided
     *  as a template and writes to the URL specified by the template
     *  context. */
    TemplateWriter(Reader templateReader, HTMLUtil hu,TemplateContext context){
	this(hu.fileWriter(context.curURL, context.options), templateReader,
	     context);
    }
    /** Creates a <code>TemplateWriter</code> which uses the resource with
     *  the specified name as a template, and writes to the URL specified
     *  by the template context. */
    TemplateWriter(String resourceName, HTMLUtil hu, TemplateContext context) {
	this(hu.resourceReader(resourceName), hu, context);
    }
    // helper functions.
    /** Returns the topmost context for this <code>TemplateWriter</code>. */
    private TemplateContext topContext() {
	return contextStack.get(0).contexts.get(0);
    }
    /** Returns false if the <code>TemplateWriter</code> is currently
     *  suppressing output. */
    private boolean isEcho() { return contextStack.peek().isEcho(); }
    /** Copy all remaining text from the template and close the files. */
    public void copyRemainder(DocErrorReporter reporter) {
	try {
	    copyRemainder();
	} catch (IOException e) {
	    reporter.printError("Couldn't emit "+topContext().curURL+": "+e);
	}
    }
    /** Read from the template, performing macro substition, until the
     *  occurrence of the string @SPLIT@ or end-of-file, whichever comes
     *  first.
     * @return true if split found, false if EOF found first. */
    public boolean copyToSplit(DocErrorReporter reporter) {
	try {
	    return copyToSplit();
	} catch (IOException e) {
	    reporter.printError("Couldn't emit "+topContext().curURL+": "+e);
	    return false;
	}
    }
    /** Copy all remaining text from the template and close the files. */
    void copyRemainder() throws IOException {
	boolean failure=true;
	try {
	    while (copyToSplit())
		/* repeat */;
	    failure=false;
	} finally {
	    close();
	    templateReader.close();
	    assert failure || contextStack.size()==1;
	}
    }
    /** Read from the template, performing macro substition, until the
     *  occurrence of the string @SPLIT@ or end-of-file, whichever comes
     *  first.
     * @return true if split found, false if EOF found first. */
    boolean copyToSplit() throws IOException {
	char[] buf = new char[1024];
	int r; boolean eof=false;
	while (!eof) {
	    r = templateReader.read();
	    if (r<0) { eof=true; break; /* end of stream */}
	    if (r!='@') {
		if (isEcho()) write(r);
		continue;
	    }
	    // ooh, ooh, saw a '@'
	    StringBuffer tag = new StringBuffer("@");
	    while (!eof) {
		r = templateReader.read();
		if (r<0) {
		    eof=true;
		    if (isEcho()) write(tag.toString());
		    break;
		}
		tag.append((char)r);
		if (Character.isJavaIdentifierPart((char)r) && r!='@')
		    continue; // part of the tag, keep going.
		// saw closing '@'.  is this a valid tag?
		String tagName = tag.toString();
		if (tagName.equals("@SPLIT@")) {
		    if (isEcho()) return true; // done.
		} else if (tagName.equals("@END@")) {
		    // repeat from mark or pop context.
		    if (contextStack.peek().contexts.size()>1) {
			// remove first element from context list.
			ExtendedContext ec = contextStack.peek();
			ec.contexts=ec.contexts.subList(1,ec.contexts.size());
			ec.isFirst=false;
			// reset reader to mark.
			templateReader.reset(ec.replayMark);
		    } else if (contextStack.size()>1) {
			// done with this block. pop context.
			contextStack.pop();
		    } else assert false : "too many @END@ tags";
		} else if (macroMap.containsKey(tagName)) {
		    ExtendedContext ec = contextStack.peek();
		    List<TemplateContext> ltc = macroMap.get(tagName).doMacro
			(this, ec.curContext(), ec.isFirst(), ec.isLast());
		    if (ltc!=null)
			contextStack.push(new ExtendedContext
					  (ltc, templateReader));
		} else // invalid tag.
		    if (isEcho()) write(tag.toString());
		break;
	    }
	}
	return false; // eof found.
    }
    /** An extended context object that allows for template conditionals
     *  and repeats. */
    static final class ExtendedContext {
	boolean isFirst=true;
	List<TemplateContext> contexts;
	final ReplayReader.Mark replayMark;
	ExtendedContext(List<TemplateContext> contexts,
			ReplayReader.Mark replayMark) {
	    this.contexts = contexts;
	    this.replayMark = replayMark;
	}
	ExtendedContext(List<TemplateContext> contexts, ReplayReader r) {
	    // only need to make mark if contexts.size()>1.
	    this(contexts, contexts.size()>1 ? r.getMark() : null);
	}
	public boolean isFirst() { return isFirst; }
	public boolean isLast() { return contexts.size()==1; }
	boolean isEcho() { return contexts.size() > 0; }
	public TemplateContext curContext() {
	    return contexts.size()==0 ? null : contexts.get(0);
	}
	public String toString() {
	    return "EC["+isFirst+","+contexts+","+replayMark+"]";
	}
    }
    /** Encapsulates a macro definition. */
    static abstract class TemplateMacro {
	/** This is the most general interface to the macro-expansion
	 *  engine.  The method should write to <code>tw</code> whatever
	 *  is necessary for the expansion of the macro.  If the
	 *  return value is non-null, the template text between this
	 *  macro and a corresponding <code>@END@</code> macro will
	 *  be repeated once for every element in the
	 *  <code>TemplateContext</code> list.  Note that returning
	 *  a list of size 0 is allowed, and suppresses output until the
	 *  matching <code>@END@</code> macro is found.  The
	 *  <code>context</code> parameter will be <code>null</code> if
	 *  output is currently suppressed. The <code>isFirst</code>
	 *  parameter will be true if this is the first repetition of
	 *  this block.  The <code>isLast</code> parameter will be true
	 *  if this is the last repetition of this block. */
	abstract List<TemplateContext> doMacro
	    (TemplateWriter tw, TemplateContext context,
	     boolean isFirst, boolean isLast);
    }
    static abstract class TemplateAction extends TemplateMacro {
	final List<TemplateContext> doMacro
	    (TemplateWriter tw, TemplateContext context,
	     boolean isFirst, boolean isLast) {
	    // process the macro...
	    if (context!=null) process(tw, context);
	    // no new contexts added, so return null.
	    return null;
	}
	/** Expand the macro by writing to <code>tw</code>.  This method
	 *  will only be invoked if output is not currently suppressed. */
	abstract void process(TemplateWriter tw, TemplateContext context);
    }
    static abstract class TemplateForAll extends TemplateMacro {
	final List<TemplateContext> doMacro
	    (TemplateWriter tw, TemplateContext context,
	     boolean isFirst, boolean isLast) {
	    // don't really push new contexts if we're not currently echoing.
	    if (context==null) return EMPTY_CONTEXT_LIST;
	    // otherwise, go ahead and process this.
	    return process(tw, context, isFirst, isLast);
	}
	/** Return a list of <code>TemplateContext</code>s; each one will
	 *  be used in turn to process this block.  So if you return a
	 *  list of size two, the block will be repeated twice, etc.  This
	 *  method will only be invoked if output is not currently
	 *  suppressed. */
	abstract List<TemplateContext> process
	    (TemplateWriter tw, TemplateContext context,
	     boolean isFirst, boolean isLast);
	/** An empty list object, static for efficiency. */
	static final List<TemplateContext> EMPTY_CONTEXT_LIST =
	    Arrays.asList(new TemplateContext[0]);
    }
    static abstract class TemplateConditional extends TemplateForAll {
	final List<TemplateContext> process
	    (TemplateWriter tw, TemplateContext context,
	     boolean isFirst, boolean isLast) {
	    assert context!=null;
	    if (!isBlockEmitted(context, isFirst, isLast))
		return EMPTY_CONTEXT_LIST;
	    return Collections.singletonList(context);
	}
	/** Return true if this block following this conditional should be
	 *  emitted, else return false.  This method will only be invoked
	 *  if output is not currently suppressed. */
	abstract boolean isBlockEmitted(TemplateContext context,
					boolean isFirst, boolean isLast);
    }
    static abstract class TemplateSimpleForAll extends TemplateForAll {
	final List<TemplateContext> process
	    (TemplateWriter tw, TemplateContext context,
	     boolean isFirst, boolean isLast) {
	    return process(context);
	}
	/** Return a list of <code>TemplateContext</code>s; each one will
	 *  be used in turn to process this block.  So if you return a
	 *  list of size two, the block will be repeated twice, etc.  This
	 *  method will only be invoked if output is not currently
	 *  suppressed. */
	abstract List<TemplateContext> process(TemplateContext context);
    }
    /** A map from macro names to definitions. */
    private static final Map<String, TemplateMacro> macroMap =
	new HashMap<String,TemplateMacro>();
    /** Convenience method to register macro definitions. */
    private static final void register(String name, TemplateMacro action) {
	assert (!name.startsWith("@")) && (!name.endsWith("@"));
	assert name.startsWith("IF")==(action instanceof TemplateConditional);
	name = "@"+name+"@";
	assert !macroMap.containsKey(name) : "duplicate macro: "+name;
	macroMap.put(name, action);
    }
    private static final void registerConditional(String name,
						  final TemplateConditional c){
	register("IF_"+name, (TemplateMacro) c);
	register("IFNOT_"+name, (TemplateMacro) new TemplateConditional() {
		boolean isBlockEmitted(TemplateContext context,
				       boolean isFirst, boolean isLast) {
		    return !c.isBlockEmitted(context, isFirst, isLast);
		}
	    });
    }
    private static final void registerForAll(String name,
					     final TemplateForAll c) {
	register("FORALL_"+name, c);
	registerConditional(name, new TemplateConditional() {
		boolean isBlockEmitted(TemplateContext context,
				       boolean isFirst, boolean isLast) {
		    return c.process(null,context,isFirst,isLast).size() > 0;
		}
	    });
    }
    static {
	// macro definitions.  java is so noisy!
	register("CHARSET", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    tw.write(context.options.charSet.name());
		}
	    });
	register("GENERATOR", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    tw.write(Version.PACKAGE_STRING);
		}
	    });
	register("WINDOWTITLE", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    if (context.options.windowTitle==null) return;
		    tw.write(context.options.windowTitle);
		}
	    });
	register("TITLESUFFIX", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    if (context.options.windowTitle==null) return;
		    tw.write(" (");
		    tw.write(context.options.windowTitle);
		    tw.write(")");
		}
	    });
	register("DOCTITLE", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    if (context.options.docTitle==null) return;
		    tw.write(context.options.docTitle);
		}
	    });
	register("ROOT", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    tw.write(context.curURL.makeRelative(""));
		}
	    });
	register("HEADER", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    if (context.options.header==null) return;
		    tw.write(context.options.header);
		}
	    });
	register("FOOTER", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    if (context.options.footer==null) return;
		    tw.write(context.options.footer);
		}
	    });
	register("BOTTOM", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    if (context.options.bottom==null) return;
		    tw.write(context.options.bottom);
		}
	    });
	register("CLASSSHORTNAME", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curClass!=null;
		    // should include containing classes.
		    StringBuffer sb=new StringBuffer(context.curClass.name());
		    for(ClassDoc p=context.curClass.containingClass();
			p!=null; p=p.containingClass())
			sb.insert(0, p.name()+".");
		    tw.write(sb.toString());
		}
	    });
	register("CLASSNAME", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curClass!=null;
		    tw.write(context.curClass.canonicalName());
		}
	    });
	register("GROUPNAME", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curGroup!=null;
		    tw.write(context.curGroup.heading);
		}
	    });
	register("PKGNAME", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curPackage!=null;
		    tw.write(context.curPackage.name());
		}
	    });
	register("PKGFRAMELINK", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curPackage!=null;
		    tw.write(HTMLUtil.toLink(context.curURL,context.curPackage,
					     "package-frame.html"));
		}
	    });
	register("PKGSUMMARYLINK", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curPackage!=null;
		    tw.write(HTMLUtil.toLink(context.curURL,context.curPackage,
					     "package-summary.html"));
		}
	    });
	register("CLASSTYPE", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curClass!=null;
		    if (context.curClass.isOrdinaryClass())
			tw.write("class");
		    else if (context.curClass.isInterface())
			tw.write("interface");
		    else if (context.curClass.isException())
			tw.write("exception");
		    else if (context.curClass.isError())
			tw.write("error");
		    else assert false : "what is it?";
		}
	    });
	register("CLASSLINK_P", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curClass!=null;
		    tw.write(HTMLUtil.toLink(context.curURL, context.curClass,
					     true/*with params*/));
		}
	    });
	register("CLASSLINK_NP", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curClass!=null;
		    tw.write(HTMLUtil.toLink(context.curURL, context.curClass,
					     false/*no params*/));
		}
	    });
	register("TAG_SUMMARY", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    TagEmitter.emit(tw, context.specificItem()
				    .firstSentenceTags(), context);
		}
	    });
	register("TAGS", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    TagEmitter.emit(tw, context.specificItem().tags(),
				    context);
		}
	    });
	register("SUPERCLASS", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curClass!=null;
		    tw.write(HTMLUtil.toLink(context.curURL,
					     context.curClass.superclass()));
		}
	    });
	register("SUPERINTERFACES", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curClass!=null;
		    for (Iterator<Type> it=context.curClass.interfaces()
			     .iterator(); it.hasNext(); ) {
			tw.write(HTMLUtil.toLink(context.curURL, it.next()));
			if (it.hasNext()) tw.write(", ");
		    }
		}
	    });
	register("MODIFIER_SUMMARY", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curMember!=null || context.curClass!=null;
		    int m = (context.curMember!=null) ?
			context.curMember.modifierSpecifier() :
			context.curClass.modifierSpecifier();
		    // only include PROTECTED STATIC ABSTRACT in summary?
		    m &= (Modifier.PROTECTED | Modifier.STATIC |
			  Modifier.ABSTRACT);
		    tw.write(Modifier.toString(m));
		}
	    });
	register("MODIFIERS", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curMember!=null || context.curClass!=null;
		    int m = (context.curMember!=null) ?
			context.curMember.modifierSpecifier() :
			context.curClass.modifierSpecifier();
		    // modifiers always hide NATIVE and SYNCHRONIZED
		    m &= ~(Modifier.NATIVE | Modifier.SYNCHRONIZED);
		    tw.write(Modifier.toString(m));
		}
	    });
	register("FIELD_TYPE", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curMember!=null;
		    FieldDoc fd = (FieldDoc) context.curMember;
		    tw.write(HTMLUtil.toLink(context.curURL, fd.type()));
		}
	    });
	register("METHOD_RETURN_TYPE", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curMember!=null;
		    Type ty = ((MethodDoc)context.curMember).returnType();
		    if (ty==null) tw.write("void");
		    else tw.write(HTMLUtil.toLink(context.curURL, ty));
		}
	    });
	register("METHOD_SIGNATURE", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curMember!=null;
		    tw.write(((ExecutableMemberDoc)context.curMember)
			     .signature());
		}
	    });
	register("METHOD_PARAMS", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curMember!=null;
		    for(Iterator<Parameter> it =
			    ((ExecutableMemberDoc)context.curMember)
			    .parameters().iterator(); it.hasNext(); ) {
			Parameter p = it.next();
			tw.write(HTMLUtil.toLink(context.curURL,
						 p.printableType()));
			if (p.isVarArgs()) tw.write("...");
			tw.write(" ");
			tw.write(p.name());
			if (it.hasNext()) tw.write(", ");
		    }
		}
	    });
	register("MEMBER_NAME", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curMember!=null;
		    tw.write(context.curMember.name());
		}
	    });
	register("MEMBER_NAME_LINK", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    assert context.curMember!=null;
		    tw.write(HTMLUtil.toLink(context.curURL,
					     context.curMember));
		}
	    });
	register("TYPEPARAMS_SUMMARY", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    // no link, no bounds, no anchor
		    tw.write(HTMLUtil.toLink(context.curURL,
					     context.specificTypeVariables(),
					     false, false, false));
		}
	    });
	register("TYPEPARAMS_LINK", new TemplateAction() {
		// link and bounds.
		void process(TemplateWriter tw, TemplateContext context) {
		    tw.write(HTMLUtil.toLink(context.curURL,
					     context.specificTypeVariables(),
					     true, false, true));
		}
	    });
	register("TYPEPARAMS_DECL", new TemplateAction() {
		void process(TemplateWriter tw, TemplateContext context) {
		    // anchor and linked bounds.
		    tw.write(HTMLUtil.toLink(context.curURL,
					     context.specificTypeVariables(),
					     true, true, true));
		}
	    });
	registerConditional("TRUE", new TemplateConditional() {
		boolean isBlockEmitted(TemplateContext c,
				       boolean isFirst, boolean isLast) {
		    return true;
		}
	    });
	registerConditional("FALSE", new TemplateConditional() {
		boolean isBlockEmitted(TemplateContext c,
				       boolean isFirst, boolean isLast) {
		    return false;
		}
	    });
	registerConditional("FIRST", new TemplateConditional() {
		boolean isBlockEmitted(TemplateContext c,
				       boolean isFirst, boolean isLast) {
		    return isFirst;
		}
	    });
	registerConditional("LAST", new TemplateConditional() {
		boolean isBlockEmitted(TemplateContext c,
				       boolean isFirst, boolean isLast) {
		    return isLast;
		}
	    });
	registerConditional("TAGS", new TemplateConditional() {
		boolean isBlockEmitted(TemplateContext c,
				       boolean isFirst, boolean isLast) {
		    List<Tag> lt = c.specificItem().tags();
		    return lt.size()>1 ||
			(lt.size()==1 && 
			 !(lt.get(0).isText() &&
			   lt.get(0).text().trim().length()==0));
		}
	    });
	registerConditional("SUPERCLASS", new TemplateConditional() {
		boolean isBlockEmitted(TemplateContext c,
				       boolean isFirst, boolean isLast) {
		    assert c.curClass!=null;
		    return c.curClass.superclass()!=null;
		}
	    });
	registerConditional("INTERFACE", new TemplateConditional() {
		boolean isBlockEmitted(TemplateContext c,
				       boolean isFirst, boolean isLast) {
		    assert c.curClass!=null;
		    return c.curClass.isInterface();
		}
	    });
	registerConditional("SUPERINTERFACES", new TemplateConditional() {
		boolean isBlockEmitted(TemplateContext c,
				       boolean isFirst, boolean isLast) {
		    assert c.curClass!=null;
		    return c.curClass.interfaces().size() > 0;
		}
	    });
	// iterator over all package groups
	registerForAll("GROUPS", new TemplateSimpleForAll() {
		List<TemplateContext> process(final TemplateContext c) {
		    return new FilterList<PackageGroup,TemplateContext>
			(c.options.groups) {
			public TemplateContext filter(PackageGroup pg) {
			    return new TemplateContext(c.root, c.options,
						       c.curURL, pg);
			}
		    };
		}
	    });
	// iterator over all packages with documented classes
	registerForAll("PACKAGES", new TemplateSimpleForAll() {
		List<TemplateContext> process(final TemplateContext c) {
		    List<PackageDoc> pkgs =
			(c.curGroup!=null) ? c.curGroup.packages() :
			HTMLUtil.allDocumentedPackages(c.root);
		    return new FilterList<PackageDoc,TemplateContext>
			(sorted(pkgs)) {
			public TemplateContext filter(PackageDoc pd) {
			    return new TemplateContext(c.root, c.options,
						       c.curURL, pd);
			}
		    };
		}
	    });
	// iterator over included classes (of the package).
	registerForAll("CLASSES", new TemplateSimpleForAll() {
		List<TemplateContext> process(final TemplateContext c) {
		    // if no package, then all in root. else all in pkg.
		    Collection<ClassDoc> l = (c.curPackage==null) ?
			c.root.classes() : c.curPackage.includedClasses();
		    return new FilterList<ClassDoc,TemplateContext>(sorted(l)){
			public TemplateContext filter(ClassDoc cd) {
			    return new TemplateContext(c.root, c.options,
						       c.curURL, c.curPackage,
						       cd);
			}
		    };
		}
	    });
	// iterator over included interfaces of the package.
	registerForAll("INTERFACES", new TemplateSimpleForAll() {
		List<TemplateContext> process(final TemplateContext c) {
		    // xxx in non-package context, all interfaces in root?
		    return new FilterList<ClassDoc,TemplateContext>
			(sorted(c.curPackage.includedInterfaces())) {
			public TemplateContext filter(ClassDoc cd) {
			    return new TemplateContext(c.root, c.options,
						       c.curURL, c.curPackage,
						       cd);
			}
		    };
		}
	    });
	// iterator over included ordinary classes of the package.
	registerForAll("ORDINARYCLASSES", new TemplateSimpleForAll() {
		List<TemplateContext> process(final TemplateContext c) {
		    // xxx in non-package context, all o-classes in root?
		    return new FilterList<ClassDoc,TemplateContext>
			(sorted(c.curPackage.includedOrdinaryClasses())) {
			public TemplateContext filter(ClassDoc cd) {
			    return new TemplateContext(c.root, c.options,
						       c.curURL, c.curPackage,
						       cd);
			}
		    };
		}
	    });
	// iterator over included exceptions of the package.
	registerForAll("EXCEPTIONS", new TemplateSimpleForAll() {
		List<TemplateContext> process(final TemplateContext c) {
		    // xxx in non-package context, all exceptions in root?
		    return new FilterList<ClassDoc,TemplateContext>
			(sorted(c.curPackage.includedExceptions())) {
			public TemplateContext filter(ClassDoc cd) {
			    return new TemplateContext(c.root, c.options,
						       c.curURL, c.curPackage,
						       cd);
			}
		    };
		}
	    });
	// iterator over included errors of the package.
	registerForAll("ERRORS", new TemplateSimpleForAll() {
		List<TemplateContext> process(final TemplateContext c) {
		    // xxx in non-package context, all errors in root?
		    return new FilterList<ClassDoc,TemplateContext>
			(sorted(c.curPackage.includedErrors())) {
			public TemplateContext filter(ClassDoc cd) {
			    return new TemplateContext(c.root, c.options,
						       c.curURL, c.curPackage,
						       cd);
			}
		    };
		}
	    });
	// iterator over included nested types of the class.
	registerForAll("NESTED", new TemplateSimpleForAll() {
		List<TemplateContext> process(final TemplateContext c) {
		    assert c.curClass!=null;
		    return new FilterList<ClassDoc,TemplateContext>
			(sorted(visible(c.curClass.innerClasses()))) {
			public TemplateContext filter(ClassDoc cd) {
			    return new TemplateContext(c.root, c.options,
						       c.curURL, c.curPackage,
						       cd);
			}
		    };
		}
	    });
	// iterator over included fields of the class.
	registerForAll("FIELDS", new TemplateSimpleForAll() {
		List<TemplateContext> process(final TemplateContext c) {
		    assert c.curClass!=null;
		    return new FilterList<FieldDoc,TemplateContext>
			(sorted(visible(c.curClass.fields()))) {
			public TemplateContext filter(FieldDoc fd) {
			    return new TemplateContext(c.root, c.options,
						       c.curURL, c.curPackage,
						       c.curClass, fd);
			}
		    };
		}
	    });
	// iterator over included constructors of the class.
	registerForAll("CONSTRUCTORS", new TemplateSimpleForAll() {
		List<TemplateContext> process(final TemplateContext c) {
		    assert c.curClass!=null;
		    return new FilterList<ConstructorDoc,TemplateContext>
			(sorted(visible(c.curClass.constructors()))) {
			public TemplateContext filter(ConstructorDoc cd) {
			    return new TemplateContext(c.root, c.options,
						       c.curURL, c.curPackage,
						       c.curClass, cd);
			}
		    };
		}
	    });
	// iterator over included non-constructor methods of the class.
	registerForAll("METHODS", new TemplateSimpleForAll() {
		List<TemplateContext> process(final TemplateContext c) {
		    assert c.curClass!=null;
		    return new FilterList<MethodDoc,TemplateContext>
			(sorted(visible(c.curClass.methods()))) {
			public TemplateContext filter(MethodDoc md) {
			    return new TemplateContext(c.root, c.options,
						       c.curURL, c.curPackage,
						       c.curClass, md);
			}
		    };
		}
	    });
    }

    /** Helper function. */
    private static <D extends Doc> List<D> visible(Collection<D> l) {
	List<D> result = new ArrayList<D>(l);
	for (Iterator<D> it=result.iterator(); it.hasNext(); )
	    if (!it.next().isIncluded()) it.remove();
	return result;
    }
    /** Helper function. */
    private static <D extends Doc> List<D> sorted(Collection<D> l) {
	List<D> result = new ArrayList<D>(l);
	Collections.sort(result, new DocComparator<D>());
	return result;
    }
    /** Helper class to turn one type of list into another. */
    private static abstract class FilterList<A,B> extends AbstractList<B> {
	final List<A> source;
	FilterList(List<A> source) { this.source = source; }
	public abstract B filter(A a);
	public int size() { return source.size(); }
	public B get(int i) { return filter(source.get(i)); }
    }
}
