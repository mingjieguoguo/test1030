.. _thrift_exceptions:

Thrift Exception Mapping
========================

It is recommended in Finatra that you generally prefer to use exceptions for flow control in your
controller and services and rely on the `c.t.finatra.thrift.exceptions.ExceptionMapper <https://github.com/twitter/finatra/blob/develop/thrift/src/main/scala/com/twitter/finatra/thrift/exceptions/ExceptionMapper.scala>`__
to convert exceptions into Thrift IDL defined exceptions or responses.

Why?
----

Exception mapping is meant to provide a server-wide mechanism for handling an exception in a
standard manner.

The framework is not proscriptive about this, however. Sometimes it may make sense to catch and
handle `FooException` directly in your controller (e.g., convert it to a Thrift response or perform
another action). You are encouraged to do what makes sense for your use case or team.


How?
----

The Finatra framework adds a `default <#default-exception-mapper>`__ `ExceptionMapper` to your
`ThriftServer` which provides a root-level *no-op* mapping for exceptions. You can register
additional mappers or override the default altogether.

For instance, if you want to map a `java.lang.ClassCastException` to a `ThriftException` -- e.g.,
`ClientError(ClientErrorCause, errorMessage)`, which is defined in `finatra_thrift_exceptions.thrift <https://github.com/twitter/finatra/blob/develop/thrift/src/main/thrift/finatra-thrift/finatra_thrift_exceptions.thrift>`__
you could create the following `ExceptionMapper`:

.. code:: scala

  import com.twitter.finatra.thrift.exceptions.ExceptionMapper
  import com.twitter.finatra.thrift.thriftscala.{ClientError, ClientErrorCause}
  import com.twitter.util.Future
  import java.lang.ClassCastException
  import javax.inject.Singleton

  @Singleton
  class ClassCastExceptionMapper
    extends ExceptionMapper[ClassCastException, ClientError] {

    def handleException(throwable: ClassCastException): Future[ClientError] = {
      Future.exception(ClientError(ClientErrorCause.BadRequest, throwable.getMessage))
    }
  }

Then register this exception mapper in your server.

.. code:: scala

    import com.twitter.finatra.thrift.ThriftServer
    import com.twitter.finatra.thrift.filters.ExceptionMappingFilter
    import com.twitter.finatra.thrift.routing.ThriftRouter

    class MyThriftServer extends ThriftServer {

      override def configureThrift(router: ThriftRouter): Unit = {
        router
          .filter[ExceptionMappingFilter]
          .exceptionMapper[ClassCastExceptionMapper]
        ...
      }

You can see here we register the exception mapper *by type* allowing the framework to instantiate
an instance.

More examples of mapping exceptions to Thrift responses are located in the test `mappers <https://github.com/twitter/finatra/blob/develop/thrift/src/test/scala/com/twitter/finatra/thrift/tests/doeverything/exceptions/mappers.scala>`__.

.. tip::

    Servers written in Java should extend the `AbstractThriftServer`, which assumes you are using
    generated Java code and thus requires you to also use the `JavaThriftRouter`. See the section
    on defining `servers <server.html>`__ for more information.

ExceptionMappingFilter
----------------------

Using exception mappers requires you to include the `c.t.finatra.thrift.filters.ExceptionMappingFilter <https://github.com/twitter/finatra/blob/develop/thrift/src/main/scala/com/twitter/finatra/thrift/filters/ExceptionMappingFilter.scala>`__
in your server's Filter chain.

For information on how to add a `Filter` to your `ThriftServer` see the `Filters <filters.html>`__
section.

.. important::

    The `ExceptionMappingFilter` works by attempting to match an incoming Exception type to a registered mapper for that type
    via the `ExceptionManager`. This means that you may need to pay attention to any generated exceptions or errors if
    you are generating code in multiple languages in your project. A generated Java Thrift Exception will be considered
    *different* from the same Exception generated in Scala.

Default Exception Mapper
------------------------

The framework adds only the `ThrowableExceptionMapper` to the `ExceptionManager` by default which
simply throws back any uncaught `Throwable`.

==============================  ==================================================================================================================================================================================
`Throwable`                     `ThrowableExceptionMapper <https://github.com/twitter/finatra/blob/develop/thrift/src/main/scala/com/twitter/finatra/thrift/internal/exceptions/ThrowableExceptionMapper.scala>`__
==============================  ==================================================================================================================================================================================

This default `ExceptionMapper` is a *no-op* because the framework does not have a way to turn an
exception into a meaningful Thrift IDL defined response. It is meant only to provide a backstop for
unhandled exception types since the `ExceptionManager` walks the exception type hierarchy starting
at the given exception type, moving up the inheritance chain until it finds a mapper configured for
the type.

In this manner, an `ExceptionMapper[Throwable]` will be the last mapper invoked and acts as the
"default". Therefore to change the framework "default" mapper, simply add a new mapper over the
`Throwable` type (i.e., `ExceptionMapper[Throwable]`) to the `ExceptionManager`.

Finatra Thrift Exceptions and Mapper
------------------------------------

The Finatra framework provides both a `FinatraThriftExceptionMapper and FinatraJavaThriftExceptionMapper <https://github.com/twitter/finatra/blob/develop/thrift/src/main/scala/com/twitter/finatra/thrift/exceptions/finatrathriftexceptions.scala>`__
for mapping exceptions to the `finatra_thrift_exceptions.thrift <https://github.com/twitter/finatra/blob/develop/thrift/src/main/thrift/finatra-thrift/finatra_thrift_exceptions.thrift>`__
defined exceptions.

For an example of including and using these exceptions see the test Thrift IDL defined `here <https://github.com/twitter/finatra/blob/develop/thrift/src/test/thrift/doeverything.thrift>`__.

If you are using `finatra_thrift_exceptions.thrift <https://github.com/twitter/finatra/blob/develop/thrift/src/main/thrift/finatra-thrift/finatra_thrift_exceptions.thrift>`__,
then it is recommended that your register one of the above mappers appropriate to the generated
language you are using.

Override Default Behavior
-------------------------

The `ExceptionManager <https://github.com/twitter/finatra/blob/develop/thrift/src/main/scala/com/twitter/finatra/thrift/exceptions/ExceptionManager.scala>`__
is the class that handles registration of exception mappers. In the example above, the `ThriftRouter#exceptionMapper <https://github.com/twitter/finatra/blob/c6e4716f082c0c8790d06d9e1664aacbd0c3fede/thrift/src/main/scala/com/twitter/finatra/thrift/routing/ThriftRouter.scala#L46>`__
method is simply registering the given mapper with the `ExceptionManager`.

The `ExceptionManager` is configured by the inclusion of the `ExceptionManagerModule <https://github.com/twitter/finatra/blob/develop/thrift/src/main/scala/com/twitter/finatra/thrift/modules/ExceptionManagerModule.scala>`__
as a framework module in every `ThriftServer <https://github.com/twitter/finatra/blob/c6e4716f082c0c8790d06d9e1664aacbd0c3fede/thrift/src/main/scala/com/twitter/finatra/thrift/ThriftServer.scala#L23>`__.

If a new mapper is added over an exception type already registered in the `ExceptionManager`, the
previous mapper will be overwritten.

Thus, the last registered mapper for an exception type wins.

Register an Exception Mapper
----------------------------

There are several ways to add a mapper.

Either directly through the `ThriftRouter`:

.. code:: scala

      override def configureThrift(router: ThriftRouter): Unit = {
        router
          .filter[ExceptionMappingFilter]
          .exceptionMapper[MyThrowableExceptionMapper]
          .exceptionMapper[OtherExceptionMapper]
      }

With the `JavaThriftRouter`:

.. code:: java

     @Override
     public void configureThrift(router: JavaThriftRouter) {
        router
          .filter(ExceptionMappingFilter.class)
          .exceptionMapper(MyThrowableExceptionMapper.class)
          .exceptionMapper(OtherExceptionMapper.class);
      }

Or in a module which is then added to the Server, e.g.,

.. code:: scala

      object MyExceptionMapperModule extends TwitterModule {
        override def singletonStartup(injector: Injector): Unit = {
          val manager = injector.instance[ExceptionManager]
          manager.add[MyThrowableExceptionMapper]
          manager.add[OtherExceptionMapper]
        }
      }

      ...

      override val modules = Seq(
        MyExceptionMapperModule)

in Java:

.. code:: java

      public class MyExceptionMapperModule extends TwitterModule {
        @Override
        public void singletonStartup(Injector injector) {
          ExceptionManager manager = injector.instance(ExceptionManager.class);
          manager.add(MyThrowableExceptionMapper.class);
          manager.add(OtherExceptionMapper.class);
        }
      }

      ...

      @Override
      public Collection<Module> javaModules() {
        return ImmutableList.<Module>of(new MyExceptionMapperModule());
      }
