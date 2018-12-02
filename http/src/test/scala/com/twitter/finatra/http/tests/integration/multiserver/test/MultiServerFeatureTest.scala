package com.twitter.finatra.http.tests.integration.multiserver.test

import com.twitter.finagle.http.Status
import com.twitter.finatra.http.tests.integration.multiserver.add1server.Add1Server
import com.twitter.finatra.http.tests.integration.multiserver.add2server.Add2Server
import com.twitter.finatra.http.{EmbeddedHttpServer, HttpTest}
import com.twitter.inject.Test

class MultiServerFeatureTest extends Test with HttpTest {

  val add1Server = new EmbeddedHttpServer(new Add1Server)

  val add2Server =
    new EmbeddedHttpServer(new Add2Server, flags = Map(resolverMap("add1-server", add1Server)))

  override def beforeAll() = {
    assert(!add1Server.isStarted)
    assert(!add2Server.isStarted)

    add1Server.assertHealthy()
    add2Server.assertHealthy()
  }

  override def afterAll() = {
    add1Server.close()
    add2Server.close()
  }

  test("add2#num = 5") {
    add2Server.httpGet("/add2?num=5", andExpect = Status.Ok, withBody = "7")
  }
}
