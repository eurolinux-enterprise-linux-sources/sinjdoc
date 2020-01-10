// PEagerClassType.java, created Wed Mar 26 11:55:02 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ClassTypeVariable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/**
 * The <code>PEagerClassType</code> class represents a fully-resolved
 * non-parameterized class type.
 *
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PEagerClassType.java,v 1.10 2003/05/08 03:54:26 cananian Exp $
 */
class PEagerClassType extends PClassType {
    final String packageName;
    final String className; // dots here indicate inner classes.
    PEagerClassType(ParseControl pc,
		    String packageName, String className) {
	super(pc);
	this.packageName = packageName.intern();
	this.className = className.intern();
    }
    public String typeName() { return className; }
    public String canonicalTypeName() {
	if (packageName.length()==0) return className;
	return packageName+"."+className;
    }
}// PEagerClassType
