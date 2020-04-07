package hygrid.plugin.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import hygrid.deployment.dataModel.AgentOperatingMode;

/**
 * {@link ListCellRenderer} for items of the type {@link AgentOperatingMode}
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 *
 */
public class ListCellRendererForAgentOperatingMode extends DefaultListCellRenderer {

	private static final long serialVersionUID = -5999053331829322593L;

	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index,
			boolean isSelected, boolean cellHasFocus) {
		
		// --- Use the AgentOperatingMode's value() method instead of toString() --------------
		AgentOperatingMode operatingMode = (AgentOperatingMode) value;
		return super.getListCellRendererComponent(list, operatingMode.value(), index, isSelected, cellHasFocus);
	}

}
