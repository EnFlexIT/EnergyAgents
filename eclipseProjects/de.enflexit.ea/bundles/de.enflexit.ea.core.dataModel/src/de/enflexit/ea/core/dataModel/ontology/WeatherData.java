package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: WeatherData
* @author ontology bean generator
* @version 2022/01/17, 15:51:08
*/
public class WeatherData implements Concept {

   /**
   * Date of Prediction
* Protege name: PredictedDate
   */
   private String predictedDate;
   public void setPredictedDate(String value) { 
    this.predictedDate=value;
   }
   public String getPredictedDate() {
     return this.predictedDate;
   }

   /**
   * Hour to predict
* Protege name: PredictedHour
   */
   private int predictedHour;
   public void setPredictedHour(int value) { 
    this.predictedHour=value;
   }
   public int getPredictedHour() {
     return this.predictedHour;
   }

   /**
   * Predicted Temperature in °C
* Protege name: PredictedTemperature
   */
   private float predictedTemperature;
   public void setPredictedTemperature(float value) { 
    this.predictedTemperature=value;
   }
   public float getPredictedTemperature() {
     return this.predictedTemperature;
   }

   /**
   * Predicted Wind Speed in m/s
* Protege name: PredictedWindSpeed
   */
   private float predictedWindSpeed;
   public void setPredictedWindSpeed(float value) { 
    this.predictedWindSpeed=value;
   }
   public float getPredictedWindSpeed() {
     return this.predictedWindSpeed;
   }

   /**
   * Sunny or cloudy weather condition
* Protege name: PredictedWeatherCondition
   */
   private String predictedWeatherCondition;
   public void setPredictedWeatherCondition(String value) { 
    this.predictedWeatherCondition=value;
   }
   public String getPredictedWeatherCondition() {
     return this.predictedWeatherCondition;
   }

}
