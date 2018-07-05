package io.github.pauljamescleary.petstore.domain.todos

import cats._
import cats.data._
import io.github.pauljamescleary.petstore.domain.TodoNotFoundError

import scala.language.higherKinds

/**
  * The entry point to our domain, works with repositories and validations to implement behavior
  * @param repository where we get our data
  * @tparam F - this is the container for the things we work with, could be scala.concurrent.Future, Option, anything
  *           as long as it is a Monad
  */
class TodoService[F[_]](repository: TodoRepositoryAlgebra[F]) {
  import cats.syntax.all._

  def create(todo: Todo): F[Todo] = repository.create(todo)

  /* Could argue that we could make this idempotent on put and not check if the pet exists */
  def update(todo: Todo)(implicit M: Monad[F]): EitherT[F, TodoNotFoundError.type, Todo] = for {
    saved <- EitherT.fromOptionF(repository.update(todo), TodoNotFoundError)
  } yield saved

  def get(id: Long)(implicit M: Monad[F]): EitherT[F, TodoNotFoundError.type, Todo] =
    EitherT.fromOptionF(repository.get(id), TodoNotFoundError)

  /* In some circumstances we may care if we actually delete the to-do; here we are idempotent and do not care */
  def delete(id: Long)(implicit M: Monad[F]): F[Unit] = repository.delete(id).as(())

  def list(pageSize: Int, offset: Int): F[List[Todo]] =
    repository.list(pageSize, offset)
}

object TodoService {
  def apply[F[_]: Monad](repository: TodoRepositoryAlgebra[F]) =
    new TodoService[F](repository)
}


