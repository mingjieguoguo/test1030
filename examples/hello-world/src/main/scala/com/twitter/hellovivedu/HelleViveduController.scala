package com.twitter.hello

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class HelleViveduController extends Controller {

  get("/hi") { request: Request =>
    info("hi")
    "Hello " + request.params.getOrElse("name", "unnamed")
  }

  post("/hi") { hiRequest: HiRequest =>
    "Hello " + hiRequest.name + " with id " + hiRequest.id
  }

  put("/hello") {hiRequest: HiRequest =>
    "byebye" + hiRequest.name
  }

  delete("/delete") {userId:String =>
    println(userId)
  }

  delete("/deleteA") { user: User =>
    println(user.getUserId)
  }
}
