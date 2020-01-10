// HTMLUtil.java, created Mon Mar 31 13:43:45 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.html;

import net.cscott.sinjdoc.ArrayType;
import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.ClassType;
import net.cscott.sinjdoc.ClassTypeVariable;
import net.cscott.sinjdoc.ConstructorDoc;
import net.cscott.sinjdoc.DocErrorReporter;
import net.cscott.sinjdoc.ExecutableMemberDoc;
import net.cscott.sinjdoc.FieldDoc;
import net.cscott.sinjdoc.MemberDoc;
import net.cscott.sinjdoc.MethodTypeVariable;
import net.cscott.sinjdoc.PackageDoc;
import net.cscott.sinjdoc.ParameterizedType;
import net.cscott.sinjdoc.RootDoc;
import net.cscott.sinjdoc.Type;
import net.cscott.sinjdoc.TypeArgument;
import net.cscott.sinjdoc.TypeVariable;
import net.cscott.sinjdoc.TypeVisitor;

import java.nio.charset.CharsetEncoder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Pattern;
/**
 * The <code>HTMLUtil</code> class encapsulates several generally-used
 * functions used by the <code>HTMLDoclet</code>.
 *
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: HTMLUtil.java,v 1.19 2003/08/01 00:01:16 cananian Exp $
 */
class HTMLUtil {
    DocErrorReporter reporter;
    HTMLUtil(DocErrorReporter reporter) { this.reporter = reporter; }

    /** Collect all documented packages. */
    public static List<PackageDoc> allDocumentedPackages(RootDoc root) {
	// first collect all referenced packages.
	Map<String,PackageDoc> pkgMap = new LinkedHashMap<String,PackageDoc>();
	for (ClassDoc cd : root.classes()) {
	    PackageDoc pd = cd.containingPackage();
	    pkgMap.put(pd.name(), pd);
	}
	Collection<PackageDoc> c = pkgMap.values();
	return Arrays.asList(c.toArray(new PackageDoc[c.size()]));
    }

    /** Construct the URL for a page corresponding to the specified
     *  class type variable. */
    public static String toURL(ClassTypeVariable ctv) {
	StringBuffer sb = new StringBuffer(toURL(ctv.declaringClass()));
	sb.append("#!tv!");
	sb.append(ctv.getName());
	return sb.toString();
    }
    /** Construct the URL for a page corresponding to the specified
     *  method type variable. */
    public static String toURL(MethodTypeVariable mtv) {
	StringBuffer sb = new StringBuffer(toURL(mtv.declaringMethod()));
	sb.append(mtv.getName());
	return sb.toString();
    }
    /** Construct the URL for a page corresponding to the specified
     *  class member. */
    public static String toURL(MemberDoc m) {
	StringBuffer sb = new StringBuffer(toURL(m.containingClass()));
	sb.append("#");
	sb.append(m.name());
	if (m instanceof ExecutableMemberDoc)
	    sb.append(((ExecutableMemberDoc)m).signature());
	return sb.toString();
    }
    /** Construct the URL for a page corresponding to the specified class. */
    public static String toURL(ClassDoc c) {
	StringBuffer sb = new StringBuffer();
	for (ClassDoc p=c; p!=null; ) {
	    sb.insert(0, p.name());
	    p = p.containingClass();
	    if (p!=null) sb.insert(0, '.');
	}
	sb.insert(0, toBaseURL(c.containingPackage()));
	sb.append(".html");
	return sb.toString();
    }
    /** Construct the URL for the package-summary page for the given package.
     *  The pageName parameter should be one of "package-summary.html",
     *  "package-frame.html", or "package-tree.html". */
    public static String toURL(PackageDoc p, String pageName) {
	assert Pattern.matches("package-(summary|frame|tree)\\.html",pageName);
	return toBaseURL(p)+pageName;
    }
    /** Construct the URL representing the *directory* information
     *  regarding the specified package (including pages for classes
     *  in that package) should be stored in. */
    public static String toBaseURL(PackageDoc p) {
	String name = p.name();
	if (name.length()==0) return name;
	return name.replace('.','/')+"/";
    }
    /** Return a link to the specified package-related page in the package.
     *  The pageName parameter should be one of "package-summary.html",
     *  "package-frame.html", or "package-tree.html". */
    public static String toLink(URLContext context, PackageDoc p,
				String pageName) {
	StringBuffer sb = new StringBuffer("<a href=\"");
	sb.append(context.makeRelative(toURL(p, pageName)));
	sb.append("\" class=\"packageRef\">");
	if (p.name().length()==0)
	    sb.append("&lt;unnamed package&gt;");
	else
	    sb.append(p.name());
	sb.append("</a>");
	return sb.toString();
    }
    // abbreviates the type parameters, even when withParam is true:
    // never emits bounds information.
    public static String toLink(URLContext context, ClassDoc c,
				boolean withParam) {
	StringBuffer sb=new StringBuffer(c.name()); 
	for (ClassDoc p=c.containingClass(); p!=null; p=p.containingClass())
	    sb.insert(0, p.name()+"."); // parent class also in link text
	if (withParam && c.typeParameters().size()>0) {
	    sb.append("&lt;");
	    for (Iterator<ClassTypeVariable> it=c.typeParameters().iterator();
		 it.hasNext(); ) {
		sb.append(it.next().getName());
		if (it.hasNext()) sb.append(",");
	    }
	    sb.append("&gt;");
	}
	return toLink(context, c, sb.toString()/*link text*/);
    }
    // allows caller-specified link text.
    public static String toLink(URLContext context, ClassDoc c,
				String linkText) {
	StringBuffer sb = new StringBuffer("<a href=\"");
	sb.append(context.makeRelative(toURL(c)));
	sb.append("\" class=\"");
	if (c.isInterface()) sb.append("interfaceRef");
	else if (c.isError()) sb.append("errorRef");
	else if (c.isException()) sb.append("exceptionRef");
	else sb.append("classRef");
	sb.append("\">");
	sb.append(linkText);
	sb.append("</a>");
	return sb.toString();
    }
    public static String toLink(URLContext context, MemberDoc md) {
	StringBuffer sb = new StringBuffer("<a href=\"");
	sb.append(context.makeRelative(toURL(md)));
	sb.append("\" class=\"");
	if (md instanceof FieldDoc) sb.append("fieldRef");
	else if (md instanceof ConstructorDoc) sb.append("constructorRef");
	else sb.append("methodRef");
	sb.append("\">");
	sb.append(md.name());
	sb.append("</a>");
	return sb.toString();
    }
    public static String toLink(final URLContext context, Type t) {
	return t.accept(new TypeVisitor<String>() {
	    public String visit(ArrayType t) {
		StringBuffer sb = new StringBuffer
		    (t.baseType().accept(this));
		for (int i=0; i<t.dimension(); i++)
		    sb.append("[]");
		return sb.toString();
	    }
	    public String visit(ClassType t) {
		ClassDoc cd = t.asClassDoc();
		if (cd!=null && cd.isIncluded()) {
		    // individually link inner classes.
		    StringBuffer sb=new StringBuffer();
		    for ( ; cd!=null; cd=cd.containingClass()) {
			if (sb.length()>0) sb.insert(0, ".");
			sb.insert(0, toLink(context, cd, cd.name()));
		    }
		    return sb.toString();
		}
		// XXX look up w/ -link options here.
		return t.typeName();
	    }
	    public String visit(ParameterizedType t) {
		StringBuffer sb = new StringBuffer();
		// recurse on declaringType:
		Type declaringType = t.getDeclaringType();
		if (declaringType!=null) {
		    sb.append(declaringType.accept(this));
		    sb.append('.');
		}
		// now print out our own simple name.
		ClassDoc cd = t.getBaseType().asClassDoc();
		if (cd==null || !cd.isIncluded())
		    sb.append(t.getBaseType().name());
		else
		    sb.append(toLink(context, cd, cd.name()));
		assert cd==null ||
		    cd.typeParameters().size() ==
		    t.getActualTypeArguments().size();
		// and our parameters.
		Iterator<TypeArgument> it =
		    t.getActualTypeArguments().iterator();
		if (it.hasNext()) {
		    sb.append("&lt;");
		    while (it.hasNext()) {
			TypeArgument a = it.next();
			if (a.isCovariant())
			    if (a.isContravariant())
				sb.append('?');
			    else
				sb.append("? extends ");
			else
			    if (a.isContravariant())
				sb.append("? super ");
			if (!(a.isCovariant() && a.isContravariant()))
			    sb.append(a.getType().accept(this));
			if (it.hasNext())
			    sb.append(',');
		    }
		    sb.append("&gt;");
		}
		// done!
		return sb.toString();
	    }
	    public String visit(TypeVariable t) { assert false; return null; }
	    public String visit(ClassTypeVariable t) {
		// the link is to the class var definition
		StringBuffer sb = new StringBuffer("<a href=\"");
		sb.append(context.makeRelative(toURL(t)));
		sb.append("\" class=\"typeVarRef\">");
		sb.append(t.getName());
		sb.append("</a>");
		return sb.toString();
	    }
	    public String visit(MethodTypeVariable t) {
		// the link is to the method var definition
		StringBuffer sb = new StringBuffer("<a href=\"");
		sb.append(context.makeRelative(toURL(t)));
		sb.append("\" class=\"typeVarRef\">");
		sb.append(t.getName());
		sb.append("</a>");
		return sb.toString();
	    }
	});
    }
    /** Return an HTML representation of the given list of type parameters.
     *  If doAnchor is true, then the parameters will become anchors in
     *  the HTML document with appropriate names according to their type.
     *  Else if doLink is true, then the parameters will be hyperlinked
     *  to their declaration point.  If doBounds is true, then the
     *  bounds declaration of each type variable will be emitted. */
    static <TV extends TypeVariable> String toLink
		       (URLContext context, List<TV> ltv,
			boolean doLink, boolean doAnchor, boolean doBounds) {
	if (ltv.size()==0) return ""; // nothing to see/do here.
	StringBuffer sb = new StringBuffer();
	sb.append("&lt;");
	for(Iterator<TV> it=ltv.iterator(); it.hasNext(); ) {
	    TV tv = it.next();
	    if (doAnchor) {
		String prefix;
		if (tv instanceof MethodTypeVariable) {
		    ExecutableMemberDoc md =
			((MethodTypeVariable)tv).declaringMethod();
		    prefix = md.name() + md.signature();
		} else { // must be a class type variable
		    prefix = "!tv!";
		}
		String anchor = prefix+tv.getName();
		sb.append("<a name=\"");
		sb.append(anchor);
		sb.append("\" id=\"");
		sb.append(anchor);
		sb.append("\">");
		sb.append(tv.getName());
		sb.append("</a>");
	    } else if (doLink) {
		sb.append(HTMLUtil.toLink(context, tv));
	    } else
		sb.append(tv.getName());
	    if (doBounds) {
		List<Type> bounds = tv.getBounds();
		if (bounds.size() > 1 ||
		    !(bounds.get(0) instanceof ClassType &&
		      bounds.get(0).signature().equals("java.lang.Object"))) {
		    // "interesting" bounds.  bounds are always linked.
		    sb.append("&nbsp;extends&nbsp;");
		    for (Iterator<Type> it2=bounds.iterator(); it2.hasNext();){
			sb.append(HTMLUtil.toLink(context, it2.next()));
			if (it2.hasNext())
			    sb.append("&amp;");
		    }
		}
	    }
	    if (it.hasNext()) sb.append(",");
	}
	sb.append("&gt;");
	return sb.toString();
    }
    /** Copy the contents of a <code>Reader</code> to a <code>Writer</code>.
     */
    void copy(Reader r, Writer w) {
	try {
	    char[] buf = new char[1024];
	    while (true) {
		int st = r.read(buf, 0, buf.length);
		if (st<0) break; // end of stream.
		w.write(buf, 0, st);
	    }
	    r.close();
	    w.close();
	} catch (IOException e) {
	    reporter.printError(e.toString());
	}
    }

    /** Return a <code>Reader</code> for a resource stored at
     *  <code>net.cscott.sinjdoc.html.templates</code>. */
    static Reader resourceReader(String resource) {
	InputStream is = HTMLUtil.class.getResourceAsStream
	    ("templates/"+resource);
	if (is==null) return null;
	try {
	    return new InputStreamReader(is, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    assert false : "UTF-8 should always be supported";
	    return null;
	}
    }

    /** Return a <code>Writer</code> for the document at the given URL. */
    PrintWriter fileWriter(String url, HTMLOptions options) {
	return fileWriter(new URLContext(url), options);
    }
    /** Return a <code>Writer</code> for the document with the given
     *  <code>URLContext</code>. */
    PrintWriter fileWriter(URLContext url, HTMLOptions options) {
	File f = new File(options.docRoot, url.toFile().toString());
	reporter.printNotice("Generating "+f+"...");
	// make directory for file.
	if (f.getParentFile()!=null)
	    f.getParentFile().mkdirs();
	// now make a proper escaping-writer.
	assert options.charSet!=null;
	CharsetEncoder encoder1 = options.charSet.newEncoder();
	CharsetEncoder encoder2 = options.charSet.newEncoder();
	// can't share these encoders or else we'll eventually get an
	// IllegalStateException from the canEncode() method when we
	// (in HTMLWriter.write) interrupt a coding operation in
	// OutputStreamWriter.write().
	try {
	    return new PrintWriter
		(new HTMLWriter
		 (new BufferedWriter
		  (new OutputStreamWriter
		   (new FileOutputStream(f), encoder1)), encoder2));
	} catch (FileNotFoundException e) {
	    reporter.printError("Couldn't open file "+f+": "+e.toString());
	    return new PrintWriter(new NullWriter());
	}
    }

    /** Escapes any un-encodable characters using HTML entity escapes. */
    private static class HTMLWriter extends Writer {
	final Writer delegate;
	final CharsetEncoder encoder;
	HTMLWriter(Writer delegate, CharsetEncoder encoder) {
	    this.delegate = delegate;
	    this.encoder = encoder;
	}
	public void close() throws IOException { delegate.close(); }
	public void flush() throws IOException { delegate.flush(); }
	public void write(char[] cbuf, int off, int len)
	    throws IOException {
	    if (len==0) return; // quickly done.
	    CharSequence cs = new SimpleCharSequence(cbuf, off, len);
	    if (encoder.canEncode(cs))
		delegate.write(cbuf, off, len); // everything okie-dokie.
	    else if (len>1) { // divide and conquer.
		int half = len/2;
		this.write(cbuf, off, half);
		this.write(cbuf, off+half, len-half);
	    } else { // HTML-escape exactly one character.
		delegate.write("&#"+Integer.toString(cbuf[off], 10)+";");
	    }
	}
    }
    /** Throws away all output. */
    private static class NullWriter extends Writer {
	NullWriter() { }
	public void close() { }
	public void flush() { }
	public void write(char[] cbuf, int off, int len) { }
    }
}// HTMLUtil
