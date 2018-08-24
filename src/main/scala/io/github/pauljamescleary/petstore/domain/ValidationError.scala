package io.github.pauljamescleary.petstore.domain

sealed trait ValidationError extends Product with Serializable
trait TodoError extends ValidationError
case object TodoNotFoundError extends TodoError
case object TodoDescriptionTooLongError extends TodoError
