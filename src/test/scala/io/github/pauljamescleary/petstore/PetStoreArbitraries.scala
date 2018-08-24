package io.github.pauljamescleary.petstore

import io.github.pauljamescleary.petstore.domain.todos.Todo
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck._


trait PetStoreArbitraries {

  implicit val todo = Arbitrary[Todo] {
    for {
      description <- arbitrary[String]
      completed <- arbitrary[Boolean]
      id <- Gen.option(Gen.posNum[Long])
    } yield Todo(description, completed, id)
  }
}

object PetStoreArbitraries extends PetStoreArbitraries
