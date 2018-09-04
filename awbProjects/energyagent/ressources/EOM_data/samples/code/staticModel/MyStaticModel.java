package energy.samples.staticModel;

import java.awt.Frame;
import java.io.Serializable;

import energy.OptionModelController;
import energy.optionModel.gui.sysVariables.AbstractStaticModel;
import energy.optionModel.gui.sysVariables.AbstractStaticModelDialog;

/**
 * The Class MyStaticModel represents an example for an adapter to
 * an individual static data model.
 * 
 * @see AnyDataModel
 * @see MyStaticModelDialog
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class MyStaticModel extends AbstractStaticModel {

	private AnyDataModel anyDataModel;
	
	/**
	 * Instantiates a new my static model (default constructor).
	 * @param optionModelController the option model controller
	 */
	public MyStaticModel(OptionModelController optionModelController) {
		super(optionModelController);
	}
	
	/* (non-Javadoc)
	 * @see energy.optionModel.gui.sysVariables.AbstractStaticModel#getStaticDataModel()
	 */
	@Override
	public Serializable getStaticDataModel() {
		return anyDataModel;
	}
	/* (non-Javadoc)
	 * @see energy.optionModel.gui.sysVariables.AbstractStaticModel#setStaticDataModel(java.io.Serializable)
	 */
	@Override
	public void setStaticDataModel(Serializable staticModel) {
		this.anyDataModel = (AnyDataModel) staticModel;
	}
	
	/* (non-Javadoc)
	 * @see energy.optionModel.gui.sysVariables.AbstractStaticModel#getNewModelDialog(java.awt.Frame)
	 */
	@Override
	public AbstractStaticModelDialog getNewModelDialog(Frame owner) {
		return new MyStaticModelDialog(owner, this);
	}

}
