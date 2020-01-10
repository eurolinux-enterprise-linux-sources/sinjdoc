// ClassDoc.java, created Wed Mar 19 11:39:43 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.ClassType;
import net.cscott.sinjdoc.ClassTypeVariable;
import net.cscott.sinjdoc.ConstructorDoc;
import net.cscott.sinjdoc.FieldDoc;
import net.cscott.sinjdoc.MethodDoc;
import net.cscott.sinjdoc.PackageDoc;
import net.cscott.sinjdoc.Type;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
/**
 * The <code>PClassDoc</code> interface represents a java class and
 * raw type and provides access to information about the class, the
 * class' comment and tags, and the members of the class.  A
 * <code>PClassDoc</code> only exists if it was processed in this
 * run of javadoc.  References to classes which may or may not have
 * been processed in this run and parameterized types are referred to
 * using <code>Type</code> (components of which can be converted to
 * <code>ClassDoc</code>, if possible).
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PClassDoc.java,v 1.20 2003/08/13 19:05:53 cananian Exp $
 */
class PClassDoc extends PProgramElementDoc
    implements net.cscott.sinjdoc.ClassDoc {
    final TypeContext typeContext, tagContext;
    final List<ClassTypeVariable> typeParameters =
	new ArrayList<ClassTypeVariable>(2);
    final String name;
    final boolean isInterface;
    final List<ConstructorDoc> constructors = new ArrayList<ConstructorDoc>();
    boolean definesSerializableFields = false;
    final List<FieldDoc> fields = new ArrayList<FieldDoc>();
    final List<ClassDoc> innerClasses = new ArrayList<ClassDoc>();
    final List<Type> interfaces = new ArrayList<Type>();
    final List<MethodDoc> methods = new ArrayList<MethodDoc>();
    Type superclass = null;
    final String commentText;
    final PSourcePosition commentPosition;

    PClassDoc(ParseControl pc, PPackageDoc containingPackage,
	      PCompilationUnit compilationUnit,
	      PClassDoc containingClass, int modifiers, String name,
	      PSourcePosition position, boolean isInterface,
	      String commentText, PSourcePosition commentPosition) {
	super(pc, containingPackage, containingClass, modifiers, position);
	this.name = name;
	this.isInterface = isInterface;
	this.typeContext =
	    new TypeContext(pc, containingPackage, compilationUnit,
			    this, null);
	this.tagContext = this.typeContext;
	this.commentText = commentText;
	this.commentPosition = commentPosition;
    }
    public List<ClassTypeVariable> typeParameters() {
	return Collections.unmodifiableList(typeParameters);
    }
    public ClassType type() {
	StringBuffer className = new StringBuffer(name());
	for (PClassDoc outerClass = this.containingClass(); outerClass != null;
	     outerClass = outerClass.containingClass()) {
	    className.insert(0, '.');
	    className.insert(0, outerClass.name());
	}
	return new PEagerClassType(pc, containingPackage().name(),
				   className.toString());
    }
    public List<ConstructorDoc> constructors() {
	return Collections.unmodifiableList(constructors);
    }
    public boolean definesSerializableFields() {
	return definesSerializableFields;
    }
    public List<FieldDoc> fields() {
	return Collections.unmodifiableList(fields);
    }
    public final ClassDoc findClass(String className) {
	return typeContext.lookupClassTypeName(className,false).asClassDoc();
    }
    public final List<ClassType> importedClasses() {
	ArrayList<ClassType> result = new ArrayList<ClassType>();
	for (String name : typeContext.compilationUnit.singleTypeImport)
	    result.add(typeContext.lookupClassTypeName(name, false));
	result.trimToSize();
	return Collections.unmodifiableList(result);
    }
    public final List<PackageDoc> importedPackages() {
	return Collections.unmodifiableList(new ArrayList<PackageDoc>(typeContext.compilationUnit.onDemandImport));
    }
    public List<ClassDoc> innerClasses() {
	return Collections.unmodifiableList(innerClasses);
    }
    public List<Type> interfaces() {
	return Collections.unmodifiableList(interfaces);
    }
    public final boolean isAbstract() {
	return Modifier.isAbstract(modifierSpecifier());
    }
    public final boolean isExternalizable() {
	return instanceOf(new PEagerClassType
	    (pc, "java.io", "Externalizable"));
    }
    public final boolean isSerializable() {
	return instanceOf(new PEagerClassType
	    (pc, "java.io", "Serializable"));
    }

    public List<MethodDoc> methods() {
	return Collections.unmodifiableList(methods);
    }
    public List<FieldDoc> serializableFields() {
	throw new RuntimeException("unimplemented");
    }
    public List<MethodDoc> serializationMethods() {
	throw new RuntimeException("unimplemented");
    }
    public boolean subclassOf(ClassDoc cd) {
	throw new RuntimeException("unimplemented");
    }
    public Type superclass() {
	if (canonicalName().equals("java.lang.Object")) return null;
	return superclass;
    }
    public boolean instanceOf(Type t) {
	return TypeUtil.isInstanceOf(this.type(), t);
    }
    // methods abstract in PProgramElementDoc
    public String canonicalName() {
	StringBuffer sb = new StringBuffer();
	if (containingClass()!=null) {
	    sb.append(containingClass().canonicalName());
	    sb.append('.');
	} else if (containingPackage().name().length()>0) {
	    sb.append(containingPackage().name());
	    sb.append('.');
	}
	sb.append(name());
	return sb.toString();
    }
    public String toString() { return canonicalName(); }
    // methods abstract in PDoc
    public String getRawCommentText() { return commentText; }
    public PSourcePosition getRawCommentPosition() { return commentPosition; }
    public TypeContext getCommentContext() { return tagContext; }
    public String name() { return name; }
    // override methods in PDoc
    public boolean isClass() { return !isInterface; }
    public boolean isOrdinaryClass() {
	return !isInterface() && !isError() && !isException();
    }
    public boolean isInterface() { return isInterface; }
    public boolean isError() {
	return instanceOf(new PEagerClassType
	    (pc, "java.lang", "Error"));
    }
    public boolean isException() {
	return instanceOf(new PEagerClassType
	    (pc, "java.lang", "Exception"));
    }
}
