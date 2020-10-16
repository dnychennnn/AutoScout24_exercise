package v1.car

import javax.inject.{Inject, Provider}
import java.util.Date

import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.data.format.Formats.dateFormat


/**
  * DTO for displaying car information.
  */
case class CarResource(id: String, title: String, fuel: String, price: String, isnew: String, mileage: Option[String], first_registration: Option[Date])

object CarResource {
  /**
    * Mapping to read/write a CarResource out as a JSON value.
    */
    implicit val format: Format[CarResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[CarResource]]
  */
class CarResourceHandler @Inject()(
    routerProvider: Provider[CarRouter],
    carRepository: CarRepository)(implicit ec: ExecutionContext) {

  def create(carInput: CarFormInput)(
      implicit mc: MarkerContext): Future[CarResource] = {
    val data = CarData(999, carInput.title, carInput.price, carInput.fuel, carInput.isnew, carInput.mileage, carInput.first_registration)
    // We don't actually create the car, so return what we have
    carRepository.create(data).map { id =>
      createCarResource(data)
    }
  }

  def lookup(id: String)(
      implicit mc: MarkerContext): Future[Option[CarResource]] = {
    val carFuture = carRepository.get(Integer.parseInt(id))
    carFuture.map { maybeCarData => 
      maybeCarData.map { carData =>
        createCarResource(carData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[CarResource]] = {
    carRepository.list().map { carDataList =>
      carDataList.map(carData => createCarResource(carData))
    }
  }

  private def createCarResource(c: CarData): CarResource = {
    CarResource(c.id.toString(), c.title, c.price.toString(), c.fuel.toString(), c.isnew.toString(), Some(c.mileage.toString()), c.first_registration)
  }

}
