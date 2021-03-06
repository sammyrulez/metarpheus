package io.buildo.baseexample

import metarpheus.annotation.{publishRoute, alias}

import models._

import spray.routing._
import spray.routing.Directives._
import spray.httpx.SprayJsonSupport._

import scala.concurrent.ExecutionContext.Implicits.global

trait CampingRouterModule extends io.buildo.base.MonadicCtrlRouterModule
  with io.buildo.base.MonadicRouterHelperModule
  with io.buildo.base.ConfigModule
  with JsonSerializerModule

  with CampingControllerModule {

  import ExampleJsonProtocol._
  import RouterHelpers._

  /**
   * @param coolness how cool it is
   * @param size the number of tents
   */
  case class GetByQueryParams(
    coolness: Option[String],
    size: Int
  )

  /** whether there's a beach */
  @alias val `?hasBeach` = parameter('hasBeach.as[Boolean])

  @publishRoute(authenticated = false)
  val campingRoute = {
    pathPrefix("campings") {
      (get & pathEnd & parameters('coolness.as[String], 'size.as[Int].?, 'nickname) /**
        get campings matching the requested coolness and size
        @param coolness how cool it is
        @param size the number of tents
        @param nickname a friendly name for the camping
      */) (returns[List[Camping]].ctrl(campingController.getByCoolnessAndSize _)) ~
      (get & pathEnd & parameters('size.as[Int], 'distance.as[Int]).as(Foo) /**
        get campings matching the requested size and distance
        @param size the number of tents
        @param distance how distant it is
      */) (returns[List[Camping]].ctrl(campingController.getBySizeAndDistance _)) ~
      withUserAuthentication {
        (get & path(IntNumber) /**
          get a camping by id
          @pathParam id camping id
        */) (returns[Camping].ctrl(campingController.getById _)) ~
        (get & path(`Id`[Camping]) /**
          get a camping by typed id
        */) (returns[Camping].ctrl(campingController.getByTypedId _)) ~
      } ~
      (get & pathEnd & `?hasBeach` /**
        get campings based on whether they're close to a beach
      */) (returns[List[Camping]].ctrl(campingController.getByHasBeach _)) ~
      (post & pathEnd & entity(as[Camping]) /**
        create a camping
      */) (returns[Camping].ctrl(campingController.create _)) ~
      withRole(Admin) { user =>
        (get & path("by_query") & params[GetByQueryParams] /**
          get multiple campings by params with case class
        */) (returns[List[Camping]].ctrl(campingController.getByQuery _))
      }
    }
  }
}
