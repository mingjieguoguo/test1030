package com.twitter.finatra.http.marshalling

import com.google.common.net.MediaType

// TODO: replace guava MediaType
case class WriterResponse(
  contentType: MediaType,
  body: Any,
  headers: Map[String, String] = Map.empty
)
