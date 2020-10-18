package v1.car

import javax.inject.{Inject, Provider}
import java.util.Date
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.UUID


import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.data.format.Formats.dateFormat


/**
  * DTO for displaying car information.
  */
case class CarResource(id: String, title: String, fuel: String, price: String, isnew: String, mileage: String, first_registration: String)

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
    val data = CarData(UUID.randomUUID, carInput.title, carInput.price, carInput.fuel, carInput.isnew, carInput.mileage, carInput.first_registration)
    carRepository.create(data).map { id =>
      createCarResource(data)
    }
  }

  def lookup(id: String)(
      implicit mc: MarkerContext): Future[Option[CarResource]] = {
    val carFuture = carRepository.get(UUID.fromString(id))
    carFuture.map { maybeCarData => 
      maybeCarData.map { carData =>
        createCarResource(carData)
      }
    }
  }

  def find(attr: String)(implicit mc: MarkerContext): Future[Iterable[CarResource]] = {
    carRepository.list(attr).map { carDataList =>
      carDataList.map(carData => createCarResource(carData))
    }
  }

  def delete(id:String)(
      implicit mc: MarkerContext): Future[Option[CarResource]] = {
    val carFuture = carRepository.delete(UUID.fromString(id))
    carFuture.map{ id =>
      None
    }
  }

  def update(id: String, carUpdate: CarFromUpdate)(
      implicit mc: MarkerContext): Future[Option[CarResource]] = {
    val data = CarUpdate(UUID.fromString(id), carUpdate.title, carUpdate.price, carUpdate.fuel, carUpdate.isnew, carUpdate.mileage, carUpdate.first_registration)
    val carFuture = carRepository.update(UUID.fromString(id), data)
    carFuture.map { maybeCarData => 
      maybeCarData.map { carData =>
        createCarResource(carData)
    }
  }
}

  private def createCarResource(c: CarData): CarResource = {

    // handle date format pattern
    val pattern = "yyyy-MM-dd"
    val simpleDateFormat = new SimpleDateFormat(pattern)
    var dateString: String = null
    val first_registration = c.first_registration
    first_registration match {
      case Some(first_registration) => dateString = simpleDateFormat.format(first_registration)
      case None => dateString = "None"
    }

    CarResource(c.id.toString(), c.title, c.fuel.toString(), c.price.toString(), c.isnew.toString(), c.mileage.getOrElse(None).toString(), dateString)
  }

}
