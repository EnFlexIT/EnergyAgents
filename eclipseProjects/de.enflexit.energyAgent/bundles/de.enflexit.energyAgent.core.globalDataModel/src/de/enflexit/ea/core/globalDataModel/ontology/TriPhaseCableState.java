package de.enflexit.ea.core.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: TriPhaseCableState
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class TriPhaseCableState extends CableState{ 

//////////////////////////// User code
/**
	 * Gets the first phase, creates it if necessary. Due to code generation,
	 * this behaviour cannot be implemented within the regular getter.
	 * @return the first phase
	 */
	private UniPhaseCableState getFirstPhase() {
		if (this.phase1==null) {
			this.phase1 = new UniPhaseCableState();
		}
		return this.phase1;
	}
	
	/**
	 * Gets the second phase, creates it if necessary. Due to code generation,
	 * this behaviour cannot be implemented within the regular getter.
	 * @return the second phase
	 */
	private UniPhaseCableState getSecondPhase() {
		if (this.phase2==null) {
			this.phase2 = new UniPhaseCableState();
		}
		return this.phase2;
	}
	
	/**
	 * Gets the third phase, creates it if necessary. Due to code generation,
	 * this behaviour cannot be implemented within the regular getter.
	 * @return the third phase
	 */
	private UniPhaseCableState getThirdPhase() {
		if (this.phase3==null) {
			this.phase3 = new UniPhaseCableState();
		}
		return this.phase3;
	}

// --- Old style getters and setters for backwards compatibility - L1 ------
	public void setCurrent_L1(float value) { 
		this.getFirstPhase().setCurrent(new UnitValue(value, "A"));
	}
	public float getCurrent_L1() {
		return this.getFirstPhase().getCurrent().getValue();
	}
	public void setUtil_L1(float value) { 
		this.getFirstPhase().setUtilization(value);
	}
	public float getUtil_L1() {
		return this.getFirstPhase().getUtilization();
	}
	public void setP_L1(float value) { 
		this.getFirstPhase().setP(new UnitValue(value, "W"));
	}
	public float getP_L1() {
		return this.getFirstPhase().getP().getValue();
	}
	public void setQ_L1(float value) { 
		this.getFirstPhase().setQ(new UnitValue(value, "var"));
	}
	public float getQ_L1() {
		return this.getFirstPhase().getQ().getValue();
	}
	public void setCosPhi_L1(float value) { 
		this.getFirstPhase().setCosPhi(value);
	}
	public float getCosPhi_L1() {
		return this.getFirstPhase().getCosPhi();
	}
	
	// --- Old style getters and setters for backwards compatibility - L2 ------
	public void setCurrent_L2(float value) { 
		this.getSecondPhase().setCurrent(new UnitValue(value, "A"));
	}
	public float getCurrent_L2() {
		return this.getSecondPhase().getCurrent().getValue();
	}
	public void setUtil_L2(float value) { 
		this.getSecondPhase().setUtilization(value);
	}
	public float getUtil_L2() {
		return this.getSecondPhase().getUtilization();
	}
	public void setP_L2(float value) { 
		this.getSecondPhase().setP(new UnitValue(value, "W"));
	}
	public float getP_L2() {
		return this.getSecondPhase().getP().getValue();
	}
	public void setQ_L2(float value) { 
		this.getSecondPhase().setQ(new UnitValue(value, "var"));
	}
	public float getQ_L2() {
		return this.getSecondPhase().getQ().getValue();
	}
	public void setCosPhi_L2(float value) { 
		this.getSecondPhase().setCosPhi(value);
	}
	public float getCosPhi_L2() {
		return this.getSecondPhase().getCosPhi();
	}
	
	// --- Old style getters and setters for backwards compatibility - L3 ------
	public void setCurrent_L3(float value) { 
		this.getThirdPhase().setCurrent(new UnitValue(value, "A"));
	}
	public float getCurrent_L3() {
		return this.getThirdPhase().getCurrent().getValue();
	}
	public void setUtil_L3(float value) { 
		this.getThirdPhase().setUtilization(value);
	}
	public float getUtil_L3() {
		return this.getThirdPhase().getUtilization();
	}
	public void setP_L3(float value) { 
		this.getThirdPhase().setP(new UnitValue(value, "W"));
	}
	public float getP_L3() {
		return this.getThirdPhase().getP().getValue();
	}
	public void setQ_L3(float value) { 
		this.getThirdPhase().setQ(new UnitValue(value, "var"));
	}
	public float getQ_L3() {
		return this.getThirdPhase().getQ().getValue();
	}
	public void setCosPhi_L3(float value) { 
		this.getThirdPhase().setCosPhi(value);
	}
	public float getCosPhi_L3() {
		return this.getThirdPhase().getCosPhi();
	}
   /**
* Protege name: phase1
   */
   private UniPhaseCableState phase1;
   public void setPhase1(UniPhaseCableState value) { 
    this.phase1=value;
   }
   public UniPhaseCableState getPhase1() {
     return this.phase1;
   }

   /**
* Protege name: phase3
   */
   private UniPhaseCableState phase3;
   public void setPhase3(UniPhaseCableState value) { 
    this.phase3=value;
   }
   public UniPhaseCableState getPhase3() {
     return this.phase3;
   }

   /**
* Protege name: phase2
   */
   private UniPhaseCableState phase2;
   public void setPhase2(UniPhaseCableState value) { 
    this.phase2=value;
   }
   public UniPhaseCableState getPhase2() {
     return this.phase2;
   }

}
