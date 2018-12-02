.. _mocks:

Working with Mocks
==================

|c.t.inject.Mockito|_ provides `Specs2 <https://etorreborre.github.io/specs2/>`__ Mockito syntatic
sugar for `ScalaTest <http://www.scalatest.org/>`__.

This is a drop-in replacement for |org.specs2.mock.Mockito|_ and we encourage you to **not** use
|org.specs2.mock.Mockito|_ directly. Otherwise, Mockito match failures will **not propagate as ScalaTest
test failures**.

See the `Override Modules <override_modules.html>`__ or `Explicit Binding with #bind[T] <bind_dsl.html>`__
sections on using mocks in combination with other Finatra testing features.

More Information
----------------

- :doc:`index`
- :doc:`embedded`
- :doc:`feature_tests`
- :doc:`integration_tests`
- :doc:`startup_tests`
- :doc:`mixins`
- :doc:`override_modules`
- :doc:`bind_dsl`

.. |c.t.inject.Mockito| replace:: `c.t.inject.Mockito`
.. _c.t.inject.Mockito: https://github.com/twitter/finatra/blob/develop/inject/inject-core/src/test/scala/com/twitter/inject/Mockito.scala

.. |org.specs2.mock.Mockito| replace:: `org.specs2.mock.Mockito`
.. _org.specs2.mock.Mockito: http://etorreborre.github.io/specs2/guide/SPECS2-3.9.1/org.specs2.guide.UseMockito.html
