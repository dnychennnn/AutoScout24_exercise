package v1.car

import javax.inject.Inject
import java.util.Date

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._


import scala.concurrent.{ExecutionContext, Future}

case class CarFormInput(title: String, fuel: Int, price: Int, isnew: Boolean, mileage: Option[Int] = None, first_registration: Option[Date] = None)

/**
  * Takes HTTP requests and produces JSON.
  */
class CarController @Inject()(cc: CarControllerComponents)(
    implicit ec: ExecutionContext)
    extends CarBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[CarFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> text,
        "fuel" -> number,
        "price" -> number,          
        "isnew" -> boolean,
        "mileage" -> optional(number),
        "first_registration" -> optional(date).verifying("Invalid Date", date => true),
      )(CarFormInput.apply _)(CarFormInput.unapply _)
      .verifying(
        "Mileage is required for used car.",
        form => if(!form.isnew) form.mileage.isDefined else true
      ).verifying(
        "First registration is required for used car.",
        form => if(!form.isnew) form.first_registration.isDefined else true
      )
    )
  }

  def show(id: String): Action[AnyContent] = CarAction.async {
  implicit request =>
    logger.trace(s"show: id = $id")
    carResourceHandler.lookup(id).map { car =>
      Ok(Json.toJson(car))
    }
  }

  def index: Action[AnyContent] = CarAction.async { implicit request =>
    logger.trace("index: ")
    carResourceHandler.find.map { cars =>
      Ok(Json.toJson(cars))
    }
  }
  
  def process: Action[AnyContent] = CarAction.async { implicit request =>
    logger.trace("process: ")
    processJsonCar()
  }

  def delete(id:String): Action[AnyContent] = CarAction.async { implicit request =>
    logger.trace("delete: id = $id")
    carResourceHandler.delete(id).map { car =>
      NoContent
    }
  }

  private def processJsonCar[A]()(
      implicit request: CarRequest[A]): Future[Result] = {
    def failure(badForm: Form[CarFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: CarFormInput) = {
      carResourceHandler.create(input).map { car =>
        Created(Json.toJson(car))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
