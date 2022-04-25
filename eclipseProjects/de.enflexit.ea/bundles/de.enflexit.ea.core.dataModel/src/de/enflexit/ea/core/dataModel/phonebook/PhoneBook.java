package de.enflexit.ea.core.dataModel.phonebook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.enflexit.ea.core.dataModel.DirectoryHelper;
import jade.core.AID;

/**
 * This class can be used to store agent AIDs and make them accessible by the local name.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 */
@XmlRootElement
public class PhoneBook<T extends PhoneBookEntry> implements Serializable {
	
	private static final long serialVersionUID = 6718209842107620111L;

	@XmlTransient
	private File phoneBookFile;  
	
	@XmlElementWrapper(name = "phoneBookEntries")
	private TreeMap<String, T> internalPhoneBook;
	
	/**
	 * Gets the internal phone book.
	 * @return the internal phone book
	 */
	private TreeMap<String, T> getInternalPhoneBook() {
		if (internalPhoneBook==null) {
			internalPhoneBook = new TreeMap<>();
		}
		return internalPhoneBook;
	}
	
	/**
	 * Gets an agent AID from the phone book.
	 * @param localName the agent's local name (= component ID)
	 * @return the agent's AID, null if not found
	 */
	public AID getAgentAID(String localName) {
		if (localName==null || localName.isEmpty()==true) return null;
		AID aid = null;
		T phoneBookEntry = this.getInternalPhoneBook().get(localName);
		if (phoneBookEntry!=null) {
			aid = phoneBookEntry.getAID();
		}
		return aid;
	}
	
//	/**
//	 * Adds an agent's AID to the phone book.
//	 * @param agentAID the agent AID
//	 */
//	public void addAgentAID(AID agentAID) {
//		
//		T phoneBookEntry = this.getInternalPhoneBook().get(agentAID.getLocalName());
//		// --- If there is no phone book entry for this agent yet yet, create one -------
//		if (phoneBookEntry==null) {
//			phoneBookEntry = new PhoneBookEntry();
//			this.getInternalPhoneBook().put(agentAID.getLocalName(), phoneBookEntry);
//		}
//		phoneBookEntry.setAID(agentAID);
//		this.save();
//	}
	
	/**
	 * Gets a phone book entry to the phone book.
	 * @param localName the local name to look up
	 * @return the corresponding phone book entry
	 */
	public PhoneBookEntry getPhoneBookEntry(String localName) {
		return this.getInternalPhoneBook().get(localName);
	}
	
	/**
	 * Adds a phone book entry to the phone book.
	 * @param phoneBookEntry the phone book entry
	 */
	public void addPhoneBookEntry(T phoneBookEntry) {
		this.getInternalPhoneBook().put(phoneBookEntry.getAID().getLocalName(), phoneBookEntry);
		this.save();
	}
	
	/**
	 * Gets the {@link AID}s of all agents representing active components
	 * @return the AIDs
	 */
	public ArrayList<AID> getControllableComponentAIDs() {
		ArrayList<AID> aids = new ArrayList<>();
		ArrayList<PhoneBookEntry> allEntries = new ArrayList<>(this.getInternalPhoneBook().values());
		for (int i=0; i<allEntries.size(); i++) {
			PhoneBookEntry pbe = allEntries.get(i);
			if (allEntries.get(i).isControllable()==true) {
				aids.add(pbe.getAID());
			}
		}
		return aids;
	}
	
	/**
	 * Gets the {@link AID}s of all agents representing a specific component type
	 * @param componentType the component type
	 * @return the AIDs
	 */
	public ArrayList<AID> getAIDsByComponentType(String componentType) {
		ArrayList<AID> aids = new ArrayList<>();
		ArrayList<PhoneBookEntry> allEntries = new ArrayList<>(this.getInternalPhoneBook().values());
		for (int i=0; i<allEntries.size(); i++) {
			PhoneBookEntry pbe = allEntries.get(i);
			if (allEntries.get(i).getComponentType().equals(componentType)) {
				aids.add(pbe.getAID());
			}
		}
		return aids;
	}
	
	/**
	 * Gets {@link PhoneBookEntry}s of all agents representing active components.
	 * @return the phone book entries
	 */
	public ArrayList<PhoneBookEntry> getControllableComponentEntries(){
		ArrayList<PhoneBookEntry> entries = new ArrayList<>();
		ArrayList<PhoneBookEntry> allEntries = new ArrayList<>(this.getInternalPhoneBook().values());
		for (int i=0; i<allEntries.size(); i++) {
			PhoneBookEntry pbe = allEntries.get(i);
			if (allEntries.get(i).isControllable()==true) {
				entries.add(pbe);
			}
		}
		return entries;
	}
	
	/**
	 * Gets the {@link PhoneBookEntry}s of all agents representing a specific component type
	 * @param componentType the component type
	 * @return the phone book entries
	 */
	public ArrayList<PhoneBookEntry> getEntriesByComponentType(String componentType){
		ArrayList<PhoneBookEntry> entries = new ArrayList<>();
		ArrayList<PhoneBookEntry> allEntries = new ArrayList<>(this.getInternalPhoneBook().values());
		for (int i=0; i<allEntries.size(); i++) {
			PhoneBookEntry pbe = allEntries.get(i);
			if (pbe.getComponentType()!=null && pbe.getComponentType().equals(componentType)) {
				entries.add(pbe);
			}
		}
		return entries;
	}
	
	/**
	 * Removes an agent's AID from the phone book.
	 * @param agentAID the agent AID
	 */
	public void removeAgentAID(AID agentAID) {
		PhoneBookEntry phoneBookEntry = this.getInternalPhoneBook().get(agentAID.getLocalName());
		if (phoneBookEntry!=null) {
			this.getInternalPhoneBook().remove(agentAID.getLocalName());
			this.save();
		}
	}

	/**
	 * Returns all phone book entries.
	 * @return the phone book entries
	 */
	public List<PhoneBookEntry> getPhoneBookEntries() {
		return new ArrayList<>(this.getInternalPhoneBook().values());
	}
	
	
	/**
	 * Load phone book.
	 * @param phoneBookFile the phone book file
	 * @return the phone book
	 */
	public static <T extends PhoneBookEntry> PhoneBook<T> loadPhoneBook(File phoneBookFile, Class<T> classInstance) {
		
		PhoneBook<T> pb = null;
		if (phoneBookFile.exists()==false) {
			// --- Create a new PhoneBook -----------------
			pb = new PhoneBook<>();
			pb.setPhoneBookFile(phoneBookFile);
			if (pb.save()==false) {
				System.err.println("[" + PhoneBook.class.getSimpleName() + "] Could not create PhoneBook file!");
				pb = null;
			}
			
		} else {
			// --- Check if the directory is available ----
			if (DirectoryHelper.isAvailableDirectory(phoneBookFile)==true) {
				// --- Load the PhoneBook from file -------
				FileReader fileReader = null;
				try {
					fileReader = new FileReader(phoneBookFile);
					JAXBContext pbc = JAXBContext.newInstance(PhoneBook.class, classInstance);
					Unmarshaller um = pbc.createUnmarshaller();
					pb = (PhoneBook) um.unmarshal(fileReader);
					pb.setPhoneBookFile(phoneBookFile);

				} catch (FileNotFoundException | JAXBException ex) {
					ex.printStackTrace();
				} finally {
					try {
						if (fileReader!=null) fileReader.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				
			} else {
				System.err.println("[" + PhoneBook.class.getSimpleName() + "] Could not access directory '" + phoneBookFile.getParentFile().getAbsolutePath() + "'!");
			}
		}
		return pb;
	}
	/**
	 * Saves the current phone book.
	 */
	public boolean save() {
		return PhoneBook.saveAs(this, this.phoneBookFile);
	}
	/**
	 * Saves the current PhoneBook instance to the specified file
	 * @param phoneBookFile the phone book file
	 */
	public static <T extends PhoneBookEntry> boolean saveAs(PhoneBook<T> phoneBook, File phoneBookFile) {
		
		boolean successful = false;
		if (phoneBookFile!=null && DirectoryHelper.isAvailableDirectory(phoneBookFile)==true) {

			FileWriter fileWriter = null;
			try {
				// --- Save the PhoneBook to file ---------
				JAXBContext pbc = JAXBContext.newInstance(phoneBook.getClass());
				Marshaller pbm = pbc.createMarshaller();
				pbm.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
				pbm.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

				// --- Write to XML-File ------------------
				fileWriter = new FileWriter(phoneBookFile);
				pbm.marshal(phoneBook, fileWriter);
				successful = true;
				
			} catch (JAXBException | IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (fileWriter!=null)  fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return successful;
	}
	
	/**
	 * Returns the phone book file.
	 * @return the phone book file
	 */
	@XmlTransient
	public File getPhoneBookFile() {
		return phoneBookFile;
	}
	/**
	 * Sets the phone book file.
	 * @param phoneBookFile the new phone book file
	 */
	public void setPhoneBookFile(File phoneBookFile) {
		this.phoneBookFile = phoneBookFile;
	}
	
	
}
