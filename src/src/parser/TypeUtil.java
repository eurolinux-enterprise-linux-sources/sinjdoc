// TypeUtil.java, created Mon Apr  7 12:11:40 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cscott@cscott.net>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.ArrayType;
import net.cscott.sinjdoc.ClassDoc;
import net.cscott.sinjdoc.ClassType;
import net.cscott.sinjdoc.ClassTypeVariable;
import net.cscott.sinjdoc.ParameterizedType;
import net.cscott.sinjdoc.Type;
import net.cscott.sinjdoc.TypeArgument;
import net.cscott.sinjdoc.TypeVisitor;
import net.cscott.sinjdoc.TypeVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * The <code>TypeUtil</code> class contains various <code>Type</code>-related
 * utility functions.
 * 
 * @author  C. Scott Ananian <cscott@cscott.net>
 * @version $Id: TypeUtil.java,v 1.10 2003/08/14 02:04:07 cananian Exp $
 */
abstract class TypeUtil {

    /** Return true iff the given type represents a (possibly-parameterized)
     *  interface. */
    public static boolean isInterface(Type a) {
	return a.accept(IS_INTERFACE_VISITOR).booleanValue();
    }
    private static final TypeVisitor<Boolean> IS_INTERFACE_VISITOR =
	new TypeVisitor<Boolean>() {
	    // arrays are not interfaces.
	    public Boolean visit(ArrayType t) { return Boolean.FALSE; }
	    // the ClassDoc lookup here *must not fail* if the results
	    // are to be trusted.
	    public Boolean visit(ClassType t) {
		ClassDoc cd = t.asClassDoc();
		if (cd==null) return Boolean.FALSE; // XXX a wild guess.
		return Boolean.valueOf(cd.isInterface());
	    }
	    // a parameterized type is an interface iff its base type is.
	    public Boolean visit(ParameterizedType t) {
		return t.getBaseType().accept(this);
	    }
	    // type variables are unknowable.
	    public Boolean visit(TypeVariable t) {
		assert false : "Can't determine if type variable "+t+
		" is an interface.";
		return Boolean.FALSE; // bogus.
	    }
	};
    /* Return true iff the given type represents a primitive type. */
    public static boolean isPrimitive(Type t) {
	return t.accept(IS_PRIMITIVE_VISITOR).booleanValue();
    }
    private static final TypeVisitor<Boolean> IS_PRIMITIVE_VISITOR =
	new TypeVisitor<Boolean>() {
	    public Boolean visit(ArrayType t) { return Boolean.FALSE; }
	    public Boolean visit(ParameterizedType t) { return Boolean.FALSE; }
	    public Boolean visit(TypeVariable t) { return Boolean.FALSE; }
	    public Boolean visit(ClassType t) {
		return Boolean.valueOf
		(PRIMITIVE_TYPES.matcher(t.canonicalTypeName()).matches());
	    }
	};
    private static final Pattern PRIMITIVE_TYPES = Pattern.compile
	("boolean|byte|short|int|long|char|float|double|void");

    /** Returns <code>true</code> iff objects of type a are instances of
     *  type b. */
    public static boolean isInstanceOf(final Type a, final Type b) {
	// inspired by HClass.isInstanceOf.
	return a.accept(new TypeVisitor<Boolean>() { // typeswitch 'a'
	    public Boolean visit(final ArrayType a) {
		return b.accept(new TypeVisitor<Boolean>() { // typeswitch 'b'
		    public Boolean visit(ArrayType b) {
			Type SC = a.baseType();
			Type TC = b.baseType();
			return Boolean.valueOf
			    ((isPrimitive(SC) && isPrimitive(TC) &&
			      SC.signature().equals(TC.signature())) ||
			     (!isPrimitive(SC) && !isPrimitive(TC) &&
			      isInstanceOf(SC, TC)));
		    }
		    public Boolean visit(ClassType b) {
			// see http://java.sun.com/docs/books/jls/clarify.html
			return Boolean.valueOf(Pattern.matches
					       ("java\\.lang\\.Cloneable|"+
						"java\\.io\\.Serializable|"+
						"java\\.lang\\.Object",
						b.canonicalTypeName()));
		    }
		    public Boolean visit(ParameterizedType b) {
			// arrays can never be instances of parameterized types
			return Boolean.FALSE;
		    }
		    public Boolean visit(TypeVariable b) {
			assert false : "impossible to determine instanceof "+
			    "type variable "+b;
			return Boolean.FALSE; // bogus.
		    }
		});
	    }
	    public Boolean visit(ClassType a) {
		return Boolean.valueOf(visitClassOrInterface(a));
	    }
	    public Boolean visit(ParameterizedType a) {
		return Boolean.valueOf(visitClassOrInterface(a));
	    }
	    private boolean visitClassOrInterface(Type a) {
		// xxx deal with 'param type instanceof raw type'
		if (isInterface(b))
		    return isSuperInterfaceOf(b, a);
		else if (isInterface(a))//in recursive eval of array instanceof
		    return b.signature().equals("java.lang.Object");
		else return isSuperClassOf(b, a);
	    }
	    public Boolean visit(TypeVariable a) {
		assert false : "impossible to determine if type variable "+a+
		    "is instanceof "+b;
		return Boolean.FALSE; // bogus.
	    }
	}).booleanValue();
    }

    /** Returns true if <code>left</code> and <code>right</code> refer to the
     *  same type. */
    public static boolean areEqual(Type left, Type right) {
	// XXX!  uses erased types.
	return left.signature().equals(right.signature());
    }
    /** Returns true if left is a super-interface of right. */
    public static boolean isSuperInterfaceOf(Type left, Type right) {
	assert isInterface(left);
	Set<String> seen = new HashSet<String>();
	LinkedList<Type> workList = new LinkedList<Type>();
	// seed the worklist with all the superclasses of 'right'
	for ( ; right!=null; right=superclass(right)) {
	    workList.add(right);
	    // note signature safe here because we can't inherit interfaces
	    // with two different parameterizations.
	    seen.add(right.signature());
	}
	while (!workList.isEmpty()) {
	    Type ty = workList.removeFirst();
	    if (areEqual(ty, left))
		return true;
	    for (Type nxty : superinterfaces(ty))
		if (!seen.contains(nxty.signature()))
		    workList.addLast(nxty);
	}
	// ran out of possibilities.
	return false;
    }
    /** Returns true iff <code>left</code> is a superclass of
     *  <code>right</code>.
     *  Does not look at interface information. */
    public static boolean isSuperClassOf(Type left, Type right) {
	assert !isInterface(left);
	// XXX all the right-hand types must resolve for the results to mean
	//     anything.
	for ( ; right!=null ; right=superclass(right))
	    if (areEqual(left, right))
		return true;
	return false;
    }

    /** Make the substitutions of types for type variables specified by the
     *  given parameterized type in the specified type. */
    private static Type subst(ParameterizedType substitutions, Type type) {
	return subst(makeSubstMap(substitutions), type);
    }
    /** Make the substitutions of types for type variables specified by the
     *  given parameterized type in the specified list of types. */
    private static List<Type> subst(ParameterizedType substitutions,
				    List<Type> typeList) {
	return subst(makeSubstMap(substitutions), typeList);
    }
    /** Make the substitutions of types for type variables specified by the
     *  given parameterized type in the specified list of type arguments. */
    private static List<TypeArgument> substA(ParameterizedType substitutions,
					     List<TypeArgument> argsList) {
	return substA(makeSubstMap(substitutions), argsList);
    }
    /** Helper function: make a mapping from <code>TypeVariable</code>s to
     *  <code>Type</code>s based on the given <code>ParameterizedType</code>.
     */
    private static Map<TypeVariable,Type> makeSubstMap(ParameterizedType pt) {
	Map<TypeVariable,Type> substMap = new HashMap<TypeVariable,Type>();
	for (Type t = pt; t instanceof ParameterizedType; ) {
	    ParameterizedType ptt = (ParameterizedType) t;
	    ClassDoc cd = ptt.getBaseType().asClassDoc();
	    if (cd!=null) {
		Iterator<ClassTypeVariable> it1=cd.typeParameters().iterator();
		Iterator<TypeArgument> it2 =
		    ptt.getActualTypeArguments().iterator();
		while (it1.hasNext() && it2.hasNext()) {
		    ClassTypeVariable ctv = it1.next();
		    TypeArgument arg = it2.next();
		    // wildcard type arguments aren't allowed in substitutions
		    assert !arg.isCovariant();
		    assert !arg.isContravariant();
		    substMap.put(ctv, arg.getType());
		}
		// lists should be the same size:
		assert !it1.hasNext();
		assert !it2.hasNext();
	    }
	    t = ptt.getDeclaringType();
	}
	return substMap;
    }

    /** Substitute <code>Type</code>s for <code>TypeVariable</code>s in
     *  the <code>typeList</code> according to the given <code>Map</code>. */
    static List<Type> subst(final Map<TypeVariable,Type> substMap,
			    List<Type> typeList) {
	List<Type> result = new ArrayList<Type>(typeList.size());
	for (Type ty : typeList)
	    result.add(subst(substMap, ty));
	return result;
    }
    /** Substitute <code>Type</code>s for <code>TypeVariable</code>s in
     *  the <code>argsList</code> according to the given <code>Map</code>. */
    private static List<TypeArgument> substA
	(final Map<TypeVariable,Type> substMap, List<TypeArgument> argsList) {
	List<TypeArgument> result=new ArrayList<TypeArgument>(argsList.size());
	for (TypeArgument a : argsList)
	    result.add(new PTypeArgument(subst(substMap, a.getType()),
					 a.isCovariant(),a.isContravariant()));
	return result;
    }
    /** Substitute <code>Type</code>s for <code>TypeVariable</code>s in
     *  <code>t</code> according to the given <code>Map</code>. */
    static Type subst(final Map<TypeVariable,Type> substMap, Type t) {
	return t.accept(new TypeVisitor<Type>() {
	    public Type visit(ArrayType t) {
		return new PArrayType(subst(substMap, t.baseType()),
				      t.dimension());
	    }
	    public Type visit(ClassType t) {
		return t; // no type variables here.
	    }
	    public Type visit(ParameterizedType t) {
		// do substitution on each type in arguments list
		// and create a new parameterized type from these args.
		return new PParameterizedType
		    (t.getBaseType(),
		     t.getDeclaringType()==null ? null :
		     subst(substMap, t.getDeclaringType()),
		     substA(substMap, t.getActualTypeArguments()));
	    }
	    public Type visit(TypeVariable t) {
		if (substMap.containsKey(t)) return substMap.get(t);
		return t;
	    }
	});
    }

    /** Return the superclass of the given type. */
    public static Type superclass(Type t) {
	return t.accept(SUPERCLASS_VISITOR);
    }
    private static final TypeVisitor<Type> SUPERCLASS_VISITOR =
	new TypeVisitor<Type>() {
	    public Type visit(ArrayType t) {
		int d = t.dimension();
		Type sc = superclass(t.baseType());
		if (sc!=null) return new PArrayType(sc, d);
		if (t.baseType().signature().equals("java.lang.Object"))
		    d--;
		ParseControl pc = ((PClassType)erasedType(t.baseType())).pc;
		ClassType objType = new PEagerClassType
		    (pc, "java.lang", "Object");
		if (d==0) return objType;
		return new PArrayType(objType, d);
	    }
	    public Type visit(ClassType t) {
		// raw type.
		ClassDoc cd = t.asClassDoc();
		if (cd==null) return null; // XXX bogus!!
		return cd.superclass();
	    }
	    public Type visit(ParameterizedType t) {
		// return superclass of raw type, with type variable
		// substitution.
		Type sc = superclass(t.getBaseType());
		if (sc==null) return null;//prob'ly bogus; Object has no params
		return subst(t, sc);
	    }
	    public Type visit(TypeVariable t) {
		assert false: "superclass of a type variable!?";
		// return type of non-interface bound, if there is one.
		for (Type bound : t.getBounds())
		    if (!isInterface(bound)) return bound;
		return null; // no superclasses here. (just interfaces)
	    }
	};

    /** Return the superinterfaces of the given type. */
    public static List<Type> superinterfaces(Type t) {
	return t.accept(SUPERINTERFACE_VISITOR);
    }
    private static final TypeVisitor<List<Type>> SUPERINTERFACE_VISITOR =
	new TypeVisitor<List<Type>>() {
	    public List<Type> visit(ArrayType t) {
		// see http://java.sun.com/docs/books/jls/clarify.html
		ParseControl pc = ((PClassType)erasedType(t.baseType())).pc;
		Type[] in = new Type[] {
		    new PEagerClassType(pc, "java.lang", "Cloneable"),
		    new PEagerClassType(pc, "java.io", "Serializable"),
		};
		return Arrays.asList(in);
	    }
	    public List<Type> visit(ClassType t) {
		// raw type.
		ClassDoc cd = t.asClassDoc();
		if (cd==null) return NO_TYPES; // XXX bogus!!
		return cd.interfaces();
	    }
	    public List<Type> visit(ParameterizedType t) {
		// return interfaces of raw type, with type variable
		// substitution.
		return subst(t, superinterfaces(t.getBaseType()));
	    }
	    public List<Type> visit(TypeVariable t) {
		assert false : "superinterfaces of a type variable?";
		// kinda bogus; return least interface bound, if there is one.
		Type least=null;
		for (Type bound : t.getBounds()) {
		    if (!isInterface(bound)) continue;
		    if (least==null ||
			least.signature().compareTo(bound.signature()) > 0)
			least = bound;
		}
		if (least!=null) return Arrays.asList(new Type[] { least });
		return NO_TYPES; // no interfaces here.
	    }
	    private final List<Type> NO_TYPES = Arrays.asList(new Type[0]);
	};

    /** Return the erasure of the given type t. */
    public static Type erasedType(Type t) {
	return t.accept(ERASURE_VISITOR);
    }
    private static final TypeVisitor<Type> ERASURE_VISITOR =
	new TypeVisitor<Type>() {
	    public Type visit(ArrayType t) {
		return new PArrayType(erasedType(t.baseType()),
				      t.dimension());
	    }
	    public Type visit(ClassType t) {
		return t;
	    }
	    public Type visit(ParameterizedType t) {
		return t.getBaseType();
	    }
	    public Type visit(TypeVariable t) {
		Type least=null;
		// the erasure of a type variable is:
		//  a) if the variable as a class type among its bounds, the
		//     erasure of that class type. (erase type variable
		//     references to determine whether they are class type)
		//  b) otherwise, if the bound consists of interface types only
		//     (erase type variables to tell; array type are illegal in
		//      bounds) the interface among the erasures of all
		//      interface types which has the least canonical name
		//      (JLS 6.7) using lexicographic ordering.
		for (Type origBound : t.getBounds() ) {
		    Type bound = erasedType(origBound);
		    assert !(bound instanceof TypeVariable);
		    if (!isInterface(bound)) return bound;
		    if (least==null ||
			least.signature().compareTo(bound.signature()) > 0)
			least = bound;
		}
		assert least!=null; // at least one bound!
		return least;
	    }
	};
}
