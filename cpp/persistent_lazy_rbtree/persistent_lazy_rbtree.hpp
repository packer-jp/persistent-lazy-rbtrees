#include <cstddef>
#include <memory>

enum class Color { Red, Black };
enum class Type { Leaf, Tree };
template <typename T> class Node;
template <typename T> using ptr = std::shared_ptr<Node<T>>;
template <typename T> struct Tree {
    Color color;
    size_t rank, size;
    ptr<T> left, right;
    Tree(Color color, ptr<T> left, ptr<T> right)
        : color(color), rank(left->rank() + (left->color() == Color::Black ? 1 : 0)),
          size(left->size() + right->size()), left(left), right(right) {}
};
template <typename T> class Node {
    Type type;
    union {
        T leaf;
        Tree<T> tree;
    };

  public:
    Node(T val) : type(Type::Leaf), leaf(val) {}
    Node(Color color, ptr<T> left, ptr<T> right) : type(Type::Tree) { new (&tree) Tree(color, left, right); }
    ~Node() { tree.~Tree(); }
    Color color() { return type == Type::Leaf ? Color::Black : tree.color; }
    size_t rank() { return type == Type::Leaf ? 0 : tree.rank; }
    size_t size() { return type == Type::Leaf ? 1 : tree.size; }
    ptr<T> left() { return tree.left; }
    ptr<T> right() { return tree.right; }
    T index(size_t index) {
        if (type == Type::Leaf) {
            return leaf;
        } else {
            if (index < left()->size()) {
                return left()->index(index);
            } else {
                return right()->index(index - left()->size());
            }
        }
    }
    static ptr<T> merge(ptr<T> left, ptr<T> right) {
        if (left->rank() < right->rank()) {
            left = merge(left, right->left());
            if (left->color() == Color::Red && left->left()->color() == Color::Red && right->color() == Color::Black) {
                if (right->right()->color() == Color::Black) {
                    return ptr<T>(new Node(Color::Black, left->left(),
                                           ptr<T>(new Node(Color::Red, left->right(), right->right()))));
                } else {
                    return ptr<T>(
                        new Node(Color::Red, ptr<T>(new Node(Color::Black, left->left(), left->right())),
                                 ptr<T>(new Node(Color::Black, right->right()->left(), right->right()->right()))));
                }
            } else {
                return ptr<T>(new Node(right->color(), left, right));
            }
        } else if (left->rank() > right->rank()) {
            right = merge(left->right(), right);
            if (left->color() == Color::Black && right->right()->color() == Color::Red &&
                right->color() == Color::Red) {
                if (left->left()->color() == Color::Black) {
                    return ptr<T>(new Node(Color::Black, ptr<T>(new Node(Color::Red, left->left(), right->left())),
                                           right->right()));
                } else {
                    return ptr<T>(new Node(Color::Red,
                                           ptr<T>(new Node(Color::Black, left->left()->left(), left->left()->right())),
                                           ptr<T>(new Node(Color::Black, right->left(), right->right()))));
                }
            } else {
                return ptr<T>(new Node(left->color(), left->left(), right));
            }
        } else {
            return ptr<T>(new Node(Color::Red, left, right));
        }
    }
    std::pair<ptr<T>, ptr<T>> split(size_t index) {
        if (index < left()->size()) {
            auto [left_left, left_right] = left()->split(index);
            return {left_left, merge(left_right, right())};
        } else if (index > left()->size()) {
            auto [right_left, right_right] = right()->split(index - left()->size());
            return {merge(left(), right_left), right_right};
        } else {
            return {left(), right()};
        }
    }
};
template <typename T> class PersistentLazyRBTree {
    ptr<T> root;
    PersistentLazyRBTree(ptr<T> root) : root(root) {}

  public:
    PersistentLazyRBTree() : root(nullptr) {}
    size_t size() { return root ? root->size() : 0; }
    T operator[](size_t index) { return root->index(index); }
    static PersistentLazyRBTree merge(const PersistentLazyRBTree &left, const PersistentLazyRBTree &right) {
        if (left.root == nullptr) {
            return right;
        } else if (right.root == nullptr) {
            return left;
        } else {
            ptr<T> root = Node<T>::merge(left.root, right.root);
            return PersistentLazyRBTree(ptr<T>(new Node(Color::Black, root->left(), root->right())));
        }
    }
    std::pair<PersistentLazyRBTree, PersistentLazyRBTree> split(size_t index) {
        if (index == 0) {
            return {PersistentLazyRBTree(), *this};
        } else if (index == size()) {
            return {*this, PersistentLazyRBTree()};
        } else {
            auto [left, right] = root->split(index);
            return {PersistentLazyRBTree(left), PersistentLazyRBTree(right)};
        }
    }
    PersistentLazyRBTree insert(size_t index, T val) {
        auto [left, right] = split(index);
        return merge(merge(left, PersistentLazyRBTree(ptr<T>(new Node(val)))), right);
    }
    PersistentLazyRBTree erase(size_t index) {
        auto [left, right] = split(index);
        right = right.split(1).second;
        return merge(left, right);
    }
};