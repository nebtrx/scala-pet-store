package io.github.pauljamescleary.petstore.infrastructure.repository.inmemory

import java.util.Random

import cats._
import cats.implicits._
import io.github.pauljamescleary.petstore.domain.todos.{Todo, TodoRepositoryAlgebra}

import scala.collection.concurrent.TrieMap

class TodoRepositoryInMemoryInterpreter[F[_]: Applicative] extends TodoRepositoryAlgebra[F] {

  private val cache = new TrieMap[Long, Todo]

  private val random = new Random

  def create(todo: Todo): F[Todo] = {
    val id = random.nextLong
    val toSave = todo.copy(id = id.some)
    cache += (id -> todo.copy(id = id.some))
    toSave.pure[F]
  }

  def update(user: Todo): F[Option[Todo]] = user.id.traverse { id =>
    cache.update(id, user)
    user.pure[F]
  }

  def get(id: Long): F[Option[Todo]] = cache.get(id).pure[F]

  def delete(id: Long): F[Option[Todo]] = cache.remove(id).pure[F]

  def list(pageSize: Int, offset: Int): F[List[Todo]] =
    cache.values.toList.sortBy(_.completed).reverse.slice(offset, offset + pageSize).pure[F]
}

object TodoRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() = new TodoRepositoryInMemoryInterpreter[F]
}


