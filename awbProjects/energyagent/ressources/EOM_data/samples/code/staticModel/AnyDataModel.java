package energy.samples.staticModel;

import java.io.Serializable;
import java.util.Vector;

/**
 * The Class AnyDataModel is just an example for a user specific data model.
 * Additionally, to the variables shown below, specific types can be used
 * as well. Make sure that your individual model implements {@link Serializable}!  
 */
public class AnyDataModel implements Serializable {

	private static final long serialVersionUID = -7860756931641086569L;
	
	private boolean myBoolean;
	private Integer myInteger;
	private Double myDouble;
	private Vector<String> myStringVector;

	
	/**
	 * Checks if is my boolean.
	 * @return the myBoolean
	 */
	public boolean isMyBoolean() {
		return myBoolean;
	}
	/**
	 * Sets the my boolean.
	 * @param myBoolean the myBoolean to set
	 */
	public void setMyBoolean(boolean myBoolean) {
		this.myBoolean = myBoolean;
	}
	
	/**
	 * Gets the my integer.
	 * @return the myInteger
	 */
	public Integer getMyInteger() {
		return myInteger;
	}
	/**
	 * Sets the my integer.
	 * @param myInteger the myInteger to set
	 */
	public void setMyInteger(Integer myInteger) {
		this.myInteger = myInteger;
	}
	
	/**
	 * Gets the my double.
	 * @return the myDouble
	 */
	public Double getMyDouble() {
		return myDouble;
	}
	/**
	 * Sets the my double.
	 * @param myDouble the myDouble to set
	 */
	public void setMyDouble(Double myDouble) {
		this.myDouble = myDouble;
	}
	
	/**
	 * Gets the my string vector.
	 * @return the myStringVector
	 */
	public Vector<String> getMyStringVector() {
		if (myStringVector==null) {
			myStringVector = new Vector<String>();
		}
		return myStringVector;
	}
	/**
	 * Sets the my string vector.
	 * @param myStringVector the myStringVector to set
	 */
	public void setMyStringVector(Vector<String> myStringVector) {
		this.myStringVector = myStringVector;
	}
	
}
