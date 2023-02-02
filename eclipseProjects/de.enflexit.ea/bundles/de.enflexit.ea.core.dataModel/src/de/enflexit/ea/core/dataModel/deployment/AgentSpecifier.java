package de.enflexit.ea.core.dataModel.deployment;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.enflexit.common.StringHelper;
import jade.core.AID;


/**
 * <p>Java-Klasse für AgentSpecifier complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AgentSpecifier">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AgentName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PlatformName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="UrlOrIp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="JadePort" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="MtpPort" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="MtpType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AgentSpecifier", propOrder = {
    "agentName",
    "platformName",
    "urlOrIp",
    "jadePort",
    "mtpPort",
    "mtpType"
})
public class AgentSpecifier
    implements Serializable, Cloneable
{

    private final static long serialVersionUID = 201404191434L;
    @XmlElement(name = "AgentName", required = true)
    protected String agentName;
    @XmlElement(name = "PlatformName", required = true)
    protected String platformName;
    @XmlElement(name = "UrlOrIp", required = true)
    protected String urlOrIp;
    @XmlElement(name = "JadePort")
    protected int jadePort;
    @XmlElement(name = "MtpPort")
    protected int mtpPort;
    @XmlElement(name = "MtpType", required = true)
    protected String mtpType;
    
    
    /**
     * Gets the agent name.
     * @return the agent name
     */
    public String getAgentName() {
        return agentName;
    }
    /**
     * Sets the agent name.
     * @param value the new agent name
     */
    public void setAgentName(String value) {
        this.agentName = value;
    }

    /**
     * Gets the platform name.
     * @return the platform name
     */
    public String getPlatformName() {
        return platformName;
    }
    /**
     * Sets the platform name.
     * @param value the new platform name
     */
    public void setPlatformName(String value) {
        this.platformName = value;
    }

    /**
     * Gets the url or ip.
     * @return the url or ip
     */
    public String getUrlOrIp() {
        return urlOrIp;
    }
    /**
     * Sets the url or ip.
     * @param value the new url or ip
     */
    public void setUrlOrIp(String value) {
        this.urlOrIp = value;
    }

    /**
     * Gets the jade port.
     * @return the jade port
     */
    public int getJadePort() {
        return jadePort;
    }
    /**
     * Sets the jade port.
     * @param value the new jade port
     */
    public void setJadePort(int value) {
        this.jadePort = value;
    }

    /**
     * Gets the mtp port.
     * @return the mtp port
     */
    public int getMtpPort() {
        return mtpPort;
    }
    /**
     * Sets the mtp port.
     * @param value the new mtp port
     */
    public void setMtpPort(int value) {
        this.mtpPort = value;
    }
    
    /**
     * Gets the mtp type.
     * @return the mtp type
     */
    public String getMtpType() {
        return mtpType;
    }
    /**
     * Sets the mtp type.
     * @param value the new mtp type
     */
    public void setMtpType(String value) {
        this.mtpType = value;
    }
    
    /**
     * Return the corresponding {@link AID} for this {@link AgentSpecifier}.
     * @return the AID derived from the local settings (can be null also)
     */
    public AID getAID(){
		
    	String agentName = this.getAgentName();
		String platformName = this.getPlatformName();
		String mtpProtocol = this.getMtpType();
		String mtpUrl = this.getUrlOrIp();
		int mtpPort = this.getMtpPort();
		
		boolean isInvalidCeaName = agentName==null || agentName.isEmpty();
		boolean isInvalidPlatformName = platformName==null || platformName.isEmpty();
		boolean isInvalidMtpProtocol = mtpProtocol==null || mtpProtocol.isEmpty();
		boolean isInvalidMtpUrl = mtpUrl==null || mtpUrl.isEmpty();
		boolean isInvalidMtpPort = mtpPort==0;

		
		// --- Create an AID for the the agent ------------
		AID aid = null;
		if (isInvalidCeaName==false) {
			if (isInvalidPlatformName==true) {
				aid = new AID(agentName, AID.ISLOCALNAME);
			} else {
				aid = new AID(agentName + "@" + platformName, AID.ISGUID);
			}
		}
		
		// --- Add a MTP address? -------------------------
		if (aid!=null && isInvalidMtpProtocol==false && isInvalidMtpUrl==false && isInvalidMtpPort==false) {
			String mtpAddress = mtpProtocol.toLowerCase() + "://" + mtpUrl + ":" + mtpPort + "/acc";
			aid.addAddresses(mtpAddress);
		}
		
		return aid;
	}
    
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object compObject) {
		
		if (compObject==null) return false;
		if (!(compObject instanceof AgentSpecifier)) return false;
		
		AgentSpecifier otherInstance = (AgentSpecifier) compObject;
		
		return (StringHelper.isEqualString(this.getAgentName(), otherInstance.getAgentName())
				&& StringHelper.isEqualString(this.getPlatformName(), otherInstance.getPlatformName())
				&& StringHelper.isEqualString(this.getUrlOrIp(), otherInstance.getUrlOrIp())
				&& this.getJadePort()==otherInstance.getJadePort()
				&& this.getMtpPort()==otherInstance.getMtpPort()
				&& StringHelper.isEqualString(this.getMtpType(), otherInstance.getMtpType())
		);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		AgentSpecifier clone = new AgentSpecifier();
		clone.setAgentName(this.getAgentName());
		clone.setPlatformName(this.getPlatformName());
		clone.setUrlOrIp(this.getUrlOrIp());
		clone.setJadePort(this.getJadePort());
		clone.setMtpPort(this.getMtpPort());
		clone.setMtpType(this.getMtpType());
		return clone;
	}
    
}
