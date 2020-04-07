package hygrid.plugin.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;

import de.enflexit.common.swing.KeyAdapter4Numbers;
import de.enflexit.common.swing.TableCellColorHelper;
import hygrid.env.ColorSettingsIntervalBased;


/**
 * Renderer/editor component for a table of {@link ColorSettingsIntervalBased} instances. 
 * 
 * @author Christian Derksen - SOFTEC - University of Duisburg-Essen
 * @author Nils Loose - SOFTEC  - University of Duisburg-Essen
 */
public class ColorSettingPanelRenderEditor extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

	private static final long serialVersionUID = 9016943219972725634L;
	
	private static final String NEGATIVE_INFINITY = "-∞";
	private static final String POSITIVE_INFINITY = "+∞";

	private ColorSettingsIntervalBased colorSetting;
	private int column;
    private JComponent editor;
    
	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		JComponent renderer = null;
		ColorSettingsIntervalBased colorSetting = (ColorSettingsIntervalBased) value; 
		switch (column) {
		case 0:
			renderer = this.getJTextField(colorSetting.getLowerBound());
			break;
			
		case 1:
			renderer = this.getJTextField(colorSetting.getUpperBound());
			break;

		case 2:
			renderer = this.getJButtonColor(colorSetting.getValueColor());
			break;
		}
		
		if (!(renderer instanceof JButton)) {
			TableCellColorHelper.setTableCellRendererColors(renderer, row, isSelected);
		}
		if (renderer instanceof JTextField && colorSetting.hasError()) {
			((JTextField)renderer).setForeground(Color.RED);
		}
		return renderer;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    	
    	this.colorSetting = (ColorSettingsIntervalBased) value;
    	this.column = column;
		switch (column) {
		case 0:
			this.editor = this.getJTextField(colorSetting.getLowerBound());
			break;
			
		case 1:
			this.editor = this.getJTextField(colorSetting.getUpperBound());
			break;

		case 2:
			this.editor = this.getJButtonColor(colorSetting.getValueColor());
			break;
		}
		if (!(this.editor instanceof JButton)) {
			TableCellColorHelper.setTableCellRendererColors(this.editor, row, isSelected);
		}
		if (editor instanceof JTextField && colorSetting.hasError()) {
			((JTextField)editor).setForeground(Color.RED);
		}
		return this.editor;
    }
    
    /**
     * Returns a JTextField for double numbers with the specified default text (double value).
     * Double.MIN/MAX_VALUE will be displayed as infinity signs
     *
     * @param defaultValue the default value
     * @return the j text field
     */
    private JTextField getJTextField(double initialValue) {
    	
    	String initialString;
    	// --- Translate min/max double to infinity signs -----------
    	if (initialValue==Double.MIN_VALUE) {
    		initialString = NEGATIVE_INFINITY;
    	} else if (initialValue==Double.MAX_VALUE) {
    		initialString = POSITIVE_INFINITY;
    	} else {
    		initialString = new Double(initialValue).toString();
    	}
    	
    	JTextField textField = new JTextField(initialString);
		textField.setOpaque(true);
		textField.setBorder(BorderFactory.createEmptyBorder());
		textField.addKeyListener(new KeyAdapter4Numbers(true));
		textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent de) {
				this.updateValue(de);
			}
			@Override
			public void removeUpdate(DocumentEvent de) {
				this.updateValue(de);
			}
			@Override
			public void changedUpdate(DocumentEvent de) {
				this.updateValue(de);
			}
			private void updateValue(DocumentEvent de) {
				
				Double value = 0.0;
				String text = null;
				
				try {
					text = de.getDocument().getText(0, de.getDocument().getLength());
					
					// --- Translate infinity signs to min/max double ---------
					if (text.equals(POSITIVE_INFINITY)) {
						value = Double.MAX_VALUE;
					} else if (text.equals(NEGATIVE_INFINITY)) {
						value = Double.MIN_VALUE;
					} else 	if (text!=null && text.equals("")==false) {
						try {
							value = Double.parseDouble(text);	
						} catch(NumberFormatException nfe) {
							
						}
					}
					
				} catch (BadLocationException ble) {
					//ble.printStackTrace();
				}
				
				if (column==0) {
					colorSetting.setLowerBound(value);
				} else if (column==1) {
					colorSetting.setUpperBound(value);
				}
			}
		});
    	return textField;
    }
    /**
     * Gets the JBbutton to chose a new {@link Color}.
     * @param color the initial {@link Color}
     * @return the JButton 
     */
    private JButton getJButtonColor(Color color) {
    	JButton button = new JButton();
    	button.setBackground(color);
    	button.addActionListener(new ActionListener() {
			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent ae) {
				JButton button = (JButton) ae.getSource();
				Color newColor = JColorChooser.showDialog(button, "Pick a Color", colorSetting.getValueColor());
	            // - - - Wait for user - - - -
				if (newColor!=null) {
					colorSetting.setValueColor(newColor);
		            button.setBackground(newColor);	
				}
				fireEditingStopped();
			}
		});
    	return button;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    @Override
    public Object getCellEditorValue() {
    	return this.colorSetting;
    }
    
}
