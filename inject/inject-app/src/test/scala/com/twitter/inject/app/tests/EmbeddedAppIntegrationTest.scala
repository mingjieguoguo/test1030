package com.twitter.inject.app.tests

import com.twitter.inject.{Logging, Mockito, Test, TwitterModule}
import com.twitter.inject.app.{App, EmbeddedApp}
import javax.inject.Inject

class EmbeddedAppIntegrationTest extends Test with Mockito {

  test("start app") {
    val sampleApp = new SampleApp
    val app = new EmbeddedApp(sampleApp)

    app.main()

    sampleApp.sampleServiceResponse should be("hi yo")
  }

  test("exception in App#run() throws") {
    val app = new EmbeddedApp(new SampleApp {
      override protected def run(): Unit = {
        throw new RuntimeException("FORCED EXCEPTION")
      }
    })

    intercept[Exception] {
      app.main()
    }
  }

  test("start app with FooModule") {
    val app = new EmbeddedApp(new SampleApp {
      addFrameworkModule(FooModule)

      override protected def run(): Unit = {
        super.run()
        assert(injector.instance[Foo].name == "bar")
      }
    })

    app.main()
  }

  test("call injector before main") {
    val e = intercept[Exception] {
      new SampleApp {
        addFrameworkModules(FooModule)
        injector.instance[Foo]
      }
    }
    e.getMessage should startWith("injector is not available")
  }

  test("error in run fails startup") {
    val app = new SampleApp {
      override protected def run(): Unit = {
        super.run()
        throw new scala.Exception("oops")
      }
    }

    intercept[Exception] {
      app.main()
    }
  }

  test("two apps starting") {
    val a = new EmbeddedApp(new com.twitter.inject.app.App {})
    a.main()

    val b = new EmbeddedApp(new com.twitter.inject.app.App {})
    b.main()
  }

  test("bind") {
    val mockSampleService = mock[SampleService]
    mockSampleService.sayHi(any[String]) returns "hi mock"

    val sampleApp = new SampleApp

    val app = new EmbeddedApp(sampleApp).bind[SampleService].toInstance(mockSampleService)
    app.main()

    sampleApp.sampleServiceResponse should be("hi mock")
  }
}

object SampleAppMain extends SampleApp

class SampleApp extends App {
  var sampleServiceResponse: String = ""

  override val name = "sample-app"

  override val modules = Seq()

  override protected def run(): Unit = {
    sampleServiceResponse = injector.instance[SampleManager].start()
  }
}

class SampleManager @Inject()(sampleService: SampleService) extends Logging {
  def start(): String = {
    info("SampleManager started")
    val response = sampleService.sayHi("yo")
    info("Service said " + response)
    response
  }
}

class SampleService {
  def sayHi(msg: String): String = {
    "hi " + msg
  }
}

object FooModule extends TwitterModule {
  override def configure(): Unit = {
    bind[Foo].toInstance(new Foo("bar"))
  }
}

case class Foo(name: String)

