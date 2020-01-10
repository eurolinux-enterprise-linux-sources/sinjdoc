// PLazyClassType.java, created Mon Mar 24 14:08:22 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.ClassType;
import net.cscott.sinjdoc.ClassTypeVariable;
import net.cscott.sinjdoc.Type;

import java.io.File;
import java.util.Iterator;
import java.util.List;
/**
 * The <code>PLazyClassType</code> class represents an unresolved class
 * type.  Resolution of the exact type specified is deferred until
 * its methods are invoked, at which time the given <code>TypeContext</code>
 * is used to resolve the name.  NOTE THAT this will NOT work for
 * type variable names! (They should be instances of PTypeVariable, not
 * PClassType.)
 *
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PLazyClassType.java,v 1.10 2003/08/14 04:02:22 cananian Exp $
 */
class PLazyClassType extends PClassType {
    ClassType cache;
    TypeContext typeContext;
    String typeName;
    PLazyClassType(TypeContext typeContext, String typeName) {
	super(typeContext.pc);
	this.cache = null;
	this.typeContext = typeContext;
	this.typeName = typeName;
	assert isValid();
    }
    public String canonicalTypeName() {
	if (cache==null) lookup();
	return cache.canonicalTypeName();
    }
    public String typeName() {
	if (cache==null) lookup();
	return cache.typeName();
    }
    private void lookup() {
	assert typeContext.pc.isParsingComplete :
	    "Premature type resolution: "+typeName;
	assert cache==null && isValid();
	cache=typeContext.lookupClassTypeName(typeName,false/*lazy no more*/);
	typeContext=null;
	typeName=null;
	assert cache!=null && isValid();
    }
    private boolean isValid() {
	assert cache==null ?
	    (typeContext!=null && typeName!=null) :
	    (typeContext==null && typeName==null);
	return true;
    }
}// PLazyClassType
