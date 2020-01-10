// TemplateContext.java, created Tue Apr  1 11:58:12 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cscott@cscott.net>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.html;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.Doc;
import net.cscott.sinjdoc.ExecutableMemberDoc;
import net.cscott.sinjdoc.MemberDoc;
import net.cscott.sinjdoc.PackageDoc;
import net.cscott.sinjdoc.RootDoc;
import net.cscott.sinjdoc.TypeVariable;

import java.util.ArrayList;
import java.util.List;
/**
 * The <code>TemplateContext</code> class encapsulates all the information
 * required to emit an HTML page using the macro expander.
 * 
 * @author  C. Scott Ananian <cscott@cscott.net>
 * @version $Id: TemplateContext.java,v 1.9 2003/05/08 03:54:25 cananian Exp $
 */
class TemplateContext {
    public final RootDoc root;
    public final HTMLOptions options;
    public final PackageGroup curGroup;
    public final PackageDoc curPackage;
    public final ClassDoc curClass;
    public final MemberDoc curMember;
    public final URLContext curURL;
    
    /** Creates a <code>TemplateContext</code> appropriate for a top-level
     *  overview page (no package- or class-specific information). */
    public TemplateContext(RootDoc root, HTMLOptions options,
			   URLContext curURL) {
	this(root,options,curURL,null,null,null,null);
    }
    /** Creates a <code>TemplateContext</code> which holds the
     *  current package group (for overview page). */
    public TemplateContext(RootDoc root, HTMLOptions options,
			   URLContext curURL, PackageGroup curGroup) {
	this(root,options,curURL,curGroup,null,null,null);
    }
    /** Creates a <code>TemplateContext</code> appropriate for a
     *  package information page (no class-specific information). */
    public TemplateContext(RootDoc root, HTMLOptions options,
			   URLContext curURL, PackageDoc curPackage) {
	this(root,options,curURL,curPackage,null);
    }
    /** Creates a <code>TemplateContext</code> appropriate for a
     *  class information page (no member-specific information). */
    public TemplateContext(RootDoc root, HTMLOptions options,
			   URLContext curURL, PackageDoc curPackage,
			   ClassDoc curClass) {
	this(root,options,curURL,curPackage,curClass,null);
    }
    /** Creates a <code>TemplateContext</code> appropriate for
     *  formatting member-specific information. */
    public TemplateContext(RootDoc root, HTMLOptions options,
			   URLContext curURL, PackageDoc curPackage,
			   ClassDoc curClass, MemberDoc curMember) {
	this(root,options,curURL,null,curPackage,curClass,curMember);
    }
    /** Most-general constructor.  Not for external use. */
    private TemplateContext(RootDoc root, HTMLOptions options,
			    URLContext curURL, PackageGroup curGroup,
			    PackageDoc curPackage, ClassDoc curClass,
			    MemberDoc curMember) {
	this.root = root;
	this.options = options;
	this.curGroup = curGroup;
	this.curPackage = curPackage;
	this.curClass = curClass;
	this.curMember = curMember;
	this.curURL = curURL;
	assert root!=null && options!=null && curURL!=null;
    }
    /** Returns the most specific documentation item in this context. */
    public Doc specificItem() {
	if (curMember!=null) return curMember;
	else if (curClass!=null) return curClass;
	else if (curPackage!=null) return curPackage;
	else return root;
    }
    /** Return the type variables of the most specific documentation item
     *  in this context. */
    List<TypeVariable> specificTypeVariables() {
	List<TypeVariable> ltv = new ArrayList<TypeVariable>();
	if (curMember!=null &&
	    curMember instanceof ExecutableMemberDoc) {
	    ExecutableMemberDoc md = (ExecutableMemberDoc) curMember;
	    ltv.addAll(md.typeParameters());
	} else if (curClass!=null) {
	    ltv.addAll(curClass.typeParameters());
	}
	return ltv;
    }
    // for debugging.
    public String toString() {
	StringBuffer sb = new StringBuffer("TC[");
	sb.append(curURL); sb.append(",");
	sb.append(specificItem().toString());
	sb.append("]");
	return sb.toString();
    }
}
