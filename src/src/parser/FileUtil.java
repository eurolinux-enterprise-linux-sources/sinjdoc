// FileUtil.java, created Thu Mar 20 15:06:21 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.lexer.EscapedUnicodeReader;
import net.cscott.sinjdoc.DocErrorReporter;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The <code>FileUtil</code> class contains some useful methods
 * for testing filenames and enumerating source files in packages.
 *
 * @author C. Scott Ananian <cananian@alumni.ptinceton.edu>
 * @version $Id: FileUtil.java,v 1.18 2003/08/11 22:32:39 cananian Exp $
 */
public class FileUtil {
    final List<File> sourcePath = new ArrayList<File>();
    final int sourceVersion;
    FileUtil(String sourcePath, int sourceVersion) {
	assert sourceVersion>=1 && sourceVersion<=5;
	this.sourceVersion = sourceVersion;
	for (String filename : splitPath(sourcePath)) {
	    File f = new File(filename);
	    if (f.isDirectory())
		this.sourcePath.add(f);
	}
    }

    /** Returns true if the given string is a valid java package name. */
    public boolean isValidPackageName(String str) {
	for (String name : DOT.split(str,-1))
	    if (!isValidIdentifier(name))
		return false;
	return true;
    }
    /** Returns true if the given string is a valid java source file name. */
    public boolean isValidClassName(String str) {
	if (!str.toLowerCase().endsWith(".java")) return false;
	return isValidIdentifier(str.substring(0,str.length()-5));
    }
    /** Returns true if the given string is a valid java identifier. */
    public boolean isValidIdentifier(String str) {
	if (str.length()<1) return false;
	if (!Character.isJavaIdentifierStart(str.charAt(0))) return false;
	for(int i=1; i<str.length(); i++)
	    if (!Character.isJavaIdentifierPart(str.charAt(i)))
		return false;
	// check that it is not a reserved word.
	if (sourceVersion<4 && str.equals("assert")) return true;
	if (sourceVersion<2 && str.equals("strictfp")) return true;
	if (stoplist.matcher(str).matches()) return false;
	// okay, we've passed.
	return true;
    }
    /** a list of non-identifier words */
    private static final Pattern stoplist = Pattern.compile
	("true|false|null|abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while");
   
    /** Split the given string at the path separator character. */
    public static List<String> splitPath(String str) {
	String pathSep = System.getProperty("path.separator");
	if (pathSep==null) pathSep=":"; // safe default.
	return Arrays.asList(Pattern.compile(pathSep).split(str));
    }
    /** Split the given string at colons. */
    public static List<String> splitColon(String str) {
	return Arrays.asList(Pattern.compile(":").split(str));
    }
    /** Expand a fileglob with the '*' character to a list of matching
     *  files. */
    public static List<File> expandFileStar(String str) {
	// xxx note this doesn't work for strings like: asd/*/asdas.java
	int idx = str.indexOf('*');
	if (idx<0) return Collections.singletonList(new File(str));
	String lhs = str.substring(0, idx);
	final String rhs = str.substring(idx+1);
	File lhsF = new File(lhs);
	final String namePrefix = lhsF.isDirectory()?"":lhsF.getName();
	File dir = lhsF.isDirectory()?lhsF:lhsF.getParentFile();
	if (dir==null) dir=new File(".");
	return Arrays.asList(dir.listFiles(new FilenameFilter() {
		public boolean accept(File dir, String name) {
		    return
			name.length()>=(namePrefix.length()+rhs.length()) &&
			name.startsWith(namePrefix) &&
			name.endsWith(rhs);
		}
	    }));
    }
    /** Expand the given subpackage and exclude strings to yield a full list
     *  of specified packages. */
    public List<String> expandPackages(List<String> subpackages,
				       List<String> exclude) {
	List<String> result = new ArrayList<String>();
	for (String subpkg : subpackages)
	    // expand the packages one at a time.
	    _expandOnePackage_(result, subpkg, exclude);
	return result;
    }
    private void _expandOnePackage_(List<String> result,
				    String packageName,
				    List<String> exclude) {
	// if this package is on the exclude list, we're done.
	for (String ex : exclude)
	    if (packageName.equals(ex) || packageName.startsWith(ex+"."))
		return; // done already!  quick finish.

	// okay, it's not to be excluded.  Does it exist on the source path?
	File pkgDir = findPackage(packageName);
	if (pkgDir==null) return; // doesn't exist; we're done.
	// add this package to the result if it's got at least one valid
	// java source file in it.
	if (sourceFilesInPackage(pkgDir).size()>0)
	    result.add(packageName);
	// now look for subdirectories containing java source files.
	for (File subDir : pkgDir.listFiles(new FileFilter(){
		public boolean accept(File f) { return f.isDirectory(); }
	    })) {
	    String newPkg = packageName+"."+subDir.getName();
	    // must be a valid name for a java package.
	    if (!isValidPackageName(newPkg)) continue;
	    // okay, then!
	    _expandOnePackage_(result, newPkg, exclude);
	}
    }
    /** Returns a list of all the source files found in the source path
     *  corresponding to the given package name. */
    public List<File> sourceFilesInPackage(String packageName) {
	File pkgDir = findPackage(packageName);
	if (pkgDir==null) return Arrays.asList(new File[0]);
	return sourceFilesInPackage(pkgDir);
    }
    private List<File> sourceFilesInPackage(File packageDir) {
	return Arrays.asList(packageDir.listFiles(new FileFilter() {
		public boolean accept(File f) {
		    if (!f.isFile()) return false;
		    if (!f.exists()) return false;
		    return isValidClassName(f.getName());
		}
	    }));
    }
    /** Find a directory in the source path corresponding to the given
     *  package. */
    File findPackage(String packageName) {
	// convert package name to path.
	String path = replaceAllLiterally
	    (DOT.matcher(packageName), File.separator);
	for (File sourcePathPiece : sourcePath ) {
	    File candidate = new File(sourcePathPiece, path);
	    if (candidate.isDirectory() && candidate.exists())
		return candidate;
	}
	return null; // couldn't find it.
    }
    private static final Pattern DOT = Pattern.compile("[.]");

    /** Replacement for Matcher.replaceAll() that doesn't do fancy-pants
     *  backslash-and-dollar substitution on the replacement string. */
    private static String replaceAllLiterally
	(Matcher m, String replacement) {
	StringBuffer sb = new StringBuffer();
	while (m.find()) {
	    m.appendReplacement(sb, "");
	    sb.append(replacement);
	}
	m.appendTail(sb);
	return sb.toString();
    }

    /** Extract the text between &lt;body&gt; and &lt;/body&gt; tags
     *  in the given file. */
    static Pair<String,PSourcePosition> rawFileText
	(File f, String encoding, boolean isUnicodeEscaped,
	 DocErrorReporter reporter) {
	Pair<String,PSourcePosition> pair = snarf(f, encoding,
						  isUnicodeEscaped, reporter);
	Matcher matcher = BODY_PATTERN.matcher(pair.left);
	if (!matcher.find()) return pair; // return unchanged.
	return new Pair<String,PSourcePosition>
	    (matcher.group(1), pair.right.add(matcher.start(1)));
    }
    private final static Pattern BODY_PATTERN = Pattern.compile
	("<\\s*body[^>]*>(.*)</\\s*body",
	 Pattern.CASE_INSENSITIVE|Pattern.DOTALL);

    /** Snarf up the contents of a file as a string. */
    static Pair<String,PSourcePosition> snarf
	(File f, String encoding, boolean isUnicodeEscaped,
	 DocErrorReporter reporter) {
	if (f==null)
	    return new Pair<String,PSourcePosition>
		("", PSourcePosition.NO_INFO);
	if (!(f.exists() && f.isFile())) {
	    reporter.printError("Can't open file: "+f);
	    return new Pair<String,PSourcePosition>
		("", new PSourcePosition(f, encoding, isUnicodeEscaped,
					 reporter));
	}
	StringBuffer sb=new StringBuffer();
	try {
	    Reader reader = fileReader(f, encoding, reporter);
	    if (isUnicodeEscaped) reader = new EscapedUnicodeReader(reader);
	    char[] buf=new char[8192];
	    int len;
	    while (-1!=(len=reader.read(buf)))
		sb.append(buf, 0, len);
	    reader.close();
	} catch (IOException e) {
	    reporter.printError("Trouble reading "+f+": "+e);
	}
	return new Pair<String,PSourcePosition>
	    (sb.toString(), new PSourcePosition(f, encoding, isUnicodeEscaped,
						reporter));
    }
    /** Return a <code>Reader</code> for the given file using the given
     *  encoding.  Reports any encoding errors using the given reporter. */
    public static Reader fileReader(File f, String encoding,
				    DocErrorReporter reporter)
	throws java.io.FileNotFoundException {
	if (encoding!=null) try {
	    return new InputStreamReader(new FileInputStream(f), encoding);
	} catch (java.io.UnsupportedEncodingException e) {
	    reporter.printWarning("Encoding "+encoding+" is not supported; "+
				  "using default.");
	}
	return new FileReader(f);
    }
}// FileUtil
