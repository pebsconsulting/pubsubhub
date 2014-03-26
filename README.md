##About

A framework for building a [pubsubhub](https://pubsubhubbub.googlecode.com/git/pubsubhubbub-core-0.4.html).
A fully-functional implementation that uses [MySQL](http://www.mysql.com/) is included. A barely tested (OK. Untested.)
notification component is included that uses [Redis](http://redis.io/) queues to store notifications to allow
cooperating hubs.

##Building

The build uses [Apache Ant](http://ant.apache.org/) and
[Apache Ivy](https://ant.apache.org/ivy/) to resolve dependencies. The following ant tasks
are available:

* compile - Compiles the source
* dist - Resolves dependencies, compiles the source, and creates a jar in dist/lib. This is the default task.
* full-dist - Resolves dependencies, compiles the source, creates a jar in dist/lib, and copies dependencies to dist/extlib
* clean - Removes all build files and jars.

##Framework Dependencies

* [commons-codec](http://commons.apache.org/proper/commons-codec/)
* [Google Guava](https://code.google.com/p/guava-libraries/)
* [Metrics](http://metrics.codahale.com/)
* [Log4J](http://logging.apache.org/log4j/2.x/)
* TODO: Several Attribyte libraries are included with no source. In-progress.

##Additional Dependencies for Default (MySQL) Implementation

* [commons-httpclient](http://hc.apache.org/httpclient-3.x/)
* [Jetty](http://www.eclipse.org/jetty/documentation/current/)
* [MySQL Connector (GPL)](http://dev.mysql.com/downloads/connector/j/)
* [Typesafe Config](https://github.com/typesafehub/config)

##Additional Dependencies for Default Implementation with Redis-Stored Notifications

* [Jedis](https://github.com/xetorthio/jedis)

##License

Copyright 2014 [Attribyte, LLC](https://attribyte.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.