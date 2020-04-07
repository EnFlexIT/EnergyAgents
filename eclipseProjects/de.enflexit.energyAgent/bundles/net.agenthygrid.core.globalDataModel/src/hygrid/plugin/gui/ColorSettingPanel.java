package hygrid.plugin.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import hygrid.env.ColorSettingsCollection;
import hygrid.env.ColorSettingsIntervalBased;
import javax.swing.JLabel;
import java.awt.Insets;

/**
 * The Class ColorSettingPanel can be used in oder to configure the 
 * background color settings for NetworkComponnets at runtime.
 * 
 * @see ColorSettings4ComponentUsage
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class ColorSettingPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final int COLUMN_INDEX_LOWER_BOUND = 0;
	private static final int COLUMN_INDEX_UPPER_BOUND = 1;
	
	private ColorSettingsCollection colorSettings; 
	private Vector<ActionListener> actionListenerVector;
	
	private JCheckBox jCheckBoxEnableColorSettings;
	
	private JScrollPane scrollPaneState;
	private JTable tableColorSettings;
	private DefaultTableModel defaultTableModel;
	
	private JButton jButtonInfinity;
	private JButton jButtonAddColorSetting;
	private JButton jButtonRemoveColorSetting;
	private JLabel jLabelProblemNotifications;
	
	/**
	 * Instantiates a new ColorSettingPanel for Nodes (values <=).
	 * @param colorSettings the ColorSettings4ComponentUsage to work on
	 */
	public ColorSettingPanel() {
		super();
		this.initialize();
	}
	
	/**
	 * Returns the ColorSettings4ComponentUsage.
	 * @return the colour settings
	 */
	public ColorSettingsCollection getColorSettings() {
		return colorSettings;
	}
	/**
	 * Sets the color settings.
	 * @param colorSettings the new color settings
	 */
	public void setColorSettings(ColorSettingsCollection colorSettings) {
		this.colorSettings = colorSettings;
		this.setData();
	}
	
	/**
	 * Gets the action listener vector.
	 * @return the action listener vector
	 */
	private Vector<ActionListener> getActionListenerVector() {
		if (actionListenerVector==null) {
			actionListenerVector = new Vector<ActionListener>();
		}
		return actionListenerVector;
	}
	/**
	 * Adds the specified action listener.
	 * @param actionListener the action listener
	 */
	public void addActionListener(ActionListener actionListener) {
		if (this.getActionListenerVector().contains(actionListener)==false) {
			this.getActionListenerVector().add(actionListener);
		}
	}
	/**
	 * Removes the specified action listener.
	 * @param actionListener the action listener
	 */
	public void removeActionListener(ActionListener actionListener) {
		this.getActionListenerVector().remove(actionListener);
	}
	/**
	 * Fires an action event.
	 */
	private void fireActionEvent() {
		ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
		for (ActionListener al : this.getActionListenerVector()) {
			al.actionPerformed(event);	
		}
	}
	
	/**
	 * Initializes this.
	 */
	private void initialize() {
		
		this.setSize(new Dimension(280, 150));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		GridBagConstraints gbcLabelHeading = new GridBagConstraints();
		gbcLabelHeading.insets = new Insets(0, 0, 5, 5);
		gbcLabelHeading.anchor = GridBagConstraints.WEST;
		gbcLabelHeading.gridwidth = 1;
		gbcLabelHeading.gridx = 0;
		gbcLabelHeading.gridy = 0;
		
		GridBagConstraints gbc_jButtonInfinity = new GridBagConstraints();
		gbc_jButtonInfinity.insets = new Insets(0, 0, 5, 5);
		gbc_jButtonInfinity.anchor = GridBagConstraints.EAST;
		gbc_jButtonInfinity.gridx = 1;
		gbc_jButtonInfinity.gridy = 0;
		
		GridBagConstraints gbcButtonAddColorSettings = new GridBagConstraints();
		gbcButtonAddColorSettings.insets = new Insets(0, 0, 5, 5);
		gbcButtonAddColorSettings.anchor = GridBagConstraints.EAST;
		gbcButtonAddColorSettings.gridx = 2;
		gbcButtonAddColorSettings.gridy = 0;
		
		GridBagConstraints gbcButtonRemoveColorSettings = new GridBagConstraints();
		gbcButtonRemoveColorSettings.insets = new Insets(0, 0, 5, 0);
		gbcButtonRemoveColorSettings.anchor = GridBagConstraints.EAST;
		gbcButtonRemoveColorSettings.gridx = 3;
		gbcButtonRemoveColorSettings.gridy = 0;
		
		GridBagConstraints gbcScrollPaneColorSettings = new GridBagConstraints();
		gbcScrollPaneColorSettings.anchor = GridBagConstraints.WEST;
		gbcScrollPaneColorSettings.gridwidth = 4;
		gbcScrollPaneColorSettings.fill = GridBagConstraints.BOTH;
		gbcScrollPaneColorSettings.gridx = 0;
		gbcScrollPaneColorSettings.weightx = 1.0;
		gbcScrollPaneColorSettings.weighty = 1.0;
		gbcScrollPaneColorSettings.gridy = 1;
		
		GridBagConstraints gbc_jLabelProblemNotifications = new GridBagConstraints();
		gbc_jLabelProblemNotifications.anchor = GridBagConstraints.WEST;
		gbc_jLabelProblemNotifications.gridwidth = 4;
		gbc_jLabelProblemNotifications.insets = new Insets(5, 5, 5, 5);
		gbc_jLabelProblemNotifications.gridx = 0;
		gbc_jLabelProblemNotifications.gridy = 2;
		
		this.add(this.getJCheckBoxEnableColorSettings(), gbcLabelHeading);
		this.add(this.getScrollPaneStates(), gbcScrollPaneColorSettings);
		this.add(getJButtonInfinity(), gbc_jButtonInfinity);
		this.add(this.getJButtonAddColorSetting(), gbcButtonAddColorSettings);
		this.add(this.getJButtonRemoveColorSetting(), gbcButtonRemoveColorSettings);
		this.add(getJLabelProblemNotifications(), gbc_jLabelProblemNotifications);
		
		
	}
	/**
	 * Gets the scroll pane for the states.
	 * @return the scroll pane for the states
	 */
	private JScrollPane getScrollPaneStates() {
		if (scrollPaneState == null) {
			scrollPaneState = new JScrollPane();
			scrollPaneState.setMinimumSize(new Dimension(100, 78));
			scrollPaneState.setSize(new Dimension(100, 78));
			scrollPaneState.setPreferredSize(new Dimension(100, 78));
			scrollPaneState.setViewportView(getJTableColorSettings());
		}
		return scrollPaneState;
	}
	/**
	 * Gets the table for the state
	 * @return the table for the states 
	 */
	private JTable getJTableColorSettings() {
		if (tableColorSettings == null) {
			tableColorSettings = new JTable(this.getDefaultTableModel());
			tableColorSettings.setFillsViewportHeight(true);
			tableColorSettings.setFont(new Font("Dialog", Font.PLAIN, 12));
			tableColorSettings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tableColorSettings.getTableHeader().setReorderingAllowed(false);
			
			// --- Configure columns ----------------------
			ColorSettingPanelRenderEditor renderEditor = new ColorSettingPanelRenderEditor();
			TableColumnModel tcm = tableColorSettings.getColumnModel();
			
			tcm.getColumn(0).setWidth(45);
			tcm.getColumn(0).setPreferredWidth(45);
			tcm.getColumn(0).setCellRenderer(renderEditor);
			tcm.getColumn(0).setCellEditor(renderEditor);
			
			tcm.getColumn(1).setWidth(45);
			tcm.getColumn(1).setPreferredWidth(45);
			tcm.getColumn(1).setCellRenderer(renderEditor);
			tcm.getColumn(1).setCellEditor(renderEditor);
			
			tcm.getColumn(2).setCellRenderer(renderEditor);
			tcm.getColumn(2).setCellEditor(renderEditor);
			
			tcm.getSelectionModel().addListSelectionListener(this.getListSelectionListener());
			tableColorSettings.getModel().addTableModelListener(new TableModelListener() {
				
				/* (non-Javadoc)
				 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
				 */
				@Override
				public void tableChanged(TableModelEvent e) {
					// --- Check for errors ---------------
					getJLabelProblemNotifications().setText(colorSettings.getErrorMessage());
				}
			});
			
		}
		return tableColorSettings;
	}
	/**
	 * Stops the current cell editing.
	 */
	private void stopCellEditing() {
		if (this.getJTableColorSettings().getCellEditor()!=null) {
			this.getJTableColorSettings().getCellEditor().stopCellEditing();
		}
	}

	/**
	 * Gets the default table model.
	 * @return the default table model
	 */
	private DefaultTableModel getDefaultTableModel() {
		if(defaultTableModel == null) {
			Vector<String> header = new Vector<String>();
			
			header.add("From ( >= )");
			header.add("To ( < )");
			header.add("Color");
			defaultTableModel = new DefaultTableModel(null, header);
		}
		return defaultTableModel;
	}
	/**
	 * Empties the table model.
	 */
	private void emptyDefaultTableModel() {
		this.getJTableColorSettings().clearSelection();
		while (this.getDefaultTableModel().getRowCount()>0) {
			this.getDefaultTableModel().removeRow(0);
		}
	}
	/**
	 * Adds the specified ColorSeting4Component to the table model row.
	 * @param cs the ColorSeting4Component to add
	 */
	private void addTableModelRow(ColorSettingsIntervalBased cs) {
		Vector<ColorSettingsIntervalBased> row = new Vector<ColorSettingsIntervalBased>();
		row.add(cs);
		row.add(cs);
		row.add(cs);
		this.getDefaultTableModel().addRow(row);
	}
	
	/**
	 * Gets the JCheckBox enable color settings.
	 * @return the JCheckBox that enables the color settings
	 */
	private JCheckBox getJCheckBoxEnableColorSettings() {
		if (jCheckBoxEnableColorSettings == null) {
			jCheckBoxEnableColorSettings = new JCheckBox("Enable");
			jCheckBoxEnableColorSettings.setToolTipText("Enable color indication at runtime");
			jCheckBoxEnableColorSettings.setFont(new Font("Dialog", Font.BOLD, 12));
			jCheckBoxEnableColorSettings.addActionListener(this);
		}
		return jCheckBoxEnableColorSettings;
	}
	
	/**
	 * Gets the j button infinity.
	 * @return the j button infinity
	 */
	private JButton getJButtonInfinity() {
		if (jButtonInfinity == null) {
			jButtonInfinity = new JButton("");
			jButtonInfinity.setToolTipText("No upper/lower bound for this interval");
			jButtonInfinity.setIcon(new ImageIcon(this.getClass().getResource("/hygrid/plugin/gui/img/Infinity.png")));
			jButtonInfinity.setPreferredSize(new Dimension(28, 26));
			jButtonInfinity.setEnabled(false);
			jButtonInfinity.addActionListener(this);
		}
		return jButtonInfinity;
	}
	/**
	 * Gets the button add states.
	 * @return the button add states
	 */
	private JButton getJButtonAddColorSetting() {
		if (jButtonAddColorSetting == null) {
			jButtonAddColorSetting = new JButton();
			jButtonAddColorSetting.setToolTipText("Add Color Setting");
			jButtonAddColorSetting.setIcon(new ImageIcon(this.getClass().getResource("/hygrid/plugin/gui/img/ListPlus.png")));
			jButtonAddColorSetting.setPreferredSize(new Dimension(28, 26));
			jButtonAddColorSetting.addActionListener(this);
		}
		return jButtonAddColorSetting;
	}
	/**
	 * Gets the button remove states.
	 * @return the button remove states
	 */
	private JButton getJButtonRemoveColorSetting() {
		if (jButtonRemoveColorSetting == null) {
			jButtonRemoveColorSetting = new JButton();
			jButtonRemoveColorSetting.setToolTipText("Remove Color Setting");
			jButtonRemoveColorSetting.setIcon(new ImageIcon(getClass().getResource("/hygrid/plugin/gui/img/ListMinus.png")));
			jButtonRemoveColorSetting.setPreferredSize(new Dimension(28, 26));
			jButtonRemoveColorSetting.addActionListener(this);
		}
		return jButtonRemoveColorSetting;
	}
	
	/**
	 * Sets the data.
	 */
	private void setData() {

		// --- Set the check box ------------------------------------
		if (this.colorSettings!=null) {
			boolean enabled = this.colorSettings.isEnabled();
			this.getJCheckBoxEnableColorSettings().setSelected(enabled);
			this.getJButtonAddColorSetting().setEnabled(enabled);
			this.getJButtonRemoveColorSetting().setEnabled(enabled);
			this.getJTableColorSettings().setEnabled(enabled);
			
			
			// -- Refill the table model ----------------------
			this.emptyDefaultTableModel();
			Vector<ColorSettingsIntervalBased> colorSettings = this.colorSettings.getColorSettingsVector();
			Collections.sort(colorSettings);
			for (int i = 0; i < colorSettings.size(); i++) {
				ColorSettingsIntervalBased cs = colorSettings.get(i);
				this.addTableModelRow(cs);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		if (ae.getSource() == this.getJCheckBoxEnableColorSettings()) {
			// --- Enable/Disable the colour settings ---------------
			this.stopCellEditing();
			boolean enabled = this.getJCheckBoxEnableColorSettings().isSelected();
			this.colorSettings.setEnabled(enabled);
			this.getJButtonAddColorSetting().setEnabled(enabled);
			this.getJButtonRemoveColorSetting().setEnabled(enabled);
			this.getJTableColorSettings().setEnabled(enabled);
			this.fireActionEvent();
			
		} else if(ae.getSource() == getJButtonAddColorSetting()) {
			// --- Button Add State ---------------------------------
			this.stopCellEditing();
			// --- Create new ColorSeting4Component -------------
			ColorSettingsIntervalBased newCS = this.colorSettings.addNewColorSettings(0, 0, new Color(255, 255, 255));
			this.addTableModelRow(newCS);
			int focusTo = this.getJTableColorSettings().convertRowIndexToView(this.getDefaultTableModel().getRowCount()-1); 
			this.getJTableColorSettings().setRowSelectionInterval(focusTo, focusTo);
			this.fireActionEvent();
			
		} else if(ae.getSource() == getJButtonRemoveColorSetting()) {
			// --- Button Remove State ------------------------------
			this.stopCellEditing();
			// --- Get selected table row -----------------------
			int tableRowSelected = this.getJTableColorSettings().getSelectedRow();
			if(tableRowSelected!=-1) {
				// --- convert to model row ---------------------
				int modelRowSelected = this.getJTableColorSettings().convertRowIndexToModel(tableRowSelected);
				ColorSettingsIntervalBased cs2Delete = (ColorSettingsIntervalBased) this.getDefaultTableModel().getValueAt(modelRowSelected, 0);
				// --- Remove colour setting from Model ---------
				this.colorSettings.getColorSettingsVector().remove(cs2Delete);
				// --- Remove from table and option model -------
				this.getDefaultTableModel().removeRow(modelRowSelected);
				// --- Set focus to first row -------------------
				if (this.getDefaultTableModel().getRowCount()>0) {
					int focusTo = this.getJTableColorSettings().convertRowIndexToView(this.getDefaultTableModel().getRowCount()-1); 
					this.getJTableColorSettings().setRowSelectionInterval(focusTo, focusTo);	
				}
				this.fireActionEvent();
				
			} else {
				JOptionPane.showMessageDialog(this.getParent().getParent(), "Please select the setting that you want to remove!", "Missing color setting selection!", JOptionPane.WARNING_MESSAGE);
			}
		
		} else if (ae.getSource()==this.getJButtonInfinity()) {
			// --- Set the lower/upper bound for the selected interval to Double.MIN/MAX_VALUE
			int rowSelected = this.getJTableColorSettings().getSelectedRow();
			int colSelected = this.getJTableColorSettings().getSelectedColumn();
			int modelRowSelected = this.getJTableColorSettings().convertRowIndexToModel(this.getJTableColorSettings().getSelectedRow());
			ColorSettingsIntervalBased selectedSetings = (ColorSettingsIntervalBased) this.getDefaultTableModel().getValueAt(modelRowSelected, 0);
			if (this.getJTableColorSettings().getSelectedColumn()==COLUMN_INDEX_LOWER_BOUND) {
				selectedSetings.setLowerBound(Double.MIN_VALUE);
			} else if (this.getJTableColorSettings().getSelectedColumn()==COLUMN_INDEX_UPPER_BOUND) {
				selectedSetings.setUpperBound(Double.MAX_VALUE);
			}
			if (this.getJTableColorSettings().isEditing()) {
				this.getJTableColorSettings().getCellEditor().stopCellEditing();
			}
			((AbstractTableModel)this.getJTableColorSettings().getModel()).fireTableCellUpdated(rowSelected, colSelected);
		}
	}
	
	
	private ListSelectionListener getListSelectionListener() {
		return new ListSelectionListener() {
			
			/* (non-Javadoc)
			 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
			 */
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// --- Enable the infinity button if a lower/upper bound field is selected
				int selectedRow = getJTableColorSettings().getSelectedRow();
				int selectedColumn = getJTableColorSettings().getSelectedColumn();
				boolean rowSelected = (selectedRow>-1 && (selectedColumn==COLUMN_INDEX_LOWER_BOUND||selectedColumn==COLUMN_INDEX_UPPER_BOUND));
				getJButtonInfinity().setEnabled(rowSelected);
			}
		};
	}
	private JLabel getJLabelProblemNotifications() {
		if (jLabelProblemNotifications == null) {
			jLabelProblemNotifications = new JLabel("");
			jLabelProblemNotifications.setForeground(Color.RED);
			jLabelProblemNotifications.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelProblemNotifications;
	}
}  