package de.enflexit.ea.electricity.transformer.eomDataModel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import de.enflexit.common.swing.AwbBasicTabbedPaneUI;
import de.enflexit.common.swing.JDialogSizeAndPostionController;
import de.enflexit.common.swing.JDialogSizeAndPostionController.JDialogPosition;
import energy.optionModel.gui.sysVariables.AbstractStaticModel;
import energy.optionModel.gui.sysVariables.AbstractStaticModelDialog;

/**
 * The Class TransformerStaticModelDialog.
 * 
 * @author Christian Derksen - SOFTEC - University Duisburg-Essen
 */
public class JDialogTransformerDataModel extends AbstractStaticModelDialog implements ActionListener {

	private static final long serialVersionUID = 8027574099572403096L;

	private TransformerDataModel transformerDataModel;
	
	private boolean isCanceledEditing = true;

	private JTabbedPane jTabbedPaneStaticModel;
	private JPanelTransformerBaseSettings jPanelTransformerBaseSettings;
	private JPanelTransformerControlSettings jPanelTransformerControlSettings;
	
	private JPanel jPanelButtons;
	private JButton jButtonOk;
	private JButton jButtonCancel;
	
	
	/**
	 * Instantiates a new transformer static model dialog.
	 *
	 * @param owner the owner
	 * @param staticModel the static model
	 */
	public JDialogTransformerDataModel(Frame owner, AbstractStaticModel staticModel) {
		super(owner, staticModel);
		this.setTransformerDataModel((TransformerDataModel) staticModel.getStaticDataModel());
		this.initialize();
		this.loadDataModelToDialog();
	}
	private void initialize(){
		
		this.setTitle("Transformer Configuration");
		this.setSize(620, 580);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{564, 0};
		gridBagLayout.rowHeights = new int[]{450, 51, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		GridBagConstraints gbc_jTabbedPaneStaticModel = new GridBagConstraints();
		gbc_jTabbedPaneStaticModel.insets = new Insets(10, 10, 0, 10);
		gbc_jTabbedPaneStaticModel.fill = GridBagConstraints.BOTH;
		gbc_jTabbedPaneStaticModel.gridx = 0;
		gbc_jTabbedPaneStaticModel.gridy = 0;
		this.getContentPane().add(this.getJTabbedPaneStaticModel(), gbc_jTabbedPaneStaticModel);
		
		GridBagConstraints gbc_jPanelButtons = new GridBagConstraints();
		gbc_jPanelButtons.anchor = GridBagConstraints.NORTH;
		gbc_jPanelButtons.fill = GridBagConstraints.HORIZONTAL;
		gbc_jPanelButtons.gridx = 0;
		gbc_jPanelButtons.gridy = 1;
		this.getContentPane().add(this.getJPanelButtons(), gbc_jPanelButtons);
		
		JDialogSizeAndPostionController.setJDialogPositionOnScreen(this, JDialogPosition.ParentCenter);
	}
	
	
	/**
	 * Returns the JTabbedPane for the static model configuration.
	 * @return the j tabbed pane static model
	 */
	private JTabbedPane getJTabbedPaneStaticModel() {
		if (jTabbedPaneStaticModel == null) {
			jTabbedPaneStaticModel = new JTabbedPane(JTabbedPane.TOP);
			jTabbedPaneStaticModel.setUI(new AwbBasicTabbedPaneUI());
			jTabbedPaneStaticModel.setFont(new Font("Dialog", Font.BOLD, 12));
			jTabbedPaneStaticModel.addTab("Base Settings", null, getJPanelTransformerBaseSettings(), null);
			jTabbedPaneStaticModel.addTab("Control Settings", null, getJPanelTransformerControlSettings(), null);
		}
		return jTabbedPaneStaticModel;
	}
	private JPanelTransformerBaseSettings getJPanelTransformerBaseSettings() {
		if (jPanelTransformerBaseSettings == null) {
			jPanelTransformerBaseSettings = new JPanelTransformerBaseSettings(this);
		}
		return jPanelTransformerBaseSettings;
	}
	private JPanelTransformerControlSettings getJPanelTransformerControlSettings() {
		if (jPanelTransformerControlSettings == null) {
			jPanelTransformerControlSettings = new JPanelTransformerControlSettings(this);
		}
		return jPanelTransformerControlSettings;
	}
	
	
	/**
	 * Return the JPanel with the buttons.
	 * @return the j panel buttons
	 */
	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			GridBagLayout gbl_jPanelButtons = new GridBagLayout();
			gbl_jPanelButtons.columnWidths = new int[]{0, 0, 0};
			gbl_jPanelButtons.rowHeights = new int[]{0, 0};
			gbl_jPanelButtons.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
			gbl_jPanelButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			jPanelButtons.setLayout(gbl_jPanelButtons);
			GridBagConstraints gbc_jButtonOk = new GridBagConstraints();
			gbc_jButtonOk.insets = new Insets(10, 10, 15, 0);
			gbc_jButtonOk.gridx = 0;
			gbc_jButtonOk.gridy = 0;
			jPanelButtons.add(getJButtonOk(), gbc_jButtonOk);
			GridBagConstraints gbc_jButtonCancel = new GridBagConstraints();
			gbc_jButtonCancel.insets = new Insets(10, 0, 15, 10);
			gbc_jButtonCancel.gridx = 1;
			gbc_jButtonCancel.gridy = 0;
			jPanelButtons.add(getJButtonCancel(), gbc_jButtonCancel);
		}
		return jPanelButtons;
	}
	private JButton getJButtonOk() {
		if (jButtonOk == null) {
			jButtonOk = new JButton("OK");
			jButtonOk.setPreferredSize(new Dimension(85, 26));
			jButtonOk.setForeground(new Color(0, 153, 0));
			jButtonOk.setFont(new Font("Dialog", Font.BOLD, 11));
			jButtonOk.addActionListener(this);
		}
		return jButtonOk;
	}
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton("Cancel");
			jButtonCancel.setPreferredSize(new Dimension(85, 26));
			jButtonCancel.setForeground(new Color(153, 0, 0));
			jButtonCancel.setFont(new Font("Dialog", Font.BOLD, 11));
			jButtonCancel.addActionListener(this);
		}
		return jButtonCancel;
	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		if (ae.getSource()==getJButtonOk()) {
			// --- The OK - save action -----------------------------
			this.loadDialogDataToDataModel();
			this.isCanceledEditing = false;
			this.dispose();
			
		} else if (ae.getSource()==getJButtonCancel()) {
			// --- The cancel action --------------------------------
			this.isCanceledEditing = true;
			this.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see energy.optionModel.gui.sysVariables.AbstractStaticModelDialog#isCanceled()
	 */
	@Override
	public boolean isCanceled() {
		return this.isCanceledEditing;
	}
	
	
	/**
	 * Returns the current transformer data model.
	 * @return the transformer data model
	 */
	public TransformerDataModel getTransformerDataModel() {
		if (transformerDataModel == null){
			transformerDataModel = new TransformerDataModel();
			staticModel.setStaticDataModel(transformerDataModel);
		}
		return transformerDataModel;
	}
	/**
	 * Sets the transformer data model.
	 * @param transformerDataModel the new transformer data model
	 */
	public void setTransformerDataModel(TransformerDataModel transformerDataModel) {
		this.transformerDataModel = transformerDataModel;
	}
	
	/**
	 * Load the data model to the dialog.
	 */
	private void loadDataModelToDialog(){
		this.getJPanelTransformerBaseSettings().loadDataModelToDialog();
		this.getJPanelTransformerControlSettings().loadDataModelToDialog();
	}
	/**
	 * Loads the dialog data to the data model.
	 */
	private void loadDialogDataToDataModel() {
		this.getJPanelTransformerBaseSettings().loadDialogDataToDataModel();
		this.getJPanelTransformerControlSettings().loadDialogDataToDataModel();
	}
	
	
	/**
	 * Returns the integer value from the specified JTextField.
	 *
	 * @param textField the text field
	 * @return the integer value
	 */
	public int getIntegerValue(JTextField textField) {
		int num = 0;
		String content = this.getTextFieldContent(textField);
		if (content!=null) {
			try {
				num = Integer.parseInt(content);
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		}
		return num;
	}
	/**
	 * Returns the double value from the specified JTextField.
	 *
	 * @param textField the text field
	 * @return the numeric value
	 */
	public double getDoubleValue(JTextField textField) {
		double num = 0.0;
		String content = this.getTextFieldContent(textField);
		if (content!=null) {
			try {
				num = Double.parseDouble(content);
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		}
		return num;
	}
	/**
	 * Gets the text field content.
	 *
	 * @param textField the text field
	 * @return the text field content
	 */
	public String getTextFieldContent(JTextField textField) {
		String content = null;
		if (textField.getText()!=null && textField.getText().equals("")==false) {
			content = textField.getText().trim();
		}
		return content;
	}
}
