from enum import Enum, auto


class Color(Enum):
    RED = auto()
    BLACK = auto()


class Leaf:
    def __init__(self, val):
        self.__val = val

    def __len__(self):
        return 1

    def __getitem__(self, _):
        return self.__val

    def color(self):
        return Color.BLACK

    def rank(self):
        return 0


class Tree:
    def __init__(self, color, left, right):
        self.__color = color
        self.__len = len(left) + len(right)
        self.__rank = left.rank() + (1 if color == Color.BLACK else 0)
        self.left = left
        self.right = right

    def __len__(self):
        return self.__len

    def __getitem__(self, index):
        if index < len(self.left):
            return self.left[index]
        else:
            return self.right[index - len(self.left)]

    def color(self):
        return self.__color

    def rank(self):
        return self.__rank

    @staticmethod
    def merge(left, right):
        if left.rank() < right.rank():
            left = Tree.merge(left, right.left)
            if left.color() == Color.RED and left.left.color() == Color.RED and right.color() == Color.BLACK:
                if right.right.color() == Color.BLACK:
                    return Tree(Color.BLACK, left.left, Tree(Color.RED, left.right, right.right))
                else:
                    return Tree(Color.RED, Tree(Color.BLACK, left.left, left.right), Tree(Color.RED, right.right.left, right.right.right))
            else:
                return Tree(right.color(), left, right.right)
        elif left.rank() > right.rank():
            right = Tree.merge(left.right, right)
            if left.color() == Color.BLACK and right.right.color() == Color.RED and right.color() == Color.RED:
                if left.left.color():
                    return Tree(Color.BLACK, Tree(Color.RED, left.left, right.left), right.right)
                else:
                    return Tree(Color.RED, Tree(Color.Black, left.left.left, left.left.right), Tree(Color.BLACK, right.left, right.right))
            else:
                return Tree(left.color(), left.left, right)
        else:
            return Tree(Color.RED, left, right)

    def split(self, index):
        if index < len(self.left):
            left_left, left_right = self.left.split(index)
            return left_left, Tree.merge(left_right, self.right)
        elif index > len(self.left):
            right_left, right_right = self.right.split(index - len(self.left))
            return Tree.merge(self.left, right_left), right_right
        else:
            self.left, self.right


class PersistentLazyRBTree:
    def __set(self, root):
        self.__root = root

    def __getitem__(self, index):
        return self.__root[index]

    @staticmethod
    def merge(left, right):
        if left.__root == None:
            return right
        elif right.__root == None:
            return left
        else:
            root = Tree.merge(left.__root, right.__root)
            return PersistentLazyRBTree().__set(Tree(Color.BLACK, root.left, root.right))

    def split(self, index):
        if index == 0:
            return (PersistentLazyRBTree(), self)
        elif index == len(self):
            return (self, PersistentLazyRBTree())
        else:
            left, right = self.__root.split(index)
            return PersistentLazyRBTree().__set(left), PersistentLazyRBTree().__set(right)

    def insert(self, index, val):
        left, right = self.split(index)
        return PersistentLazyRBTree.merge(PersistentLazyRBTree.merge(left, PersistentLazyRBTree().__set(Leaf(val))), right)

    def erase(self, index):
        left, right = self.split(index)
        _, right = right.split(1)
        return PersistentLazyRBTree.merge(left, right)
