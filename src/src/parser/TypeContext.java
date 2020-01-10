// PTypeContext.java, created Wed Mar 26 11:33:08 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.ClassType;
import net.cscott.sinjdoc.ClassTypeVariable;
import net.cscott.sinjdoc.MethodTypeVariable;
import net.cscott.sinjdoc.Type;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/**
 * The <code>TypeContext</code> class encapsulates all of the information
 * needed to resolve a string specifying a type info a fully-qualified
 * type name.
 *
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: TypeContext.java,v 1.15 2003/08/01 00:01:26 cananian Exp $
 */
class TypeContext {
    final ParseControl pc;
    final PPackageDoc packageScope;
    final PCompilationUnit compilationUnit;
    final PClassDoc classScope; // class type variables available here.
    final PExecutableMemberDoc methodScope;// method type variables avail here.
    // type variables are useful in the context for after-the-fact resolution
    // of @throws and @serialField tags, which may mention type variables.

    public TypeContext(ParseControl pc, PPackageDoc packageScope,
		       PCompilationUnit compilationUnit, PClassDoc classScope,
		       PExecutableMemberDoc methodScope) {
	this.pc = pc;
	this.packageScope = packageScope;
	this.compilationUnit = compilationUnit;
	this.classScope = classScope;
	this.methodScope = methodScope;
    }
    /** A "no context" constructor. Appropriate for fully-qualified names. */
    public TypeContext(ParseControl pc) {
	this(pc, null);
    }
    /** A "package context" constructor.  */
    public TypeContext(ParseControl pc, PPackageDoc packageScope) {
	this(pc, packageScope, null, null, null);
    }
    public String toString() {
	return "TypeContext["+
	    "packageScope="+packageScope+", "+
	    "compilationUnit="+compilationUnit+", "+
	    "classScope="+classScope+", "+
	    "methodScope="+methodScope+"]";
    }

    // type resolution methods.

    /** Look up the given type name in this type context. The result can
     *  be a type variable in the classScope. */
    Type lookupTypeName(String typeName, boolean lazy) {
	// from experiments, qualified or inherited references to type
	// variables seem to be disallowed.  So just check type
	// variables in method & class scopes, and then fall back to
	// lookupClassTypeName(typeName, lazy)
	if (methodScope!=null)
	    for (MethodTypeVariable mtv : methodScope.typeParameters())
		if (typeName.equals(mtv.getName())) return mtv;
	if (methodScope==null || !methodScope.isStatic()) {
	    // non-static methods should look for type variables of
	    // their enclosing class.
	    PClassDoc enclosing = classScope;
	    while (enclosing!=null) {
		// class type variables.
		for (ClassTypeVariable ctv : enclosing.typeParameters())
		    if (typeName.equals(ctv.getName())) return ctv;
		// now go up to outer class if non-static.
		if (enclosing.isStatic()) break;
		enclosing = enclosing.containingClass();
	    }
	}
	// nope, fall back to lookupClassTypeName()
	return lookupClassTypeName(typeName, lazy);
    }

    /** Look up the given type name in this type context.  The result is
     *  guaranteed not to be a type variable. */
    ClassType lookupClassTypeName(String typeName, boolean lazy) {
	// if we're lazy, defer the real work.
	if (lazy) return new PLazyClassType(this, typeName);
	// okay, we're not lazy, let's actually do the work.
	int idx = typeName.lastIndexOf('.');
	if (idx<0) return lookupSimpleTypeName(typeName);
	else return lookupQualifiedTypeName(typeName.substring(0,idx),
					    typeName.substring(idx+1));
    }
    // look up a simple type name; that is, one without a '.'
    private ClassType lookupSimpleTypeName(String id) {
	assert id.indexOf('.')<0;
	// 1. not handling local class declarations.
	// (1b) not handling method type variables.
	// 2. if the simple type name occurs within the scope of a visible
	//    member type, it denotes that type (if more than one, error)
	//    (not handling class type variables)
	PClassDoc enclosing = classScope;
	while (enclosing!=null) {
	    // class member types
	    for (ClassDoc cd : enclosing.innerClasses())
		if (id.equals(cd.name())) return cd.type();
	    // now go up to outer class
	    enclosing = enclosing.containingClass();
	}
	// 3. if type is declared in the current compilation unit, either
	//    by simple-type-import or by declaration, then denotes that type
	if (compilationUnit!=null) {
	    for (String qualName : compilationUnit.singleTypeImport)
		if (("."+qualName).endsWith("."+id))
		    return new TypeContext(pc).lookupClassTypeName
			(qualName, false/* don't be lazy */);
	    for (PClassDoc cd : compilationUnit.classes) {
		if (cd.containingClass()!=null) continue; // not outer class.
		if (id.equals(cd.name())) return cd.type();
	    }
	}
	// 4. if type is declared in other compilation unit of the package
	//    containing the identifier, then denotes that type.
	if (packageScope!=null)
	    for (ClassType t : packageScope.allClasses())
		if (id.equals(t.typeName())) return t;
	// 5. if type is declared by a type-import-on-demand delcaration,
	//    then denotes that type (if more than one, error)
	if (compilationUnit!=null)
	    for (PPackageDoc ppkg : compilationUnit.onDemandImport)
		for (ClassType t : ppkg.allClasses())
		    if (id.equals(t.typeName())) return t;
	//   (5b) The java.lang package is always implicitly imported.
	PPackageDoc pkg = pc.rootDoc.packageNamed("java.lang");
	if (pkg!=null)
	    for (ClassType t : pkg.allClasses())
		if (id.equals(t.typeName())) return t;
	// 6. Otherwise, undefined; if not error then it could be in
	//    same package or in an opaque type-import-on-demand.
	//    we'll just make it opaque.
	return new PEagerClassType(pc, "<unknown>", id);
    }
    private ClassType lookupQualifiedTypeName(String Q, String id) {
	// recursively determine whether Q is a package or type name.
	// then determine if 'id' is a type within Q
	//   1) try package first.
	PPackageDoc pkg = pc.rootDoc.packageNamed(Q);
	// XXX also try creating an 'unincluded' package for reflection?
	if (pkg!=null)
	    for (ClassType t : pkg.allClasses())
		if (id.equals(t.typeName())) return t;
	//   2) try class named Q.
	ClassDoc cls = lookupClassTypeName(Q,false/*not lazy*/).asClassDoc();
	if (cls!=null)
	    for (ClassDoc cd : cls.innerClasses())
		if (id.equals(cd.name())) return cd.type();
	//      XXX try this using reflection.
	// give up; assume is fully qualified.
	// XXX should add to pkg if pkg!=null?  should create package?
	return new PEagerClassType(pc, Q, id);
    }
}// TypeContext
