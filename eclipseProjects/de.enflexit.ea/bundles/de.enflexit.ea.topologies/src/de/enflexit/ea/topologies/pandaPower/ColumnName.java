package de.enflexit.ea.topologies.pandaPower;

/**
 * The Enumeration ColumnName.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public enum ColumnName {
	Bus_Index,
	Bus_Name,
	Bus_Type,
	Bus_VoltageLevel,
	Bus_Geodata,
	Bus_Coordinates, 

	BusGeoData_Index,
	BusGeoData_x,
	BusGeoData_y,
	
	Trafo_Index,
	Trafo_Name,
	Trafo_LV_Bus,
	Trafo_HV_Bus,
	Trafo_sn_mva,
	Trafo_vn_hv_kv,
	Trafo_vn_lv_kv,
	Trafo_vk_percent,
	Trafo_vkr_percent,
	Trafo_pfe_kw,
	Trafo_i0_percent,
	Trafo_shift_degree,
	Trafo_tap_side,
	Trafo_tap_neutral,
	Trafo_tap_min,
	Trafo_tap_max,
	Trafo_tap_step_percent,
	Trafo_tap_step_degree,
	Trafo_tap_pos,
	Trafo_tap_phase_shifter,
	
	Load_Index,
	Load_Bus, 
	Load_Name,
	Load_p_mw,
	Load_q_mvar,
	
	Line_index,
	line_name,
	line_stdType,
	Line_from_bus,
	Line_to_bus,
	Line_length_km,
	Line_r_ohm_per_km,
	Line_x_ohm_per_km,
	Line_c_nf_per_km,
	Line_g_us_per_km,
	Line_max_i_ka,
	Line_df,
	Line_parallel,
	Line_type,
	Line_in_service,

	StdLineType_ID,
	StdLineType_c_nf_per_km,
	StdLineType_r_ohm_per_km,
	StdLineType_x_ohm_per_km,
	StdLineType_max_i_ka,
	StdLineType_type,
	StdLineType_q_mm2,
	StdLineType_alpha,
	StdLineType_voltage_rating,
	
	Switch_Index,
	Switch_Bus,
	Switch_Name,
	Switch_Element,
	Switch_Closed,
	Switch_Z_Ohm,
	
}
