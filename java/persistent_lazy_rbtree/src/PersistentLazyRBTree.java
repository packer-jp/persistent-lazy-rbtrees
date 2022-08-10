package persistent_lazy_rbtree.src;

enum Color {
    RED,
    BLACK,
}

interface Node<T> {

    class Pair<T> {
        final Node<T> e0, e1;

        Pair(Node<T> e0, Node<T> e1) {
            this.e0 = e0;
            this.e1 = e1;
        }
    }

    Color color();

    int rank();

    int size();

    Node<T> left();

    Node<T> right();

    T index(int index);

    Pair<T> split(int index);

    static <T> Node<T> merge(Node<T> left, Node<T> right) {
        if (left.rank() < right.rank()) {
            left = merge(left, right.left());
            if (left.color() == Color.RED && left.left().color() == Color.RED && right.color() == Color.BLACK) {
                if (right.right().color() == Color.BLACK) {
                    return new Tree<T>(Color.BLACK, left.left(), new Tree<T>(Color.RED, left.right(), right.right()));
                } else {
                    return new Tree<T>(Color.RED, new Tree<T>(Color.BLACK, left.left(), left.right()),
                            new Tree<T>(Color.BLACK, right.right().left(), right.right().right()));
                }
            } else {
                return new Tree<T>(right.color(), left, right.right());
            }
        } else if (left.rank() > right.rank()) {
            right = merge(left.right(), right);
            if (left.color() == Color.BLACK && right.right().color() == Color.RED && right.color() == Color.RED) {
                if (left.left().color() == Color.BLACK) {
                    return new Tree<T>(Color.BLACK, new Tree<T>(Color.RED, left.left(), right.left()), right.right());
                } else {
                    return new Tree<T>(Color.RED, new Tree<T>(Color.BLACK, left.left().left(), left.left().right()),
                            new Tree<T>(Color.BLACK, right.left(), right.right()));
                }
            } else {
                return new Tree<T>(left.color(), left.left(), right);
            }
        } else {
            return new Tree<T>(Color.RED, left, right);
        }
    }
}

class InvalidMethodException extends RuntimeException {
    InvalidMethodException() {
        super();
    }
}

class Leaf<T> implements Node<T> {
    final T val;

    Leaf(T val) {
        this.val = val;
    }

    @Override
    public Color color() {
        return Color.BLACK;
    }

    @Override
    public int rank() {
        return 0;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Node<T> left() {
        throw new InvalidMethodException();
    }

    @Override
    public Node<T> right() {
        throw new InvalidMethodException();
    }

    @Override
    public T index(int index) {
        return val;
    }

    @Override
    public Pair<T> split(int index) {
        throw new InvalidMethodException();
    }
}

class Tree<T> implements Node<T> {
    final Color color;
    final int rank, size;
    final Node<T> left, right;

    Tree(Color color, Node<T> left, Node<T> right) {
        this.color = color;
        this.rank = left.rank() + (left.color() == Color.BLACK ? 1 : 0);
        this.size = left.size() + right.size();
        this.left = left;
        this.right = right;
    }

    @Override
    public Color color() {
        return Color.BLACK;
    }

    @Override
    public int rank() {
        return 0;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Node<T> left() {
        throw new InvalidMethodException();
    }

    @Override
    public Node<T> right() {
        throw new InvalidMethodException();
    }

    @Override
    public T index(int index) {
        if (index < left.size()) {
            return left.index(index);
        } else {
            return right.index(index - left.size());
        }
    }

    @Override
    public Pair<T> split(int index) {
        if (index < left.size()) {
            Pair<T> _left = left.split(index);
            return new Pair<T>(_left.e0, Node.merge(_left.e1, right));
        } else if (index > left.size()) {
            Pair<T> _right = right.split(index - left.size());
            return new Pair<T>(Node.merge(left, _right.e0), _right.e1);
        } else {
            return new Pair<T>(left, right);
        }
    }
}

public class PersistentLazyRBTree<T> {
    public class Pair {
        final PersistentLazyRBTree<T> e0, e1;

        Pair(PersistentLazyRBTree<T> e0, PersistentLazyRBTree<T> e1) {
            this.e0 = e0;
            this.e1 = e1;
        }
    }

    private final Node<T> root;

    private PersistentLazyRBTree(Node<T> root) {
        this.root = root;
    }

    public PersistentLazyRBTree() {
        this.root = null;
    }

    public int size() {
        if (root == null) {
            return 0;
        } else {
            return root.size();
        }
    }

    public Pair split(int index) {
        if (index == 0) {
            return new Pair(new PersistentLazyRBTree<T>(), this);
        } else if (index == size()) {
            return new Pair(this, new PersistentLazyRBTree<T>());
        } else {
            Node.Pair<T> result = root.split(index);
            return new Pair(new PersistentLazyRBTree<T>(result.e0), new PersistentLazyRBTree<T>(result.e1));
        }
    }

    public static <T> PersistentLazyRBTree<T> merge(PersistentLazyRBTree<T> left, PersistentLazyRBTree<T> right) {
        if (left.root == null) {
            return right;
        } else if (right.root == null) {
            return left;
        } else {
            Node<T> root = Node.merge(left.root, right.root);
            return new PersistentLazyRBTree<T>(new Tree<T>(Color.BLACK, root.left(), root.right()));
        }
    }
}