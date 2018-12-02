.. _embedded:

Embedded Servers and Apps
=========================

Finatra provides a way to run an embedded version of your service or app running locally on ephemeral
ports. This allows you to run *actual* requests against an *actual* version of your server when testing.

The embedded utilities are also useful for testing and debugging your code when prototyping. If your
service or API makes calls to other services, instead of mocking out or overriding those dependencies
with dummy implementations you can always write a test using an Embedded version of your server which
talks to *real* downstream services (of course you'd never want to commit a test like this to your
source repository, especially if you run any type of `continuous integration <https://en.wikipedia.org/wiki/Continuous_integration>`__ system).
You'll be able to run this test normally through the test runner of an IDE which would allow you to
easily set breakpoints and step-through code for debugging. As opposed to needing to build and run
your service locally and attach a remote debugger.

See:

-  `c.t.inject.app.EmbeddedApp <https://github.com/twitter/finatra/blob/develop/inject/inject-app/src/test/scala/com/twitter/inject/app/EmbeddedApp.scala>`__
-  `c.t.inject.server.EmbeddedTwitterServer <https://github.com/twitter/finatra/blob/develop/inject/inject-server/src/test/scala/com/twitter/inject/server/EmbeddedTwitterServer.scala>`__
-  `c.t.finatra.http.EmbeddedHttpServer <https://github.com/twitter/finatra/blob/develop/http/src/test/scala/com/twitter/finatra/http/EmbeddedHttpServer.scala>`__
-  `c.t.finatra.thrift.EmbeddedThriftServer <https://github.com/twitter/finatra/blob/develop/thrift/src/test/scala/com/twitter/finatra/thrift/EmbeddedThriftServer.scala>`__


.. image:: ../../_static/embedded.png

You'll notice that this hierarchy generally follows the server trait hierarchy as |c.t.finatra.http.HttpServer|_
and |c.t.finatra.thrift.ThriftServer|_ extend from |c.t.server.TwitterServer|_ which extends from
|c.t.app.App|_.

InMemoryStatsReceiver
---------------------

The |EmbeddedTwitterServer|_ (and thus its subclasses: |EmbeddedHttpServer|_ and |EmbeddedThriftServer|_)
binds an instance of the `com.twitter.finagle.stats.InMemoryStatsReceiver <https://github.com/twitter/util/blob/develop/util-stats/src/main/scala/com/twitter/finagle/stats/InMemoryStatsReceiver.scala>`__
to the underlying server's object graph (if the underlying server supports injection). This will
override any other bound implementation of a `c.t.finagle.stats.StatsReceiver <https://github.com/twitter/util/blob/develop/util-stats/src/main/scala/com/twitter/finagle/stats/StatsReceiver.scala>`__
in the server's object graph.

The |EmbeddedTwitterServer|_ exposes the bound `StatsReceiver <https://github.com/twitter/util/blob/develop/util-stats/src/main/scala/com/twitter/finagle/stats/StatsReceiver.scala>`__
along with helper methods for asserting `counter <https://github.com/twitter/finatra/blob/c6e4716f082c0c8790d06d9e1664aacbd0c3fede/inject/inject-server/src/test/scala/com/twitter/inject/server/EmbeddedTwitterServer.scala#L323>`__,
`stat <https://github.com/twitter/finatra/blob/c6e4716f082c0c8790d06d9e1664aacbd0c3fede/inject/inject-server/src/test/scala/com/twitter/inject/server/EmbeddedTwitterServer.scala#L335>`__,
and `gauge <https://github.com/twitter/finatra/blob/c6e4716f082c0c8790d06d9e1664aacbd0c3fede/inject/inject-server/src/test/scala/com/twitter/inject/server/EmbeddedTwitterServer.scala#L343>`__
values, such that you can expect behavior against the underlying server's recorded stats in tests.

`Feature Tests <#feature_tests>`__ also `print all recorded stats <https://github.com/twitter/finatra/blob/c6e4716f082c0c8790d06d9e1664aacbd0c3fede/inject/inject-server/src/test/scala/com/twitter/inject/server/FeatureTestMixin.scala#L50>`__
to stdout after each test by default.

See: `c.t.finatra.multiserver.test.MultiServerFeatureTest <https://github.com/twitter/finatra/blob/develop/inject-thrift-client-http-mapper/src/test/scala/com/twitter/finatra/multiserver/test/MultiServerFeatureTest.scala>`__
for an example usage.

More Information
----------------

- :doc:`index`
- :doc:`feature_tests`
- :doc:`integration_tests`
- :doc:`startup_tests`
- :doc:`mixins`
- :doc:`mocks`
- :doc:`override_modules`
- :doc:`bind_dsl`

.. |c.t.finatra.http.HttpServer| replace:: `c.t.finatra.http.HttpServer`
.. _c.t.finatra.http.HttpServer: https://github.com/twitter/finatra/blob/develop/http/src/main/scala/com/twitter/finatra/http/HttpServer.scala

.. |c.t.finatra.thrift.ThriftServer| replace:: `c.t.finatra.thrift.ThriftServer`
.. _c.t.finatra.thrift.ThriftServer: https://github.com/twitter/twitter-server/blob/develop/src/main/scala/com/twitter/server/TwitterServer.scala

.. |c.t.server.TwitterServer| replace:: `c.t.server.TwitterServer`
.. _c.t.server.TwitterServer: https://github.com/twitter/twitter-server/blob/develop/src/main/scala/com/twitter/server/TwitterServer.scala

.. |c.t.app.App| replace:: `c.t.app.App`
.. _c.t.app.App: https://github.com/twitter/util/blob/develop/util-app/src/main/scala/com/twitter/app/App.scala

.. |EmbeddedTwitterServer| replace:: `EmbeddedTwitterServer`
.. _EmbeddedTwitterServer: https://github.com/twitter/finatra/blob/develop/inject/inject-server/src/test/scala/com/twitter/inject/server/EmbeddedTwitterServer.scala

.. |EmbeddedHttpServer| replace:: `EmbeddedHttpServer`
.. _EmbeddedHttpServer: https://github.com/twitter/finatra/blob/develop/http/src/test/scala/com/twitter/finatra/http/EmbeddedHttpServer.scala

.. |EmbeddedThriftServer| replace:: `EmbeddedThriftServer`
.. _EmbeddedThriftServer: https://github.com/twitter/finatra/blob/develop/thrift/src/test/scala/com/twitter/finatra/thrift/EmbeddedThriftServer.scala
