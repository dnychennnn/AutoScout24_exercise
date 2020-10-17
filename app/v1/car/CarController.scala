package v1.car

import javax.inject.Inject
import java.util.Date

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._


import scala.concurrent.{ExecutionContext, Future}

case class CarFormInput(title: String, fuel: Int, price: Int, isnew: Boolean, mileage: Option[Int] = None, first_registration: Option[Date] = None)
case class CarFromUpdate(title: Option[String]=None, fuel: Option[Int]=None, price: Option[Int]=None, isnew: Option[Boolean]=None, mileage: Option[Int]=None, first_registration: Option[Date] = None)
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

  private val updateform: Form[CarFromUpdate] = {
    import play.api.data.Forms._

      Form(
      mapping(
        "title" -> optional(text),
        "fuel" -> optional(number), 
        "price" -> optional(number),          
        "isnew" -> optional(boolean),
        "mileage" -> optional(number),
        "first_registration" -> optional(date).verifying("Invalid Date", date => true),
      )(CarFromUpdate.apply _)(CarFromUpdate.unapply _)
      .verifying(
        "Mileage is required for used car.",
        updateform => if(updateform.isnew != None){
          if(!updateform.isnew.get) updateform.mileage.isDefined else true
        } else {
          true
        }
      ).verifying(
        "First registration is required for used car.",
        updateform => if(updateform.isnew != None){
          if(!updateform.isnew.get) updateform.first_registration.isDefined else true
        } else {
          true
        }
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

  def index(sort_by: Option[String]): Action[AnyContent] = CarAction.async { implicit request =>
    logger.trace(s"index by : $sort_by")
    sort_by match {
      case Some(sort_by) =>  carResourceHandler.find(sort_by).map { cars =>
        Ok(Json.toJson(cars))
      }
      case None => carResourceHandler.find("id").map { cars =>
        Ok(Json.toJson(cars))
      }
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

  def update(id: String): Action[AnyContent] = CarAction.async { implicit request =>
    logger.trace("update: id = $id")
      updateJsonCar(id)
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

  private def updateJsonCar[A](id : String)(
    implicit request: CarRequest[A]): Future[Result] = {
  def failure(badForm: Form[CarFromUpdate]) = {
    Future.successful(BadRequest(badForm.errorsAsJson))
  }

  def success(input: CarFromUpdate) = {
    carResourceHandler.update(id, input).map { car =>
      Ok(Json.toJson(car))
    }
  }

  updateform.bindFromRequest().fold(failure, success)
}
}
