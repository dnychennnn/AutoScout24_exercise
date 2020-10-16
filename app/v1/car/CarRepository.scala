package v1.car

import javax.inject.{Inject, Singleton}
import java.util.Date
import java.text.SimpleDateFormat
import java.util.UUID

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future
import scala.collection.mutable


final case class CarData(id: UUID, title: String, fuel: Int, price: Int, isnew: Boolean, mileage: Option[Int], first_registration: Option[Date])

class CarExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the CarRepository.
  */
trait CarRepository {
  def create(data: CarData)(implicit mc: MarkerContext): Future[UUID]

  def list()(implicit mc: MarkerContext): Future[Iterable[CarData]]

  def get(id: UUID)(implicit mc: MarkerContext): Future[Option[CarData]]

  def delete(id: UUID)(implicit mc: MarkerContext): Future[Iterable[CarData]]

  // def update(id:UUID)(implicit mc: MarkerContext): Future[Option[CarData]]
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
 
  private val carList = mutable.ListBuffer(
    CarData(UUID.randomUUID(), "Audi A4 Avant", 1, 34848, true, None, None),
    CarData(UUID.randomUUID(), "Nissan GT-R", 0, 104293, true, None, None),
    CarData(UUID.randomUUID(), "Mercedez-Benz G 65 AMG", 0, 155880, false, Some(8703), Some(format.parse("2012-10-1"))),
    CarData(UUID.randomUUID(), "Tesla Model S", 1, 64990, false, Some(71820), Some(format.parse("2012-10-1"))),
    CarData(UUID.randomUUID(), "Audi e-tron", 1, 53926, true, None, None)
  )

  override def list()(
      implicit mc: MarkerContext): Future[Iterable[CarData]] = {
    Future {
      logger.trace(s"list: ")
      carList
    }
  }

  override def get(id: UUID)(
      implicit mc: MarkerContext): Future[Option[CarData]] = {
    Future {
      logger.trace(s"get: id = $id")
      carList.find(car => car.id == id)
    }
  }

  override def create(data: CarData)(implicit mc: MarkerContext): Future[UUID] = {
    Future {
      logger.trace(s"create: data = $data")
      carList+=data
      data.id 
    }
  }

  override def delete(id: UUID)(implicit mc: MarkerContext): Future[Iterable[CarData]] = {
    Future {
      logger.trace(s"delete: id = $id")
      val car = carList.find(car => car.id == id) 
      carList-=car.getOrElse(throw new RuntimeException("No such car to be deleted!"))
    }
  }

}
