package io.github.pauljamescleary.petstore.infrastructure.repository.doobie

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import io.github.pauljamescleary.petstore.PetStoreArbitraries.todo
import org.scalatest._

class TodoQueryTypeCheckSpec extends FunSuite with Matchers with IOChecker {
  override val transactor : Transactor[IO] = testTransactor

  import TodoSQL._

  test("Typecheck todo queries") {
    todo.arbitrary.sample.foreach{ p =>
      check(insert(p))
      p.id.foreach(id => check(TodoSQL.update(p, id)))
    }

    check(select(1L))
    check(selectAll)
    check(delete(1L))
  }
}
