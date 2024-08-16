package de.enflexit.ea.topologies.pandaPower;

import java.util.TreeMap;

import de.enflexit.ea.topologies.pandaPower.psIngo.PSIngoNamingMap;

/**
 * The Class PandaPowerNamingMap.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class PandaPowerNamingMap extends TreeMap<ColumnName, String>{

	private static final long serialVersionUID = 4221509656649560306L;
	
	/**
	 * Instantiates a new panda power column naming.
	 */
	public PandaPowerNamingMap() {
		this.addDefaultColumnNames();
	}
	/**
	 * Adds the default column names.
	 */
	private void addDefaultColumnNames() {

		this.put(ColumnName.Bus_Index, "index");
		this.put(ColumnName.Bus_Name, "name");
		this.put(ColumnName.Bus_Type, "type");
		this.put(ColumnName.Bus_VoltageLevel, "vn_kv");
		this.put(ColumnName.Bus_Geodata, "geodata");
		this.put(ColumnName.Bus_Coordinates, "coords");
		
		this.put(ColumnName.BusGeoData_Index, "index");
		this.put(ColumnName.BusGeoData_x, "x");
		this.put(ColumnName.BusGeoData_y, "y");
		
		this.put(ColumnName.Trafo_Index, "index");
		this.put(ColumnName.Trafo_Name, "name");
		this.put(ColumnName.Trafo_LV_Bus, "lv_bus");
		this.put(ColumnName.Trafo_HV_Bus, "hv_bus");
		this.put(ColumnName.Trafo_sn_mva, "sn_mva");
		this.put(ColumnName.Trafo_vn_hv_kv, "vn_hv_kv");
		this.put(ColumnName.Trafo_vn_lv_kv, "vn_lv_kv");
		this.put(ColumnName.Trafo_vk_percent, "vk_percent");
		this.put(ColumnName.Trafo_vkr_percent, "vkr_percent");
		this.put(ColumnName.Trafo_pfe_kw, "pfe_kw");
		this.put(ColumnName.Trafo_i0_percent, "i0_percent");
		this.put(ColumnName.Trafo_shift_degree, "shift_degree");
		this.put(ColumnName.Trafo_tap_side, "tap_side");
		this.put(ColumnName.Trafo_tap_neutral, "tap_neutral");
		this.put(ColumnName.Trafo_tap_min, "tap_min");
		this.put(ColumnName.Trafo_tap_max, "tap_max");
		this.put(ColumnName.Trafo_tap_step_percent, "tap_step_percent");
		this.put(ColumnName.Trafo_tap_step_degree, "tap_step_degree");
		this.put(ColumnName.Trafo_tap_pos, "tap_pos");
		this.put(ColumnName.Trafo_tap_phase_shifter, "tap_phase_shifter");
		
		this.put(ColumnName.Load_Index, "index");
		this.put(ColumnName.Load_Name, "name");
		this.put(ColumnName.Load_Bus, "bus");
		this.put(ColumnName.Load_p_mw, "p_mw");
		this.put(ColumnName.Load_q_mvar, "q_mvar");
		
		this.put(ColumnName.Line_index, "index");
		this.put(ColumnName.line_name, "name");
		this.put(ColumnName.line_stdType, "std_type");
		this.put(ColumnName.Line_from_bus, "from_bus");
		this.put(ColumnName.Line_to_bus, "to_bus");
		this.put(ColumnName.Line_length_km, "length_km");
		this.put(ColumnName.Line_r_ohm_per_km, "r_ohm_per_km");
		this.put(ColumnName.Line_x_ohm_per_km, "x_ohm_per_km");
		this.put(ColumnName.Line_c_nf_per_km, "c_nf_per_km");
		this.put(ColumnName.Line_g_us_per_km, "g_us_per_km");
		this.put(ColumnName.Line_max_i_ka, "max_i_ka");
		this.put(ColumnName.Line_df, "df");
		this.put(ColumnName.Line_parallel, "parallel");
		this.put(ColumnName.Line_type, "type");
		this.put(ColumnName.Line_in_service, "in_service");
		
		this.put(ColumnName.StdLineType_ID, "ID");
		this.put(ColumnName.StdLineType_c_nf_per_km, "c_nf_per_km");
		this.put(ColumnName.StdLineType_r_ohm_per_km, "r_ohm_per_km");
		this.put(ColumnName.StdLineType_x_ohm_per_km, "x_ohm_per_km");
		this.put(ColumnName.StdLineType_max_i_ka, "max_i_ka");
		this.put(ColumnName.StdLineType_type, "type");
		this.put(ColumnName.StdLineType_q_mm2, "q_mm2");
		this.put(ColumnName.StdLineType_alpha, "alpha");
		this.put(ColumnName.StdLineType_voltage_rating, "voltage_rating");
	
		this.put(ColumnName.Switch_Index, "index");
		this.put(ColumnName.Switch_Bus, "bus");
		this.put(ColumnName.Switch_Name, "name");
		this.put(ColumnName.Switch_Element, "element");
		this.put(ColumnName.Switch_Closed, "closed");
		this.put(ColumnName.Switch_Z_Ohm, "z_ohm");
		
	}
	
	/**
	 * Checks if is the specified node type is to be added to the new network model.
	 *
	 * @param type the type
	 * @return true, if the node type is to be added to the new network model.
	 */
	public boolean isAddNodeTypeToNetworkModel(String type) {
		
		// --- PandaPower-Doc: Type of the bus. n=node, b=busbar, m=muff ------
		if (type.toLowerCase().equals("n")==true) {
			return true;
		} else if (type.toLowerCase().equals("m")==true) {
			return true;
		} else if (type.toLowerCase().equals("b")==true) {
			return true;
		}
		return false;
	}

	
	// ------------------------------------------------------------------------
	// --- From here, global static setting for current naming map ------------
	// ------------------------------------------------------------------------
	/**
	 * The enumeration of known PandaPower NamingMaps.
	 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
	 */
	public static enum NamingMap {
		PandaPower,
		PandaPowerOfPSIingo
	}
	
	private static PandaPowerNamingMap namingMap;
	/**
	 * Returns the current naming map for a PandaPower JSON file import.
	 * By Default, the class {@link PandaPowerNamingMap} is used
	 * @return the naming map
	 */
	public static PandaPowerNamingMap getPandaPowerNamingMap() {
		if (namingMap==null) {
			namingMap = new PandaPowerNamingMap();
		}
		return namingMap;
	}
	/**
	 * Sets the current naming map.
	 * @param newNamingMap the new naming map
	 */
	public static void setPandaPowerNamingMap(PandaPowerNamingMap newNamingMap) {
		if (PandaPowerNamingMap.namingMap==null || newNamingMap!=PandaPowerNamingMap.namingMap) {
			PandaPowerNamingMap.namingMap = newNamingMap;
		}
	}
	/**
	 * Sets the current naming map by using the enumeration of known mappings.
	 * @param namingMap the new naming map
	 */
	public static void setPandaPowerNamingMap(NamingMap namingMap) {
		
		switch (namingMap) {
		case PandaPower:
			PandaPowerNamingMap.setPandaPowerNamingMap(new PandaPowerNamingMap());
			break;
		case PandaPowerOfPSIingo:
			PandaPowerNamingMap.setPandaPowerNamingMap(new PSIngoNamingMap());
			break;
		}
	}
	/**
	 * Based on the enumeration value {@link ColumnName}, returns the column name as String.
	 *
	 * @param columnName the column name
	 * @return the column name
	 */
	public static String getColumnName(ColumnName columnName) {
		return getPandaPowerNamingMap().get(columnName);
	}
	
}
