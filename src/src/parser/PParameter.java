// Parameter.java, created Wed Mar 19 12:38:54 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ArrayType;
import net.cscott.sinjdoc.Type;

/**
 * The <code>PParameter</code> interface represents parameter information,
 * including parameter type and parameter name.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PParameter.java,v 1.5 2003/07/29 16:02:47 cananian Exp $
 */
class PParameter implements net.cscott.sinjdoc.Parameter {
    final Type type;
    final String name;
    final boolean isVarArgs;
    PParameter(Type type, String name, boolean isVarArgs) {
	this.type = type; this.name = name; this.isVarArgs = isVarArgs;
	assert isVarArgs ? (type instanceof ArrayType) : true;
    }
    public Type type() { return type; }
    public String name() { return name; }
    public boolean isVarArgs() { return isVarArgs; }

    public Type printableType() {
	Type ty = type();
	if (isVarArgs) {
	    ArrayType at = (ArrayType) ty;
	    ty = (at.dimension() > 1) ?
		new PArrayType(at.baseType(), at.dimension()-1) :
		at.baseType();
	}
	return ty;
    }

    public final String toString() {
	return printableType().toString()+(isVarArgs?"... ":" ")+name();
    }
}
