package de.enflexit.ea.electricity.transformer.eomDataModel;

import java.awt.Frame;
import java.io.Serializable;

import de.enflexit.ea.electricity.transformer.TransformerDataModel;
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
	public void setStaticDataModel(Serializable staticModel) {
		this.transformerDataModel = (TransformerDataModel) staticModel;
	}
	/* (non-Javadoc)
	 * @see energy.optionModel.gui.sysVariables.AbstractStaticModel#getNewModelDialog(java.awt.Frame)
	 */
	@Override
	public AbstractStaticModelDialog getNewModelDialog(Frame owner) {
		return new JDialogTransformerDataModel(owner, this);
	}

	
}
