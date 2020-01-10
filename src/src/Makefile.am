## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
SUBDIRS = . lexer parser html main
EXTRA_DIST := package.html overview.html.in

AM_JAVACFLAGS = -g -source 1.5 -warnunchecked
# JAVAROOT directory must exist!
JAVAROOT=$(top_builddir)/classes

CLASSPATH_ENV = CLASSPATH=$(JAVAROOT):$(top_srcdir)/lib/jutil.jar:$$CLASSPATH

noinst_JAVA = \
ArrayType.java \
ClassDoc.java \
ClassType.java \
ClassTypeVariable.java \
ConstructorDoc.java \
Doc.java \
DocErrorReporter.java \
Doclet.java \
ExecutableMemberDoc.java \
FieldDoc.java \
MemberDoc.java \
MethodDoc.java \
MethodTypeVariable.java \
PackageDoc.java \
ParamTag.java \
Parameter.java \
ParameterizedType.java \
ProgramElementDoc.java \
RootDoc.java \
SeeTag.java \
SerialFieldTag.java \
SourcePosition.java \
Tag.java \
TagVisitor.java \
ThrowsTag.java \
Type.java \
TypeArgument.java \
TypeVariable.java \
TypeVisitor.java

# copy Main.java over here so that javadoc can find it.
# (we can't build it here because of dependency ordering problems)
all-local: Main.java
Main.java: main/Main.java
	$(RM) $@
	ln -s $< $@
clean-local:
	$(RM) Main.java

# automake 1.6.3 is broken: it doesn't actually include java sources
# in the distribution.  The following line works around this.
EXTRA_DIST += $(noinst_JAVA)
