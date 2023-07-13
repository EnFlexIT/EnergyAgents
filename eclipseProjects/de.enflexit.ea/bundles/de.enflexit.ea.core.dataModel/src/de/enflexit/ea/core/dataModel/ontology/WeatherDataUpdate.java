package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * Several Information about weather
* Protege name: WeatherDataUpdate
* @author ontology bean generator
* @version 2023/07/13, 21:15:48
*/
public class WeatherDataUpdate extends GridStateAgentManagement{ 

   /**
* Protege name: PredictedWeatherData
   */
   private List predictedWeatherData = new ArrayList();
   public void addPredictedWeatherData(WeatherData elem) { 
     List oldList = this.predictedWeatherData;
     predictedWeatherData.add(elem);
   }
   public boolean removePredictedWeatherData(WeatherData elem) {
     List oldList = this.predictedWeatherData;
     boolean result = predictedWeatherData.remove(elem);
     return result;
   }
   public void clearAllPredictedWeatherData() {
     List oldList = this.predictedWeatherData;
     predictedWeatherData.clear();
   }
   public Iterator getAllPredictedWeatherData() {return predictedWeatherData.iterator(); }
   public List getPredictedWeatherData() {return predictedWeatherData; }
   public void setPredictedWeatherData(List l) {predictedWeatherData = l; }

   /**
* Protege name: TimeStampWeather
   */
   private String timeStampWeather;
   public void setTimeStampWeather(String value) { 
    this.timeStampWeather=value;
   }
   public String getTimeStampWeather() {
     return this.timeStampWeather;
   }

}
