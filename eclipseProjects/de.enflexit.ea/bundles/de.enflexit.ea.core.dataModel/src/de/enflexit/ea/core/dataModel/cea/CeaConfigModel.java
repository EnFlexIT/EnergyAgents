package de.enflexit.ea.core.dataModel.cea;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import agentgui.core.config.GlobalInfo.MtpProtocol;
import de.enflexit.common.StringHelper;

/**
 * The Class CeaConfigModel represents the actual data model for the CEA configuration.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
@XmlRootElement
public class CeaConfigModel implements Serializable, Cloneable {

	private static final long serialVersionUID = 2173789533665654027L;

	protected boolean startSecondMTP;
	
	protected MtpProtocol mtpProtocol;
	protected String urlOrIp;
	protected int mtpPort;
	
	protected String mirrorSourceP2Repository;
	protected String mirrorDestinationP2Repository;
	protected String mirrorProviderURLP2Repository;
	
	protected String mirrorSourceProjectRepository;
	protected String mirrorDestinationProjectRepository;
	protected String mirrorProviderURLProjectRepository;
	
	protected int mirrorInterval;
	
	
	public boolean isStartSecondMTP() {
		return startSecondMTP;
	}
	public void setStartSecondMTP(boolean startSecondMTP) {
		this.startSecondMTP = startSecondMTP;
	}
	
	public MtpProtocol getMtpProtocol() {
		return mtpProtocol;
	}
	public void setMtpProtocol(MtpProtocol mtpProtocol) {
		this.mtpProtocol = mtpProtocol;
	}
	public String getUrlOrIp() {
		return urlOrIp;
	}
	public void setUrlOrIp(String value) {
		this.urlOrIp = value;
	}
	public int getMtpPort() {
		return mtpPort;
	}
	public void setMtpPort(int value) {
		this.mtpPort = value;
	}

	/**
	 * Returns the compete MTP address.
	 * @return the MTP address to use with the current configuration
	 */
	public String getCompleteMTPAddress() {
		String mtpAddress = this.getMtpProtocol().toString().toLowerCase() + "://";
		if (this.getUrlOrIp()!=null) {
			mtpAddress += this.getUrlOrIp().trim() + ":";
		} else {
			mtpAddress += "127.0.0.1" + ":";
		}
		mtpAddress += this.getMtpPort() + "/acc";
		return mtpAddress.trim();
	}
	
	
	public String getMirrorSourceP2Repository() {
		return mirrorSourceP2Repository;
	}
	public void setMirrorSourceP2Repository(String mirrorSourceP2Repository) {
		this.mirrorSourceP2Repository = mirrorSourceP2Repository;
	}
	
	public String getMirrorDestinationP2Repository() {
		return mirrorDestinationP2Repository;
	}
	public void setMirrorDestinationP2Repository(String mirrorDestinationP2Repository) {
		this.mirrorDestinationP2Repository = mirrorDestinationP2Repository;
	}
	
	public String getMirrorProviderURLP2Repository() {
		return mirrorProviderURLP2Repository;
	}
	public void setMirrorProviderURLP2Repository(String mirrorProviderURLP2Repository) {
		this.mirrorProviderURLP2Repository = mirrorProviderURLP2Repository;
	}
	
	
	public String getMirrorSourceProjectRepository() {
		return mirrorSourceProjectRepository;
	}
	public void setMirrorSourceProjectRepository(String mirrorSourceProjectRepository) {
		this.mirrorSourceProjectRepository = mirrorSourceProjectRepository;
	}
	
	public String getMirrorDestinationProjectRepository() {
		return mirrorDestinationProjectRepository;
	}
	public void setMirrorDestinationProjectRepository(String mirrorDestinationProjectRepository) {
		this.mirrorDestinationProjectRepository = mirrorDestinationProjectRepository;
	}
	
	public String getMirrorProviderURLProjectRepository() {
		return mirrorProviderURLProjectRepository;
	}
	public void setMirrorProviderURLProjectRepository(String mirrorProviderURLProjectRepository) {
		this.mirrorProviderURLProjectRepository = mirrorProviderURLProjectRepository;
	}

	
	public int getMirrorInterval() {
		return mirrorInterval;
	}
	public void setMirrorInterval(int mirrorInterval) {
		this.mirrorInterval = mirrorInterval;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println("Cloning is not supported.");
			return this;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object compObject) {

		if (compObject==null) return false;
		if (compObject==this) return true;
		
		if (!(compObject instanceof CeaConfigModel)) return false;
		
		CeaConfigModel comp = (CeaConfigModel) compObject;
		
		if (this.isStartSecondMTP()!=comp.isStartSecondMTP()) return false;
		if (this.getMtpProtocol().equals(comp.getMtpProtocol())==false) return false;
		if (StringHelper.isEqualString(this.getUrlOrIp(), comp.getUrlOrIp())==false) return false;
		if (this.getMtpPort()!=comp.getMtpPort()) return false;
		
		if (StringHelper.isEqualString(this.getMirrorSourceP2Repository(), comp.getMirrorSourceP2Repository())==false) return false;
		if (StringHelper.isEqualString(this.getMirrorDestinationP2Repository(), comp.getMirrorDestinationP2Repository())==false) return false;
		if (StringHelper.isEqualString(this.getMirrorProviderURLP2Repository(), comp.getMirrorProviderURLP2Repository())==false) return false;

		if (StringHelper.isEqualString(this.getMirrorSourceProjectRepository(), comp.getMirrorSourceProjectRepository())==false) return false;
		if (StringHelper.isEqualString(this.getMirrorDestinationProjectRepository(), comp.getMirrorDestinationProjectRepository())==false) return false;
		if (StringHelper.isEqualString(this.getMirrorProviderURLProjectRepository(), comp.getMirrorProviderURLProjectRepository())==false) return false;

		if (this.getMirrorInterval()!=comp.getMirrorInterval()) return false;
		
		return true;
	}
	
	
}
