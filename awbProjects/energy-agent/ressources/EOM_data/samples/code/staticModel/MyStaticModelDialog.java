package energy.samples.staticModel;

import energy.optionModel.gui.common.KeyAdapter4Numbers;
import energy.optionModel.gui.sysVariables.AbstractStaticModel;
import energy.optionModel.gui.sysVariables.AbstractStaticModelDialog;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;

import java.awt.Insets;

import javax.swing.DefaultListModel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JList;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * This is an example class for an editable visualisation of 
 * the individual static data model {@link AnyDataModel}.<br>
 * This example was build with the Window Builder und Eclipse.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class MyStaticModelDialog extends AbstractStaticModelDialog implements ActionListener, DocumentListener {

	private static final long serialVersionUID = 738502194747789611L;

	private AnyDataModel anyDataModel;
	
	private JLabel jLabelMyHeader;
	private JLabel jLabelMyCheckbox;
	private JCheckBox jCheckBoxMyboolean;
	
	private JLabel jLabelMyInteger;
	private JTextField jTextFieldMyInteger;
	private JLabel jLabelMyDouble;
	private JTextField jTextFieldMyDouble;
	
	private JLabel jLabelVectorValue;
	private JTextField jTextFieldVectorValue;
	private JButton jButtonAddConfig;
	private JButton jButtonRemoveConfig;

	private JLabel jLabelMyVector;
	private JScrollPane jScrollPaneVectorData;
	private DefaultListModel<String> myListModel;
	private JList<String> jListVectorData;

	
	/**
	 * Instantiates my static model dialog.
	 * @param staticModel the current static model that will be assigned to protected local variable 'staticModel'
	 */
	public MyStaticModelDialog(Frame owner, AbstractStaticModel staticModel) {
		super(owner, staticModel);
		this.setAnyDataModel((AnyDataModel) staticModel.getStaticDataModel());
		this.initialize();
		this.loadDataModelToDialog();
	}
	
	private void initialize() {
		
		this.setTitle("MyStaticDataModelDialog - An Example!");
		this.setSize(500, 400);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		GridBagConstraints gbc_jLabelMyHeader = new GridBagConstraints();
		gbc_jLabelMyHeader.anchor = GridBagConstraints.WEST;
		gbc_jLabelMyHeader.gridwidth = 2;
		gbc_jLabelMyHeader.insets = new Insets(10, 10, 10, 10);
		gbc_jLabelMyHeader.gridx = 0;
		gbc_jLabelMyHeader.gridy = 0;
		GridBagConstraints gbc_jLabelMyCheckbox = new GridBagConstraints();
		gbc_jLabelMyCheckbox.anchor = GridBagConstraints.WEST;
		gbc_jLabelMyCheckbox.insets = new Insets(0, 10, 5, 5);
		gbc_jLabelMyCheckbox.gridx = 0;
		gbc_jLabelMyCheckbox.gridy = 1;
		GridBagConstraints gbc_jCheckBoxMyboolean = new GridBagConstraints();
		gbc_jCheckBoxMyboolean.anchor = GridBagConstraints.WEST;
		gbc_jCheckBoxMyboolean.insets = new Insets(0, 0, 5, 5);
		gbc_jCheckBoxMyboolean.gridx = 1;
		gbc_jCheckBoxMyboolean.gridy = 1;
		GridBagConstraints gbc_jLabelMyInteger = new GridBagConstraints();
		gbc_jLabelMyInteger.insets = new Insets(0, 10, 5, 5);
		gbc_jLabelMyInteger.anchor = GridBagConstraints.WEST;
		gbc_jLabelMyInteger.gridx = 0;
		gbc_jLabelMyInteger.gridy = 2;
		GridBagConstraints gbc_jTextFieldMyInteger = new GridBagConstraints();
		gbc_jTextFieldMyInteger.gridwidth = 3;
		gbc_jTextFieldMyInteger.insets = new Insets(0, 0, 5, 10);
		gbc_jTextFieldMyInteger.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldMyInteger.gridx = 1;
		gbc_jTextFieldMyInteger.gridy = 2;
		GridBagConstraints gbc_jLabelMyDouble = new GridBagConstraints();
		gbc_jLabelMyDouble.insets = new Insets(0, 10, 5, 5);
		gbc_jLabelMyDouble.anchor = GridBagConstraints.WEST;
		gbc_jLabelMyDouble.gridx = 0;
		gbc_jLabelMyDouble.gridy = 3;
		GridBagConstraints gbc_jTextFieldMyDouble = new GridBagConstraints();
		gbc_jTextFieldMyDouble.gridwidth = 3;
		gbc_jTextFieldMyDouble.insets = new Insets(0, 0, 5, 10);
		gbc_jTextFieldMyDouble.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldMyDouble.gridx = 1;
		gbc_jTextFieldMyDouble.gridy = 3;
		GridBagConstraints gbc_jLabelVectorvalue = new GridBagConstraints();
		gbc_jLabelVectorvalue.anchor = GridBagConstraints.WEST;
		gbc_jLabelVectorvalue.insets = new Insets(0, 10, 5, 5);
		gbc_jLabelVectorvalue.gridx = 0;
		gbc_jLabelVectorvalue.gridy = 4;
		GridBagConstraints gbc_jTextFieldVectorValue = new GridBagConstraints();
		gbc_jTextFieldVectorValue.insets = new Insets(0, 0, 5, 5);
		gbc_jTextFieldVectorValue.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldVectorValue.gridx = 1;
		gbc_jTextFieldVectorValue.gridy = 4;
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 4;
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 10);
		gbc_btnNewButton_1.gridx = 3;
		gbc_btnNewButton_1.gridy = 4;
		GridBagConstraints gbc_jLabelMyVector = new GridBagConstraints();
		gbc_jLabelMyVector.anchor = GridBagConstraints.NORTHWEST;
		gbc_jLabelMyVector.insets = new Insets(0, 10, 0, 5);
		gbc_jLabelMyVector.gridx = 0;
		gbc_jLabelMyVector.gridy = 5;
		GridBagConstraints gbc_jScrollPaneVectorData = new GridBagConstraints();
		gbc_jScrollPaneVectorData.gridwidth = 3;
		gbc_jScrollPaneVectorData.insets = new Insets(0, 0, 10, 10);
		gbc_jScrollPaneVectorData.fill = GridBagConstraints.BOTH;
		gbc_jScrollPaneVectorData.gridx = 1;
		gbc_jScrollPaneVectorData.gridy = 5;
		
		this.getContentPane().add(this.getJLabelMyHeader(), gbc_jLabelMyHeader);
		this.getContentPane().add(this.getJLabelMyCheckbox(), gbc_jLabelMyCheckbox);
		this.getContentPane().add(this.getJCheckBoxMyboolean(), gbc_jCheckBoxMyboolean);
		this.getContentPane().add(this.getJLabelMyInteger(), gbc_jLabelMyInteger);
		this.getContentPane().add(this.getJTextFieldMyInteger(), gbc_jTextFieldMyInteger);
		this.getContentPane().add(this.getJLabelMyDouble(), gbc_jLabelMyDouble);
		this.getContentPane().add(this.getJTextFieldMyDouble(), gbc_jTextFieldMyDouble);
		this.getContentPane().add(this.getJLabelVectorvalue(), gbc_jLabelVectorvalue);
		this.getContentPane().add(this.getJTextFieldVectorValue(), gbc_jTextFieldVectorValue);
		this.getContentPane().add(this.getJButtonAddConfig(), gbc_btnNewButton);
		this.getContentPane().add(this.getJButtonRemoveConfig(), gbc_btnNewButton_1);
		this.getContentPane().add(this.getJLabelMyVector(), gbc_jLabelMyVector);
		this.getContentPane().add(this.getJScrollPaneVectorData(), gbc_jScrollPaneVectorData);
	}
	
	private JLabel getJLabelMyHeader() {
		if (jLabelMyHeader == null) {
			jLabelMyHeader = new JLabel("MyDataModel => MyDataModelDialog => AnyDataModel");
			jLabelMyHeader.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelMyHeader;
	}
	private JLabel getJLabelMyCheckbox() {
		if (jLabelMyCheckbox == null) {
			jLabelMyCheckbox = new JLabel("My CheckBox");
			jLabelMyCheckbox.setFont(new Font("Dialog", Font.BOLD, 11));
		}
		return jLabelMyCheckbox;
	}
	private JCheckBox getJCheckBoxMyboolean() {
		if (jCheckBoxMyboolean == null) {
			jCheckBoxMyboolean = new JCheckBox("");
			jCheckBoxMyboolean.addActionListener(this);
		}
		return jCheckBoxMyboolean;
	}
	private JLabel getJLabelMyInteger() {
		if (jLabelMyInteger == null) {
			jLabelMyInteger = new JLabel("My Integer");
			jLabelMyInteger.setFont(new Font("Dialog", Font.BOLD, 11));
		}
		return jLabelMyInteger;
	}
	private JTextField getJTextFieldMyInteger() {
		if (jTextFieldMyInteger == null) {
			jTextFieldMyInteger = new JTextField();
			jTextFieldMyInteger.setColumns(10);
			jTextFieldMyInteger.addKeyListener(new KeyAdapter4Numbers(false));
			jTextFieldMyInteger.getDocument().addDocumentListener(this);
		}
		return jTextFieldMyInteger;
	}
	private JLabel getJLabelMyDouble() {
		if (jLabelMyDouble == null) {
			jLabelMyDouble = new JLabel("My Double");
			jLabelMyDouble.setFont(new Font("Dialog", Font.BOLD, 11));
		}
		return jLabelMyDouble;
	}
	private JTextField getJTextFieldMyDouble() {
		if (jTextFieldMyDouble == null) {
			jTextFieldMyDouble = new JTextField();
			jTextFieldMyDouble.setColumns(10);
			jTextFieldMyDouble.addKeyListener(new KeyAdapter4Numbers(true));
			jTextFieldMyDouble.getDocument().addDocumentListener(this);
		}
		return jTextFieldMyDouble;
	}
	
	private JLabel getJLabelVectorvalue() {
		if (jLabelVectorValue == null) {
			jLabelVectorValue = new JLabel("Vector Value");
			jLabelVectorValue.setFont(new Font("Dialog", Font.BOLD, 11));
		}
		return jLabelVectorValue;
	}
	private JTextField getJTextFieldVectorValue() {
		if (jTextFieldVectorValue == null) {
			jTextFieldVectorValue = new JTextField();
			jTextFieldVectorValue.setColumns(10);
		}
		return jTextFieldVectorValue;
	}
	private JButton getJButtonAddConfig() {
		if (jButtonAddConfig == null) {
			jButtonAddConfig = new JButton();
			jButtonAddConfig.setToolTipText("Add Interface Configuration");
			jButtonAddConfig.setIcon(this.staticModel.getOptionModelController().getGlobalInfo().getImageIcon("ListPlus.png"));
			jButtonAddConfig.setPreferredSize(new Dimension(28, 26));
			jButtonAddConfig.addActionListener(this);
		}
		return jButtonAddConfig;
	}
	private JButton getJButtonRemoveConfig() {
		if (jButtonRemoveConfig == null) {
			jButtonRemoveConfig = new JButton();
			jButtonRemoveConfig.setToolTipText("Remove Interface Configuration");
			jButtonRemoveConfig.setIcon(this.staticModel.getOptionModelController().getGlobalInfo().getImageIcon("ListMinus.png"));
			jButtonRemoveConfig.setPreferredSize(new Dimension(28, 26));
			jButtonRemoveConfig.addActionListener(this);
		}
		return jButtonRemoveConfig;
	}
	
	private JLabel getJLabelMyVector() {
		if (jLabelMyVector == null) {
			jLabelMyVector = new JLabel("My Vector");
			jLabelMyVector.setFont(new Font("Dialog", Font.BOLD, 11));
		}
		return jLabelMyVector;
	}
	private JScrollPane getJScrollPaneVectorData() {
		if (jScrollPaneVectorData == null) {
			jScrollPaneVectorData = new JScrollPane();
			jScrollPaneVectorData.setViewportView(getJListVectorData());
		}
		return jScrollPaneVectorData;
	}
	private DefaultListModel<String> getDefaultListModel() {
		if (myListModel==null) {
			myListModel = new DefaultListModel<String>();
		}
		return myListModel;
	}
	private JList<String> getJListVectorData() {
		if (jListVectorData == null) {
			jListVectorData = new JList<String>(this.getDefaultListModel());
		}
		return jListVectorData;
	}

	
	private AnyDataModel getAnyDataModel() {
		if (anyDataModel==null) {
			anyDataModel = new AnyDataModel();
			staticModel.setStaticDataModel(anyDataModel);
		}
		return anyDataModel;
	}
	private void setAnyDataModel(AnyDataModel newDataModel) {
		this.anyDataModel = newDataModel;
	}
	
	
	/**
	 * Load the data model to the dialog.
	 */
	private void loadDataModelToDialog() {
		
		this.getJCheckBoxMyboolean().setSelected(this.getAnyDataModel().isMyBoolean());
		
		if (this.getAnyDataModel().getMyInteger()==null) {
			this.getJTextFieldMyInteger().setText("0");
			this.getAnyDataModel().setMyInteger(0);
		} else {
			this.getJTextFieldMyInteger().setText(this.getAnyDataModel().getMyInteger().toString());
		}
		
		if (this.getAnyDataModel().getMyDouble()==null) {
			this.getJTextFieldMyDouble().setText("0");
			this.getAnyDataModel().setMyDouble(0.0);
		} else {
			this.getJTextFieldMyDouble().setText(this.getAnyDataModel().getMyDouble().toString());
		}
		
		for (String listValue : this.getAnyDataModel().getMyStringVector()) {
			this.getDefaultListModel().addElement(listValue);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		if (ae.getSource()==this.getJCheckBoxMyboolean()) {
			this.getAnyDataModel().setMyBoolean(this.getJCheckBoxMyboolean().isSelected());
			
		} else if (ae.getSource()==this.getJButtonAddConfig()) {
			String newListValue = this.getJTextFieldVectorValue().getText();
			if (newListValue!=null && newListValue.equals("")==false) {
				this.getDefaultListModel().addElement(newListValue);
				this.getAnyDataModel().getMyStringVector().add(newListValue);
				this.getJTextFieldVectorValue().setText("");
			}
			
		} else if (ae.getSource()==this.getJButtonRemoveConfig()) {
			if (this.getJListVectorData().getSelectedValue()!=null) {
				String selectedValue = (String) this.getJListVectorData().getSelectedValue();
				this.getDefaultListModel().removeElement(selectedValue);
				this.getAnyDataModel().getMyStringVector().remove(selectedValue);
			}
			
		}
		
	}

	@Override
	public void insertUpdate(DocumentEvent de) {
		this.doDocumentUpdate(de);
	}
	@Override
	public void removeUpdate(DocumentEvent de) {
		this.doDocumentUpdate(de);
	}
	@Override
	public void changedUpdate(DocumentEvent de) {
		this.doDocumentUpdate(de);
	}
	private void doDocumentUpdate(DocumentEvent de) {
		
		try {
			
			Document doc = de.getDocument();
			String textValue = doc.getText(0, doc.getLength());
			
			// --- Convert ---
			if (de.getDocument()==this.getJTextFieldMyInteger().getDocument()) {
				int intValue = Integer.parseInt(textValue);
				this.getAnyDataModel().setMyInteger(intValue);
				
			} else if (de.getDocument()==this.getJTextFieldMyDouble().getDocument()) {
				double doubleValue = Double.parseDouble(textValue);
				this.getAnyDataModel().setMyDouble(doubleValue);
			}
		
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			
		} catch (NumberFormatException nfe) {
			// nfe.printStackTrace();
			// --- Set default values ---
			if (de.getDocument()==this.getJTextFieldMyInteger().getDocument()) {
				this.getAnyDataModel().setMyInteger(0);
			} else if (de.getDocument()==this.getJTextFieldMyDouble().getDocument()) {
				this.getAnyDataModel().setMyDouble(0.0);
			}
		}
		
	}
	
}
