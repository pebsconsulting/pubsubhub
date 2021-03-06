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

package org.attribyte.api.pubsub.impl.server;

import org.attribyte.api.http.Response;
import org.attribyte.api.http.ResponseBuilder;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.ObjectOutputStream;

@SuppressWarnings("serial")
public abstract class ServletBase extends HttpServlet {

   private void writeObject(ObjectOutputStream o) throws IOException {
      throw new IOException("Serialization of servlets is not supported");
   }

   /**
    * The response sent when the topic for broadcast does not exist.
    */
   protected static final Response ACCEPTED_RESPONSE =
           new ResponseBuilder(Response.Code.ACCEPTED, "").create();

   /**
    * The response sent when the topic for broadcast does not exist.
    */
   protected static final Response UNKNOWN_TOPIC_RESPONSE =
           new ResponseBuilder(Response.Code.BAD_REQUEST, "The topic does not exist").create();

   /**
    * The response sent when the topic was not specified.
    */
   protected static final Response NO_TOPIC_RESPONSE =
           new ResponseBuilder(Response.Code.BAD_REQUEST, "A topic must be specified").create();

   /**
    * The response sent when a notification exceeds that maximum allowed size.
    */
   protected static final Response NOTIFICATION_TOO_LARGE =
           new ResponseBuilder(Response.Code.BAD_REQUEST, "The notification is too large").create();

   /**
    * The response sent on internal server errors.
    */
   protected static final Response INTERNAL_ERROR_RESPONSE =
           new ResponseBuilder(Response.Code.SERVER_ERROR, "The hub is currently unable to accept broadcast requests").create();

   /**
    * The response sent on internal server errors due to capacity constraints.
    */
   protected static final Response CAPACITY_ERROR_RESPONSE =
           new ResponseBuilder(Response.Code.SERVER_UNAVAILABLE, "The hub is currently unable to accept broadcast requests due to capacity issues").create();

   /**
    * The response sent an operation is expected.
    */
   protected static final Response NO_OPERATION_RESPONSE =
           new ResponseBuilder(Response.Code.BAD_REQUEST, "An operation must be specified").create();
}
