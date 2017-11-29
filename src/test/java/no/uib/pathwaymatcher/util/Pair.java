package no.uib.pathwaymatcher.util;


/**
 * @author Luis Francisco Hernández Sánchez <luis.sanchez@uib.no>
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */

public class Pair<S, T> {
    private S left;
    private T right;

    public Pair(S left, T right) {
        this.left = left;
        this.right = right;
    }

    public S getLeft() {
        return left;
    }

    public T getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (left != null ? !left.equals(pair.left) : pair.left != null) return false;
        if (right != null ? !right.equals(pair.right) : pair.right != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return (this.getLeft() == null ? "null" : getLeft()) + ":" + (this.getRight() == null ? "null" : this.getRight());
    }
}
