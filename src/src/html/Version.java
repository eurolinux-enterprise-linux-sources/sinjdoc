// -*-java-*-
package net.cscott.sinjdoc.html;

/**
 * The <code>Version</code> object contains fields naming the current version
 * (0.5) of the SinjDoc tool.
 * @version $Id: Version.java.in,v 1.5 2003/05/08 03:54:25 cananian Exp $
 */
public abstract class Version {
    /** The name of the package.
     *  @return "SinjDoc" */
    public static final String PACKAGE_NAME = "SinjDoc";
    /** The version of the package.
     *  @return "0.5" */
    public static final String PACKAGE_VERSION = "0.5";
    /** The package name and version as one string.
     *  @return "SinjDoc 0.5" */
    public static final String PACKAGE_STRING = "SinjDoc 0.5";
    /** The address to which bug reports should be sent.
     */ //  @return "cscott@cscott.net" // keep email address off the 'net.
    public static final String PACKAGE_BUGREPORT = "cscott@cscott.net";

    /** Prints the package version if invoked. */
    public static void main(String[] args) {
	System.out.println(PACKAGE_STRING);
	System.out.println("Bug reports to "+PACKAGE_BUGREPORT);
    }
}
