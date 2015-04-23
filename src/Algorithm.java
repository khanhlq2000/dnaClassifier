import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;

/**
 * main class that implements the decision tree algorithm
 * 
 * @author niranjanhumagain
 *
 */
public class Algorithm {

	int a_p = 0, a_n = 0, g_p = 0, g_n = 0, c_p = 0, c_n = 0, t_p = 0, t_n = 0;

	int currentIndex = 0;
	int noOfInstances = 71;
	int defaultPosInstance = 37;
	int defaultNegInstance = 34;

	Tree rootTree;
	char[] attrValues = { 'a', 'g', 'c', 't' };

	double chiSquareValue = 0;
	int infGainValue = 1; // determines whether to use entropy or
							// misclassification for information gain
							// calculation

	ArrayList<Integer> arrVisitedNodes = new ArrayList<Integer>();
	private HashMap<String, Character> dnaMap = new HashMap<String, Character>();
	private TreeMap<Integer, Double> informationGainMap = new TreeMap<Integer, Double>();
	private HashMap<Index.Key, Index.Value> countMap = new HashMap<Index.Key, Index.Value>();

	private File trainingFile = new File(System.getProperty("user.dir")
			+ "/dataset/training.txt");
	private File validatingFile = new File(System.getProperty("user.dir")
			+ "/dataset/validation.txt");

	public Algorithm(String[] args) {

		infGainValue = Integer.parseInt(args[0]);
		chiSquareValue = Double.parseDouble(args[1]);

		getData();
		storeInformationGainData();
		calculateInformationGain();
		getDecisionTree();
		checkValidatingData();

	}

	public static void main(String[] args) {

		if (args.length > 0) {
			new Algorithm(args);
		}

	}

	/**
	 * Gets training data from a source file
	 * 
	 */
	public void getData() {
		if (trainingFile.exists()) {
			try {
				String str = null;
				BufferedReader br = new BufferedReader(new FileReader(
						trainingFile));
				while ((str = br.readLine()) != null) {
					String dnaSequence = str.substring(0, str.length() - 2);
					Character dnaClass = str.charAt(str.length() - 1);
					dnaMap.put(dnaSequence, dnaClass);
				}
				br.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			System.out.println("doesnot exists");
		}

	}

	/**
	 * Gets number of positive and negative counts for given attributeValue of
	 * particular index
	 * 
	 * @param index
	 * @param attributeValue
	 * @return
	 */

	Index.Value getIndexValue(int index, char attributeValue) {
		Index.Key indexKey = new Index().new Key(index, attributeValue);
		Index.Value indexValue = null;

		for (Index.Key indexKeyMap : countMap.keySet()) {
			if (indexKey.equals(indexKeyMap)) {
				indexValue = countMap.get(indexKeyMap);
			}
		}

		return indexValue;
	}

	/**
	 * Calculates the mis classification error for attributeValue of particular
	 * index
	 * 
	 * @param index
	 * @param attributeValue
	 * @return
	 */
	public double getMisClassificationError(int index, char attributeValue) {

		Index.Value indexValue = getIndexValue(index, attributeValue);
		double pos = indexValue.getPositive() / indexValue.getTotal();
		double neg = indexValue.getNegative() / indexValue.getTotal();
		return 1 - Math.max(pos, neg);
	}

	/**
	 * default misclassification error
	 * 
	 * @return
	 */
	public double getDefaultMisclassificationError() {
		double pos = defaultPosInstance / noOfInstances;
		double neg = defaultNegInstance / noOfInstances;
		return 1 - Math.max(pos, neg);
	}

	/**
	 * Calculates entropy for the given set of parameters
	 * 
	 * @param pos
	 * @param neg
	 * @param total
	 * @return entropy value
	 */

	double getDefaultEntropy(int pos, int neg, int total) {

		if (pos == 0 || neg == 0) {
			return 0;
		}
		return ((-pos / ((double) total) * (Math.log((double) pos / total)) / (Math
				.log((double) 2))) - (((double) neg / total)
				* (Math.log((double) neg / total)) / (Math.log((double) 2))));
	}

	/**
	 * Calculates entropy for the subset of examples containing the particular
	 * attributeValue (Sv)
	 * 
	 * @param index
	 * @param attributeValue
	 * @return
	 */

	double getEntropyofAttribute(int index, char attributeValue) {
		Index.Key indexKey = new Index().new Key(index, attributeValue);
		Index.Value indexValue = null;

		for (Index.Key indexKeyMap : countMap.keySet()) {
			if (indexKey.equals(indexKeyMap)) {
				indexValue = countMap.get(indexKeyMap);
				break;
			}
		}

		return getDefaultEntropy(indexValue.getPositive(),
				indexValue.getNegative(), indexValue.getTotal());

	}

	/**
	 * 
	 * Stores data in hashtable that are necessary for calculating information
	 * gain
	 */

	void storeInformationGainData() {
		for (int index = 0; index < 57; index++)

		{
			getPositiveNegativeCount(index);

			countMap.put(new Index().new Key(index, 'a'),
					new Index().new Value(a_p, a_n, 'a'));
			countMap.put(new Index().new Key(index, 'g'),
					new Index().new Value(g_p, g_n, 'g'));
			countMap.put(new Index().new Key(index, 'c'),
					new Index().new Value(c_p, c_n, 'c'));
			countMap.put(new Index().new Key(index, 't'),
					new Index().new Value(t_p, t_n, 't'));

		}

	}

	/**
	 * determine the root node for the decision tree
	 * 
	 * @return
	 */

	public int getAttributeWithMaximumInformationGain() {

		double maxInformationGain = -100;
		int maxInformationGainIndex = -1;

		for (Entry<Integer, Double> entry : informationGainMap.entrySet()) {
			if (entry.getValue() > maxInformationGain) {
				maxInformationGain = entry.getValue();
				maxInformationGainIndex = entry.getKey();
			}
		}

		return maxInformationGainIndex;

	}

	/**
	 * Implements logic related to recursion
	 * 
	 * @param parentTree
	 * @param subTree
	 */
	public void proceedToRecursion(Tree parentTree, Tree subTree) {
		parentTree.addLeaf(subTree);
		arrVisitedNodes.add(subTree.getLabel());
		currentIndex = subTree.getLabel();
		getRecursiveTree(subTree);
	}

	/**
	 * implements logic related to termination
	 * 
	 * @param parentTree
	 * @param currentIndex
	 * @param attributeValue
	 */
	public void terminateRecursion(Tree parentTree, int currentIndex,
			char attributeValue) {
		Tree subTree = new Tree(-1, attributeValue);
		parentTree.addLeaf(subTree);

		Index.Value indexValue = getIndexValue(currentIndex, attributeValue);
		subTree.addClass(indexValue.getPositive() > indexValue.getNegative() ? '+'
				: '-');

		currentIndex = parentTree.getLabel();
	}

	/**
	 * Creates our decision tree
	 * 
	 */

	void getDecisionTree() {

		rootTree = new Tree(getAttributeWithMaximumInformationGain(), null);

		currentIndex = rootTree.getLabel();
		arrVisitedNodes.add(currentIndex);

		// System.out.println("currentIndex="+currentIndex);

		// below a
		for (char attr : attrValues) {
			calculateInformationGain(currentIndex, attr);
			Tree subTree = new Tree(getAttributeWithMaximumInformationGain(),
					attr);
			if (isRecursionValid(currentIndex, attr)) {
				proceedToRecursion(rootTree, subTree);
			}

			else {
				terminateRecursion(rootTree, currentIndex, attr);
			}

		}

	}

	/**
	 * get recursive tree
	 * 
	 * @param tree
	 */
	void getRecursiveTree(Tree t) {

		for (char attr : attrValues) {
			calculateInformationGain(currentIndex, attr);
			Tree subTree = new Tree(getAttributeWithMaximumInformationGain(),
					attr);

			if (isRecursionValid(currentIndex, attr)) {
				proceedToRecursion(rootTree, subTree);
			}

			else {
				terminateRecursion(rootTree, currentIndex, attr);
			}

		}

	}

	/**
	 * Checks whether we came to the decision point (i.e; at the leaf of tree)
	 * using chi-square
	 * 
	 * @param index
	 * @param attributeValue
	 * @return
	 */

	boolean isRecursionValid(int index, char attributeValue) {
		// calculate chi-square value

		double chiSquare = 0;

		Index.Value indexValue = getIndexValue(index, attributeValue);

		Index.Value indexValueA = getIndexValue(index, 'a');
		Index.Value indexValueG = getIndexValue(index, 'g');
		Index.Value indexValueC = getIndexValue(index, 'c');
		Index.Value indexValueT = getIndexValue(index, 't');

		// Case "+"

		double expectedPositiveCountA = (double) indexValueA.getTotal()
				* (double) indexValue.getPositive()
				/ (double) indexValue.getTotal();

		double expectedPositiveCountG = (double) indexValueG.getTotal()
				* (double) indexValue.getPositive()
				/ (double) indexValue.getTotal();

		double expectedPositiveCountC = (double) indexValueC.getTotal()
				* (double) indexValue.getPositive()
				/ (double) indexValue.getTotal();

		double expectedPositiveCountT = (double) indexValueT.getTotal()
				* (double) indexValue.getPositive()
				/ (double) indexValue.getTotal();

		// Case "-"

		double expectedNegativeCountA = (double) indexValueA.getTotal()
				* (double) indexValue.getNegative()
				/ (double) indexValue.getTotal();

		double expectedNegativeCountG = (double) indexValueG.getTotal()
				* (double) indexValue.getNegative()
				/ (double) indexValue.getTotal();

		double expectedNegativeCountC = (double) indexValueC.getTotal()
				* (double) indexValue.getNegative()
				/ (double) indexValue.getTotal();

		double expectedNegativeCountT = (double) indexValueT.getTotal()
				* (double) indexValue.getNegative()
				/ (double) indexValue.getTotal();

		chiSquare = ((double) indexValueA.getPositive() - expectedPositiveCountA)
				/ expectedPositiveCountA
				+ ((double) indexValueA.getNegative() - expectedNegativeCountA)
				/ expectedNegativeCountA
				+ ((double) indexValueG.getPositive() - expectedPositiveCountG)
				/ expectedPositiveCountG
				+ ((double) indexValueG.getNegative() - expectedNegativeCountG)
				/ expectedNegativeCountG
				+ ((double) indexValueC.getPositive() - expectedPositiveCountC)
				/ expectedPositiveCountC
				+ ((double) indexValueC.getNegative() - expectedNegativeCountC)
				/ expectedNegativeCountC
				+ ((double) indexValueT.getPositive() - expectedPositiveCountT)
				/ expectedPositiveCountT
				+ ((double) indexValueT.getNegative() - expectedNegativeCountT)
				/ expectedNegativeCountT;

		int dof = (2 - 1) * (4 - 1);

		if (chiSquare <= chiSquareValue || arrVisitedNodes.size() >= 56) {
			// prune the node
			return false;
		}

		else {

			return true;

		}

	}

	/**
	 * calculates information gain based on particular attribute(index here) and
	 * attribute value
	 * 
	 * @param index
	 * @return
	 */

	double calculateInformationGain(int index, char attributeValue) {

		informationGainMap.clear();
		Index.Value indValAttr = getIndexValue(index, attributeValue);

		if (infGainValue > 1)// misclassification)
		{

			for (int i = 0; i < 57; i++) {

				if (!arrVisitedNodes.contains(i)) {

					double a = getMisClassificationError(index, 'a');
					double g = getMisClassificationError(index, 'g');
					double c = getMisClassificationError(index, 'c');
					double t = getMisClassificationError(index, 't');

					Index.Value indexValueA = getIndexValue(i, 'a');
					Index.Value indexValueG = getIndexValue(i, 'g');
					Index.Value indexValueC = getIndexValue(i, 'c');
					Index.Value indexValueT = getIndexValue(i, 't');

					double informationGain = 0;

					informationGain = getMisClassificationError(index,
							attributeValue)
							- ((double) indexValueA.getTotal() / (double) noOfInstances)
							* a
							- ((double) indexValueG.getTotal() / (double) noOfInstances)
							* g
							- ((double) indexValueC.getTotal() / (double) noOfInstances)
							* c
							- ((double) indexValueT.getTotal() / (double) noOfInstances)
							* t;

					informationGainMap.put(i, informationGain);
				}

			}
		} else// entropy
		{
			for (int i = 0; i < 57; i++) {

				if (!arrVisitedNodes.contains(i)) {

					double a = getEntropyofAttribute(i, 'a');
					double g = getEntropyofAttribute(i, 'g');
					double c = getEntropyofAttribute(i, 'c');
					double t = getEntropyofAttribute(i, 't');

					Index.Value indexValueA = getIndexValue(i, 'a');
					Index.Value indexValueG = getIndexValue(i, 'g');
					Index.Value indexValueC = getIndexValue(i, 'c');
					Index.Value indexValueT = getIndexValue(i, 't');

					double informationGain = 0;

					informationGain = getDefaultEntropy(
							indValAttr.getPositive(), indValAttr.getNegative(),
							indValAttr.getTotal())
							- ((double) indexValueA.getTotal() / (double) noOfInstances)
							* a
							- ((double) indexValueG.getTotal() / (double) noOfInstances)
							* g
							- ((double) indexValueC.getTotal() / (double) noOfInstances)
							* c
							- ((double) indexValueT.getTotal() / (double) noOfInstances)
							* t;

					informationGainMap.put(i, informationGain);
				}
			}

		}
		return 0;
	}

	/**
	 * Calculates information gain for all the attribute using entropy or
	 * misclassfication
	 * 
	 * @param index
	 * @return
	 */

	double calculateInformationGain() {
		informationGainMap.clear();

		if (infGainValue > 1)// misclassification
		{
			for (int index = 0; index < 57; index++) {

				double a = getMisClassificationError(index, 'a');
				double g = getMisClassificationError(index, 'g');
				double c = getMisClassificationError(index, 'c');
				double t = getMisClassificationError(index, 't');

				Index.Value indexValueA = getIndexValue(index, 'a');
				Index.Value indexValueG = getIndexValue(index, 'g');
				Index.Value indexValueC = getIndexValue(index, 'c');
				Index.Value indexValueT = getIndexValue(index, 't');

				double informationGain = 0;

				informationGain = getDefaultMisclassificationError()
						+ (-((double) indexValueA.getTotal() / (double) noOfInstances)
								* a
								- ((double) indexValueG.getTotal() / (double) noOfInstances)
								* g
								- ((double) indexValueC.getTotal() / (double) noOfInstances)
								* c - ((double) indexValueT.getTotal() / (double) noOfInstances)
								* t);

				informationGainMap.put(index, informationGain);

			}
		} else// entropy
		{
			for (int index = 0; index < 57; index++) {

				double a = getEntropyofAttribute(index, 'a');
				double g = getEntropyofAttribute(index, 'g');
				double c = getEntropyofAttribute(index, 'c');
				double t = getEntropyofAttribute(index, 't');

				Index.Value indexValueA = getIndexValue(index, 'a');
				Index.Value indexValueG = getIndexValue(index, 'g');
				Index.Value indexValueC = getIndexValue(index, 'c');
				Index.Value indexValueT = getIndexValue(index, 't');

				double informationGain = 0;

				informationGain = getDefaultEntropy(defaultPosInstance,
						defaultNegInstance, noOfInstances)
						+ (-((double) indexValueA.getTotal() / (double) noOfInstances)
								* a
								- ((double) indexValueG.getTotal() / (double) noOfInstances)
								* g
								- ((double) indexValueC.getTotal() / (double) noOfInstances)
								* c - ((double) indexValueT.getTotal() / (double) noOfInstances)
								* t);

				informationGainMap.put(index, informationGain);

			}
		}
		return 0;
	}

	/**
	 * reads the number of positive and negative count from dnaSequenceMap
	 * 
	 * @param index
	 */
	void getPositiveNegativeCount(int index) {

		a_p = 0;
		a_n = 0;
		g_p = 0;
		g_n = 0;
		c_p = 0;
		c_n = 0;
		t_p = 0;
		t_n = 0;
		for (String dnaSequence : dnaMap.keySet()) {

			Character promoterClass = dnaMap.get(dnaSequence);
			Character ch = dnaSequence.charAt(index);

			if (promoterClass.equals('+')) {
				if (ch.equals('a')) {
					a_p++;
				}
				if (ch.equals('g')) {
					g_p++;
				}
				if (ch.equals('c')) {
					c_p++;
				}
				if (ch.equals('t')) {
					t_p++;
				}
			}

			else if (promoterClass.equals('-')) {
				if (ch.equals('a')) {
					a_n++;
				}
				if (ch.equals('g')) {
					g_n++;
				}
				if (ch.equals('c')) {
					c_n++;
				}
				if (ch.equals('t')) {
					t_n++;
				}

			}

		}

	}

	/**
	 * draws sample tree
	 */

	public void drawTree(Tree rootTree) {

		for (Tree subTree : rootTree.arrSubTrees) {
			System.out.println("Treelevel= " + subTree.getLevel()
					+ " Attribute= " + subTree.getLabel() + "  " + "cameFrom="
					+ subTree.getAttributeValue() + " parentNode= "
					+ subTree.getParentTree().getLabel() + "" + "category= "
					+ subTree.getCategory());

			if (subTree.arrSubTrees.size() > 0) {

				drawTree(subTree);
			}

		}
	}

	/**
	 * fetch data from validating file to find the accuracy rate
	 * 
	 */

	public void checkValidatingData() {

		int correctValidation = 0;
		int wrongValidation = 0;

		if (validatingFile.exists()) {
			try {
				String str = null;
				BufferedReader br = new BufferedReader(new FileReader(
						validatingFile));
				while ((str = br.readLine()) != null) {
					String dnaSequence = str.substring(0, str.length() - 2);
					Character dnaClass = str.charAt(str.length() - 1);
					// pass the sequence
					Character actualClass = getClass(dnaSequence);
					if (actualClass == dnaClass) {
						correctValidation++;
					} else {
						wrongValidation++;
					}
				}
				br.close();
				System.out.println("Chi-Square value= "+chiSquareValue);
				String methodUsed=infGainValue>1 ? "Misclassification" :"Entropy";
				System.out.println("Method used= "+ methodUsed);
				System.out.println("Accuracy rate= "
						+ (double) correctValidation * 100
						/ (double) (correctValidation + wrongValidation));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			System.out.println("doesnot exists");
		}
	}

	/**
	 * traverse through the decision tree for dnaSequence
	 * 
	 * @param dnaSequence
	 * @return
	 */

	public char getClass(String dnaSequence) {

		char[] charArray = dnaSequence.toCharArray();

		char sequenceAttributeValue = charArray[rootTree.getLabel()];
		Tree treeToTraverse = null;

		for (Tree subTree : rootTree.arrSubTrees) {
			if (sequenceAttributeValue == subTree.getAttributeValue()) {
				treeToTraverse = subTree;
			}

		}
		return traverseTree(sequenceAttributeValue, treeToTraverse, charArray);

	}

	/**
	 * Used for traversing the tree based on attribute values
	 * 
	 * @param sequenceAttributeValue
	 * @param subTree
	 * @param charArray
	 * @return
	 */

	public char traverseTree(char sequenceAttributeValue, Tree subTree,
			char[] charArray) {
		if (subTree.arrSubTrees.size() > 0)// if node is not leaf
		{
			Tree treeToTraverse = null;

			for (Tree childTree : subTree.arrSubTrees) {
				if (sequenceAttributeValue == childTree.getAttributeValue()) {
					treeToTraverse = childTree;
				}

			}
			return traverseTree(sequenceAttributeValue, treeToTraverse,
					charArray);
		} else// if is a leaf
		{
			return subTree.getCategory();
		}

	}
}
