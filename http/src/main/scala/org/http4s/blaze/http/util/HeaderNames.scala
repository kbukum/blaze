package org.http4s.blaze.http.util

/**
  * Incomplete collection of header names, all lower case.
  */
private[blaze] object HeaderNames {
  val Connection = "connection"
  val ContentLength = "content-length"
  val ContentType = "content-type"
  val Date = "date"
  val TransferEncoding = "transfer-encoding"
}
