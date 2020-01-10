## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
CLEANFILES = Java15.java Sym.java
AM_JAVACFLAGS = -g -source 1.5 -warnunchecked
EXTRA_DIST := package.html Java15.cup

JAVAROOT=$(top_builddir)/classes

CLASSPATH_ENV = CLASSPATH=$(JAVAROOT):$(top_srcdir)/lib/cup.jar:$(top_srcdir)/lib/jutil.jar:$$CLASSPATH

noinst_JAVA = \
Pair.java \
Java15.java \
Sym.java \
FileUtil.java \
TypeContext.java \
TypeUtil.java \
ParseControl.java \
PArrayType.java \
PClassDoc.java \
PClassType.java \
PClassTypeVariable.java \
PCompilationUnit.java \
PConstructorDoc.java \
PDoc.java \
PEagerClassType.java \
PExecutableMemberDoc.java \
PFile.java \
PFieldDoc.java \
PLazyClassType.java \
PLazyInnerClassType.java \
PMemberDoc.java \
PMethodDoc.java \
PMethodTypeVariable.java \
PPackageDoc.java \
PParamTag.java \
PParameter.java \
PParameterizedType.java \
PProgramElementDoc.java \
PRootDoc.java \
PSeeTag.java \
PSerialFieldTag.java \
PSourcePosition.java \
PTag.java \
PThrowsTag.java \
PTypeArgument.java \
PTypeVariable.java \
UniqueVector.java

# CUP
%.java : %.cup
	if test "$(JAVA)" = "not-found" ; then \
	   echo Not able to regenerate "parser;" java interpreter not found.;\
	   exit 1; \
	fi
	cd `dirname $@` && \
	$(JAVA) -cp $(top_srcdir)/lib/cup.jar java_cup.Main \
		-parser `basename $@ .java` -symbols Sym \
	< `basename $<`
# don't know how to automagically generate this dependency.
Sym.java: Java15.java

# automake 1.6.3 is broken: it doesn't actually include java sources
# in the distribution.  The following line works around this.
EXTRA_DIST += $(noinst_JAVA)
