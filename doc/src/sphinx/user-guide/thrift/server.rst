.. _thrift_server:

Note: the most up-to-date examples are located in the `finatra/examples <https://github.com/twitter/finatra/tree/master/examples>`__
project.

See `examples/thrift-server <https://github.com/twitter/finatra/tree/master/examples/thrift-server>`__
for an example `ThriftServer`.

Thrift Server Definition
========================

To start, add a dependency on the `finatra-thrift <http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.twitter%22%20AND%20a%3A%22finatra-thrift_2.12%22>`__
library. We also recommend using `Logback <http://logback.qos.ch/>`__ as your
`SLF4J <http://www.slf4j.org/manual.html>`__ implementation. E.g., with sbt:

.. parsed-literal::

    "com.twitter" %% "finatra-thrift" % "\ |release|\ "
    "ch.qos.logback" % "logback-classic" % versions.logback,

For more information on logging with Finatra see: `Introduction to Logging With Finatra <../logging/index.html#introduction-to-logging-with-finatra>`__.

Create a new class that extends `c.t.finatra.thrift.ThriftServer <https://github.com/twitter/finatra/blob/700e741f41368d9bc2ba76489e0641c4cfb8545a/thrift/src/main/scala/com/twitter/finatra/thrift/servers.scala#L143>`__:

.. code:: scala

    import com.twitter.finatra.thrift.ThriftServer
    import com.twitter.finatra.thrift.routing.ThriftRouter

    object ExampleServerMain extends ExampleServer

    class ExampleServer extends ThriftServer {

      override def configureThrift(router: ThriftRouter): Unit = {
        ???
      }
    }

Or in Java, that extends the `c.t.finatra.thrift.AbstractThriftServer <https://github.com/twitter/finatra/blob/700e741f41368d9bc2ba76489e0641c4cfb8545a/thrift/src/main/scala/com/twitter/finatra/thrift/servers.scala#L168>`__:

.. code:: java

    import com.twitter.finatra.thrift.AbstractThriftServer;
    import com.twitter.finatra.thrift.routing.JavaThriftRouter;

    public class ExampleServer extends AbstractThriftServer {

      @Override
      public void configureThrift(JavaThriftRouter router) {
        ...
      }
    }

A more complete example includes adding Modules, a Controller, and Filters.

.. code:: scala

    import DoEverythingModule
    import com.twitter.finatra.thrift.ThriftServer
    import com.twitter.finatra.thrift.filters.{LoggingMDCFilter, TraceIdMDCFilter}
    import com.twitter.finatra.thrift.routing.ThriftRouter

    object ExampleServerMain extends ExampleServer

    class ExampleServer extends ThriftServer {

      override val modules = Seq(
        DoEverythingModule)

      override def configureThrift(router: ThriftRouter): Unit = {
        router
          .filter[LoggingMDCFilter]
          .filter[TraceIdMDCFilter]
          .add[ExampleThriftController]
      }
    }

in Java:

.. code:: java

    import DoEverythingModule;
    import com.google.common.collect.ImmutableList;
    import com.google.inject.Module;
    import com.twitter.finatra.thrift.AbstractThriftServer;
    import com.twitter.finatra.thrift.filters.LoggingMDCFilter;
    import com.twitter.finatra.thrift.filters.TraceIdMDCFilter;
    import com.twitter.finatra.thrift.routing.JavaThriftRouter;
    import java.util.Collection;

    public class ExampleServer extends AbstractThriftServer {

      @Override
      public Collection<Module> javaModules() {
        return ImmutableList.<Module>of(new DoEverythingModule());
      }

      @Override
      public void configureThrift(JavaThriftRouter router) {
        router
          .filter(LoggingMDCFilter.class)
          .filter(TraceIdMDCFilter.class)
          .add(ExampleThriftController.class);
      }
    }

.. tip::

    Note: to add `Modules <../getting-started/modules.html>`__ to your Java server override the
    `javaModules()` method.

This should look familiar as the structure is similar to creating an `HttpServer <../http/server.html>`__.
The server can be thought of as a collection of `controllers <controllers.html>`__ composed with
`filters <filters.html>`__. Additionally, a server can define `modules <../getting-started/modules.html>`__
for providing instances to the object graph.

Using Generated Java Code
-------------------------

Finatra assumes that users are using the `Scrooge Thrift code generator <https://twitter.github.io/scrooge/>`__
for generating code from a Thrift IDL. `Scrooge <https://twitter.github.io/scrooge/>`__ allows for
code generation in multiple languages but Finatra supports either generated Java or generated Scala
code only.

If you choose to use generated Java code for your service (instead of generated Scala), you MUST
extend the `AbstractThriftServer` and configure the `JavaThriftRouter`.

Most typically this is done when the server is being written in Java but is not exclusively so. This
means:

- if you write your server in Scala you can use either generated Java or generated Scala code.
- if you write your server in Java you MUST use generated Java code.

Naming Convention
-----------------

The Finatra convention is to create a Scala `object <https://twitter.github.io/scala_school/basics2.html#object>`__
with a name ending in "Main" that extends your server class. The server *class* can be used in
testing as this allows your server to be instantiated multiple times in tests without worrying about
static state persisting across test runs in the same JVM.

.. code:: scala

    object ExampleServerMain extends ExampleServer

The static object, e.g., `ExampleServerMain`, which then contains a static main method for the server
would then be used as the `application entry point <https://docs.oracle.com/javase/tutorial/deployment/jar/appman.html>`__
for running the server in all other cases.

Java Naming Convention
~~~~~~~~~~~~~~~~~~~~~~

In Java you would create a separate "main" class which defines a main method and accepts args
for flag parsing:

.. code:: java

    public final class ExampleServerMain {
        private ExampleServerMain() {
        }

        public static void main(String[] args) {
            new ExampleServer().main(args);
        }
    }

This would be the class used as the `application entry point <https://docs.oracle.com/javase/tutorial/deployment/jar/appman.html>`__
for running the server.

Override Default Behavior
-------------------------

Flags
~~~~~

Some deployment environments may make it difficult to set `Flag values <../getting-started/flags.html>`__
with command line arguments. If this is the case, Finatra's `ThriftServer <https://github.com/twitter/finatra/blob/develop/thrift/src/main/scala/com/twitter/finatra/thrift/ThriftServer.scala>`__'s
core flags can be set from code.

For example, instead of setting the `-thrift.port` flag, you can override the following method in
your server.

.. code:: scala

    import com.twitter.finatra.thrift.ThriftServer
    import com.twitter.finatra.thrift.routing.ThriftRouter

    class ExampleServer extends ThriftServer {

      override val defaultThriftPort: String = ":9090"

      override def configureThrift(router: ThriftRouter): Unit = {
        ???
      }
    }


For a list of what flags can be set programmatically, please see the `ThriftServer <https://github.com/twitter/finatra/blob/develop/thrift/src/main/scala/com/twitter/finatra/thrift/ThriftServer.scala>`__ class.

For more information on using and setting command-line flags see `Flags <../getting-started/flags.html#passing-flag-values-as-command-line-arguments>`__.

Finagle Server Configuration
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If you want to further configure the underlying `Finagle <https://github.com/twitter/finagle>`__
server you can override `configureThriftServer` in your server to specify additional configuration
on (or override the default configuration of) the underlying Finagle server.

For example:

.. code:: scala

    import com.twitter.finagle.ThriftMux
    import com.twitter.finatra.thrift.ThriftServer
    import com.twitter.finatra.thrift.routing.ThriftRouter

    class ExampleServer extends ThriftServer {

      override def configureThrift(router: ThriftRouter): Unit = {
        ...
      }

      override def configureThriftServer(server: ThriftMux.Server): ThriftMux.Server = {
        server
          .withMaxRequestSize(???)
          .withAdmissionControl.concurrencyLimit(
            maxConcurrentRequests = ???,
            maxWaiters = ???)
      }
    }


For more information on `Finagle <https://github.com/twitter/finagle>`__ server configuration see
the documentation `here <https://twitter.github.io/finagle/guide/Configuration.html>`__; specifically
the server documentation `here <https://twitter.github.io/finagle/guide/Servers.html>`__.

Server-side Response Classification
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To configure server-side `Response Classification <https://twitter.github.io/finagle/guide/Servers.html#response-classification>`__
you could choose to set the classifier directly on the underlying Finagle server by overriding the
`configureThriftServer` in your server, e.g.,

.. code:: scala

    override def configureThriftServer(server: ThriftMux.Server): ThriftMux.Server = {
        server.withResponseClassifier(???)
    }

However, since the server-side ResponseClassifier could affect code not just at the Finagle level,
we actually recommend overriding the specific framework module, `ThriftResponseClassifierModule`
instead. This binds an instance of an `ThriftResponseClassifier <https://github.com/twitter/finatra/blob/develop/thrift/src/main/scala/com/twitter/finatra/thrift/response/ThriftResponseClassifier.scala>`__
to the object graph that is then available to be injected into things like the Thrift
`StatsFilter <https://github.com/twitter/finatra/blob/develop/thrift/src/main/scala/com/twitter/finatra/thrift/filters/StatsFilter.scala>`__
for a more accurate reporting of metrics that takes into account server-side response classification.

For example, in your `ThriftServer` you would do:

.. code:: scala

    import com.google.inject.Module
    import com.twitter.finatra.http.HttpServer
    import com.twitter.finatra.http.routing.HttpRouter

    class ExampleServer extends ThriftServer {

      override thriftResponseClassifierModule: Module = ???
    }

The bound value is also then `set on the underlying Finagle server <https://github.com/twitter/finatra/blob/9d7b430ce469d1542b603938e3ec24cf6ff79d64/thrift/src/main/scala/com/twitter/finatra/thrift/ThriftServer.scala#L71>`__
before serving.

Testing
-------

For information on testing a Thrift server see the Thrift Server `Feature Tests <../testing/feature_tests.html#thrift-server>`__
section.
