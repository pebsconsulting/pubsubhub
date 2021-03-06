/*
 * Copyright 2014 Attribyte, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package org.attribyte.api.pubsub.impl.server.admin.model;

import org.attribyte.api.pubsub.CallbackMetrics;

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
