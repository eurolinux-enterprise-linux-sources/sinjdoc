// ClassTypeVariable.java, created Wed Mar 19 15:01:41 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.Type;
import net.cscott.sinjdoc.TypeVisitor;

import java.util.List;
/**
 * The <code>PClassTypeVariable</code> interface represents a type variable
 * declared as a formal parameter to a generic class or interface.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PClassTypeVariable.java,v 1.6 2003/05/08 03:54:26 cananian Exp $
 */
class PClassTypeVariable extends PTypeVariable
    implements net.cscott.sinjdoc.ClassTypeVariable {
    final ClassDoc declaringClass;
    PClassTypeVariable(ClassDoc declaringClass, String name) {
	super(name);
	this.declaringClass = declaringClass;
    }
    public ClassDoc declaringClass() { return declaringClass; }
    public <T> T accept(TypeVisitor<T> visitor) { return visitor.visit(this); }
}
