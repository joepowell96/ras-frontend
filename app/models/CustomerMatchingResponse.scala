package models

import play.api.libs.json.Json

case class CustomerMatchingResponse (links: List[Link])

object CustomerMatchingResponse {
  implicit val format = Json.format[CustomerMatchingResponse]
}


case class Link(name: String, href: String)

object Link {
  implicit val format = Json.format[Link]
}