package de.enflexit.ea.core.globalDataModel.blackboard;

import java.util.Vector;

/**
 * The Class MultipleBlackboardAnswer.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class MultipleBlackboardAnswer extends AbstractBlackoardAnswer {

	private static final long serialVersionUID = -5055204723904200895L;

	private Vector<AbstractBlackoardAnswer> answerVector;

	
	/**
	 * Instantiates a new multiple blackboard answer.
	 * @param answerVector the answer vector
	 */
	public MultipleBlackboardAnswer(Vector<AbstractBlackoardAnswer> answerVector) {
		this.setAnswerVector(answerVector);
	}
	
	/**
	 * Returns the answer vector.
	 * @return the answer vector
	 */
	public Vector<AbstractBlackoardAnswer> getAnswerVector() {
		if (answerVector==null) {
			answerVector = new Vector<>();
		}
		return answerVector;
	}
	/**
	 * Sets the answer vector.
	 * @param answerVector the new answer vector
	 */
	public void setAnswerVector(Vector<AbstractBlackoardAnswer> answerVector) {
		this.answerVector = answerVector;
	}
	
}
