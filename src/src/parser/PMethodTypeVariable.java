// MethodTypeVariable.java, created Wed Mar 19 15:05:26 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ExecutableMemberDoc;
import net.cscott.sinjdoc.Type;
import net.cscott.sinjdoc.TypeVisitor;

import java.util.List;
/**
 * The <code>PMethodTypeVariable</code> interface represents a type
 * variable declared as a formal parameter to a generic method.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PMethodTypeVariable.java,v 1.8 2003/05/08 03:54:26 cananian Exp $
 */
class PMethodTypeVariable extends PTypeVariable
    implements net.cscott.sinjdoc.MethodTypeVariable {
    // allow changes to this field, as method type variables are seen & used
    // before we know the name of the method they are associated with.
    ExecutableMemberDoc declaringMethod;
    PMethodTypeVariable(String name) {
	super(name);
    }
    public ExecutableMemberDoc declaringMethod() {
	assert declaringMethod!=null; // should be set by now.
	return declaringMethod;
    }
    public <T> T accept(TypeVisitor<T> visitor) { return visitor.visit(this); }
}
