package de.enflexit.ea.electricity.transformer.eomDataModel;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import energy.helper.NumberHelper;

/**
 * The Class TransformerCharacteristicsHandler.
 * 
 * @deprecated the TransformerDataModel class to use was moved to the core electricity bundle.
 * This class is only used to handle persisted legacy models and convert them into the new class type.
 *    
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
 @Deprecated
public class TransformerCharacteristicsHandler {

	private TransformerDataModel transformerDataModel;
	
	/**
	 * Instantiates a new transformer characteristics handler.
	 * @param transformerDataModel the transformer data model
	 */
	public TransformerCharacteristicsHandler(TransformerDataModel transformerDataModel) {
		this.transformerDataModel = transformerDataModel;
	}

	
	/**
	 * Return the lower boundary in V.
	 *
	 * @param loadInKW the load in KW
	 * @return the lower boundary
	 */
	public double getLowerBoundaryInV(double loadInKW) {
		double targetVoltageLevel = this.getTargetVoltageLevelForLowVoltageLoadInV(loadInKW);
		double boundaryStep = targetVoltageLevel * this.getAllowedDeviation() / 100.0;
		return targetVoltageLevel - boundaryStep;
	}
	/**
	 * Returns the upper boundary in V.
	 *
	 * @param loadInKW the load in KW
	 * @return the upper boundary
	 */
	public double getUpperBoundaryInV(double loadInKW) {
		double targetVoltageLevel = this.getTargetVoltageLevelForLowVoltageLoadInV(loadInKW);
		double boundaryStep = targetVoltageLevel * this.getAllowedDeviation() / 100.0;
		return targetVoltageLevel + boundaryStep;
	}
	/**
	 * Return the deviation step in V.
	 *
	 * @param loadInKW the load in KW
	 * @return the deviation step in V
	 */
	public double getDeviationStepInV(double loadInKW) {
		double targetVoltageLevel = this.getTargetVoltageLevelForLowVoltageLoadInV(loadInKW);
		return targetVoltageLevel * this.getAllowedDeviation() / 100.0;
	}
	
	
	
	/**
	 * Return the lower boundary in percent to the nominal voltage level.
	 *
	 * @param loadInKW the load in KW
	 * @return the lower boundary
	 */
	public double getLowerBoundaryInPercent(double loadInKW) {
		return this.getLowerBoundaryInV(loadInKW) / this.getNominalLowVoltageLevelInV() * 100.0;
	}
	/**
	 * Returns the upper boundary in percent to the nominal voltage level.
	 *
	 * @param loadInKW the load in KW
	 * @return the upper boundary
	 */
	public double getUpperBoundaryInPercent(double loadInKW) {
		return this.getUpperBoundaryInV(loadInKW) / this.getNominalLowVoltageLevelInV() * 100.0;	
	}
	/**
	 * Returns the deviation step in in percent to the nominal voltage level.
	 *
	 * @param loadInKW the load in KW
	 * @return the deviation step in %
	 */
	public double getDeviationStepInPercent(double loadInKW) {
		return this.getDeviationStepInV(loadInKW) / this.getNominalLowVoltageLevelInV() * 100.0;
	}
	
	
	
	/**
	 * Returns the target voltage level for the specified load 
	 * that can be derived by the current transformer characteristics.
	 *
	 * @param loadInKW the load in kW
	 * @return the voltage level for residual load
	 */
	public double getTargetVoltageLevelForLowVoltageLoadInV(double loadInKW) {
		
		double voltageLevelInVolt = -1;
		
		Double u_to_un_InPercent = this.getVoltageToNominalVoltageLevelInPercent(loadInKW);
		if (u_to_un_InPercent!=null) {
			voltageLevelInVolt = this.getNominalLowVoltageLevelInV() * (u_to_un_InPercent / 100.0);
		}
		return voltageLevelInVolt;
	}

	/**
	 * Returns the target voltage level for the specified residual load 
	 * that can be derived by the current transformer cthis.getharacteristics.
	 *
	 * @param loadInKW the load in kW
	 * @return the voltage level for residual load
	 */
	public double getTargetVoltageLevelForLowVoltageLoadInkV(double loadInKW) {
		
		double voltageLevelKiloVolt = -1;
		
		Double u_to_un_InPercent = this.getVoltageToNominalVoltageLevelInPercent(loadInKW);
		if (u_to_un_InPercent!=null) {
			voltageLevelKiloVolt = this.getNominalLowVoltageLevelInkV() * (u_to_un_InPercent / 100.0);
		}
		return voltageLevelKiloVolt;
	}
	
	/**
	 * Return the ratio of voltage to nominal voltage level in percent out of the current transformer characteristics .
	 *
	 * @param residualLoadInKW the residual load in kW
	 * @return the voltage to nominal voltage level or <code>null</code> if no characteristics can be found
	 */
	public Double getVoltageToNominalVoltageLevelInPercent(double residualLoadInKW) {
		
		Double u_to_un = null;
		
		boolean debugPrintXySeries = false; 
		
		// --- Try to get the control characteristics from model ----
		XYSeries xySeries = this.transformerDataModel.getControlCharacteristicsXySeries();
		if (xySeries!=null && xySeries.getItemCount()>0) {
			
			// --- Check each x-range -------------------------------
			for (int i=0; i<xySeries.getItemCount()-1; i++) {

				XYDataItem dataItem1 = xySeries.getDataItem(i);
				double x1Value = dataItem1.getXValue();
				double y1Value = dataItem1.getYValue();
				
				XYDataItem dataItem2 = xySeries.getDataItem(i+1);
				double x2Value = dataItem2.getXValue();
				double y2Value = dataItem2.getYValue();

				if (debugPrintXySeries==true) {
					System.out.println("x1: " + x1Value + ", y1: " + y1Value + ", x2: " + x2Value + ", y2: " + y2Value);
				}
				
				// --------------------------------------------------
				// --- First or last element? -----------------------
				// --------------------------------------------------
				if (i==1) {
					// --- First range ------------------------------
					if (residualLoadInKW <= x1Value) {
						// --- Ahead characteristics x-range !!! ----
						u_to_un = y1Value;
						break;
					}
					
				} else if (i==xySeries.getItemCount()-2) {
					// --- Last range -------------------------------
					if (residualLoadInKW >= x2Value) {
						// --- After characteristics x-range !!! ----
						u_to_un = y2Value;
						break;
					}
				}
				
				// --------------------------------------------------
				// --- Check if we're in the right range ------------
				// --------------------------------------------------
				if (residualLoadInKW == x1Value) {
					u_to_un = y1Value;
					break;
				} else if (residualLoadInKW == x2Value) {
					u_to_un = y2Value;
					break;
				} else if (residualLoadInKW>x1Value & residualLoadInKW<x2Value) {
					// --- Residual load in correct x range ---------
					// --- => Interpolate ---------------------------
					u_to_un = NumberHelper.interpolateLinear(x1Value, y1Value, x2Value, y2Value, residualLoadInKW);
					break;
				}
				// --------------------------------------------------
				
			}
			
		}
		return u_to_un;
	}
	
	
	/**
	 * Returns the nominal low voltage level in V.
	 * @return the nominal voltage level
	 */
	private double getNominalLowVoltageLevelInV() {
		return this.getNominalLowVoltageLevelInkV() * 1000.0;
	}
	/**
	 * Returns the nominal low voltage level in kV.
	 * @return the nominal voltage level
	 */
	private double getNominalLowVoltageLevelInkV() {
		return this.transformerDataModel.getLowerVoltage_vmLV();
	}

	/**
	 * Return the allowed deviation for the transformer characteristics.
	 * @return the allowed deviation
	 */
	private double getAllowedDeviation() {
		return this.transformerDataModel.getControlCharacteristicsAllowedDeviation();
	}
}
