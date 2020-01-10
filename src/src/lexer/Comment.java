package net.cscott.sinjdoc.lexer;

abstract class Comment extends InputElement {
  private StringBuffer comment = new StringBuffer();

  String getComment() { return comment.toString(); }

  void appendLine(String more) {
      comment.append(more);
  }
  // CSA tweak: for our purposes here we want the full comment with
  // leading white space and stars and everything.
  void appendLineNoStars(String more) { // 'more' is '\n' terminated.
    int i=0;

    // skip leading white space.
    for (; i<more.length(); i++)
      if (!Character.isSpaceChar(more.charAt(i))) 
	break;

    // skip any leading stars.
    for (; i<more.length(); i++)
      if (more.charAt(i)!='*')
	break;

    // the rest of the string belongs to the comment.
    if (i<more.length())
      comment.append(more.substring(i));
  }

}
