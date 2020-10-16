package v1.car

import javax.inject.{Inject, Singleton}
import java.util.Date
import java.text.SimpleDateFormat

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future


final case class CarData(id: Int, title: String, fuel: Int, price: Int, isnew: Boolean, mileage: Option[Int], first_registration: Option[Date])

class CarExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the CarRepository.
  */
trait CarRepository {
  def create(data: CarData)(implicit mc: MarkerContext): Future[Int]

  def list()(implicit mc: MarkerContext): Future[Iterable[CarData]]

  def get(id: Int)(implicit mc: MarkerContext): Future[Option[CarData]]
}

/**
  * A trivial implementation for the Car Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class CarRepositoryImpl @Inject()()(implicit ec: CarExecutionContext)
    extends CarRepository {

  private val logger = Logger(this.getClass)

  // define date format
  val format = new SimpleDateFormat("yyyy-mm-dd") 
 
  private val carList = List(
    CarData(1, "Audi A4 Avant", 1, 34848, true, None, None),
    CarData(2, "Nissan GT-R", 0, 104293, true, None, None),
    CarData(3, "Mercedez-Benz G 65 AMG", 0, 155880, false, Some(8703), Some(format.parse("2012-10-1"))),
    CarData(4, "Tesla Model S", 1, 64990, false, Some(71820), Some(format.parse("2012-10-1"))),
    CarData(5, "Audi e-tron", 1, 53926, true, None, None)
  )

  override def list()(
      implicit mc: MarkerContext): Future[Iterable[CarData]] = {
    Future {
      logger.trace(s"list: ")
      carList
    }
  }

  override def get(id: Int)(
      implicit mc: MarkerContext): Future[Option[CarData]] = {
    Future {
      logger.trace(s"get: id = $id")
      carList.find(car => car.id == id)
    }
  }

  def create(data: CarData)(implicit mc: MarkerContext): Future[Int] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
