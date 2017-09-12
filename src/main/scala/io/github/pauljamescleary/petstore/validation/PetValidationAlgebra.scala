package io.github.pauljamescleary.petstore.validation

import io.github.pauljamescleary.petstore.model.Pet

import scala.language.higherKinds

case class PetAlreadyExistsError(pet: Pet) extends Throwable
case class PetNotFoundError(id: Long) extends Throwable

trait PetValidationAlgebra[F[_]] {

  /* Fails with a PetAlreadyExistsError */
  def doesNotExist(pet: Pet): F[Unit]

  /* Fails with a PetNotFoundError if the pet id does not exist or if it is none */
  def exists(petId: Option[Long]): F[Unit]
}
