package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * This class describes static properties of a node component in a network model
* Protege name: NodeComponentProperties
* @author ontology bean generator
* @version 2022/01/17, 15:51:08
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
