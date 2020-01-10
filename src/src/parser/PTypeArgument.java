// PTypeArgument.java, created Mon Jul 28 22:40:29 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cscott@cscott.net>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.Type;

/**
 * A <code>PTypeArgument</code> is either a type or a wildcard
 * specification such as <code>? extends Number</code>,
 * <code>? super Integer</code>, or <code>?</code>.
 * 
 * @author  C. Scott Ananian <cscott@cscott.net>
 * @version $Id: PTypeArgument.java,v 1.1 2003/07/29 16:02:48 cananian Exp $
 */
class PTypeArgument implements net.cscott.sinjdoc.TypeArgument {
    final Type type;
    final boolean isCovariant, isContravariant;
    PTypeArgument(Type type, boolean isCovariant, boolean isContravariant) {
	this.type = type;
	this.isCovariant = isCovariant;
	this.isContravariant = isContravariant;
    }
    public Type getType() { return type; }
    public boolean isCovariant() { return isCovariant; }
    public boolean isContravariant() { return isContravariant; }

    public String toString() {
	if (isCovariant)
	    if (isContravariant)
		return "?";
	    else
		return "? extends "+getType().toString();
	else
	    if (isContravariant)
		return "? super "+getType().toString();
	    else
		return getType().toString();
    }
}
