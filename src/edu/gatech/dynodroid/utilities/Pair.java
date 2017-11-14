package edu.gatech.dynodroid.utilities;

/***
 * A genric class that provides Pair Abstraction 
 * @author machiry
 *
 * @param <A> First type of the pair
 * @param <B> Second type of the pair
 */
public class Pair<A, B> {
	private A first;
	private B second;

	public Pair(A first, B second) {
		super();
		this.first = first;
		this.second = second;
	}

	@Override
	public int hashCode() {
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;

		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Pair) {
			@SuppressWarnings("unchecked")
			Pair<A, B> otherPair = (Pair<A, B>) other;
			return ((this.first == otherPair.first || (this.first != null
					&& otherPair.first != null && this.first
						.equals(otherPair.first))) && (this.second == otherPair.second || (this.second != null
					&& otherPair.second != null && this.second
						.equals(otherPair.second))));
		}

		return false;
	}

	@Override
	public String toString() {
		String firstString = first == null ? "NULL":first.toString();
		String secondString = second == null ? "NULL":second.toString();
		return "(" + firstString + "," + secondString + ")";
	}

	public A getFirst() {
		return first;
	}

	public void setFirst(A first) {
		this.first = first;
	}

	public B getSecond() {
		return second;
	}

	public void setSecond(B second) {
		this.second = second;
	}
}
