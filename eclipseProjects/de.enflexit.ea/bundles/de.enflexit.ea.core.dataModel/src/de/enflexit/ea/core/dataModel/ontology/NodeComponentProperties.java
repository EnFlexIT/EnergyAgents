package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * This class describes static properties of a node component in a network model
* Protege name: NodeComponentProperties
* @author ontology bean generator
* @version 2020/09/18, 12:50:26
*/
public class NodeComponentProperties extends StaticComponentProperties{ 

   /**
* Protege name: description
   */
   private String description;
   public void setDescription(String value) { 
    this.description=value;
   }
   public String getDescription() {
     return this.description;
   }

}
