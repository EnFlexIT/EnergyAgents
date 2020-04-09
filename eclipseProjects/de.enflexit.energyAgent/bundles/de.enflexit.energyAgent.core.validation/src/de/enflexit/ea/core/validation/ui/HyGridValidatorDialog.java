package de.enflexit.ea.core.validation.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.enflexit.ea.core.validation.BundleHelper;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationProcess;
import de.enflexit.ea.core.validation.HyGridValidationProcessListener;

import javax.swing.JTextArea;

/**
 * The Class HyGridValidatorDialog.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class HyGridValidatorDialog extends JDialog implements HyGridValidationProcessListener {
	
	private static final long serialVersionUID = 3712840252456645433L;
	
	private static final String PREF_VALIDATOR_DIALOG_X = "Validator-Dialog-X";
	private static final String PREF_VALIDATOR_DIALOG_Y = "Validator-Dialog-Y";
	private static final String PREF_VALIDATOR_DIALOG_WIDTH  = "Validator-Dialog-WIDTH";
	private static final String PREF_VALIDATOR_DIALOG_HEIGHT = "Validator-Dialog-HEIGHT";
	private static final String PREF_VALIDATOR_DIALOG_SPLIT_LOCATION = "Validator-Dialog-Split-Location";
	
	private HyGridValidationProcess hyGridValidationProcess;
	private HyGridValidationMessage hyGridMessage;
	
	private JLabel jLabelErrors;
	private JButton jButtonCheckAgain;
	
	private JSplitPane jSplitPaneHyGridMessages;
	
	private JScrollPane jScrollPaneList;
	private JList<HyGridValidationMessage> jListErrors;
	private DefaultListModel<HyGridValidationMessage> listModelErrors;
	
	private JScrollPane jScrollPaneDescription;
	private JTextArea jTextAreaDescription;

	private Timer sizePositionWaitTimer;
	
	
	/**
	 * Instantiates a new HyGridValidatorDialog.
	 *
	 * @param owner the owner Frame
	 * @param graphController the {@link OptionModelController}
	 */
	public HyGridValidatorDialog(Frame owner, HyGridValidationProcess hyGridValidationProcess) {
		super(owner);
		if (hyGridValidationProcess==null) {
			throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "] The instance of the " + HyGridValidationProcess.class.getSimpleName() + " is not allowed to be null!");
		}
		this.hyGridValidationProcess = hyGridValidationProcess;
		this.hyGridValidationProcess.addHyGridValidationProcessListener(this);
		this.initialize();
		this.setListModelMessages(this.hyGridValidationProcess.getHygridValidationMessages());
	}

	/**
	 * Returns the {@link HyGridValidationProcess}.
	 * @return the HyGridValidationProcess
	 */
	private HyGridValidationProcess getHyGridValidationProcess() {
		return hyGridValidationProcess;
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		
		this.setTitle("Agent.HyGrid - Setup Validator");
		
		this.loadAndApplyDialogSizeAndPosition();
		this.registerEscapeKeyStroke();
		this.addSizeAndPositionsListener();
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		GridBagConstraints gbc_lblErrors = new GridBagConstraints();
		gbc_lblErrors.anchor = GridBagConstraints.WEST;
		gbc_lblErrors.insets = new Insets(10, 10, 0, 10);
		gbc_lblErrors.gridx = 0;
		gbc_lblErrors.gridy = 0;
		getContentPane().add(getJLabelErrors(), gbc_lblErrors);
		
		GridBagConstraints gbc_jButtonCheckAgain = new GridBagConstraints();
		gbc_jButtonCheckAgain.anchor = GridBagConstraints.EAST;
		gbc_jButtonCheckAgain.insets = new Insets(10, 10, 0, 10);
		gbc_jButtonCheckAgain.gridx = 1;
		gbc_jButtonCheckAgain.gridy = 0;
		getContentPane().add(getJButtonCheckAgain(), gbc_jButtonCheckAgain);
		
		GridBagConstraints gbc_scrollPaneList = new GridBagConstraints();
		gbc_scrollPaneList.gridwidth = 2;
		gbc_scrollPaneList.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneList.insets = new Insets(5, 8, 15, 8);
		gbc_scrollPaneList.gridx = 0;
		gbc_scrollPaneList.gridy = 1;
		getContentPane().add(this.getJSplitPaneHyGridMessages(), gbc_scrollPaneList);
	}

	/**
     * Registers the escape key stroke in order to close this dialog.
     */
    private void registerEscapeKeyStroke() {
    	final ActionListener listener = new ActionListener() {
            public final void actionPerformed(final ActionEvent e) {
    			setVisible(false);
            }
        };
        final KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
        this.getRootPane().registerKeyboardAction(listener, keyStroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }
    
    /**
     * Adds the size and positions listener.
     */
    private void addSizeAndPositionsListener() {
    	this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				HyGridValidatorDialog.this.startSizePositionWaitTimer();
			}
			@Override
			public void componentMoved(ComponentEvent e) {
				HyGridValidatorDialog.this.startSizePositionWaitTimer();
			}
		});
    }
    /**
     * Starts (or restarts) the size position wait timer.
     */
    private void startSizePositionWaitTimer() {
    	if (this.getSizePositionWaitTimer().isRunning()==true) {
    		this.getSizePositionWaitTimer().restart();
		} else {
			this.getSizePositionWaitTimer().start();
		}
    }
    /**
     * Returns the size position wait timer.
     * @return the size position wait timer
     */
	private Timer getSizePositionWaitTimer() {
		if (sizePositionWaitTimer==null) {
			sizePositionWaitTimer = new Timer(500, new ActionListener() {
				/* (non-Javadoc)
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed(ActionEvent ae) {
					HyGridValidatorDialog.this.saveDialogSizeAndPosition();
				}
			});
			sizePositionWaitTimer.setRepeats(false);
		}
		return sizePositionWaitTimer;
	}
    
    /**
     * Save dialog size and position.
     */
    private void saveDialogSizeAndPosition() {
    
    	if (this.isVisible()==false) return;
    	
    	Point dialogPos = this.getLocationOnScreen();
    	BundleHelper.getEclipsePreferences().putInt(PREF_VALIDATOR_DIALOG_X, dialogPos.x);
    	BundleHelper.getEclipsePreferences().putInt(PREF_VALIDATOR_DIALOG_Y, dialogPos.y);
    	BundleHelper.getEclipsePreferences().putInt(PREF_VALIDATOR_DIALOG_WIDTH,  this.getWidth());
    	BundleHelper.getEclipsePreferences().putInt(PREF_VALIDATOR_DIALOG_HEIGHT, this.getHeight());
    	BundleHelper.getEclipsePreferences().putInt(PREF_VALIDATOR_DIALOG_SPLIT_LOCATION, this.getJSplitPaneHyGridMessages().getDividerLocation());
    	BundleHelper.saveEclipsePreferences();
    }
    /**
     * Load and apply dialog size and position.
     */
    private void loadAndApplyDialogSizeAndPosition() {

    	int width  = BundleHelper.getEclipsePreferences().getInt(PREF_VALIDATOR_DIALOG_WIDTH, 0);
    	int height = BundleHelper.getEclipsePreferences().getInt(PREF_VALIDATOR_DIALOG_HEIGHT, 0);
    	if (height==0 || width==0) {
    		this.setSize(600, 400);	
    	} else {
    		this.setSize(width, height);
    	}
    	
    	int posX = BundleHelper.getEclipsePreferences().getInt(PREF_VALIDATOR_DIALOG_X, 0);
    	int posY = BundleHelper.getEclipsePreferences().getInt(PREF_VALIDATOR_DIALOG_Y, 0);
    	if (posX==0 || posY==0) {
    		this.setLocationRelativeTo(null);
    	} else {
    		this.setLocation(posX, posY);
    	}
    	
    	int dividerLoc = BundleHelper.getEclipsePreferences().getInt(PREF_VALIDATOR_DIALOG_SPLIT_LOCATION, 0);
    	if (dividerLoc!=0) {
    		this.getJSplitPaneHyGridMessages().setDividerLocation(dividerLoc);
    	}
    }
    
	private JLabel getJLabelErrors() {
		if (jLabelErrors == null) {
			jLabelErrors = new JLabel("Found for the current setup:");
			jLabelErrors.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelErrors;
	}
	private JButton getJButtonCheckAgain() {
		if (jButtonCheckAgain == null) {
			jButtonCheckAgain = new JButton("");
			jButtonCheckAgain.setToolTipText("Redo validation");
			jButtonCheckAgain.setSize(new Dimension(26, 26));
			jButtonCheckAgain.setPreferredSize(new Dimension(26, 26));
			jButtonCheckAgain.setIcon(BundleHelper.getImageIcon("Refresh.png"));
			jButtonCheckAgain.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					HyGridValidatorDialog.this.validateCurrentSetup();
				}
			});
		}
		return jButtonCheckAgain;
	}
	
	public JSplitPane getJSplitPaneHyGridMessages() {
		if (jSplitPaneHyGridMessages==null) {
			jSplitPaneHyGridMessages = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			jSplitPaneHyGridMessages.setResizeWeight(0.5);
			jSplitPaneHyGridMessages.setDividerLocation(0.7);
			jSplitPaneHyGridMessages.setContinuousLayout(true);
			jSplitPaneHyGridMessages.setDividerSize(5);
			jSplitPaneHyGridMessages.setOneTouchExpandable(false);
			jSplitPaneHyGridMessages.setTopComponent(this.getJScrollPaneList());
			jSplitPaneHyGridMessages.setBottomComponent(this.getJScrollPaneDescription());
			jSplitPaneHyGridMessages.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					HyGridValidatorDialog.this.startSizePositionWaitTimer();
				}
			});
		}
		return jSplitPaneHyGridMessages;
	}
	
	private JScrollPane getJScrollPaneList() {
		if (jScrollPaneList == null) {
			jScrollPaneList = new JScrollPane(this.getJListErrors());
		}
		return jScrollPaneList;
	}
	private JList<HyGridValidationMessage> getJListErrors() {
		if (jListErrors == null) {
			jListErrors = new JList<HyGridValidationMessage>(this.getListModelErrors());
			jListErrors.setFont(new Font("Dialog", Font.PLAIN, 12));
			jListErrors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jListErrors.setCellRenderer(new DefaultListCellRenderer() {
				private static final long serialVersionUID = 3066991260053506020L;
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					HyGridValidationMessage hvm = (HyGridValidationMessage) value;
					switch (hvm.getMessageType()) {
					case Information:
						comp.setForeground(Color.DARK_GRAY);
						break;
					case Warning:
						comp.setForeground(new Color(153, 153, 0));
						break;
					case Error:
						comp.setForeground(Color.RED);
						break;
					}
					return comp;
				}
			});
			jListErrors.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent lsEv) {
					if (lsEv.getValueIsAdjusting()==false) {
						HyGridValidationMessage hyMess = jListErrors.getSelectedValue();
						HyGridValidatorDialog.this.setHyGridValidationMessage(hyMess);
					}
				}
			});
		}
		return jListErrors;
	}
	public DefaultListModel<HyGridValidationMessage> getListModelErrors() {
		if (listModelErrors==null) {
			listModelErrors = new DefaultListModel<HyGridValidationMessage>();
		}
		return listModelErrors;
	}
	/**
	 * Updates the DefaultListModel with the specified {@link HyGridValidationMessage}'s.
	 * @param vMessages the ArrayList of {@link HyGridValidationMessage}'s 
	 */
	private void setListModelMessages(List<HyGridValidationMessage> vMessages) {
		// --- Empty list first -----------------------------
		this.getListModelErrors().removeAllElements();
		// --- Fill DeafultListModel with messages ----------
		for (int i = 0; i < vMessages.size(); i++) {
			this.getListModelErrors().addElement(vMessages.get(i));
		}
	}
	
	private JScrollPane getJScrollPaneDescription() {
		if (jScrollPaneDescription==null) {
			jScrollPaneDescription = new JScrollPane(this.getJTextAreaDescription());
		}
		return jScrollPaneDescription;
	}
	private JTextArea getJTextAreaDescription() {
		if (jTextAreaDescription == null) {
			jTextAreaDescription = new JTextArea();
			jTextAreaDescription.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextAreaDescription.setEditable(false);
		}
		return jTextAreaDescription;
	}
	
	
	/**
	 * Sets the currently selected HyGridValidationMessage.
	 * @param newHyGridMessage the HyGridValidationMessage
	 */
	private void setHyGridValidationMessage(HyGridValidationMessage newHyGridMessage) {
		this.hyGridMessage = newHyGridMessage;
		if (this.hyGridMessage==null) {
			this.getJTextAreaDescription().setText(null);
		} else {
			this.getJTextAreaDescription().setText(this.hyGridMessage.getDescription());
		}
		this.getJTextAreaDescription().setCaretPosition(0);
	}
	
	/**
	 * Validate current setup.
	 */
	public void validateCurrentSetup() {
		
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		// --- Do the validation of the TechnicalSystem -----
		this.getHyGridValidationProcess().validateCurrentSetup();
		// --- Transfer the errors to the user display ------
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				HyGridValidatorDialog.this.setHyGridValidationMessage(null);
				HyGridValidatorDialog.this.setListModelMessages(HyGridValidatorDialog.this.hyGridValidationProcess.getHygridValidationMessages());
			}
		});
		// --- Reset cursor ---------------------------------
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationProcessListener#processExecuted()
	 */
	@Override
	public void processExecuted() {
		HyGridValidatorDialog.this.setHyGridValidationMessage(null);
		this.getListModelErrors().removeAllElements();
	}
	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationProcessListener#messageReceived(net.agenthygrid.core.validation.HyGridValidationMessage)
	 */
	@Override
	public void messageReceived(HyGridValidationMessage message) {
		// --- Nothing to do here ---
	}
	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationProcessListener#processFinalized()
	 */
	@Override
	public void processFinalized() {
		HyGridValidatorDialog.this.setHyGridValidationMessage(null);
		HyGridValidatorDialog.this.setListModelMessages(HyGridValidatorDialog.this.hyGridValidationProcess.getHygridValidationMessages());
	}
	
}
