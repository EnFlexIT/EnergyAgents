package de.enflexit.ea.core.configuration.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import de.enflexit.awb.core.Application;
import de.enflexit.awb.core.ApplicationListener;
import de.enflexit.common.swing.WindowSizeAndPostionController;
import de.enflexit.common.swing.WindowSizeAndPostionController.JDialogPosition;
import de.enflexit.ea.core.configuration.BundleHelper;

/**
 * The Class SetupConfigurationDialog.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class SetupConfigurationDialog extends JDialog implements ApplicationListener {
	
	private static final long serialVersionUID = 3712840252456645433L;
	
	private static final String PREF_CONFIGURATOR_DIALOG_X = "Configurator-Dialog-X";
	private static final String PREF_CONFIGURATOR_DIALOG_Y = "Configurator-Dialog-Y";
	private static final String PREF_CONFIGURATOR_DIALOG_WIDTH  = "Configurator-Dialog-WIDTH";
	private static final String PREF_CONFIGURATOR_DIALOG_HEIGHT = "Configurator-Dialog-HEIGHT";

	private Timer sizePositionWaitTimer;
	private SetupConfigurationPanel setupConfigurationPanel;
	
	/**
	 * Instantiates a new SetupConfigurationDialog.
	 * @param owner the owner Window
	 */
	public SetupConfigurationDialog(Window owner) {
		super(owner);
		this.initialize();
	}
	/**
	 * Initialize.
	 */
	private void initialize() {
		
		this.setTitle("Energy Agent - Setup Configurator");
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		this.loadAndApplyDialogSizeAndPosition();
		this.registerEscapeKeyStroke();
		this.addSizeAndPositionsListener();

		// --- Add local listener -------------------------
		Application.addApplicationListener(this);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		this.getContentPane().setLayout(gridBagLayout);
		
		GridBagConstraints gbc_setupConfigurationPanel = new GridBagConstraints();
		gbc_setupConfigurationPanel.insets = new Insets(10, 10, 10, 10);
		gbc_setupConfigurationPanel.fill = GridBagConstraints.BOTH;
		gbc_setupConfigurationPanel.gridx = 0;
		gbc_setupConfigurationPanel.gridy = 0;
		this.getContentPane().add(getSetupConfigurationPanel(), gbc_setupConfigurationPanel);

		WindowSizeAndPostionController.setJDialogPositionOnScreen(this, JDialogPosition.ParentBottomRight);
		this.setVisible(true);
	}

	private SetupConfigurationPanel getSetupConfigurationPanel() {
		if (setupConfigurationPanel == null) {
			setupConfigurationPanel = new SetupConfigurationPanel();
		}
		return setupConfigurationPanel;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Window#dispose()
	 */
	@Override
	public void dispose() {
		Application.removeApplicationListener(this);
		this.getSetupConfigurationPanel().dispose();
		super.dispose();
	}
	
	/**
     * Registers the escape key stroke in order to close this dialog.
     */
    private void registerEscapeKeyStroke() {
    	final ActionListener listener = new ActionListener() {
            public final void actionPerformed(final ActionEvent e) {
            	SetupConfigurationDialog.this.setVisible(false);
            	SetupConfigurationDialog.this.dispose();
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
				SetupConfigurationDialog.this.startSizePositionWaitTimer();
			}
			@Override
			public void componentMoved(ComponentEvent e) {
				SetupConfigurationDialog.this.startSizePositionWaitTimer();
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
					SetupConfigurationDialog.this.saveDialogSizeAndPosition();
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
    	BundleHelper.getEclipsePreferences().putInt(PREF_CONFIGURATOR_DIALOG_X, dialogPos.x);
    	BundleHelper.getEclipsePreferences().putInt(PREF_CONFIGURATOR_DIALOG_Y, dialogPos.y);
    	BundleHelper.getEclipsePreferences().putInt(PREF_CONFIGURATOR_DIALOG_WIDTH,  this.getWidth());
    	BundleHelper.getEclipsePreferences().putInt(PREF_CONFIGURATOR_DIALOG_HEIGHT, this.getHeight());
    	BundleHelper.saveEclipsePreferences();
    }
    /**
     * Load and apply dialog size and position.
     */
    private void loadAndApplyDialogSizeAndPosition() {

    	int width  = BundleHelper.getEclipsePreferences().getInt(PREF_CONFIGURATOR_DIALOG_WIDTH, 0);
    	int height = BundleHelper.getEclipsePreferences().getInt(PREF_CONFIGURATOR_DIALOG_HEIGHT, 0);
    	if (height==0 || width==0) {
    		this.setSize(600, 400);	
    	} else {
    		this.setSize(width, height);
    	}
    	
    	int posX = BundleHelper.getEclipsePreferences().getInt(PREF_CONFIGURATOR_DIALOG_X, 0);
    	int posY = BundleHelper.getEclipsePreferences().getInt(PREF_CONFIGURATOR_DIALOG_Y, 0);
    	if (posX==0 || posY==0) {
    		this.setLocationRelativeTo(null);
    	} else {
    		this.setLocation(posX, posY);
    	}
    }
	
	/* (non-Javadoc)
	 * @see agentgui.core.application.ApplicationListener#onApplicationEvent(agentgui.core.application.ApplicationListener.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent awbEvent) {
		
		if (awbEvent.getApplicationEvent()==ApplicationEvent.PROJECT_CLOSED) {
			this.dispose();
		}
	}
	
}
