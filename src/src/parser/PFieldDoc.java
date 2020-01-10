// FieldDoc.java, created Wed Mar 19 12:17:04 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.SerialFieldTag;
import net.cscott.sinjdoc.Type;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
/**
 * The <code>PFieldDoc</code> class represents a field in a java class.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PFieldDoc.java,v 1.11 2003/05/08 03:54:26 cananian Exp $
 */
class PFieldDoc extends PMemberDoc
    implements net.cscott.sinjdoc.FieldDoc {
    final Type type;
    final TypeContext commentContext;

    PFieldDoc(ParseControl pc, PClassDoc containingClass, int modifiers,
	      Type type, String name, PSourcePosition position,
	      String commentText, PSourcePosition commentPosition,
	      TypeContext commentContext) {
	super(pc, containingClass, modifiers, name, position,
	      commentText, commentPosition);
	this.type = type;
	this.commentContext = commentContext;
    }
    public Object constantValue() {
	// XXX unimplemented.
	return null;
    }
    public String constantValueExpression() {
	// XXX unimplemented
	return null;
    }
    public final boolean isTransient() {
	return Modifier.isTransient(modifierSpecifier());
    }
    public final boolean isVolatile() {
	return Modifier.isVolatile(modifierSpecifier());
    }
    public List<SerialFieldTag> serialFieldTags() {
	// XXX unimplemented
	return Arrays.asList(new SerialFieldTag[0]);
    }
    public Type type() { return type; }
    // methods abstract in PProgramElementDoc
    public String canonicalName() {
	return containingClass().canonicalName()+"."+name();
    }
    // methods abstract in PDoc
    public TypeContext getCommentContext() { return commentContext; }
    // override methods from PDoc
    public boolean isField() { return true; }
    // for debugging
    public String toString() { return type()+" "+name(); }
}
