package io.github.pauljamescleary.petstore.domain.todos

import cats._
import cats.data.EitherT
import io.github.pauljamescleary.petstore.domain.TodoDescriptionTooLongError

class TodoValidationInterpreter[F[_]: Monad]() extends TodoValidationAlgebra[F] {
  def taskTooLong(todo: Todo): EitherT[F, TodoDescriptionTooLongError.type, Unit] = ???
//    EitherT {
//    userRepo.findByUserName(user.userName).map {
//      case None => Right(())
//      case Some(_) => Left(UserAlreadyExistsError(user))
//    }
}

object TodoValidationInterpreter {
  def apply[F[_]: Monad](): TodoValidationAlgebra[F] =
    new TodoValidationInterpreter[F]()
}


