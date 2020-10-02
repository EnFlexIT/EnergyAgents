package de.enflexit.ea.core.dataModel.blackboard;

import java.util.Vector;

/**
 * The Class MultipleBlackboardAnswer.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class MultipleBlackboardAnswer extends AbstractBlackboardAnswer {

	private static final long serialVersionUID = -5055204723904200895L;

	private Vector<AbstractBlackboardAnswer> answerVector;

	
	/**
	 * Instantiates a new multiple blackboard answer.
	 * @param answerVector the answer vector
	 */
	public MultipleBlackboardAnswer(Vector<AbstractBlackboardAnswer> answerVector) {
		this.setAnswerVector(answerVector);
	}
	
	/**
	 * Returns the answer vector.
	 * @return the answer vector
	 */
	public Vector<AbstractBlackboardAnswer> getAnswerVector() {
		if (answerVector==null) {
			answerVector = new Vector<>();
		}
		return answerVector;
	}
	/**
	 * Sets the answer vector.
	 * @param answerVector the new answer vector
	 */
	public void setAnswerVector(Vector<AbstractBlackboardAnswer> answerVector) {
		this.answerVector = answerVector;
	}
	
}
