# Process this file with autoconf to produce a configure script.
AC_INIT(SinjDoc, 0.5, cscott@cscott.net)
AC_COPYRIGHT(Copyright (c) 2003-2004 C. Scott Ananian)
AC_REVISION($Revision: 1.14 $)
AC_CONFIG_SRCDIR(configure.ac) # double check --srcdir validity.
AC_CONFIG_AUX_DIR(scripts)
AM_INIT_AUTOMAKE

# Checks for programs.
### only some versions of jikes are okay.
AC_CHECK_PROGS(JAR, fastjar jar, not-found)
AC_CHECK_PROGS(JAVA, java, not-found)
# find a version of javac that compiles generic java.
AC_MSG_CHECKING([for a jsr-14-compliant java 1.4 compiler])
JAVAC=not-found
CLS=f$$
echo "abstract class $CLS extends java.util.AbstractList<Integer> { }" \
     > /tmp/$CLS.java
for jc in jikes javac ${JCC} ${JSR14DISTR:+$JSR14DISTR/scripts/javac} \
          ${HOME}/jsr14-ea-2_4a1/scripts/javac ; do
    if $jc -d /tmp -source 1.5 /tmp/$CLS.java 1>/dev/null 2>/dev/null; then
      JAVAC="$jc"
      break
    fi
done
/bin/rm -f /tmp/$CLS.java /tmp/$CLS.class
AC_MSG_RESULT($JAVAC)
AC_SUBST(JAVAC)
if test "$JAVAC" = "not-found" -o "$JAR" = "not-found" ; then
   AC_MSG_ERROR([Can't find tools to build java programs.])
fi
# use jsr14 java script as well as compiler if necessary.
case "$JAVAC" in
*/scripts/javac)
	JAVA="$(dirname $JAVAC)/java";
	;;
esac

# Checks for libraries.

# Checks for header files.

# Checks for typedefs, structures, and compiler characteristics.

# Checks for library functions.

AC_CONFIG_FILES([Makefile src/Makefile src/parser/Makefile src/lexer/Makefile
			  src/html/Makefile src/html/Version.java
			  src/html/templates/Makefile
			  src/main/Makefile src/overview.html])
AC_OUTPUT
