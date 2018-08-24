package io.github.pauljamescleary.petstore

import cats.effect._
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import io.github.pauljamescleary.petstore.config.{DatabaseConfig, PetStoreConfig}
import io.github.pauljamescleary.petstore.domain.todos.{TodoService, TodoValidationInterpreter}
import io.github.pauljamescleary.petstore.infrastructure.endpoint.TodoEndpoints
import io.github.pauljamescleary.petstore.infrastructure.repository.doobie.DoobieTodoRepositoryInterpreter
import org.http4s.server.blaze.BlazeBuilder

object Server extends StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def stream(args: List[String], shutdown: IO[Unit]): Stream[IO, ExitCode] =
    createStream[IO](args, shutdown)


  def createStream[F[_]](args: List[String], shutdown: F[Unit])(
      implicit E: Effect[F]): Stream[F, ExitCode] =
    for {
      conf           <- Stream.eval(PetStoreConfig.load[F])
      xa             <- Stream.eval(DatabaseConfig.dbTransactor(conf.db))
      _              <- Stream.eval(DatabaseConfig.initializeDb(conf.db, xa))
      todoRepo       =  DoobieTodoRepositoryInterpreter[F](xa)
      todoValidation =  TodoValidationInterpreter[F]()
      todoService    =  TodoService[F](todoRepo, todoValidation)
      exitCode       <- BlazeBuilder[F]
        .bindHttp(8080, "localhost")
        .mountService(TodoEndpoints.endpoints[F](todoService), "/")
        .serve
    } yield exitCode
}
