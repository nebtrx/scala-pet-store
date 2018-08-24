package io.github.pauljamescleary.petstore.domain.todos
import cats.data.EitherT
import io.github.pauljamescleary.petstore.domain.TodoDescriptionTooLongError

trait TodoValidationAlgebra[F[_]] {
  def taskTooLong(todo: Todo): EitherT[F, TodoDescriptionTooLongError.type, Unit]
}
