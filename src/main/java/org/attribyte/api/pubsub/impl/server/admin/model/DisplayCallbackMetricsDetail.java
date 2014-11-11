package org.attribyte.api.pubsub.impl.server.admin.model;

import org.attribyte.api.pubsub.CallbackMetrics;

import java.text.NumberFormat;

public class DisplayCallbackMetricsDetail {

   public DisplayCallbackMetricsDetail(String name, CallbackMetrics metrics) {
      this.name = name;
      this.callbacks = new DisplayTimer("All Callbacks", metrics.callbacks);
      this.timeToCallback = new DisplayTimer("Time to Callback", metrics.timeToCallback);
      this.abandonedCallbacks = new DisplayMetered("Abandoned Callbacks", metrics.abandonedCallbacks);
      this.failedCallbacks = new DisplayMetered("Failed Callbacks", metrics.failedCallbacks);

   }

   public String getName() {
      return name;
   }

   public DisplayTimer getCallbacks() {
      return callbacks;
   }

   public DisplayTimer getTimeToCallback() {
      return timeToCallback;
   }

   public DisplayMetered getAbandonedCallbacks() {
      return abandonedCallbacks;
   }

   public DisplayMetered getFailedCallbacks() {
      return failedCallbacks;
   }

   private final String name;
   private final DisplayTimer callbacks;
   private final DisplayTimer timeToCallback;
   private final DisplayMetered abandonedCallbacks;
   private final DisplayMetered failedCallbacks;

}
