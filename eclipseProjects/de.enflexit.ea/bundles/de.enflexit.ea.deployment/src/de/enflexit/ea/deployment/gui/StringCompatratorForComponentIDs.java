package de.enflexit.ea.deployment.gui;

import java.util.Comparator;

/**
 * This {@link Comparator} implementation compares network component IDs
 * (or other Strings containing numbers) based on their numerical part.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class StringCompatratorForComponentIDs implements Comparator<String> {

	@Override
	public int compare(String s1, String s2) {
		
		// --- Extract the numeric part from the ID -------------------
		Integer n1 = null;
		Integer n2 = null;
		try {
			// --- Remove all non-digit characters, parse the result ---------- 
			n1 = Integer.parseInt(s1.replaceAll("\\D+",""));
			n2 = Integer.parseInt(s2.replaceAll("\\D+",""));
		} catch (NumberFormatException e) {
		}
		
		if (n1!=null && n2!=null) {
			// --- If successful, compare the numbers -----------------
			return n1.compareTo(n2);
		} else {
			// --- Otherwise compare the original strings -------------
			return s1.compareTo(s2);
		}
	}

}
