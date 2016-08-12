
import java.net.InetSocketAddress

import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.{Service, http}
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.{Http, Request, Response, Status}
import com.twitter.util.Future
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization



object Server {
  implicit val formats =
    Serialization.formats(FullTypeHints(List(classOf[Status])))

  val service = new Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      (request.method , Path(request.path)) match {
        case  http.Method.Get ->  Root => val response = Response ()
          response.setContentString (
            "<html>\n" +
            "<body>\n" +
            "<h1>Say hello to\n" +
            "<i>Finagle</i>\n" +
            "!</h1>\n" +
            "</body>\n" +
            "</html>")
          Future.value(response)

        // http://localhost:10000/book/add?bookId=1&name=book
        case http.Method.Get -> Root / "book" / "add" =>
          val bookId = request.getParam("bookId")
          val title = request.getParam("name")
          val book = Book(bookId.toInt, title)
          print(book)
          val response = Response ()
          response.setContentString("Book Added!")
          Future.value(response)

        case http.Method.Get -> Root / "book" / id  => val response = Response ()
          val book: Book = Book(id.toInt, "Default title")
          val json = pretty(render(Extraction.decompose(book)))
          response.setContentTypeJson()
          response.setContentString(json)
          Future.value(response)

        case http.Method.Post -> Root / "Json"  => val response = Response ()
          val json = request.getContentString()
          val decodedBook = parse(json).extract[Book]
          println("Book id: " + decodedBook.bookId)
          println("Book title: " + decodedBook.title)
          response.setStatusCode(200)
          response.setContentString("Ok")
          Future.value(response)
      }
    }
  }

  def start() = ServerBuilder()
    .codec(Http())
    .bindTo(new InetSocketAddress(10000))
    .name("httpserver")
    .build(service)

  def main(args: Array[String]){
    println("Start HTTP server on port 10000")
    val server = start()
  }
}
