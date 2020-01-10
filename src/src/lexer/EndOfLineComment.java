package net.cscott.sinjdoc.lexer;

class EndOfLineComment extends Comment {
  EndOfLineComment(String comment) { appendLine(comment); }
}
