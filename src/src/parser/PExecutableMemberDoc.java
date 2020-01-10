// ExecutableMemberDoc.java, created Wed Mar 19 12:14:29 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.MethodTypeVariable;
import net.cscott.sinjdoc.Parameter;
import net.cscott.sinjdoc.ParamTag;
import net.cscott.sinjdoc.Tag;
import net.cscott.sinjdoc.ThrowsTag;
import net.cscott.sinjdoc.Type;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
/**
 * The <code>PExecutableMemberDoc</code> class represents a method or
 * constructor of a java class.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PExecutableMemberDoc.java,v 1.15 2003/08/01 00:01:26 cananian Exp $
 */
abstract class PExecutableMemberDoc extends PMemberDoc 
    implements net.cscott.sinjdoc.ExecutableMemberDoc {
    final List<MethodTypeVariable> typeParameters =
	new ArrayList<MethodTypeVariable>(2);
    final List<Parameter> parameters;
    final List<Type> thrownExceptions;
    final TypeContext commentContext;
    <P extends Parameter, T extends Type>
    PExecutableMemberDoc(ParseControl pc, PClassDoc containingClass,
			 int modifiers, String name, PSourcePosition position,
			 List<P> parameters, List<T> thrownExceptions,
			 String commentText, PSourcePosition commentPosition,
			 TypeContext memberContext) {
	super(pc, containingClass, modifiers, name, position,
	      commentText, commentPosition);
	this.parameters = new ArrayList<Parameter>(parameters);
	this.thrownExceptions = new ArrayList<Type>(thrownExceptions);
	this.commentContext = new TypeContext
	    (pc, memberContext.packageScope, memberContext.compilationUnit,
	     memberContext.classScope, this);
    }
    public List<MethodTypeVariable> typeParameters() {
	return Collections.unmodifiableList(typeParameters);
    }
    public List<Parameter> parameters() {
	return Collections.unmodifiableList(parameters);
    }
    public List<Type> thrownExceptions() {
	return Collections.unmodifiableList(thrownExceptions);
    }

    // methods with no data storage behind them.
    public final boolean isNative() {
	return Modifier.isNative(modifierSpecifier());
    }
    public final boolean isSynchronized() {
	return Modifier.isSynchronized(modifierSpecifier());
    }
    public final List<ParamTag> paramTags() {
	List<ParamTag> result = new ArrayList<ParamTag>(tags().size());
	for (Tag t : tags())
	    if (t instanceof ParamTag)
		result.add((ParamTag)t);
	return Collections.unmodifiableList(result);
    }
    public final List<ThrowsTag> throwsTags() {
	List<ThrowsTag> result = new ArrayList<ThrowsTag>(tags().size());
	for (Tag t : tags())
	    if (t instanceof ThrowsTag)
		result.add((ThrowsTag)t);
	return Collections.unmodifiableList(result);
    }
    public final String signature() {
	StringBuffer sb = new StringBuffer("(");
	for (Iterator<Parameter> it=parameters().iterator(); it.hasNext(); ) {
	    sb.append(it.next().type().signature());
	    if (it.hasNext())
		sb.append(',');
	}
	sb.append(')');
	return sb.toString();
    }
    public final String canonicalName() {
	return containingClass().canonicalName()+"."+name()+signature();
    }
    // methods abstract in PDoc
    public String name() { return name; }
    public TypeContext getCommentContext() { return commentContext; }

    // for easier debugging.
    public String toString() { return canonicalName(); }
}
