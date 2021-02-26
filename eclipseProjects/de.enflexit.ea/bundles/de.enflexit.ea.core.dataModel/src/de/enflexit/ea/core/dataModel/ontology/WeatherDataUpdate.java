package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * Several Information about weather
* Protege name: WeatherDataUpdate
* @author ontology bean generator
* @version 2021/02/26, 14:45:40
*/
public class WeatherDataUpdate extends GridStateAgentManagement{ 

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

}
