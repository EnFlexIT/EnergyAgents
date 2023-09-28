package de.enflexit.ea.electricity.transformer;

import java.io.Serializable;

import org.jfree.data.xy.XYSeries;

import energy.optionModel.SystemVariableDefinitionBoolean;
import energy.optionModel.SystemVariableDefinitionInteger;
import energy.optionModel.SystemVariableDefinitionDouble;

/**
 * The Class TransformerDataModel represents the Serializable type to configure a transformer .
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class TransformerDataModel implements Serializable {

	private static final long serialVersionUID = 2852720060911906256L;

	public enum LowVoltageThriPhase {
		LV_P1("LV-P1"),
		LV_P2("LV-P2"),
		LV_P3("LV-P3"),
		LV_Q1("LV-Q1"),
		LV_Q2("LV-Q2"),
		LV_Q3("LV-Q3");

		private final String interfaceID;
		private LowVoltageThriPhase(String interfaceID) {
			this.interfaceID = interfaceID;
		}
		public String getInterfaceID() {
			return interfaceID;
		}
	}
	public enum LowVoltageUniPhase {
		LV_P(("LV-P")),
		LV_Q("LV-Q");
		
		private final String interfaceID;
		private LowVoltageUniPhase(String interfaceID) {
			this.interfaceID = interfaceID;
		}
		public String getInterfaceID() {
			return interfaceID;
		}
	}
	
	public enum HighVoltageThriPhase {
		HV_P1("HV-P1"),
		HV_P2("HV-P2"),
		HV_P3("HV-P3"),
		HV_Q1("HV-Q1"),
		HV_Q2("HV-Q2"),
		HV_Q3("HV-Q3");
		
		private final String interfaceID;
		private HighVoltageThriPhase(String interfaceID) {
			this.interfaceID = interfaceID;
		}
		public String getInterfaceID() {
			return interfaceID;
		}
	}
	public enum HighVoltageUniPhase {
		HV_P(("HV-P")),
		HV_Q("HV-Q");
		
		private final String interfaceID;
		private HighVoltageUniPhase(String interfaceID) {
			this.interfaceID = interfaceID;
		}
		public String getInterfaceID() {
			return interfaceID;
		}
	}
	
	
	public enum TransformerSystemVariable {
		
		hvVoltageRealAllPhases(SystemVariableDefinitionDouble.class.getName(), "HV Voltage Real", "V"),
		hvVoltageImagAllPhases(SystemVariableDefinitionDouble.class.getName(), "HV Voltage Imaginary", "V"),
		
		tapPos(SystemVariableDefinitionInteger.class.getName(), "Tap position set-point", "-"),
		
		lvVoltageRealAllPhases(SystemVariableDefinitionDouble.class.getName(), "LV Voltage Real", "V"), 
		lvVoltageImagAllPhases(SystemVariableDefinitionDouble.class.getName(), "LV Voltage Imaginary", "V"),
		
		lvVoltageRealL1(SystemVariableDefinitionDouble.class.getName(), "LV Voltage L1 Real", "V"), 
		lvVoltageImagL1(SystemVariableDefinitionDouble.class.getName(), "LV Voltage L1 Imaginary", "V"),
		lvVoltageRealL2(SystemVariableDefinitionDouble.class.getName(), "LV Voltage L2 Real", "V"), 
		lvVoltageImagL2(SystemVariableDefinitionDouble.class.getName(), "LV Voltage L2 Imaginary", "V"),
		lvVoltageRealL3(SystemVariableDefinitionDouble.class.getName(), "LV Voltage L3 Real", "V"), 
		lvVoltageImagL3(SystemVariableDefinitionDouble.class.getName(), "LV Voltage L3 Imaginary", "V"),
		
		voltageViolation(SystemVariableDefinitionBoolean.class.getName(), "Voltage Violation", "-"),
		
		tUtil(SystemVariableDefinitionDouble.class.getName(), "Trafo Utilization", "-"),
		
		tLossesPAllPhases(SystemVariableDefinitionDouble.class.getName(), "Trafo Losses P", "W"),
		tLossesQAllPhases(SystemVariableDefinitionDouble.class.getName(), "Trafo Losses Q", "var"),
		
		tLossesPL1(SystemVariableDefinitionDouble.class.getName(), "Trafo Losses P L1", "W"),
		tLossesQL1(SystemVariableDefinitionDouble.class.getName(), "Trafo Losses Q L1", "var"),
		tLossesPL2(SystemVariableDefinitionDouble.class.getName(), "Trafo Losses P L2", "W"),
		tLossesQL2(SystemVariableDefinitionDouble.class.getName(), "Trafo Losses Q L2", "var"),
		tLossesPL3(SystemVariableDefinitionDouble.class.getName(), "Trafo Losses P L3", "W"),
		tLossesQL3(SystemVariableDefinitionDouble.class.getName(), "Trafo Losses Q L3", "var"),
		
		lvTotaCurrentRealAllPhases(SystemVariableDefinitionDouble.class.getName(), "LV Total Real Current", "A"),
		lvTotaCurrentImagAllPhases(SystemVariableDefinitionDouble.class.getName(), "LV Total Imaginary Current", "A"),
		
		lvTotaCurrentRealL1(SystemVariableDefinitionDouble.class.getName(), "LV L1 Total Real Current", "A"),
		lvTotaCurrentRealL2(SystemVariableDefinitionDouble.class.getName(), "LV L2 Total Real Current", "A"),
		lvTotaCurrentRealL3(SystemVariableDefinitionDouble.class.getName(), "LV L3 Total Real Current", "A"),
		
		lvTotaCurrentImagL1(SystemVariableDefinitionDouble.class.getName(), "LV L1 Total Imaginary Current", "A"),
		lvTotaCurrentImagL2(SystemVariableDefinitionDouble.class.getName(), "LV L2 Total Imaginary Current", "A"),
		lvTotaCurrentImagL3(SystemVariableDefinitionDouble.class.getName(), "LV L3 Total Imaginary Current", "A"),
		
		cnVoltageReal(SystemVariableDefinitionDouble.class.getName(), "Control Node Voltage Real", "V"),
		cnVoltageImag(SystemVariableDefinitionDouble.class.getName(), "Control Node Voltage Imaginary", "V");
		
		
		private final String sysVarClassName;
		private final String description;
		private final String unit;
		
		private TransformerSystemVariable(String sysVarClassName, String description, String unit) {
			this.sysVarClassName = sysVarClassName;
			this.description = description;
			this.unit = unit;
		}
		
		public String getSystemVarialbleClassName() {
			return this.sysVarClassName;
		}
		public String getDescription() {
			return this.description;
		}
		public String getUnit() {
			return this.unit;
		}
		
	}
	
	public enum TapSide {
		HighVoltageSide,
		LowVoltageSide
	}
	
	
	private String libraryID;
	
	private double ratedPower_sR;
	private double upperVoltage_vmHV;
	private double lowerVoltage_vmLV;
	
	private TapSide slackNodeSide;
	private Double slackNodeVoltageLevel;
	
	private boolean upperVoltage_ThriPhase; 
	private boolean lowerVoltage_ThriPhase;
	
	private double phaseShift_va0;
	private double shortCircuitImpedance_vmImp;
	
	private double copperLosses_pCu; 
	private double ironLosses_pFe;
		
	private double idleImpedance_iNoLoad;
	
	private boolean tapable;
	private TapSide tapSide;
	
	private double voltageDeltaPerTap_dVm;
	private double phaseShiftPerTap_dVa;
	
	private int tapNeutral;
	private int tapMinimum;
	private int tapMaximum;
	
	private double numberOfViolationsToActivateController;
	
	private boolean controlBasedOnNodeVoltage;
	private String controlNodeID;
	private double controlNodeUpperVoltageLevel;
	private double controlNodeLowerVoltageLevel;
	
	private boolean controlBasedOnCharacteristics;
	private double controlCharacteristicsAllowedDeviation;
	private XYSeries controlCharacteristicsXySeries;
	private transient TransformerCharacteristicsHandler characteristicsHandler;
	
	
	
	public String getLibraryID() {
		return libraryID;
	}
	public void setLibraryID(String libraryID) {
		this.libraryID = libraryID;
	}

	
	public double getRatedPower_sR() {
		return ratedPower_sR;
	}
	public void setRatedPower_sR(double ratedPower) {
		this.ratedPower_sR = ratedPower;
	}

	
	public double getUpperVoltage_vmHV() {
		return upperVoltage_vmHV;
	}
	public void setUpperVoltage_vmHV(double upperVoltage_vmHV) {
		this.upperVoltage_vmHV = upperVoltage_vmHV;
	}
		
	public double getLowerVoltage_vmLV() {
		return lowerVoltage_vmLV;
	}
	public void setLowerVoltage_vmLV(double lowerVoltage_vmLV) {
		this.lowerVoltage_vmLV = lowerVoltage_vmLV;
	}
	
	
	public boolean isUpperVoltage_ThriPhase() {
		return upperVoltage_ThriPhase;
	}
	public void setUpperVoltage_ThriPhase(boolean upperVoltage_ThriPhase) {
		this.upperVoltage_ThriPhase = upperVoltage_ThriPhase;
	}
	public boolean isLowerVoltage_ThriPhase() {
		return lowerVoltage_ThriPhase;
	}
	public void setLowerVoltage_ThriPhase(boolean lowerVoltage_ThriPhase) {
		this.lowerVoltage_ThriPhase = lowerVoltage_ThriPhase;
	}
	
	
	public TapSide getSlackNodeSide() {
		if (slackNodeSide==null) {
			slackNodeSide = TapSide.LowVoltageSide;
		}
		return slackNodeSide;
	}
	public void setSlackNodeSide(TapSide slackNodeSide) {
		this.slackNodeSide = slackNodeSide;
	}
	public double getSlackNodeVoltageLevel() {
		if (slackNodeVoltageLevel==null) {
			switch (this.getSlackNodeSide()) {
			case HighVoltageSide:
				slackNodeVoltageLevel = this.getUpperVoltage_vmHV() * 1000.0;
				break;
			case LowVoltageSide:
				slackNodeVoltageLevel = this.getLowerVoltage_vmLV() * 1000.0;
				break;
			}
		}
		return slackNodeVoltageLevel;
	}
	public void setSlackNodeVoltageLevel(double slackNodeVoltageLevel) {
		this.slackNodeVoltageLevel = slackNodeVoltageLevel;
	}
	
	
	public double getPhaseShift_va0() {
		return phaseShift_va0;
	}
	public void setPhaseShift_va0(double phaseShift_va0) {
		this.phaseShift_va0 = phaseShift_va0;
	}
	
	public double getShortCircuitImpedance_vmImp() {
		return shortCircuitImpedance_vmImp;
	}
	public void setShortCircuitImpedance_vmImp(double shortCircuitImpedance_vmImp) {
		this.shortCircuitImpedance_vmImp = shortCircuitImpedance_vmImp;
	}
	
	
	public double getCopperLosses_pCu() {
		return copperLosses_pCu;
	}
	public void setCopperLosses_pCu(double copperLosses_pCu) {
		this.copperLosses_pCu = copperLosses_pCu;
	}
	
	public double getIronLosses_pFe() {
		return ironLosses_pFe;
	}
	public void setIronLosses_pFe(double ironLosses_pFe) {
		this.ironLosses_pFe = ironLosses_pFe;
	}
	
	
	public double getIdleImpedance_iNoLoad() {
		return idleImpedance_iNoLoad;
	}
	public void setIdleImpedance_iNoLoad(double idleImpedance) {
		this.idleImpedance_iNoLoad = idleImpedance;
	}
	
	
	public boolean isTapable() {
		return tapable;
	}
	public void setTapable(boolean tapable) {
		this.tapable = tapable;
	}
	
	public TapSide getTapSide() {
		return tapSide;
	}
	public void setTapSide(TapSide tapSide) {
		this.tapSide = tapSide;
	}
		
	
	public double getVoltageDeltaPerTap_dVm() {
		return voltageDeltaPerTap_dVm;
	}
	public void setVoltageDeltaPerTap_dVm(double voltageDeltaPerTap_dVm) {
		this.voltageDeltaPerTap_dVm = voltageDeltaPerTap_dVm;
	}
	
	public double getPhaseShiftPerTap_dVa() {
		return phaseShiftPerTap_dVa;
	}
	public void setPhaseShiftPerTap_dVa(double phaseShiftPerTap_dVa) {
		this.phaseShiftPerTap_dVa = phaseShiftPerTap_dVa;
	}
	
	
	public int getTapNeutral() {
		return tapNeutral;
	}
	public void setTapNeutral(int tapNeutral) {
		this.tapNeutral = tapNeutral;
	}
	public int getTapMinimum() {
		return tapMinimum;
	}
	public void setTapMinimum(int tapMinimum) {
		this.tapMinimum = tapMinimum;
	}
	public int getTapMaximum() {
		return tapMaximum;
	}
	public void setTapMaximum(int tapMaximum) {
		this.tapMaximum = tapMaximum;
	}
		
	
	public double getNumberOfViolationsToActivateController() {
		return numberOfViolationsToActivateController;
	}
	public void setNumberOfViolationsToActivateController(double numberOfViolationsToActivateController) {
		this.numberOfViolationsToActivateController = numberOfViolationsToActivateController;
	}
	
	
	public boolean isControlBasedOnNodeVoltage() {
		return controlBasedOnNodeVoltage;
	}
	public void setControlBasedOnNodeVoltage(boolean controlBasedOnNodeVoltage) {
		this.controlBasedOnNodeVoltage = controlBasedOnNodeVoltage;
	}
	
	public String getControlNodeID() {
		return controlNodeID;
	}
	public void setControlNodeID(String controlNodeID) {
		this.controlNodeID = controlNodeID;
	}
	
	public double getControlNodeUpperVoltageLevel() {
		return controlNodeUpperVoltageLevel;
	}
	public void setControlNodeUpperVoltageLevel(double controlNodeUpperVoltageLevel) {
		this.controlNodeUpperVoltageLevel = controlNodeUpperVoltageLevel;
	}
	
	public double getControlNodeLowerVoltageLevel() {
		return controlNodeLowerVoltageLevel;
	}
	public void setControlNodeLowerVoltageLevel(double controlNodeLowerVoltageLevel) {
		this.controlNodeLowerVoltageLevel = controlNodeLowerVoltageLevel;
	}
	

	public boolean isControlBasedOnCharacteristics() {
		return controlBasedOnCharacteristics;
	}
	public void setControlBasedOnCharacteristics(boolean controlBasedOnCharacteristics) {
		this.controlBasedOnCharacteristics = controlBasedOnCharacteristics;
	}
	
	public double getControlCharacteristicsAllowedDeviation() {
		return controlCharacteristicsAllowedDeviation;
	}
	public void setControlCharacteristicsAllowedDeviation(double AllowedDeviation) {
		this.controlCharacteristicsAllowedDeviation = AllowedDeviation;
	}
	
	public XYSeries getControlCharacteristicsXySeries() {
		return controlCharacteristicsXySeries;
	}
	public void setControlCharacteristicsXySeries(XYSeries controlCharacteristicsXySeries) {
		this.controlCharacteristicsXySeries = controlCharacteristicsXySeries;
	}
	public TransformerCharacteristicsHandler getTransformerCharacteristicsHandler() {
		if (characteristicsHandler==null) {
			characteristicsHandler = new TransformerCharacteristicsHandler(this);
		}
		return characteristicsHandler;
	}
	
	
}


