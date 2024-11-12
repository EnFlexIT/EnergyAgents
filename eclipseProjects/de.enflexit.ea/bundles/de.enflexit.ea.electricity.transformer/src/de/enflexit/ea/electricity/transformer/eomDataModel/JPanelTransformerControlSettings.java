package de.enflexit.ea.electricity.transformer.eomDataModel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.NetworkComponentSelectionDialog;
import org.awb.env.networkModel.controller.ui.NetworkComponentTableService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import agentgui.core.application.Application;
import agentgui.core.project.Project;
import de.enflexit.common.swing.KeyAdapter4Numbers;
import de.enflexit.ea.electricity.ElectricityDomainIdentification;
import de.enflexit.ea.electricity.transformer.TransformerBundleHelper;
import de.enflexit.ea.electricity.transformer.TransformerCharacteristicsHandler;
import de.enflexit.ea.electricity.transformer.TransformerDataModel;
import de.enflexit.language.Language;
import energy.GlobalInfo;
import energy.helper.NumberHelper;

/**
 * The Class JPanelTransformerControlSettings.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelTransformerControlSettings extends JPanel implements ActionListener {

	private static final long serialVersionUID = -216076966305264677L;

	private static final String XY_SERIES_NAME = "Transformer Characteristics";
	private static final float XY_SERIES_LINE_WIDTH= 3f;
	
	private final Dimension jButtonSize = new Dimension(26, 26);
	
	
	private boolean debugCharacteristicsCalculation = false;
	
	private JDialogTransformerDataModel transformerDialog;

	private JCheckBox jCheckBoxControlNodeActivation;
	private JLabel jLabelNodeID;
	private JTextField jTextFieldNodeID;
	private JButton jButtonNodeID;
	private JLabel jLabelControlNodeBoundaries;
	private JLabel jLabelControlNodeUpperBoundary;
	private JLabel jLabelControlNodeLowerBoundary;
	private JTextField jTextFieldControlNodeUpperBoundary;
	private JTextField jTextFieldControlNodeLowerBoundary;
	
	private JSeparator jSeparator;

	private JCheckBox jCheckBoxControlResiudualLoadBased;
	private JButton jButtonImportCharacteristics;

	private ChartPanel JFreeChartChartPanel;
	private JFreeChart characteristicsChart;
	private XYSeriesCollection xySeriesCollection; 
	private JTextField jTextFieldControlResidualAllowedDeviation;
	private JButton jButtonDeleteCharacteristics;
	private JLabel jLabelControlResidualAllowedDeviation;
	
	
	
	/**
	 * Instantiates a new battery static model dialog.
	 *
	 * @param owner the owner
	 * @param staticModel the static model
	 */
	public JPanelTransformerControlSettings(JDialogTransformerDataModel transformerDialog) {
		this.transformerDialog = transformerDialog;
		this.initialize();
		this.loadDataModelToDialog();
	}
	private void initialize(){
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		this.setLayout(gridBagLayout);
		GridBagConstraints gbc_jCheckBoxControlNodeActivation = new GridBagConstraints();
		gbc_jCheckBoxControlNodeActivation.insets = new Insets(10, 10, 0, 10);
		gbc_jCheckBoxControlNodeActivation.anchor = GridBagConstraints.WEST;
		gbc_jCheckBoxControlNodeActivation.gridwidth = 4;
		gbc_jCheckBoxControlNodeActivation.gridx = 0;
		gbc_jCheckBoxControlNodeActivation.gridy = 0;
		add(getJCheckBoxControlNodeActivation(), gbc_jCheckBoxControlNodeActivation);
		GridBagConstraints gbc_jLabelNodeID = new GridBagConstraints();
		gbc_jLabelNodeID.insets = new Insets(5, 10, 0, 0);
		gbc_jLabelNodeID.anchor = GridBagConstraints.WEST;
		gbc_jLabelNodeID.gridx = 0;
		gbc_jLabelNodeID.gridy = 1;
		add(getJLabelNodeID(), gbc_jLabelNodeID);
		GridBagConstraints gbc_jTextFieldNodeID = new GridBagConstraints();
		gbc_jTextFieldNodeID.gridwidth = 2;
		gbc_jTextFieldNodeID.insets = new Insets(5, 5, 0, 0);
		gbc_jTextFieldNodeID.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldNodeID.gridx = 1;
		gbc_jTextFieldNodeID.gridy = 1;
		add(getJTextFieldNodeID(), gbc_jTextFieldNodeID);
		GridBagConstraints gbc_jButtonNodeID = new GridBagConstraints();
		gbc_jButtonNodeID.insets = new Insets(5, 2, 0, 10);
		gbc_jButtonNodeID.gridx = 3;
		gbc_jButtonNodeID.gridy = 1;
		add(getJButtonNodeID(), gbc_jButtonNodeID);
		GridBagConstraints gbc_jLabelControlNodeBoundaries = new GridBagConstraints();
		gbc_jLabelControlNodeBoundaries.insets = new Insets(5, 10, 0, 10);
		gbc_jLabelControlNodeBoundaries.anchor = GridBagConstraints.WEST;
		gbc_jLabelControlNodeBoundaries.gridwidth = 4;
		gbc_jLabelControlNodeBoundaries.gridx = 0;
		gbc_jLabelControlNodeBoundaries.gridy = 2;
		add(getJLabelControlNodeBoundaries(), gbc_jLabelControlNodeBoundaries);
		GridBagConstraints gbc_jLabelControlNodeUpperBoundary = new GridBagConstraints();
		gbc_jLabelControlNodeUpperBoundary.anchor = GridBagConstraints.WEST;
		gbc_jLabelControlNodeUpperBoundary.insets = new Insets(5, 10, 0, 0);
		gbc_jLabelControlNodeUpperBoundary.gridx = 0;
		gbc_jLabelControlNodeUpperBoundary.gridy = 3;
		add(getJLabelControlNodeUpperBoundary(), gbc_jLabelControlNodeUpperBoundary);
		GridBagConstraints gbc_jTextFieldControlNodeUpperBoundary = new GridBagConstraints();
		gbc_jTextFieldControlNodeUpperBoundary.gridwidth = 2;
		gbc_jTextFieldControlNodeUpperBoundary.insets = new Insets(5, 5, 0, 0);
		gbc_jTextFieldControlNodeUpperBoundary.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldControlNodeUpperBoundary.gridx = 1;
		gbc_jTextFieldControlNodeUpperBoundary.gridy = 3;
		add(getJTextFieldControlNodeUpperBoundary(), gbc_jTextFieldControlNodeUpperBoundary);
		GridBagConstraints gbc_jLabelControlNodeLowerBoundary = new GridBagConstraints();
		gbc_jLabelControlNodeLowerBoundary.anchor = GridBagConstraints.WEST;
		gbc_jLabelControlNodeLowerBoundary.insets = new Insets(5, 10, 0, 0);
		gbc_jLabelControlNodeLowerBoundary.gridx = 0;
		gbc_jLabelControlNodeLowerBoundary.gridy = 4;
		add(getJLabelControlNodeLowerBoundary(), gbc_jLabelControlNodeLowerBoundary);
		GridBagConstraints gbc_jTextFieldControlNodeLowerBoundary = new GridBagConstraints();
		gbc_jTextFieldControlNodeLowerBoundary.gridwidth = 2;
		gbc_jTextFieldControlNodeLowerBoundary.insets = new Insets(5, 5, 0, 0);
		gbc_jTextFieldControlNodeLowerBoundary.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldControlNodeLowerBoundary.gridx = 1;
		gbc_jTextFieldControlNodeLowerBoundary.gridy = 4;
		add(getJTextFieldControlNodeLowerBoundary(), gbc_jTextFieldControlNodeLowerBoundary);
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.insets = new Insets(10, 10, 5, 10);
		gbc_separator.gridwidth = 4;
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 5;
		add(getJSeparator(), gbc_separator);
		GridBagConstraints gbc_jCheckBoxControlResiudualLoadBased = new GridBagConstraints();
		gbc_jCheckBoxControlResiudualLoadBased.insets = new Insets(5, 10, 0, 0);
		gbc_jCheckBoxControlResiudualLoadBased.anchor = GridBagConstraints.WEST;
		gbc_jCheckBoxControlResiudualLoadBased.gridwidth = 2;
		gbc_jCheckBoxControlResiudualLoadBased.gridx = 0;
		gbc_jCheckBoxControlResiudualLoadBased.gridy = 6;
		add(getJCheckBoxControlResiudualLoadBased(), gbc_jCheckBoxControlResiudualLoadBased);
		GridBagConstraints gbc_jButtonImportCharacteristics_1 = new GridBagConstraints();
		gbc_jButtonImportCharacteristics_1.insets = new Insets(5, 2, 0, 0);
		gbc_jButtonImportCharacteristics_1.gridx = 2;
		gbc_jButtonImportCharacteristics_1.gridy = 6;
		add(getJButtonDeleteCharacteristics(), gbc_jButtonImportCharacteristics_1);
		GridBagConstraints gbc_jButtonImportCharacteristics = new GridBagConstraints();
		gbc_jButtonImportCharacteristics.insets = new Insets(5, 2, 0, 10);
		gbc_jButtonImportCharacteristics.gridx = 3;
		gbc_jButtonImportCharacteristics.gridy = 6;
		add(getJButtonImportCharacteristics(), gbc_jButtonImportCharacteristics);
		GridBagConstraints gbc_jLabelAllowedDeviation = new GridBagConstraints();
		gbc_jLabelAllowedDeviation.insets = new Insets(5, 10, 0, 0);
		gbc_jLabelAllowedDeviation.anchor = GridBagConstraints.WEST;
		gbc_jLabelAllowedDeviation.gridx = 0;
		gbc_jLabelAllowedDeviation.gridy = 7;
		add(getJLabelControlResidualAllowedDeviation(), gbc_jLabelAllowedDeviation);
		GridBagConstraints gbc_jCheckBoxAddLossesToResiudalLoad = new GridBagConstraints();
		gbc_jCheckBoxAddLossesToResiudalLoad.gridwidth = 2;
		gbc_jCheckBoxAddLossesToResiudalLoad.fill = GridBagConstraints.HORIZONTAL;
		gbc_jCheckBoxAddLossesToResiudalLoad.insets = new Insets(5, 5, 0, 0);
		gbc_jCheckBoxAddLossesToResiudalLoad.gridx = 1;
		gbc_jCheckBoxAddLossesToResiudalLoad.gridy = 7;
		add(getJTextFieldControlResidualAllowedDeviation(), gbc_jCheckBoxAddLossesToResiudalLoad);
		GridBagConstraints gbc_JFreeChartChartPanel = new GridBagConstraints();
		gbc_JFreeChartChartPanel.insets = new Insets(5, 10, 5, 10);
		gbc_JFreeChartChartPanel.gridwidth = 4;
		gbc_JFreeChartChartPanel.fill = GridBagConstraints.BOTH;
		gbc_JFreeChartChartPanel.gridx = 0;
		gbc_JFreeChartChartPanel.gridy = 8;
		add(getJFreeChartChartPanel(), gbc_JFreeChartChartPanel);
		
		
	}
	
	public TransformerDataModel getTransformerDataModel() {
		if (transformerDialog!=null) {
			return transformerDialog.getTransformerDataModel();
		}
		return null;
	}
	public void setTransformerDataModel(TransformerDataModel transformerDataModel) {
		if (transformerDialog!=null) {
			this.transformerDialog.setTransformerDataModel(transformerDataModel);
		}
	}
	
	/**
	 * Load the data model to the dialog.
	 */
	public void loadDataModelToDialog(){
		
		if (this.getTransformerDataModel()==null) return;
		
		this.getJCheckBoxControlNodeActivation().setSelected(this.getTransformerDataModel().isControlBasedOnNodeVoltage());
		this.getJTextFieldNodeID().setText(this.getTransformerDataModel().getControlNodeID());
		
		this.getJTextFieldControlNodeUpperBoundary().setText(this.getTransformerDataModel().getControlNodeUpperVoltageLevel() + "");
		this.getJTextFieldControlNodeLowerBoundary().setText(this.getTransformerDataModel().getControlNodeLowerVoltageLevel() + "");
		
		
		this.getJCheckBoxControlResiudualLoadBased().setSelected(this.getTransformerDataModel().isControlBasedOnCharacteristics());
		this.getJTextFieldControlResidualAllowedDeviation().setText(this.getTransformerDataModel().getControlCharacteristicsAllowedDeviation() + "");
		
		XYSeries charSeries = this.getTransformerDataModel().getControlCharacteristicsXySeries();
		if (charSeries!=null) {
			this.setXySeries(charSeries);
			if (this.debugCharacteristicsCalculation==true) {
				// --- A single test call is enough -------
				this.debugCharacteristicsCalculation = false;
				this.debugCharacteristics();
			}
		}

		this.updateVisualization();
	}
	/**
	 * This method will call the Characteristics calculation and print its results to the console .
	 */
	private void debugCharacteristics() {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				TransformerCharacteristicsHandler tch = JPanelTransformerControlSettings.this.getTransformerDataModel().getTransformerCharacteristicsHandler();

				double[] checkValues = {100, 40, 20, 10, 0, -9, -10, -100, -240, -241, -250};
				for (int i = 0; i < checkValues.length; i++) {
					double residualLoad = checkValues[i];
					double u_to_un_In_Percent = NumberHelper.round(tch.getVoltageToNominalVoltageLevelInPercent(residualLoad), 2);
					double targetVoltageLevel = NumberHelper.round(tch.getTargetVoltageLevelForLowVoltageLoadInV(residualLoad), 2); 
					System.out.println("Residual load: " + checkValues[i] + " \t=> U/Un " + u_to_un_In_Percent + " % \t=> target voltage level " + targetVoltageLevel + " V");
				}
			}
		});
	}
	
	/**
	 * Loads the dialog data to the data model.
	 */
	public void loadDialogDataToDataModel() {
		
		this.getTransformerDataModel().setControlBasedOnNodeVoltage(this.getJCheckBoxControlNodeActivation().isSelected());
		this.getTransformerDataModel().setControlNodeID(this.getJTextFieldNodeID().getText().trim());
		
		this.getTransformerDataModel().setControlNodeUpperVoltageLevel(this.transformerDialog.getDoubleValue(this.getJTextFieldControlNodeUpperBoundary()));
		this.getTransformerDataModel().setControlNodeLowerVoltageLevel(this.transformerDialog.getDoubleValue(this.getJTextFieldControlNodeLowerBoundary()));
		
		
		this.getTransformerDataModel().setControlBasedOnCharacteristics(this.getJCheckBoxControlResiudualLoadBased().isSelected());
		this.getTransformerDataModel().setControlCharacteristicsAllowedDeviation(this.transformerDialog.getDoubleValue(this.getJTextFieldControlResidualAllowedDeviation()));
		XYSeries charSeries = null;
		if (this.getXySeriesCollection().getSeriesCount()>0) {
			charSeries = this.getXySeriesCollection().getSeries(0);
		}
		this.getTransformerDataModel().setControlCharacteristicsXySeries(charSeries);
	}
	
	
	private JCheckBox getJCheckBoxControlNodeActivation() {
		if (jCheckBoxControlNodeActivation == null) {
			jCheckBoxControlNodeActivation = new JCheckBox("Control transformer based on node voltage");
			jCheckBoxControlNodeActivation.setFont(new Font("Dialog", Font.BOLD, 11));
			jCheckBoxControlNodeActivation.addActionListener(this);
		}
		return jCheckBoxControlNodeActivation;
	}
	private JLabel getJLabelNodeID() {
		if (jLabelNodeID == null) {
			jLabelNodeID = new JLabel("Node-ID");
			jLabelNodeID.setFont(new Font("Dialog", Font.BOLD, 11));
		}
		return jLabelNodeID;
	}
	private JTextField getJTextFieldNodeID() {
		if (jTextFieldNodeID == null) {
			jTextFieldNodeID = new JTextField();
			jTextFieldNodeID.setPreferredSize(new Dimension(60, 24));
			jTextFieldNodeID.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldNodeID.setEditable(false);
			jTextFieldNodeID.addActionListener(this);
		}
		return jTextFieldNodeID;
	}
	private JButton getJButtonNodeID() {
		if (jButtonNodeID == null) {
			jButtonNodeID = new JButton();
			jButtonNodeID.setIcon(TransformerBundleHelper.getImageIcon("Search.png"));
			jButtonNodeID.setFont(new Font("Dialog", Font.PLAIN, 11));
			jButtonNodeID.setPreferredSize(this.jButtonSize);
			jButtonNodeID.setSize(this.jButtonSize);
			jButtonNodeID.setMinimumSize(this.jButtonSize);
			jButtonNodeID.addActionListener(this);
		}
		return jButtonNodeID;
	}
	
	private JLabel getJLabelControlNodeBoundaries() {
		if (jLabelControlNodeBoundaries == null) {
			jLabelControlNodeBoundaries = new JLabel("Voltage boundaries [%]");
			jLabelControlNodeBoundaries.setFont(new Font("Dialog", Font.BOLD, 11));
		}
		return jLabelControlNodeBoundaries;
	}
	private JLabel getJLabelControlNodeUpperBoundary() {
		if (jLabelControlNodeUpperBoundary == null) {
			jLabelControlNodeUpperBoundary = new JLabel("Upper limit [%]");
			jLabelControlNodeUpperBoundary.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelControlNodeUpperBoundary;
	}
	private JLabel getJLabelControlNodeLowerBoundary() {
		if (jLabelControlNodeLowerBoundary == null) {
			jLabelControlNodeLowerBoundary = new JLabel("Lower limit [%]");
			jLabelControlNodeLowerBoundary.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelControlNodeLowerBoundary;
	}
	private JTextField getJTextFieldControlNodeUpperBoundary() {
		if (jTextFieldControlNodeUpperBoundary == null) {
			jTextFieldControlNodeUpperBoundary = new JTextField();
			jTextFieldControlNodeUpperBoundary.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldControlNodeUpperBoundary.setPreferredSize(new Dimension(60, 24));
			jTextFieldControlNodeUpperBoundary.addKeyListener(new KeyAdapter4Numbers(true));
		}
		return jTextFieldControlNodeUpperBoundary;
	}
	private JTextField getJTextFieldControlNodeLowerBoundary() {
		if (jTextFieldControlNodeLowerBoundary == null) {
			jTextFieldControlNodeLowerBoundary = new JTextField();
			jTextFieldControlNodeLowerBoundary.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldControlNodeLowerBoundary.setPreferredSize(new Dimension(60, 24));
			jTextFieldControlNodeLowerBoundary.addKeyListener(new KeyAdapter4Numbers(true));
		}
		return jTextFieldControlNodeLowerBoundary;
	}
	
	
	private JSeparator getJSeparator() {
		if (jSeparator == null) {
			jSeparator = new JSeparator();
		}
		return jSeparator;
	}
	
	
	private JCheckBox getJCheckBoxControlResiudualLoadBased() {
		if (jCheckBoxControlResiudualLoadBased == null) {
			jCheckBoxControlResiudualLoadBased = new JCheckBox("Control transformer based on control characteristics");
			jCheckBoxControlResiudualLoadBased.setFont(new Font("Dialog", Font.BOLD, 11));
			jCheckBoxControlResiudualLoadBased.addActionListener(this);
		}
		return jCheckBoxControlResiudualLoadBased;
	}
	private JButton getJButtonImportCharacteristics() {
		if (jButtonImportCharacteristics == null) {
			jButtonImportCharacteristics = new JButton();
			jButtonImportCharacteristics.setIcon(TransformerBundleHelper.getImageIcon("MBopen.png"));
			jButtonImportCharacteristics.setPreferredSize(this.jButtonSize);
			jButtonImportCharacteristics.setSize(this.jButtonSize);
			jButtonImportCharacteristics.setMinimumSize(this.jButtonSize);
			jButtonImportCharacteristics.setFont(new Font("Dialog", Font.PLAIN, 11));
			jButtonImportCharacteristics.setToolTipText("Import characteristics data ...");
			jButtonImportCharacteristics.addActionListener(this);
		}
		return jButtonImportCharacteristics;
	}
	private JButton getJButtonDeleteCharacteristics() {
		if (jButtonDeleteCharacteristics == null) {
			jButtonDeleteCharacteristics = new JButton();
			jButtonDeleteCharacteristics.setIcon(TransformerBundleHelper.getImageIcon("Delete.png"));
			jButtonDeleteCharacteristics.setToolTipText("Delete characteristics data");
			jButtonDeleteCharacteristics.setPreferredSize(this.jButtonSize);
			jButtonDeleteCharacteristics.setSize(this.jButtonSize);
			jButtonDeleteCharacteristics.setMinimumSize(this.jButtonSize);
			jButtonDeleteCharacteristics.setFont(new Font("Dialog", Font.PLAIN, 11));
			jButtonDeleteCharacteristics.addActionListener(this);
		}
		return jButtonDeleteCharacteristics;
	}
	
	private JLabel getJLabelControlResidualAllowedDeviation() {
		if (jLabelControlResidualAllowedDeviation == null) {
			jLabelControlResidualAllowedDeviation = new JLabel("Allowed Deviation [%]");
			jLabelControlResidualAllowedDeviation.setFont(new Font("Dialog", Font.PLAIN, 11));
		}
		return jLabelControlResidualAllowedDeviation;
	}
	private JTextField getJTextFieldControlResidualAllowedDeviation() {
		if (jTextFieldControlResidualAllowedDeviation == null) {
			jTextFieldControlResidualAllowedDeviation = new JTextField();
			jTextFieldControlResidualAllowedDeviation.setFont(new Font("Dialog", Font.PLAIN, 11));
			jTextFieldControlResidualAllowedDeviation.setPreferredSize(new Dimension(60, 24));
			jTextFieldControlResidualAllowedDeviation.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent de) {
					JPanelTransformerControlSettings.this.updateCharacteristicsChart();
				}
				@Override
				public void insertUpdate(DocumentEvent de) {
					JPanelTransformerControlSettings.this.updateCharacteristicsChart();
				}
				@Override
				public void changedUpdate(DocumentEvent de) {
					JPanelTransformerControlSettings.this.updateCharacteristicsChart();
				}
			});
		}
		return jTextFieldControlResidualAllowedDeviation;
	}

	
	private ChartPanel getJFreeChartChartPanel() {
		if (JFreeChartChartPanel == null) {
			JFreeChartChartPanel = new ChartPanel(this.getCharacteristicsChart());
		}
		return JFreeChartChartPanel;
	}
	private JFreeChart getCharacteristicsChart() {
		if (characteristicsChart==null) {
			String title = "Control Characteristics";
            String xAxisLabel = "Resiudual Load [kW]"; 
            String yAxisLabel = "Voltage ratio U / Un [%]"; 
			
            characteristicsChart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, this.getXySeriesCollection());
			characteristicsChart.setBackgroundPaint(Color.WHITE);
			characteristicsChart.getPlot().setBackgroundPaint(Color.WHITE);

			characteristicsChart.getXYPlot().setDomainGridlinePaint(Color.BLACK);
			characteristicsChart.getXYPlot().setRangeGridlinePaint(Color.BLACK);
			
			Font titleAndHeaderFont = new Font("Dialog",Font.BOLD, 12);
			characteristicsChart.getTitle().setFont(titleAndHeaderFont);
			characteristicsChart.getXYPlot().getDomainAxis().setLabelFont(titleAndHeaderFont);
			characteristicsChart.getXYPlot().getRangeAxis().setLabelFont(titleAndHeaderFont);
			
			Font axisFont = new Font("Dialog",Font.PLAIN, 12);
			characteristicsChart.getXYPlot().getDomainAxis().setTickLabelFont(axisFont);
			characteristicsChart.getXYPlot().getRangeAxis().setTickLabelFont(axisFont);
			
			NumberAxis yAxis = (NumberAxis) characteristicsChart.getXYPlot().getRangeAxis();
			yAxis.setAutoRangeIncludesZero(false);
			
		}
		return characteristicsChart;
	}
	private XYSeriesCollection getXySeriesCollection(){
		if (xySeriesCollection == null){
			xySeriesCollection = new XYSeriesCollection();
		}
		return this.xySeriesCollection;
	}
	private void setXySeries(XYSeries xySeries) {

		XYSeries xyLower = null;
		XYSeries xyUpper = null;
		
		if (xySeries.getItemCount()>0) {
			// --- Produce two further series that consider the allowed deviation -------
			xyLower = new XYSeries("Lower Boundary");
			xyUpper = new XYSeries("Upper Boundary");
			TransformerCharacteristicsHandler charHandler = this.getTransformerDataModel().getTransformerCharacteristicsHandler();
			for (int i = 0; i < xySeries.getItemCount(); i++) {
				// --- Get item of series -----------------------------------------------
				XYDataItem dataItem = xySeries.getDataItem(i);
				double xValue = dataItem.getXValue();
				double lowerBoundaryValue = charHandler.getLowerBoundaryInPercent(xValue);
				double upperBoundaryValue = charHandler.getUpperBoundaryInPercent(xValue);
				xyLower.add(new XYDataItem(xValue, lowerBoundaryValue));
				xyUpper.add(new XYDataItem(xValue, upperBoundaryValue));
			}
		}
		
		// --- Set the data series to visualization -------------------------------------
		this.getXySeriesCollection().removeAllSeries();
		this.getXySeriesCollection().addSeries(xySeries);
		if (xyLower!=null) this.getXySeriesCollection().addSeries(xyLower);
		if (xyUpper!=null) this.getXySeriesCollection().addSeries(xyUpper);
		
		// --- Set the line type and color ----------------------------------------------
		XYItemRenderer renderer = getCharacteristicsChart().getXYPlot().getRenderer();
		for (int i = 0; i < getXySeriesCollection().getSeriesCount(); i++) {
			renderer.setSeriesPaint(i, GlobalInfo.getChartColor(0));
			BasicStroke stroke = null;
			if (i==0 ) {
				stroke = new BasicStroke(XY_SERIES_LINE_WIDTH);
			} else {
				stroke = new BasicStroke(XY_SERIES_LINE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {10.0f, 6.0f}, 0.0f);
			}
			renderer.setSeriesStroke(i, stroke);
		}
	}
	/**
	 * Update characteristics chart.
	 */
	private void updateCharacteristicsChart() {
		double newDeviation = this.transformerDialog.getDoubleValue(this.getJTextFieldControlResidualAllowedDeviation());
		this.getTransformerDataModel().setControlCharacteristicsAllowedDeviation(newDeviation);
		XYSeries charSeries = this.getTransformerDataModel().getControlCharacteristicsXySeries();
		if (charSeries!=null) {
			this.setXySeries(charSeries);
		}
	}

	
	private void updateVisualization() {
		
		boolean enableNodeControl = this.getJCheckBoxControlNodeActivation().isSelected();
		boolean enableCharacteristicsControl = ! enableNodeControl;
		
		this.getJLabelNodeID().setEnabled(enableNodeControl);
		this.getJTextFieldNodeID().setEnabled(enableNodeControl);
		this.getJButtonNodeID().setEnabled(enableNodeControl);
		
		this.getJLabelControlNodeBoundaries().setEnabled(enableNodeControl);
		this.getJLabelControlNodeUpperBoundary().setEnabled(enableNodeControl);
		this.getJLabelControlNodeUpperBoundary().setEnabled(enableNodeControl);
		this.getJTextFieldControlNodeUpperBoundary().setEnabled(enableNodeControl);
		this.getJTextFieldControlNodeLowerBoundary().setEnabled(enableNodeControl);
		
		this.getJButtonImportCharacteristics().setEnabled(enableCharacteristicsControl);
		this.getJButtonDeleteCharacteristics().setEnabled(enableCharacteristicsControl);
		this.getJTextFieldControlResidualAllowedDeviation().setEnabled(enableCharacteristicsControl);
		this.getJFreeChartChartPanel().setEnabled(enableCharacteristicsControl);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		if (ae.getSource()==this.getJCheckBoxControlNodeActivation()) {
			// --- Enable / disable control elements ----------------
			if (this.getJCheckBoxControlNodeActivation().isSelected()==true) {
				this.getJCheckBoxControlResiudualLoadBased().setSelected(false);
			}
			this.updateVisualization();
			
		} else if (ae.getSource()==this.getJButtonNodeID()) {
			// --- Select a GraphNode -------------------------------
			Window owner = this.transformerDialog;
			GraphEnvironmentController graphController = this.getGraphController();
			if (graphController==null) {
				// --- Show error message ---------------------------
				String message = "No AWB-setup could be found to select the node for the transformers voltage control!";
				JOptionPane.showMessageDialog(owner, message, this.transformerDialog.getTitle(), JOptionPane.ERROR_MESSAGE, null);
				
			} else {
				// --- Define list of column services ---------------
				List<NetworkComponentTableService> colServiceList = new ArrayList<>();
				colServiceList.add(new GraphNodeIDColumn());
				
				// --- Open the network selection dialog ------------
				NetworkComponentSelectionDialog ncsDialog = new NetworkComponentSelectionDialog(owner, graphController, false, colServiceList);
				ncsDialog.setNetworkComponentList(this.getNetworkComponentListToDisplay());
				ncsDialog.setSelectedNetworkComponent(GraphNodeIDColumn.getNetworkComponent(graphController.getNetworkModel(), this.getJTextFieldNodeID().getText()));
				ncsDialog.setVisible(true);

				// --- Wait for the user -----------
				if (ncsDialog.isCanceled()==false) {
					NetworkComponent netCompSelected = ncsDialog.getSelectedNetworkComponent();
					this.getJTextFieldNodeID().setText(GraphNodeIDColumn.getGraphNodeID(graphController.getNetworkModel(), netCompSelected));
				}
				
			}
			
		} else if (ae.getSource()==this.getJCheckBoxControlResiudualLoadBased()) {
			// --- Enable / disable control elements ----------------
			if (this.getJCheckBoxControlResiudualLoadBased().isSelected()==true) {
				this.getJCheckBoxControlNodeActivation().setSelected(false);
			}
			this.updateVisualization();
			
		} else if (ae.getSource()==this.getJButtonImportCharacteristics()) {
			// --- Import characteristics data ----------------------
			File csvFile = this.selectCsvFile();
			if (csvFile!=null) {
				this.importCharacteristics(csvFile);
			}
			
		} else if (ae.getSource()==this.getJButtonDeleteCharacteristics()) {
			// --- Delete the current transformer characteristic ----
			if (this.getXySeriesCollection().getSeriesCount()==0) return;
			
			String title = "Delete Characteristic";
			String message = "Please, confirm to delete the current characteristics data!";
			int userResponse = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);
			if (userResponse==JOptionPane.OK_OPTION) {
				this.getXySeriesCollection().removeAllSeries();
			}
			
		}
	}
	
	/**
	 * Will ask the user to select a CSV file.
	 * @return the file
	 */
	private File selectCsvFile() {
		
		File csvFile = null;
		JFileChooser jFileChooserImportCSV = new JFileChooser(Application.getGlobalInfo().getLastSelectedFolder());
		jFileChooserImportCSV.setFileFilter(new FileNameExtensionFilter(Language.translate("CSV-Dateien"), "csv"));
		jFileChooserImportCSV.setDialogTitle(Language.translate("CSV-Datei importieren"));
		if (jFileChooserImportCSV.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
			Application.getGlobalInfo().setLastSelectedFolder(jFileChooserImportCSV.getCurrentDirectory());
			csvFile = jFileChooserImportCSV.getSelectedFile();
		}
		return csvFile;
	}
	/**
	 * Imports the transformer characteristics into the local chart.
	 * @param csvFile the CSV file to import
	 */
	private void importCharacteristics(File csvFile) {
		
		// --- Check existence ------------------------------------------------
		if (csvFile==null || csvFile.exists()==false) {
			System.err.println("[" + this.getClass().getSimpleName() + "] No csv file specified or file not available!");
			return;
		}
		
		// --- Define JFreeChart series - read file ---------------------------
		XYSeries xySeries = new XYSeries(XY_SERIES_NAME);
		BufferedReader csvFileReader = null;
		try {
			csvFileReader = new BufferedReader(new FileReader(csvFile));
			String inBuffer = null;
			while((inBuffer = csvFileReader.readLine()) != null){
					
				// --- Do we have a valid line from the file here? ------------
				boolean validLine = inBuffer.matches("[-?\\d]+\\.?[\\d]*[;[-?\\d]+\\.?[\\d]*]+");
				if (validLine==true) {
					
					String[] parts = inBuffer.split(";");
					if (parts.length<2) continue;
					
					Double xValue = NumberHelper.parseDouble(parts[0]); 
					Double yValue = NumberHelper.parseDouble(parts[1]);
					if (xValue==null || yValue==null) continue;
					xySeries.add(new XYDataItem(xValue, yValue));
				}
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				csvFileReader.close();
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			}
		}
		
		// --- Check xySeries. If valid place in chart ------------------------
		if (xySeries.getItemCount()>0) {
			this.setXySeries(xySeries);
		}
		
	}

	
	/**
	 * Returns the current graph controller.
	 * @return the graph controller
	 */
	private GraphEnvironmentController getGraphController() {
		
		GraphEnvironmentController graphController = null;
		Project project = Application.getProjectFocused();
		if (project!=null) {
			if (project.getEnvironmentController() instanceof GraphEnvironmentController) {
				graphController = (GraphEnvironmentController) project.getEnvironmentController();
			}
		}
		return graphController;
	}
	
	/**
	 * Returns the network component list to display in the above NetworkComponentSelectionDialog.
	 * @return the network component list to display
	 */
	private List<NetworkComponent> getNetworkComponentListToDisplay() {
		
		List<NetworkComponent> netCompList = new ArrayList<>();

		GraphEnvironmentController graphController = this.getGraphController(); 
		if (this.getGraphController()==null) return netCompList; 

		// --- Evaluate NetworkComponents and try to add to displayList ------- 
		NetworkModel networkModel = graphController.getNetworkModel();
		Vector<NetworkComponent> netCompVector = networkModel.getNetworkComponentVectorSorted();
		for (int i = 0; i < netCompVector.size(); i++) {
			// --- Check each network component -------------------------------
			NetworkComponent netComp = netCompVector.get(i);
			
			// --- Check for the right domain ---------------------------------
			String domain = networkModel.getDomain(netComp);
			boolean isElectricity = ElectricityDomainIdentification.isElectricityDomain(domain);
			if (isElectricity==false) continue;
			
			// --- Check for single GraühNode ---------------------------------
			Vector<GraphElement> graphElementVector = networkModel.getGraphElementsFromNetworkComponent(netComp);
			boolean isGraphNode = graphElementVector.size()==1 && graphElementVector.get(0) instanceof GraphNode;
			if (isGraphNode==false) continue;
			
			// --- Add to the list of possible display elements ---------------
			netCompList.add(netComp);
			
		}
		return netCompList;
	}
	
}
