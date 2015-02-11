##About

A framework for building a [pubsubhub](https://pubsubhubbub.googlecode.com/git/pubsubhubbub-core-0.4.html).
No particular database is required to use it. However, a fully-functional implementation that
uses [MySQL](http://www.mysql.com/) for subscription storage
is included in the distribution.

##Documentation

* [Javadoc](https://www.attribyte.org/projects/pubsubhub/javadoc/index.html)

##Building

The build uses [Apache Ant](http://ant.apache.org/) and
[Apache Ivy](https://ant.apache.org/ivy/) to resolve dependencies. The following ant tasks
are available:

* `init-ivy` - Download and install ivy, if necessary
* `compile` - Compiles the source
* `dist` - Resolves dependencies, compiles the source, and creates a jar in `dist/lib`. This is the default task.
* `full-dist` - Resolves dependencies, compiles the source, creates a jar in `dist/lib`, and copies dependencies to `dist/extlib`
* `clean` - Removes all build files and jars.
* `javadoc` - Generates javadoc.
* `cleandoc` - Removes generated javadoc.

##H2 Quick Start

[H2](http://www.h2database.com/html/main.html) is an embedded database that can store tables in-memory
or on-disk.

* If ivy is not installed with your ant distribution: `ant init-ivy`
* Build: `ant full-dist`
* Create an install-specific config: `cp config/example.h2.properties config/local.properties`
* The default settings use an in-memory database that is destroyed on restart. If you wish to persist
  subscriptions, use the alternate `endpoint.acp.connection.pubsub.connectionString` property with
  a local directory.
* Start the server: `bin/start`
* Go to `http://localhost:8086/metrics` to verify that the server is running.
* Stop the server: `bin/stop`

##MySQL Quick Start

* If ivy is not installed with your ant distribution: `ant init-ivy`
* Build: `ant full-dist`
* Create a `pubsub` database.
* Create the database tables: `mysql -u [your user] -p -h [your host] pubsub < config/mysql_pubsub_hub.sql`
* Create an install-specific config: `cp config/example.mysql.properties config/local.properties`
* Edit `config/local.properties` to change `pubsub.user`, `pubsub.password` and `pubsub.connectionString` for your database.
  You may also want to change any usernames/passwords from their defaults at this time.
* Start the server: `bin/start`
* Go to `http://localhost:8086/metrics` to verify that the server is running.
* Stop the server: `bin/stop`

##Framework Dependencies

* [Attribyte http-model](https://github.com/attribyte/http-model)
* [Attribyte shared-base](https://github.com/attribyte/shared-base)
* [Apache commons-codec](http://commons.apache.org/proper/commons-codec/)
* [Google Guava](https://code.google.com/p/guava-libraries/)
* [Dropwizard Metrics](http://metrics.codahale.com/)
* [Apache Log4J](http://logging.apache.org/log4j/2.x/)

##Additional Dependencies for Default (MySQL) Implementation

* [Attribyte ACP](https://github.com/attribyte/acp)
* [Apache commons-httpclient](http://hc.apache.org/httpclient-3.x/)
* [Jetty](http://www.eclipse.org/jetty/documentation/current/)
* [MySQL Connector (GPL)](http://dev.mysql.com/downloads/connector/j/)
* [Typesafe Config](https://github.com/typesafehub/config)

##License

Copyright 2014 [Attribyte, LLC](https://attribyte.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
