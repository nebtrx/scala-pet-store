package io.github.pauljamescleary.petstore.domain.todos

import scala.language.higherKinds

trait TodoRepositoryAlgebra[F[_]] {

  def create(todo: Todo): F[Todo]

  def get(orderId: Long): F[Option[Todo]]

  def update(todo: Todo) : F[Option[Todo]]

  def list(pageSize: Int, offset: Int): F[List[Todo]]

  def delete(orderId: Long): F[Option[Todo]]
}
