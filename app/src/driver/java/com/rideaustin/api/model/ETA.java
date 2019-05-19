package com.rideaustin.api.model;

/**
 * Created by supreethks on 13/09/16.
 */
public class ETA {

   DirectionResponse.Routes.Legs.Duration duration;
   DirectionResponse.Routes.Legs.Distance distance;

   public ETA(DirectionResponse.Routes.Legs.Distance distance, DirectionResponse.Routes.Legs.Duration duration) {
      this.distance = distance;
      this.duration = duration;
   }

   /**
    * @return distance value in meter
    */
   public DirectionResponse.Routes.Legs.Distance getDistance() {
      return distance;
   }

   /**
    * @return duration value in seconds
     */
   public DirectionResponse.Routes.Legs.Duration getDuration() {
      return duration;
   }

   @Override
   public String toString() {
      return "ETA{" +
              "distance=" + distance +
              ", duration=" + duration +
              '}';
   }


}
