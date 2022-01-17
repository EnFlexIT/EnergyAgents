package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * The registration request that is sent by a remote agent
* Protege name: RemoteAgentRegistrationRequest
* @author ontology bean generator
* @version 2022/01/17, 15:51:08
*/
public class RemoteAgentRegistrationRequest extends TestbedAgentManagement{ 

   /**
* Protege name: certificateBase64
   */
   private String certificateBase64;
   public void setCertificateBase64(String value) { 
    this.certificateBase64=value;
   }
   public String getCertificateBase64() {
     return this.certificateBase64;
   }

}
