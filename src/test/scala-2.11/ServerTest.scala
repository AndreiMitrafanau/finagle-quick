import com.twitter.finagle.http._
import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.{Await, Closable}
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.{Extraction, FullTypeHints}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterEach, FunSuite}

@RunWith(classOf[JUnitRunner])
class ServerTest extends FunSuite with BeforeAndAfterEach {
  implicit val formats =
    Serialization.formats(FullTypeHints(List(classOf[Status])))
  var server: com.twitter.finagle.builder.Server = _
  var client: Service[Request, Response] = _

  override def beforeEach() {
    server = Server.start()
    client = Http.newService("localhost:10000")
  }

  override def afterEach() {
    Closable.all(server, client).close()
  }

  test("GET return right string") {
    val request = http.Request(http.Method.Get, "/")
    val responseFuture = client(request)
    val response = Await.result(responseFuture)

    assert(response.getContentString() contains "Finagle")
  }

  test("Get could add the book") {
    val request = http.Request(http.Method.Get, "/book/add?bookId=1&name=book")
    val responseFuture = client(request)
    val response = Await.result(responseFuture)

    assert(response.getContentString() === "Book Added!")
  }

  test("Get could return book json") {
    val request = http.Request(http.Method.Get, "/book/42")
    val responseFuture = client(request)
    val response = Await.result(responseFuture)
    val json = makeJson(Book(42, "Default title"))

    assert(response.getContentString() === json)
  }

  test("Post json return OK") {
    val request = http.Request(http.Method.Post, "/Json")
    val json = makeJson(Book(1, "Book from JSON"))
    request.setContentString(json)
    request.setContentTypeJson()
    val responseFuture = client(request)
    val response = Await.result(responseFuture)

    assert(response.getStatusCode() === 200)
  }

  def makeJson(book: Book) = pretty(render(Extraction.decompose(book)))
}