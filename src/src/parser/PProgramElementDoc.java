// ProgramElementDoc.java, created Wed Mar 19 12:49:27 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.PackageDoc;

import java.lang.reflect.Modifier;
/**
 * The <code>PProgramElementDoc</code> class represents a java program
 * element: class, interface, field, constructor, or method.  This is
 * an abstract class dealing with information common to these elements.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PProgramElementDoc.java,v 1.10 2003/05/08 03:54:26 cananian Exp $
 */
abstract class PProgramElementDoc extends PDoc
    implements net.cscott.sinjdoc.ProgramElementDoc {
    final PPackageDoc containingPackage;
    final PClassDoc containingClass;
    final int modifiers;
    final PSourcePosition position;
    PProgramElementDoc(ParseControl pc, PPackageDoc containingPackage,
		       PClassDoc containingClass, int modifiers,
		       PSourcePosition position) {
	super(pc);
	this.containingPackage = containingPackage;
	this.containingClass = containingClass;
	this.modifiers = modifiers;
	this.position = position;
    }
    public PPackageDoc containingPackage() { return containingPackage; }
    public PClassDoc containingClass() { return containingClass; }
    public int modifierSpecifier() { return modifiers; }
    public abstract String canonicalName();
    public final boolean isFinal() {
	return Modifier.isFinal(modifierSpecifier());
    }
    public final boolean isPackage() {
	return !(isPublic() || isProtected() || isPrivate());
    }
    public final boolean isPrivate() {
	return Modifier.isPrivate(modifierSpecifier());
    }
    public final boolean isProtected() {
	return Modifier.isProtected(modifierSpecifier());
    }
    public final boolean isPublic() {
	return Modifier.isPublic(modifierSpecifier());
    }
    public final boolean isStatic() {
	return Modifier.isStatic(modifierSpecifier());
    }
    // XXX do we spell out synchronized and native?
    public final String modifiers() {
	return Modifier.toString(modifierSpecifier());
    }
    public final boolean isIncluded() {
	// included only if we match the visibility criteria
	if (isPublic() && pc.showPublic()) return true;
	if (isProtected() && pc.showProtected()) return true;
	if (isPackage() && pc.showPackage()) return true;
	if (isPrivate() && pc.showPrivate()) return true;
	return false;
    }
    public final PSourcePosition position() { return position; }
}
