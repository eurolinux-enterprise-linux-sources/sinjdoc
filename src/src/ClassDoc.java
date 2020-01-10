// ClassDoc.java, created Wed Mar 19 11:39:43 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.util.List;
/**
 * The <code>ClassDoc</code> interface represents a java class and
 * raw type and provides access to information about the class, the
 * class' comment and tags, and the members of the class.  A
 * <code>ClassDoc</code> only exists if it was processed in this
 * run of javadoc.  References to classes which may or may not have
 * been processed in this run and parameterized types are referred to
 * using <code>Type</code> (components of which can be converted to
 * <code>ClassDoc</code>, if possible).
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: ClassDoc.java,v 1.5 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.ClassDoc
 */
public interface ClassDoc extends ProgramElementDoc {
    /** Return the <code>ClassType</code> corresponding to this
     *  <code>ClassDoc</code>. */
    public ClassType type();
    /** Return the type variables declared by this class, if it is
     *  generic; otherwise return a zero-length list.
     *  @see ClassType#typeParameters()
     */
    public List<ClassTypeVariable> typeParameters();
    /**
     * Return visible constructors in class.  An array containing the default
     * no-arg constructor is returned if no other constructors exist.
     */
    public List<ConstructorDoc> constructors();
    /**
     * Return true iif serializable fields are explicitly defined with the
     * special class member <code>serialPersistentFields</code>.
     * @see <a href="http://java.sun.com/j2se/1.4/docs/tooldocs/javadoc/doclet/com/sun/javadoc/SerialFieldTag.html">SerialFieldTag</a>
     */
    public boolean definesSerializableFields();
    /** Return visible fields in class. */
    public List<FieldDoc> fields();
    /**
     * Find a class within the context of this class.  Search order is
     * qualified name, in this class (inner), in this package, in the
     * class imports, in the package imports.  Return the <Code>ClassDoc</code>
     * if found, null if not found.
     */
    public ClassDoc findClass(String className);
    /**
     * Get the list of classes declared as imported.  These are called
     * "simple-type-import declarations" in the JLS.
     */
    public List<ClassType> importedClasses();
    /**
     * Get the list of packages declared as imported.  These are called
     * "type-import-on-demand declarations" in the JLS.
     */
    public List<PackageDoc> importedPackages();
    /**
     * Return visible inner classes within this class, not including
     * anonymous and local classes.
     */
    public List<ClassDoc> innerClasses();
    /**
     * Return interfaces implemented by this class of interfaces extended
     * by this interface.  Includes only directly declared interfaces, not
     * inherited interfaces.  Returns a zero-length array if there are no
     * interfaces.
     */
    public List<Type> interfaces();
    /** Return true if this class is abstract. */
    public boolean isAbstract();
    /** Return true if this class implements
     *  <code>java.io.Externalizable</code>.  Since
     *  <code>java.io.Externalizable</code> extends
     *  <code>java.io.Serializable</code>, Externalizable objects are
     *  also Serializable.
     */
    public boolean isExternalizable();
    /** Return true if this class implements
     *  <code>java.io.Serializable</code>.  Since
     *  <code>java.io.Externalizable</code> extends
     *  <code>java.io.Serializable</code>, Externalizable objects are
     *  also Serializable.
     */
    public boolean isSerializable();
    /**
     * Return visible methods in class, not including constructors.
     */
    public List<MethodDoc> methods();
    /**
     * Return the serializable fields of this class.
     * <p> Return either a list of default fields documented by the
     * <code>serial</code> tag or a single <code>FieldDoc</code> for
     * the <code>serialPersistentField</code> member.  There should be
     * a <code>serialField</code> tag for each serializable field defined
     * by an <code>ObjectStreamField</code> array component of
     * <code>serialPersistentField</code>.
     * @return an array of <code>FieldDoc</code> for the serializable fields
     * of this class.
     * @see <a href="http://java.sun.com/j2se/1.4/docs/tooldocs/javadoc/doclet/com/sun/javadoc/SerialFieldTag.html">SerialFieldTag</a>
     */
    public List<FieldDoc> serializableFields();
    /**
     * Return the serialization methods for this class.
     * @return an array of <code>MethodDoc</code> that represents the
     * serialization methods for this class.
     */
    public List<MethodDoc> serializationMethods();
    /**
     * Test whether this class is a subclass of the specified class.
     * @param cd the candidate superclass.
     * @return true if cd is a superclass of this class.
     */
    public boolean subclassOf(ClassDoc cd);
    /**
     * Return the superclass of this class, or null if there is no
     * superclass.
     */
    public Type superclass();
}
