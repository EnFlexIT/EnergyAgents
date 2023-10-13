package de.enflexit.ea.electricity.transformer.eomDataModel;

import java.awt.Frame;
import java.io.Serializable;

import de.enflexit.ea.electricity.transformer.TransformerDataModel;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TapSide;
import energy.OptionModelController;
import energy.optionModel.gui.sysVariables.AbstractStaticModel;
import energy.optionModel.gui.sysVariables.AbstractStaticModelDialog;

/**
 * The Class TransformerStaticDataModel.
 */
public class TransformerStaticDataModel extends AbstractStaticModel {
	
	private TransformerDataModel transformerDataModel;
	
	/**
	 * Instantiates a new my static model (default constructor).
	 * @param optionModelController the option model controller
	 */	
	public TransformerStaticDataModel(OptionModelController optionModelController) {
		super(optionModelController);
	}

	/* (non-Javadoc)
	 * @see energy.optionModel.gui.sysVariables.AbstractStaticModel#getStaticDataModel()
	 */
	public Serializable getStaticDataModel() {
		return transformerDataModel;
	}
	/* (non-Javadoc)
	 * @see energy.optionModel.gui.sysVariables.AbstractStaticModel#setStaticDataModel(java.io.Serializable)
	 */
	@SuppressWarnings("deprecation")
	public void setStaticDataModel(Serializable staticModel) {
		if (staticModel instanceof de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel) {
			this.transformerDataModel = this.convertToElectricityDataModel((de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel) staticModel);
		} else {
			this.transformerDataModel = (TransformerDataModel) staticModel;
		}
	}
	
	/**
	 * Convert to electricity data model.
	 *
	 * @param oldTransformerDM the old transformer DM
	 * @return the transformer data model
	 */
	@SuppressWarnings("deprecation")
	private TransformerDataModel convertToElectricityDataModel(de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel oldTransformerDM) {
		
		// --- Convert to new TransformerDataModel class -------------------------------- 
		TransformerDataModel newTransformerDM = new TransformerDataModel();

		newTransformerDM.setLibraryID(oldTransformerDM.getLibraryID());
		
		newTransformerDM.setRatedPower_sR(oldTransformerDM.getRatedPower_sR());
		newTransformerDM.setUpperVoltage_vmHV(oldTransformerDM.getUpperVoltage_vmHV());
		newTransformerDM.setLowerVoltage_vmLV(oldTransformerDM.getLowerVoltage_vmLV());
		
		if (oldTransformerDM.getTapSide()==de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel.TapSide.HighVoltageSide) {
			newTransformerDM.setSlackNodeSide(TapSide.HighVoltageSide);
		} else {
			newTransformerDM.setSlackNodeSide(TapSide.LowVoltageSide);
		}
		newTransformerDM.setSlackNodeVoltageLevel(oldTransformerDM.getSlackNodeVoltageLevel());
		
		newTransformerDM.setUpperVoltage_ThriPhase(oldTransformerDM.isUpperVoltage_ThriPhase()); 
		newTransformerDM.setLowerVoltage_ThriPhase(oldTransformerDM.isLowerVoltage_ThriPhase());
		
		newTransformerDM.setPhaseShift_va0(oldTransformerDM.getPhaseShift_va0());
		newTransformerDM.setShortCircuitImpedance_vmImp(oldTransformerDM.getShortCircuitImpedance_vmImp());
		
		newTransformerDM.setCopperLosses_pCu(oldTransformerDM.getCopperLosses_pCu()); 
		newTransformerDM.setIronLosses_pFe(oldTransformerDM.getIronLosses_pFe());
			
		newTransformerDM.setIdleImpedance_iNoLoad(oldTransformerDM.getIdleImpedance_iNoLoad());
		
		newTransformerDM.setTapable(oldTransformerDM.isTapable());
		if (oldTransformerDM.getTapSide()==de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel.TapSide.HighVoltageSide) {
			newTransformerDM.setTapSide(TapSide.HighVoltageSide);
		} else {
			newTransformerDM.setTapSide(TapSide.LowVoltageSide);
		}
		
		newTransformerDM.setVoltageDeltaPerTap_dVm(oldTransformerDM.getVoltageDeltaPerTap_dVm());
		newTransformerDM.setPhaseShiftPerTap_dVa(oldTransformerDM.getPhaseShiftPerTap_dVa());
		
		newTransformerDM.setTapNeutral(oldTransformerDM.getTapNeutral());
		newTransformerDM.setTapMinimum(oldTransformerDM.getTapMinimum());
		newTransformerDM.setTapMaximum(oldTransformerDM.getTapMaximum());
		
		newTransformerDM.setNumberOfViolationsToActivateController(oldTransformerDM.getNumberOfViolationsToActivateController());
		
		newTransformerDM.setControlBasedOnNodeVoltage(oldTransformerDM.isControlBasedOnNodeVoltage());
		newTransformerDM.setControlNodeID(oldTransformerDM.getControlNodeID());
		newTransformerDM.setControlNodeUpperVoltageLevel(oldTransformerDM.getControlNodeUpperVoltageLevel());
		newTransformerDM.setControlNodeLowerVoltageLevel(oldTransformerDM.getControlNodeLowerVoltageLevel());
		
		newTransformerDM.setControlBasedOnCharacteristics(oldTransformerDM.isControlBasedOnCharacteristics());
		newTransformerDM.setControlCharacteristicsAllowedDeviation(oldTransformerDM.getControlCharacteristicsAllowedDeviation());
		newTransformerDM.setControlCharacteristicsXySeries(oldTransformerDM.getControlCharacteristicsXySeries());
		
		return newTransformerDM;
	}
	
	/* (non-Javadoc)
	 * @see energy.optionModel.gui.sysVariables.AbstractStaticModel#getNewModelDialog(java.awt.Frame)
	 */
	@Override
	public AbstractStaticModelDialog getNewModelDialog(Frame owner) {
		return new JDialogTransformerDataModel(owner, this);
	}

	
}
