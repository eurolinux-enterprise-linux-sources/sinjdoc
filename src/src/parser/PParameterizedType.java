// PParameterizedType.java, created Wed Mar 19 15:06:55 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ClassType;
import net.cscott.sinjdoc.Type;
import net.cscott.sinjdoc.TypeArgument;
import net.cscott.sinjdoc.TypeVisitor;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
/**
 * The <code>PParameterizedType</code> interface represents a parameterized
 * type such as <code>Collection&lt;Integer&gt;</code> or
 * <code>HashMap&lt;String,Double&gt;</code>.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PParameterizedType.java,v 1.13 2003/07/29 16:02:47 cananian Exp $
 */
class PParameterizedType
    implements net.cscott.sinjdoc.ParameterizedType {
    final ClassType baseType;
    final Type declaringType;
    final List<TypeArgument> actualTypeArguments;
    /** Create a new parameterized class type. */
    PParameterizedType(ClassType baseType, Type declaringType,
		       List<TypeArgument> actualTypeArguments) {
	this.baseType = baseType;
	this.declaringType = declaringType;
	this.actualTypeArguments = actualTypeArguments;
	assert actualTypeArguments.size()>=0;
    }
    public ClassType getBaseType() { return baseType; }
    public Type getDeclaringType() { return declaringType; }
    public List<TypeArgument> getActualTypeArguments() {
	return Collections.unmodifiableList(actualTypeArguments);
    }
    public String signature() {
	return TypeUtil.erasedType(this).signature();
    }
    public String toString() {
	StringBuffer sb = new StringBuffer(getBaseType().toString());
	sb.append('<');
	for (Iterator<TypeArgument> it=getActualTypeArguments().iterator();
	     it.hasNext(); ) {
	    sb.append(it.next().toString());
	    if (it.hasNext()) sb.append(',');
	}
	sb.append('>');
	return sb.toString();
    }
    public <T> T accept(TypeVisitor<T> visitor) { return visitor.visit(this); }
}
