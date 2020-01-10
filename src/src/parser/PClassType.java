// PClassType.java, created Mon Mar 24 14:08:22 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.ClassType;
import net.cscott.sinjdoc.ClassTypeVariable;
import net.cscott.sinjdoc.Type;
import net.cscott.sinjdoc.TypeVisitor;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
/**
 * The <code>PClassType</code> class represents an abstract type
 * name that can possibly be converted into a <code>ClassDoc</code>
 * object.
 *
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PClassType.java,v 1.10 2003/06/25 01:40:20 cananian Exp $
 */
abstract class PClassType implements ClassType {
    final ParseControl pc;
    PClassType(ParseControl pc) {
	this.pc = pc;
    }
    public final ClassDoc asClassDoc() {
	return pc.rootDoc.classNamed(canonicalTypeName());
    }
    public final String toString() {
	return typeName();
    }
    public String name() {
	String className = typeName();
	int idx = className.lastIndexOf('.');
	if (idx < 0) return className;
	return className.substring(idx+1);
    }
    public String signature() { return canonicalTypeName(); }
    public <T> T accept(TypeVisitor<T> visitor) { return visitor.visit(this); }
}// PClassType
