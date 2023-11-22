package de.enflexit.ea.core.configuration.eom.systems.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import de.enflexit.ea.core.configuration.eom.BundleHelper;
import de.enflexit.ea.core.configuration.eom.systems.EomSystem;
import de.enflexit.ea.core.configuration.eom.systems.SystemBlueprint;
import de.enflexit.ea.core.configuration.eom.systems.SystemConfiguration;
import de.enflexit.ea.core.configuration.eom.systems.SystemConfigurationManager;
import de.enflexit.ea.core.configuration.eom.systems.ui.EomSystemCellRenderer.EomSystemInformationType;
import de.enflexit.ea.core.configuration.ui.TableRendererBoolean;

/**
 * The Class SystemConfigurationPanel.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SystemConfigurationPanel extends JSplitPane implements ActionListener {

	private static final long serialVersionUID = 5895484811771501886L;

	private static final Dimension BUTTON_SIZE = new Dimension(26, 26);
	
	private SystemConfigurationManager systemConfigurationManager;
	
	private JPanel jPanelEomModels;
	private JLabel jLabelEomModels;
	private JButton jButtonAddEomSystem;
	private JButton jButtonRemoveEomSystem;
	private JScrollPane jScrollPaneEomSystems;
	private JTable jTableEomSystems;
	private DefaultTableModel tableModelEomSystems;
	
	private JPanel jPanelSystemBluePrints;
	private JLabel jLabelSystemBlueprints;
	private JButton jButtonAddSystemBlueprint;
	private JButton jButtonRemoveSystemBlueprint;
	private JScrollPane jScrollPaneSystemBlueprints;
	private JTable jTableSystemBlueprints;
	private TableModelListener tmlSystemBlueprints;
	private DefaultTableModel tableModelSystemBlueprints;
	
	private SystemBlueprint systemBlueprintInEdit;
	
	/**
	 * Instantiates a new system configuration panel.
	 * @param systemConfigurationManager the system configuration manager
	 */
	public SystemConfigurationPanel(SystemConfigurationManager systemConfigurationManager) {
		this.systemConfigurationManager = systemConfigurationManager;
		this.initialize();
		
		this.reFillTableEomSystems();
	}
	/**
	 * Initialize.
	 */
	private void initialize() {
		
		this.setOrientation(JSplitPane.VERTICAL_SPLIT);
		this.setDividerSize(5);
		this.setDividerLocation(0.5);
		this.setResizeWeight(0.5);
		
		this.setTopComponent(this.getJPanelEomModels());
		this.setBottomComponent(this.getJPanelSystemBluePrints());
	}
	
	private JPanel getJPanelEomModels() {
		if (jPanelEomModels==null) {
			jPanelEomModels = new JPanel();
			
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
			gridBagLayout.rowHeights = new int[]{0, 0, 0};
			gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			jPanelEomModels.setLayout(gridBagLayout);
			
			GridBagConstraints gbc_jLabelEomModels = new GridBagConstraints();
			gbc_jLabelEomModels.insets = new Insets(5, 0, 5, 0);
			gbc_jLabelEomModels.anchor = GridBagConstraints.WEST;
			gbc_jLabelEomModels.gridx = 0;
			gbc_jLabelEomModels.gridy = 0;
			jPanelEomModels.add(getJLabelEomModels(), gbc_jLabelEomModels);
			GridBagConstraints gbc_jButtonAddEomSystem = new GridBagConstraints();
			gbc_jButtonAddEomSystem.insets = new Insets(0, 10, 0, 0);
			gbc_jButtonAddEomSystem.anchor = GridBagConstraints.EAST;
			gbc_jButtonAddEomSystem.gridx = 1;
			gbc_jButtonAddEomSystem.gridy = 0;
			jPanelEomModels.add(getJButtonAddEomSystem(), gbc_jButtonAddEomSystem);
			GridBagConstraints gbc_jButtonRemoveEomSystem = new GridBagConstraints();
			gbc_jButtonRemoveEomSystem.insets = new Insets(0, 5, 0, 0);
			gbc_jButtonRemoveEomSystem.gridx = 2;
			gbc_jButtonRemoveEomSystem.gridy = 0;
			jPanelEomModels.add(getJButtonRemoveEomSystem(), gbc_jButtonRemoveEomSystem);
			GridBagConstraints gbc_jScrollPaneEomModels = new GridBagConstraints();
			gbc_jScrollPaneEomModels.gridwidth = 3;
			gbc_jScrollPaneEomModels.fill = GridBagConstraints.BOTH;
			gbc_jScrollPaneEomModels.gridx = 0;
			gbc_jScrollPaneEomModels.gridy = 1;
			jPanelEomModels.add(getJScrollPaneEomSystems(), gbc_jScrollPaneEomModels);
			
		}
		return jPanelEomModels;
	}
	private JLabel getJLabelEomModels() {
		if (jLabelEomModels == null) {
			jLabelEomModels = new JLabel("EOM - Systems");
			jLabelEomModels.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelEomModels;
	}
	private JButton getJButtonAddEomSystem() {
		if (jButtonAddEomSystem == null) {
			jButtonAddEomSystem = new JButton();
			jButtonAddEomSystem.setToolTipText("Add an EOM-System to the list");
			jButtonAddEomSystem.setIcon(BundleHelper.getImageIcon("ListPlus.png"));
			jButtonAddEomSystem.setSize(BUTTON_SIZE);
			jButtonAddEomSystem.setMaximumSize(BUTTON_SIZE);
			jButtonAddEomSystem.addActionListener(this);
		}
		return jButtonAddEomSystem;
	}
	private JButton getJButtonRemoveEomSystem() {
		if (jButtonRemoveEomSystem == null) {
			jButtonRemoveEomSystem = new JButton("");
			jButtonRemoveEomSystem.setToolTipText("Remove selected EOM-System from the list");
			jButtonRemoveEomSystem.setIcon(BundleHelper.getImageIcon("ListMinus.png"));
			jButtonRemoveEomSystem.setSize(BUTTON_SIZE);
			jButtonRemoveEomSystem.setMaximumSize(BUTTON_SIZE);
			jButtonRemoveEomSystem.addActionListener(this);
		}
		return jButtonRemoveEomSystem;
	}
	
	private JScrollPane getJScrollPaneEomSystems() {
		if (jScrollPaneEomSystems == null) {
			jScrollPaneEomSystems = new JScrollPane();
			jScrollPaneEomSystems.setViewportView(getJTableEomSystems());
		}
		return jScrollPaneEomSystems;
	}
	private DefaultTableModel getTableModelEomSystems() {
		if (tableModelEomSystems==null) {
			Vector<Object> columns = new Vector<>();
			columns.add("ID");
			columns.add("EOM System-ID");
			columns.add("EOM Model-Type");
			columns.add("Storage Location");
			columns.add("Storage-Info");
			tableModelEomSystems = new DefaultTableModel(null, columns) {
				private static final long serialVersionUID = 7274313722661758685L;
				@Override
				public boolean isCellEditable(int row, int column) {
					switch (column) {
					case 0:
					case 2:
					case 3:
					case 4:
						return true;
					}
					return false;
				}
			};
		}
		return tableModelEomSystems;
	}
	private JTable getJTableEomSystems() {
		if (jTableEomSystems == null) {
			jTableEomSystems = new JTable(this.getTableModelEomSystems());
			jTableEomSystems.setFillsViewportHeight(true);
			jTableEomSystems.getTableHeader().setReorderingAllowed(false);
			jTableEomSystems.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTableEomSystems.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
			jTableEomSystems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTableEomSystems.setRowHeight(20);
			
			TableColumnModel tcm = jTableEomSystems.getColumnModel();
			
			tcm.getColumn(0).setCellEditor(new EomSystemCellEditorID(this, this.getSystemConfiguration()));
			tcm.getColumn(0).setMinWidth(100);
			tcm.getColumn(0).setMaxWidth(240);
			
			tcm.getColumn(1).setCellRenderer(new EomSystemCellRenderer(EomSystemInformationType.SystemID));
			tcm.getColumn(1).setMinWidth(200);
			tcm.getColumn(1).setMaxWidth(300);
			
			tcm.getColumn(2).setCellRenderer(new EomSystemCellRendererEomModelType());
			tcm.getColumn(2).setCellEditor(new EomSystemCellEditorEomModelType());
			tcm.getColumn(2).setMinWidth(180);
			tcm.getColumn(2).setMaxWidth(200);

			tcm.getColumn(3).setCellRenderer(new EomSystemCellRendererEomStorageLocation());
			tcm.getColumn(3).setCellEditor(new EomSystemCellEditorEomStorageLocation());
			tcm.getColumn(3).setMinWidth(130);
			tcm.getColumn(3).setMaxWidth(180);
			
			tcm.getColumn(4).setCellRenderer(new EomSystemCellRenderer(EomSystemInformationType.StorageInfo));
			tcm.getColumn(4).setCellEditor(new EomSystemCellEditorModelSelection(this.getSystemConfiguration()));
			
			
		}
		return jTableEomSystems;
	}
	/**
	 * Re fills the table of EomSystems.
	 */
	private void reFillTableEomSystems() {
		
		// --- Reset table content ------------------------
		this.getTableModelEomSystems().setRowCount(0);
		// --- Fill table model ---------------------------
		this.getSystemConfiguration().getEomSystemList().forEach(eomSystem -> this.addEomSystemToTable(eomSystem));
	}
	/**
	 * Adds the specified EomSystem to table.
	 * @param eomSystem the eom system
	 */
	private void addEomSystemToTable(EomSystem eomSystem) {
		
		if (eomSystem==null) return; 
		
		Vector<Object> rowData = new Vector<>();
		rowData.add(eomSystem);
		rowData.add(eomSystem);
		rowData.add(eomSystem);
		rowData.add(eomSystem);
		rowData.add(eomSystem);
		this.getTableModelEomSystems().addRow(rowData);
	}
	/**
	 * Sets the table selection to the specified EomSystem.
	 * @param eomSystem the new eom system selection
	 */
	private void setEomSystemSelection(EomSystem eomSystem) {
		
		if (eomSystem==null) return;
		
		for (int row = 0; row < this.getJTableEomSystems().getRowCount(); row++) {
			EomSystem eomSystemCheck = (EomSystem) this.getJTableEomSystems().getValueAt(row, 0);
			if (eomSystemCheck==eomSystem) {
				this.getJTableEomSystems().setRowSelectionInterval(row, row);
				return;
			}
		}
	}
	
	private JPanel getJPanelSystemBluePrints() {
		if (jPanelSystemBluePrints==null) {
			jPanelSystemBluePrints = new JPanel();
			
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
			gridBagLayout.rowHeights = new int[]{0, 0, 0};
			gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			jPanelSystemBluePrints.setLayout(gridBagLayout);
			
			GridBagConstraints gbc_jLabelSystemBlueprints = new GridBagConstraints();
			gbc_jLabelSystemBlueprints.insets = new Insets(5, 0, 5, 0);
			gbc_jLabelSystemBlueprints.anchor = GridBagConstraints.WEST;
			gbc_jLabelSystemBlueprints.gridx = 0;
			gbc_jLabelSystemBlueprints.gridy = 0;
			jPanelSystemBluePrints.add(getJLabelSystemBlueprints(), gbc_jLabelSystemBlueprints);
			GridBagConstraints gbc_jButtonAddSystemBlueprint = new GridBagConstraints();
			gbc_jButtonAddSystemBlueprint.insets = new Insets(0, 10, 0, 0);
			gbc_jButtonAddSystemBlueprint.anchor = GridBagConstraints.EAST;
			gbc_jButtonAddSystemBlueprint.gridx = 1;
			gbc_jButtonAddSystemBlueprint.gridy = 0;
			jPanelSystemBluePrints.add(getJButtonAddSystemBlueprint(), gbc_jButtonAddSystemBlueprint);
			GridBagConstraints gbc_jButtonRemoveSystemBlueprint = new GridBagConstraints();
			gbc_jButtonRemoveSystemBlueprint.insets = new Insets(0, 5, 0, 0);
			gbc_jButtonRemoveSystemBlueprint.gridx = 2;
			gbc_jButtonRemoveSystemBlueprint.gridy = 0;
			jPanelSystemBluePrints.add(getJButtonRemoveSystemBlueprint(), gbc_jButtonRemoveSystemBlueprint);
			GridBagConstraints gbc_jScrollPaneSystemBlueprints = new GridBagConstraints();
			gbc_jScrollPaneSystemBlueprints.gridwidth = 3;
			gbc_jScrollPaneSystemBlueprints.fill = GridBagConstraints.BOTH;
			gbc_jScrollPaneSystemBlueprints.gridx = 0;
			gbc_jScrollPaneSystemBlueprints.gridy = 1;
			jPanelSystemBluePrints.add(getJScrollPaneSystemBlueprints(), gbc_jScrollPaneSystemBlueprints);
			
		}
		return jPanelSystemBluePrints;
	}
	private JLabel getJLabelSystemBlueprints() {
		if (jLabelSystemBlueprints == null) {
			jLabelSystemBlueprints = new JLabel("System Blueprints");
			jLabelSystemBlueprints.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelSystemBlueprints;
	}
	private JButton getJButtonAddSystemBlueprint() {
		if (jButtonAddSystemBlueprint == null) {
			jButtonAddSystemBlueprint = new JButton();
			jButtonAddSystemBlueprint.setToolTipText("Add System Blueprint");
			jButtonAddSystemBlueprint.setIcon(BundleHelper.getImageIcon("ListPlus.png"));
			jButtonAddSystemBlueprint.setSize(BUTTON_SIZE);
			jButtonAddSystemBlueprint.setMaximumSize(BUTTON_SIZE);
			jButtonAddSystemBlueprint.addActionListener(this);
		}
		return jButtonAddSystemBlueprint;
	}
	private JButton getJButtonRemoveSystemBlueprint() {
		if (jButtonRemoveSystemBlueprint == null) {
			jButtonRemoveSystemBlueprint = new JButton("");
			jButtonRemoveSystemBlueprint.setToolTipText("Remove System Blueprint");
			jButtonRemoveSystemBlueprint.setIcon(BundleHelper.getImageIcon("ListMinus.png"));
			jButtonRemoveSystemBlueprint.setSize(BUTTON_SIZE);
			jButtonRemoveSystemBlueprint.setMaximumSize(BUTTON_SIZE);
			jButtonRemoveSystemBlueprint.addActionListener(this);
		}
		return jButtonRemoveSystemBlueprint;
	}
	
	private JScrollPane getJScrollPaneSystemBlueprints() {
		if (jScrollPaneSystemBlueprints == null) {
			jScrollPaneSystemBlueprints = new JScrollPane();
			jScrollPaneSystemBlueprints.setViewportView(getJTableSystemBlueprints());
		}
		return jScrollPaneSystemBlueprints;
	}
	private DefaultTableModel getTableModelSystemBlueprints() {
		if (tableModelSystemBlueprints==null) {
			this.reBuildTableModelSystemBlueprints();
			this.reFillTableSystemBlueprints();
		}
		return tableModelSystemBlueprints;
	}
	/**
	 * Re-builds the table model for system blueprints.
	 */
	private void reBuildTableModelSystemBlueprints() {
		
		// --- Define the columns -------------------------
		Vector<Object> columns = new Vector<>();
		columns.add("Blueprint-ID");
		columns.add("Description");
		for (EomSystem eomSystem : this.getSystemConfiguration().getEomSystemList()) {
			columns.add(eomSystem.getId());
		}
		
		// --- Define the table model ---------------------
		this.tableModelSystemBlueprints = new DefaultTableModel(null, columns) {
			private static final long serialVersionUID = -4838949452782863586L;
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				
				Class<?> columnClass = Boolean.class;
				switch (columnIndex) {
				case 0:
				case 1:
					columnClass = String.class;
					break;

				default:
					break;
				}
				return columnClass;
			}
		};
		this.tableModelSystemBlueprints.addTableModelListener(this.getTableModelListenerSystemBlueprints());
		
		// --- Set model to table -------------------------
		this.getJTableSystemBlueprints().setModel(this.tableModelSystemBlueprints);
		this.setJTableSystemBlueprintsColumnWidth();
	}
	
	/**
	 * Returns the table model listener system blueprints.
	 * @return the table model listener system blueprints
	 */
	private TableModelListener getTableModelListenerSystemBlueprints() {
		if (tmlSystemBlueprints==null) {
			tmlSystemBlueprints = new TableModelListener() {
				
				private boolean isPauseTableModelListener;
				
				/* (non-Javadoc)
				 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
				 */
				@Override
				public void tableChanged(TableModelEvent tmEvent) {
					
					if (this.isPauseTableModelListener==true) return;
					
					final int row = tmEvent.getFirstRow();
					final int col = tmEvent.getColumn();
					if (row==-1 || col==-1) return;
					
					// --- Correct local 'systemBlueprintInEdit'? -------------
					if (col>1) {
						String systemBluePrintID = (String) getTableModelSystemBlueprints().getValueAt(row, 0);
						SystemBlueprint systemBlueprintCheck = getSystemConfiguration().getSystemBlueprint(systemBluePrintID);
						if (systemBlueprintCheck!=null && systemBlueprintCheck!=systemBlueprintInEdit) {
							systemBlueprintInEdit = systemBlueprintCheck;
						}
					}
					
					// --- Case separation 
					Object editedValue = getTableModelSystemBlueprints().getValueAt(row, col);
					switch (col) {
					case 0:
						// ----------------------------------------------------
						// --- Edited ID --------------------------------------
						// ----------------------------------------------------
						String newID = (String) editedValue;
						if (newID!=null && newID.isBlank()==false && newID.equals(systemBlueprintInEdit.getID())==false && getSystemConfiguration().getSystemBlueprint(newID)==null) {
							systemBlueprintInEdit.setID(newID);
							getSystemConfiguration().save();
						} else {
							// --- Undo edit ----------------------------------
							this.isPauseTableModelListener = true;
							getTableModelSystemBlueprints().setValueAt(systemBlueprintInEdit.getID(), row, 0);
							this.isPauseTableModelListener = false;
						}
						break;

					case 1:
						// ----------------------------------------------------
						// --- Edited description -----------------------------
						// ----------------------------------------------------
						if (editedValue!=null && editedValue.toString().isBlank()==false) {
							systemBlueprintInEdit.setDescription(editedValue.toString());
						} else {
							systemBlueprintInEdit.setDescription(null);
						}
						getSystemConfiguration().save();
						break;

					default:
						// ----------------------------------------------------
						// --- Edited boolean selection -----------------------
						// ----------------------------------------------------
						boolean selected = (boolean) editedValue;
						String eomSystemID = getTableModelSystemBlueprints().getColumnName(col);
						if (selected==true) {
							if (systemBlueprintInEdit.getEomSystemIdList().contains(eomSystemID)==false) {
								systemBlueprintInEdit.getEomSystemIdList().add(eomSystemID);
							}
						} else {
							systemBlueprintInEdit.getEomSystemIdList().remove(eomSystemID);
						}
						getSystemConfiguration().save();
						break;		
					}
				}
			};
		}
		return tmlSystemBlueprints;
	}
	
	/**
	 * Sets the SystemBlueprint tables column width.
	 */
	private void setJTableSystemBlueprintsColumnWidth() {
		
		TableColumnModel tcm = this.jTableSystemBlueprints.getColumnModel();
		
		tcm.getColumn(0).setMinWidth(100);
		tcm.getColumn(0).setMaxWidth(240);
		
		tcm.getColumn(1).setMinWidth(200);
		tcm.getColumn(1).setMaxWidth(300);
	}
	
	/**
	 * Returns the table for system blueprints.
	 * @return the j table system blueprints
	 */
	private JTable getJTableSystemBlueprints() {
		if (jTableSystemBlueprints == null) {
			jTableSystemBlueprints = new JTable(this.getTableModelSystemBlueprints());
			jTableSystemBlueprints.setFillsViewportHeight(true);
			jTableSystemBlueprints.getTableHeader().setReorderingAllowed(false);
			jTableSystemBlueprints.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTableSystemBlueprints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTableSystemBlueprints.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			jTableSystemBlueprints.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
			jTableSystemBlueprints.setRowHeight(20);
			
			jTableSystemBlueprints.setDefaultRenderer(Boolean.class, new TableRendererBoolean());
			this.setJTableSystemBlueprintsColumnWidth();
			
			jTableSystemBlueprints.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent lsEv) {
					if (lsEv.getValueIsAdjusting()==false) {
						int rowSelected = getJTableSystemBlueprints().getSelectedRow();
						if (rowSelected!=-1) {
							String systemBlueprintID = (String) getJTableSystemBlueprints().getValueAt(rowSelected, 0);
							systemBlueprintInEdit = getSystemConfiguration().getSystemBlueprint(systemBlueprintID);
						}
					}
				}
			});
			
		}
		return jTableSystemBlueprints;
	}
	
	/**
	 * Re-fills the table data for system blueprints.
	 */
	public void reFillTableSystemBlueprints() {
		
		// --- Remind selection ---------------------------
		int rowSelected = this.getJTableSystemBlueprints().getSelectedRow();
		// ---  Build table model -------------------------
		this.reBuildTableModelSystemBlueprints();
		// --- Fill the rows ------------------------------ 
		for (SystemBlueprint systemBlueprint : this.getSystemConfiguration().getSystemBlueprintList()) {
			this.addSystemBluePrintToTable(systemBlueprint);
		}
		// --- Reselect -----------------------------------
		if (rowSelected!=-1 && rowSelected<=(this.getJTableSystemBlueprints().getRowCount()-1)) {
			this.getJTableSystemBlueprints().setRowSelectionInterval(rowSelected, rowSelected);
		}
	}
	/**
	 * Adds the specified system blue print to table.
	 * @param systemBlueprint the system blueprint
	 */
	private void addSystemBluePrintToTable(SystemBlueprint systemBlueprint) {
		
		if (systemBlueprint==null) return;
		
		Vector<Object> row = new Vector<>();
		row.add(systemBlueprint.getID());
		row.add(systemBlueprint.getDescription());
		
		if (this.getTableModelSystemBlueprints().getColumnCount()>2) {
			for (int col = 2; col < this.getTableModelSystemBlueprints().getColumnCount(); col++) {
				String colName = this.getTableModelSystemBlueprints().getColumnName(col);
				row.add(systemBlueprint.getEomSystemIdList().contains(colName));
			}
		}
		this.getTableModelSystemBlueprints().addRow(row);
	}
	/**
	 * Sets the system blueprint selection.
	 * @param systemBlueprint the new system blueprint selection
	 */
	private void setSystemBlueprintSelection(SystemBlueprint systemBlueprint) {
		
		if (systemBlueprint==null) return;
		
		for (int row = 0; row < this.getJTableSystemBlueprints().getRowCount(); row++) {
			String idToCheck = (String) this.getJTableSystemBlueprints().getValueAt(row, 0);
			if (idToCheck!=null && idToCheck.equals(systemBlueprint.getID())) {
				this.getJTableSystemBlueprints().setRowSelectionInterval(row, row);
				return;
			}
		}
	}
	
	/**
	 * Returns the system configuration.
	 * @return the system configuration
	 */
	private SystemConfiguration getSystemConfiguration() {
		return this.systemConfigurationManager.getSystemConfiguration();
		
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		if (ae.getSource()==this.getJButtonAddEomSystem()) {
			// --- Add a new EOM system to the table ----------------
			EomSystem eomSystem = this.getSystemConfiguration().createEomSystem();
			this.getSystemConfiguration().getEomSystemList().add(eomSystem);
			this.reFillTableEomSystems();
			// --- Set selection to new EomSystem -------------------
			this.setEomSystemSelection(eomSystem);
			// --- ReFill system blueprints -------------------------
			this.reFillTableSystemBlueprints();
			
		} else if (ae.getSource()==this.getJButtonRemoveEomSystem()) {
			// --- Remove the currently selected EomSystem ----------
			int selectedRowIndex = this.getJTableEomSystems().getSelectedRow();
			if (selectedRowIndex!=-1) {
				EomSystem eomSystem = (EomSystem) this.getJTableEomSystems().getValueAt(selectedRowIndex, 0);
				this.getSystemConfiguration().removeEomSystem(eomSystem);
				this.getSystemConfiguration().save();
				// --- Update the UI --------------------------------
				this.reFillTableEomSystems();
				// --- Set selection to next EomSystem --------------
				if (this.getJTableEomSystems().getRowCount()>0) {
					if (selectedRowIndex > this.getJTableEomSystems().getRowCount()-1) {
						selectedRowIndex--;
					}
					this.getJTableEomSystems().setRowSelectionInterval(selectedRowIndex, selectedRowIndex);
				}
				// --- ReFill system blueprints ---------------------
				this.reFillTableSystemBlueprints();
			}
			
		} else if (ae.getSource()==this.getJButtonAddSystemBlueprint()) {
			// --- Add a new system blueprint -----------------------
			SystemBlueprint systemBlueprint = this.getSystemConfiguration().createSystemBlueprint();
			this.getSystemConfiguration().getSystemBlueprintList().add(systemBlueprint);
			this.reFillTableSystemBlueprints();
			this.setSystemBlueprintSelection(systemBlueprint);
			
		} else if (ae.getSource()==this.getJButtonRemoveSystemBlueprint()) {
			// --- Remove the selected SystemBlueprint --------------
			int selectedRowIndex = this.getJTableSystemBlueprints().getSelectedRow();
			if (selectedRowIndex!=-1) {
				String systemBluePrintID = (String) this.getJTableSystemBlueprints().getValueAt(selectedRowIndex, 0);
				SystemBlueprint systemBlueprint = this.getSystemConfiguration().getSystemBlueprint(systemBluePrintID);
				this.getSystemConfiguration().removeSystemBlueprint(systemBlueprint);
				this.getSystemConfiguration().save();
				// --- Update the UI --------------------------------
				this.reFillTableSystemBlueprints();
				// --- Set selection to next SystemBlueprint --------
				if (this.getJTableEomSystems().getRowCount()>0) {
					if (selectedRowIndex > this.getJTableSystemBlueprints().getRowCount()-1) {
						selectedRowIndex--;
					}
					this.getJTableSystemBlueprints().setRowSelectionInterval(selectedRowIndex, selectedRowIndex);
				}
			}

		}
	}
	
}
