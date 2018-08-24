package io.github.pauljamescleary.petstore.infrastructure.endpoint

import cats.effect.Effect
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import io.github.pauljamescleary.petstore.domain.{TodoDescriptionTooLongError, TodoNotFoundError}
import io.github.pauljamescleary.petstore.domain.todos.{Todo, TodoService}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpService}

import scala.language.higherKinds

class TodoEndpoints[F[_] : Effect] extends Http4sDsl[F] {

  import Pagination._

  /* Jsonization of our User type */
  implicit val userDecoder: EntityDecoder[F, Todo] = jsonOf[F, Todo]

  private def createEndpoint(todoService: TodoService[F]): HttpService[F] =
    HttpService[F] {
      case req@POST -> Root / "todos" =>
        val computation = for {
          user <- req.as[Todo]
          result <- todoService.create(user).value
        } yield result

        computation.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(TodoDescriptionTooLongError) => BadRequest("The todo was too long")
          case Left(TodoNotFoundError) => NotFound("The todo was not found")
          case Left(_) => BadGateway("Unknown Error")
        }
    }

  private def updateEndpoint(todoService: TodoService[F]): HttpService[F] =
    HttpService[F] {
      case req@PUT -> Root / "todos" / LongVar(id) =>
        val computation = for {
          pet <- req.as[Todo]
          updated = pet.copy(id = Some(id))
          result <- todoService.update(pet).value
        } yield result

        computation.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(TodoDescriptionTooLongError) => BadRequest("The todo was too long")
          case Left(TodoNotFoundError) => NotFound("The todo was not found")
          case Left(_) => BadGateway("Unknown Error")
        }
    }


  private def listEndpoint(todoService: TodoService[F]): HttpService[F] =
    HttpService[F] {
      case GET -> Root / "todos" :? PageSizeMatcher(pageSize) :? OffsetMatcher(offset) =>
        for {
          retrived <- todoService.list(pageSize, offset)
          resp <- Ok(retrived.asJson)
        } yield resp
    }


  private def deleteEndpoint(todoService: TodoService[F]): HttpService[F] =
    HttpService[F] {
      case DELETE -> Root / "todos" / LongVar(id)  =>
        for {
          _ <- todoService.delete(id)
          resp <- Ok()
        } yield resp
    }


  def endpoints(todoService: TodoService[F]): HttpService[F] =
    createEndpoint(todoService) <+>
      updateEndpoint(todoService) <+>
      listEndpoint(todoService) <+>
      deleteEndpoint(todoService)
}

object TodoEndpoints {
  def endpoints[F[_]: Effect](todoService: TodoService[F]): HttpService[F] =
    new TodoEndpoints[F].endpoints(todoService)
}


