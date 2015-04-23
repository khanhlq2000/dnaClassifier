/**
 * Its object is used to store the positive counts and negative counts of particular attribute value
 * of particular index
 * 
 * @author niranjanhumagain
 *
 */
public class Index {

	/**
	 * class to use as a key
	 * 
	 * @author niranjanhumagain
	 *
	 */
	class Key {
		private int index;
		private char attributeValue;

		public Key(int index, char av) {
			this.index = index;
			this.attributeValue = av;
		}

		@Override
		public boolean equals(Object o) {
			Key other = (Key) o;
			return (this.index == other.index & this.attributeValue == other.attributeValue);
		}

	}

	/**
	 * class to use as a value
	 * 
	 * @author niranjanhumagain
	 *
	 */

	class Value

	{
		private int positive;
		private int negative;
		private char attributeValue;

		public Value(int pos, int neg, char attributeValue) {
			this.positive = pos;
			this.negative = neg;
			this.attributeValue = attributeValue;

		}

		public int getPositive() {
			return this.positive;
		}

		public int getNegative() {
			return this.negative;

		}

		public char getAttributeValue() {
			return this.attributeValue;
		}

		public int getTotal() {
			return this.positive + this.negative;
		}
	}

}
