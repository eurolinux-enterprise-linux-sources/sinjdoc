package net.cscott.sinjdoc.parser;

/**
 * Simple strongly-typed pair class.
 *
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: Pair.java,v 1.3 2003/05/08 03:54:26 cananian Exp $
 */

final class Pair<A,B> {
    public final A left;
    public final B right;
    public Pair (A left, B right) { this.left=left; this.right=right; }
    public int hashCode() {
	int l = (left==null)?0:left.hashCode();
	int r = (right==null)?0:right.hashCode();
	return l^r;
    }
    public boolean equals(Object o) {
	if (!(o instanceof Pair)) return false;
	Pair p = (Pair) o;
	boolean l = (left==null)?p.left==null:left.equals(p.left);
	if (!l) return false;
	boolean r = (right==null)?p.right==null:right.equals(p.right);
	if (!r) return false;
	return true;
    }
}
