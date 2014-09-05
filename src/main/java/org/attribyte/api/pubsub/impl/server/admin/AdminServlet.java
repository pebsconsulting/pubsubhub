package org.attribyte.api.pubsub.impl.server.admin;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.attribyte.api.Logger;
import org.attribyte.api.http.AuthScheme;
import org.attribyte.api.http.impl.BasicAuthScheme;
import org.attribyte.api.pubsub.CallbackMetrics;
import org.attribyte.api.pubsub.HostCallbackMetrics;
import org.attribyte.api.pubsub.HubDatastore;
import org.attribyte.api.pubsub.HubEndpoint;
import org.attribyte.api.pubsub.Subscriber;
import org.attribyte.api.pubsub.Subscription;
import org.attribyte.api.pubsub.Topic;
import org.attribyte.api.pubsub.impl.client.BasicAuth;
import org.attribyte.api.pubsub.impl.server.admin.model.DisplayCallbackMetrics;
import org.attribyte.api.pubsub.impl.server.admin.model.DisplayMetricsDetail;
import org.attribyte.api.pubsub.impl.server.admin.model.DisplaySubscribedHost;
import org.attribyte.api.pubsub.impl.server.admin.model.DisplayTopic;
import org.attribyte.util.URIEncoder;
import org.stringtemplate.v4.DateRenderer;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.STMessage;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdminServlet extends HttpServlet {

   public AdminServlet(final HubEndpoint endpoint,
                       final AdminAuth auth,
                       final String templateDirectory,
                       final Logger logger) {
      this.endpoint = endpoint;
      this.datastore = endpoint.getDatastore();
      this.auth = auth;
      this.templateGroup = new STGroupDir(templateDirectory, '$', '$');
      this.templateGroup.setListener(new ErrorListener());
      this.templateGroup.registerRenderer(java.util.Date.class, new DateRenderer());

      File globalConstantsFile = new File(templateDirectory, "constants.stg");
      STGroupFile globalConstants = null;
      if(globalConstantsFile.exists()) {
         globalConstants = new STGroupFile(globalConstantsFile.getAbsolutePath());
         this.templateGroup.importTemplates(globalConstants);
      }

      this.logger = logger;
   }

   public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      if(!auth.authIsValid(request, response)) return;
      List<String> path = splitPath(request);
      String obj = path.size() > 0 ? path.get(0) : null;
      if(obj != null) {
         if(obj.equals("subscription")) {
            postSubscriptionEdit(request, response);
         } else if(obj.equals("topic")) {
            postTopicAdd(request, response);
         } else {
            sendNotFound(response);
         }
      } else {
         sendNotFound(response);
      }
   }

   public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      if(!auth.authIsValid(request, response)) return;
      List<String> path = splitPath(request);
      String obj = path.size() > 0 ? path.get(0) : null;

      if(obj == null || obj.equals("topics")) {
         boolean activeOnly = obj == null || path.size() > 1 && path.get(1).equals("active");
         renderTopics(request, activeOnly, response);
      } else if(obj.equals("subscribers")) {
         renderSubscribers(request, response);
      } else if(obj.equals("topic")) {
         if(path.size() > 1) {
            boolean activeOnly = path.size() > 2 && path.get(2).equals("active");
            renderTopicSubscriptions(path.get(1), request, activeOnly, response);
         } else {
            sendNotFound(response);
         }
      } else if(obj.equals("host")) {
         if(path.size() > 1) {
            boolean activeOnly = path.size() > 2 && path.get(2).equals("active");
            renderHostSubscriptions(path.get(1), request, activeOnly, response);
         } else {
            sendNotFound(response);
         }
      } else if(obj.equals("subscriptions")) {
         boolean activeOnly = path.size() > 1 && path.get(1).equals("active");
         renderAllSubscriptions(request, activeOnly, response);
      } else if(obj.equals("metrics")) {
         if(path.size() > 1) {
            renderCallbackMetricsDetail(request, path.get(1), response);
         } else {
            renderCallbackMetrics(request, response);
         }
      } else {
         sendNotFound(response);
      }
   }

   /**
    * Splits the path into a list of components.
    * @param request The request.
    * @return The path.
    */
   private List<String> splitPath(final HttpServletRequest request) {
      String pathInfo = request.getPathInfo();
      if(pathInfo == null || pathInfo.length() == 0 || pathInfo.equals("/")) {
         return Collections.emptyList();
      } else {
         return Lists.newArrayList(pathSplitter.split(pathInfo));
      }
   }

   private void renderSubscribers(final HttpServletRequest request,
                                  final HttpServletResponse response) throws IOException {

      ST mainTemplate = getTemplate("main");
      ST subscriberTemplate = getTemplate("subscribers");

      try {
         List<DisplaySubscribedHost> subscribers = Lists.newArrayListWithExpectedSize(25);
         List<String> endpoints = datastore.getSubscribedHosts(0, 50);
         for(String host : endpoints) {
            subscribers.add(new DisplaySubscribedHost(host, datastore.countActiveHostSubscriptions(host)));
         }
         subscriberTemplate.add("subscribers", subscribers);
         mainTemplate.add("content", subscriberTemplate.render());
         response.setContentType("text/html");
         response.getWriter().print(mainTemplate.render());
         response.getWriter().flush();
      } catch(Exception se) {
         se.printStackTrace();
         response.sendError(500, "Datastore error");
      }
   }

   private void renderTopics(final HttpServletRequest request,
                             boolean activeOnly,
                             final HttpServletResponse response) throws IOException {

      ST mainTemplate = getTemplate("main");
      ST subscriberTemplate = getTemplate("topics");

      try {
         List<DisplayTopic> displayTopics = Lists.newArrayListWithExpectedSize(25);

         List<Topic> topics = activeOnly ? datastore.getActiveTopics(0, 50) : datastore.getTopics(0, 50);
         for(Topic topic : topics) {
            displayTopics.add(new DisplayTopic(topic, datastore.countActiveSubscriptions(topic.getId())));
         }
         subscriberTemplate.add("topics", displayTopics);
         subscriberTemplate.add("activeOnly", activeOnly);
         mainTemplate.add("content", subscriberTemplate.render());
         response.setContentType("text/html");
         response.getWriter().print(mainTemplate.render());
         response.getWriter().flush();
      } catch(Exception se) {
         se.printStackTrace();
         response.sendError(500, "Datastore error");
      }
   }


   private Set<Subscription.Status> getSubscriptionStatus(HttpServletRequest request, final boolean activeOnly) {

      if(activeOnly) {
         return Collections.singleton(Subscription.Status.ACTIVE);
      }

      String[] status = request.getParameterValues("status");
      if(status == null || status.length == 0) {
         return Collections.emptySet();
      } else {
         Set<Subscription.Status> statusSet = Sets.newHashSetWithExpectedSize(4);
         for(String s : status) {
            Subscription.Status toAdd = Subscription.Status.valueOf(s);
            if(toAdd != Subscription.Status.INVALID) {
               statusSet.add(toAdd);
            }
         }

         return statusSet;
      }
   }

   private void renderTopicSubscriptions(final String topicIdStr, final HttpServletRequest request,
                                         final boolean activeOnly,
                                         final HttpServletResponse response) throws IOException {

      ST mainTemplate = getTemplate("main");
      ST subscriptionsTemplate = getTemplate("topic_subscriptions");

      try {
         long topicId = Long.parseLong(topicIdStr);
         Topic topic = datastore.getTopic(topicId);
         if(topic == null) {
            sendNotFound(response);
            return;
         }

         List<Subscription> subscriptions = datastore.getTopicSubscriptions(topic, getSubscriptionStatus(request, activeOnly), 0, 50);
         subscriptionsTemplate.add("subscriptions", subscriptions);
         subscriptionsTemplate.add("topic", new DisplayTopic(topic, 0));
         subscriptionsTemplate.add("activeOnly", activeOnly);
         mainTemplate.add("content", subscriptionsTemplate.render());
         response.setContentType("text/html");
         response.getWriter().print(mainTemplate.render());
         response.getWriter().flush();
      } catch(NumberFormatException nfe) {
         response.sendError(400, "Invalid topic id");
      } catch(Exception se) {
         se.printStackTrace();
         response.sendError(500, "Datastore error");
      }
   }

   private void renderHostSubscriptions(final String host, final HttpServletRequest request,
                                        final boolean activeOnly,
                                        final HttpServletResponse response) throws IOException {

      ST mainTemplate = getTemplate("main");
      ST subscriptionsTemplate = getTemplate("host_subscriptions");

      try {

         List<Subscription> subscriptions = datastore.getHostSubscriptions(host, getSubscriptionStatus(request, activeOnly), 0, 50);
         subscriptionsTemplate.add("subscriptions", subscriptions);
         subscriptionsTemplate.add("host", host);
         subscriptionsTemplate.add("activeOnly", activeOnly);
         mainTemplate.add("content", subscriptionsTemplate.render());
         response.setContentType("text/html");
         response.getWriter().print(mainTemplate.render());
         response.getWriter().flush();
      } catch(NumberFormatException nfe) {
         response.sendError(400, "Invalid topic id");
      } catch(Exception se) {
         se.printStackTrace();
         response.sendError(500, "Datastore error");
      }
   }

   private void renderAllSubscriptions(final HttpServletRequest request,
                                       final boolean activeOnly,
                                       final HttpServletResponse response) throws IOException {

      ST mainTemplate = getTemplate("main");
      ST subscriptionsTemplate = getTemplate("all_subscriptions");

      try {
         List<Subscription> subscriptions = datastore.getSubscriptions(getSubscriptionStatus(request, activeOnly), 0, 50);
         subscriptionsTemplate.add("subscriptions", subscriptions);
         subscriptionsTemplate.add("activeOnly", activeOnly);
         mainTemplate.add("content", subscriptionsTemplate.render());
         response.setContentType("text/html");
         response.getWriter().print(mainTemplate.render());
         response.getWriter().flush();
      } catch(NumberFormatException nfe) {
         response.sendError(400, "Invalid topic id");
      } catch(IOException ioe) {
         throw ioe;
      } catch(Exception se) {
         se.printStackTrace();
         response.sendError(500, "Datastore error");
      }
   }

   private void renderCallbackMetrics(final HttpServletRequest request,
                                      final HttpServletResponse response) throws IOException {

      ST mainTemplate = getTemplate("main");
      ST metricsTemplate = getTemplate("callback_metrics");

      try {

         CallbackMetrics globalMetrics = endpoint.getGlobalCallbackMetrics();
         List<HostCallbackMetrics> metrics = endpoint.getHostCallbackMetrics(CallbackMetrics.Sort.THROUGHPUT_DESC, 25);

         List<DisplayCallbackMetrics> displayMetrics = Lists.newArrayListWithCapacity(metrics.size() + 1);
         displayMetrics.add(new DisplayCallbackMetrics("[all]", globalMetrics));
         for(HostCallbackMetrics hcm : metrics) {
            displayMetrics.add(new DisplayCallbackMetrics(hcm.host, hcm));
         }
         metricsTemplate.add("metrics", displayMetrics);
         mainTemplate.add("content", metricsTemplate.render());
         response.setContentType("text/html");
         response.getWriter().print(mainTemplate.render());
         response.getWriter().flush();
      } catch(IOException ioe) {
         throw ioe;
      } catch(Exception se) {
         se.printStackTrace();
         response.sendError(500, "Datastore error");
      }
   }

   private void renderCallbackMetricsDetail(final HttpServletRequest request,
                                            final String hostOrId,
                                            final HttpServletResponse response) throws IOException {

      ST mainTemplate = getTemplate("main");
      ST metricsTemplate = getTemplate("metrics_detail");

      try {
         final CallbackMetrics detailMetrics;
         final String title;

         long subscriptionId = 0L;
         if(hostOrId != null) {
            try {
               subscriptionId = Long.parseLong(hostOrId);
            } catch(NumberFormatException ne) {
               subscriptionId = 0L;
            }
         }

         final String host = hostOrId;
         if(subscriptionId > 0) {
            Subscription subscription = datastore.getSubscription(subscriptionId);
            if(subscription != null) {
               title = subscription.getCallbackURL();
               detailMetrics = endpoint.getSubscriptionCallbackMetrics(subscriptionId);
            } else {
               sendNotFound(response);
               return;
            }
         } else if(host == null || host.equalsIgnoreCase("[all]")) {
            title = "All Hosts";
            detailMetrics = endpoint.getGlobalCallbackMetrics();
         } else {
            title = host;
            detailMetrics = endpoint.getHostCallbackMetrics(host);
         }
         metricsTemplate.add("metrics", new DisplayMetricsDetail(title, detailMetrics));
         mainTemplate.add("content", metricsTemplate.render());
         response.setContentType("text/html");
         response.getWriter().print(mainTemplate.render());
         response.getWriter().flush();
      } catch(IOException ioe) {
         throw ioe;
      } catch(Exception se) {
         se.printStackTrace();
         response.sendError(500, "Datastore error");
      }
   }

   private void postTopicAdd(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
      String url = request.getParameter("url");
      if(url == null || url.trim().length() == 0) {
         response.sendError(400);
         return;
      }

      try {
         Topic topic = datastore.getTopic(url.trim(), false);
         if(topic != null) { //Exists
            response.setStatus(200);
            response.getWriter().println("false");
         } else {
            topic = datastore.getTopic(url.trim(), true);
            response.setStatus(201);
            response.getWriter().println("true");
         }
      } catch(Exception se) {
         logger.error("Problem adding topic", se);
         response.sendError(500);
      }
   }

   private final Map<String, Integer> extendLeaseValues = ImmutableMap.of(
           "hour", 3600,
           "day", 24 * 3600,
           "week", 24 * 7 * 3600,
           "month", 24 * 7 * 30 * 3600,
           "never", Integer.MAX_VALUE
   );

   private int translateExtendLease(final String extendLease) {
      Integer extendLeaseSeconds = extendLeaseValues.get(extendLease != null ? extendLease : "week");
      return extendLeaseSeconds != null ? extendLeaseSeconds : extendLeaseValues.get("week");
   }

   private void postSubscriptionEdit(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
      String idStr = request.getParameter("id");
      if(idStr == null || idStr.trim().length() == 0) {
         response.sendError(400);
         return;
      }

      String action = request.getParameter("op");
      //Expect: 'enable', disable', 'expire', 'extend'

      int extendLeaseSeconds = translateExtendLease(request.getParameter("extendLease"));

      try {
         long id = Long.parseLong(idStr);
         Subscription subscription = datastore.getSubscription(id);
         if(subscription != null) {
            if(action == null || action.length() == 0) {
               response.getWriter().print(subscription.getStatus().toString());
               response.setStatus(200);
            } else if(action.equals("enable")) {
               if(!subscription.isActive()) {
                  datastore.changeSubscriptionStatus(id, Subscription.Status.ACTIVE, extendLeaseSeconds);
               }
               response.setStatus(200);
               response.getWriter().print("ACTIVE");
            } else if(action.equals("disable")) {
               if(!subscription.isRemoved()) {
                  datastore.changeSubscriptionStatus(id, Subscription.Status.REMOVED, 0);
               }
               response.setStatus(200);
               response.getWriter().print("REMOVED");
            } else if(action.equals("expire")) {
               if(!subscription.isExpired()) {
                  datastore.expireSubscription(id);
               }
               response.setStatus(200);
               response.getWriter().print("EXPIRED");
            } else if(action.equals("extend")) {
               datastore.changeSubscriptionStatus(id, Subscription.Status.ACTIVE, extendLeaseSeconds);
               response.getWriter().print("ACTIVE");
               response.setStatus(200);
            } else {
               response.getWriter().print(subscription.getStatus().toString());
               response.setStatus(200);
            }
         } else {
            sendNotFound(response);
         }
      } catch(IOException ioe) {
         throw ioe;
      } catch(Exception se) {
         logger.error("Problem editing subscription", se);
         response.sendError(500);
      }
   }

   private final URIEncoder urlEncoder = new URIEncoder();

   private void postSubscriptionAdd(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

      try {

         String topicURL = request.getParameter("hub.topic");

         if(topicURL == null || topicURL.length() == 0) {
            response.sendError(400, "The 'hub.topic' must be specified");
            return;
         }

         try {
            topicURL = urlEncoder.recode(topicURL);
         } catch(Exception e) {
            response.sendError(400, "The 'hub.topic' is invalid");
            return;
         }


         String callbackURL = request.getParameter("hub.callback");

         if(callbackURL == null || callbackURL.length() == 0) {
            response.sendError(400, "The 'hub.callback' must be specified");
            return;
         }

         try {
            callbackURL = urlEncoder.recode(callbackURL);
         } catch(Exception e) {
            response.sendError(400, "The 'hub.callback' is invalid");
            return;
         }

         String callbackHostURL = org.attribyte.api.http.Request.getHostURL(callbackURL);
         String callbackAuthScheme = request.getParameter("hub.x-callback_auth_scheme");
         String authId = null;
         AuthScheme authScheme = null;

         if(callbackAuthScheme != null) {

            if(callbackAuthScheme.equalsIgnoreCase("basic")) {
               authScheme = datastore.resolveAuthScheme("Basic");
               String callbackUsername = request.getParameter("hub.x-callback_auth_username");
               String callbackPassword = request.getParameter("hub.x-callback_auth_password");
               if(callbackUsername != null && callbackPassword != null) {
                  authId = BasicAuthScheme.buildAuthHeaderValue(callbackUsername, callbackPassword).substring("Basic ".length()).trim();
               } else {
                  response.sendError(400, "Auth username and password must be specified");
               }

            } else {
               response.sendError(400, "Only 'Basic' auth is currently supported");
               return;
            }
         }

         Subscriber subscriber = datastore.getSubscriber(callbackHostURL, authScheme, authId, true); //Create, if required.

      } catch(IOException ioe) {
         throw ioe;
      } catch(Exception se) {
         logger.error("Problem editing subscription", se);
         response.sendError(500);
      }


      //topicURL = urlDecoder.recode(request.getParameterValue("hub.topic"));
      //callbackURL = urlDecoder.recode(request.getParameterValue("hub.callback"));
      //callbackHostURL = Request.getHostURL(callbackURL);
      //String callbackAuthScheme = request.getParameterValue("hub.x-callback_auth_scheme");
      //String callbackAuth = request.getParameterValue("hub.x-callback_auth");
      //Subscriber subscriber = datastore.getSubscriber(callbackHostURL, authScheme, authId, true); //Create...
      /*
            Subscription subscription = datastore.getSubscription(topicURL, callbackURL);
            Subscription.Builder builder;
            if(subscription == null) {
               Topic topic = datastore.getTopic(topicURL, true);
               builder = new Subscription.Builder(0L, callbackURL, topic, subscriber);
            } else {
               builder = new Subscription.Builder(subscription, subscriber);
            }

            builder.setStatus(status);
            builder.setLeaseSeconds(Integer.parseInt(leaseSecondsStr));
            builder.setSecret(hubSecret);
            Subscription updatedSubscription = datastore.updateSubscription(builder.create(), true); //Extend lease...
       */


   }

   private void sendNotFound(HttpServletResponse response) throws IOException {
      response.sendError(404, "Not Found");
   }

   /**
    * Gets a template instance.
    * @param name The template name.
    * @return The instance.
    */
   private ST getTemplate(final String name) {
      try {
         if(debug) templateGroup.unload();
         ST template = templateGroup.getInstanceOf(name);
         if(template != null && name.equals("main")) { //Add metadata...
            template.add("hostname", InetAddress.getLocalHost().getHostName());
            template.add("time", new Date());
         }
         return template;
      } catch(Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   static final class ErrorListener implements STErrorListener {

      public void compileTimeError(STMessage msg) {
         System.out.println(msg);
      }

      public void runTimeError(STMessage msg) {
         System.out.println(msg);
      }

      public void IOError(STMessage msg) {
         System.out.println(msg);
      }

      public void internalError(STMessage msg) {
         System.out.println(msg);
      }
   }

   private final HubDatastore datastore;
   private final HubEndpoint endpoint;
   private final Logger logger;
   private final AdminAuth auth;
   private final STGroup templateGroup;
   private final Splitter pathSplitter = Splitter.on('/').omitEmptyStrings().trimResults();
   private final boolean debug = true;
}