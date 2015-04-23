import java.util.*;

/**
 * This class defines our tree structure
 * 
 * @author niranjanhumagain
 *
 */
public class Tree {
	private int label;
	private int level = 0;
	private Tree parentTree;
	private char category;// gets value '+' or '-' when recursion stops else
							// null.
	private char attributeValue;// null for root node-- and attribute value that
								// extended this node from the parent node

	ArrayList<Tree> arrSubTrees = new ArrayList<Tree>();

	public Tree() {

	}

	public Tree(int label) {
		this.label = label;
	}

	public Tree(int label, Tree parentTree) {
		this.label = label;
		this.parentTree = parentTree;

	}

	public Tree(int label, char attributeValue) {
		this.label = label;
		this.attributeValue = attributeValue;
	}

	void addLeaf(Tree t, int label) {

		arrSubTrees.add(t);
		t.parentTree = this;
		t.level = this.level + 1;

	}

	void addClass(char category) {
		this.category = category;
	}

	void addLeaf(Tree t) {
		arrSubTrees.add(t);
		t.parentTree = this;
		t.level = this.level + 1;

	}

	public Tree getParentTree() {
		return this.parentTree;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public void setParentTree(Tree parentTree) {
		this.parentTree = parentTree;
	}

	public char getCategory() {
		return this.category;
	}

	public void setAttributeValue(char attributeValue) {
		this.attributeValue = attributeValue;
	}

	public char getAttributeValue() {
		return this.attributeValue;
	}

	public int getLevel() {
		return this.level;
	}

	public int getLabel() {
		return this.label;
	}

}