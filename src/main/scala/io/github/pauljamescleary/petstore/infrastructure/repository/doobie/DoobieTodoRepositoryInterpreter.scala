package io.github.pauljamescleary.petstore.infrastructure.repository.doobie

import cats._
import cats.data.OptionT
import cats.implicits._
import doobie._
import doobie.implicits._
import io.github.pauljamescleary.petstore.domain.todos.{Todo, TodoRepositoryAlgebra}
import io.github.pauljamescleary.petstore.infrastructure.repository.doobie.SQLPagination._

private object TodoSQL {
  def select(todoId: Long): Query0[Todo] = sql"""
    SELECT DESCRIPTION, COMPLETED, ID
    FROM TODOS
    WHERE ID = $todoId
  """.query[Todo]

  def selectAll : Query0[Todo] = sql"""
    SELECT DESCRIPTION, COMPLETED, ID
    FROM TODOS
    ORDER BY COMPLETED DESC
  """.query

  def insert(todo : Todo) : Update0 = sql"""
    INSERT INTO TODOS (ID, DESCRIPTION, COMPLETED)
    VALUES (${todo.id}, ${todo.description}, ${todo.completed})
  """.update

  def update(todo: Todo, id: Long) : Update0 = sql"""
    UPDATE TODOS
    SET DESCRIPTION = ${todo.description}, COMPLETED = ${todo.completed}
    WHERE id = $id
  """.update

  def delete(orderId : Long) : Update0 = sql"""
    DELETE FROM TODOS
    WHERE ID = $orderId
  """.update
}

class DoobieTodoRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
  extends TodoRepositoryAlgebra[F] {

  import TodoSQL._
  def create(todo: Todo): F[Todo] =
    insert(todo).withUniqueGeneratedKeys[Long]("ID").map(id => todo.copy(id = id.some)).transact(xa)

  def get(id: Long): F[Option[Todo]] = TodoSQL.select(id).option.transact(xa)

  def delete(id: Long): F[Option[Todo]] =
    OptionT(get(id)).semiflatMap(order =>
      TodoSQL.delete(id).run.transact(xa).as(order)
    ).value

  def update(todo: Todo): F[Option[Todo]] = OptionT.fromOption[ConnectionIO](todo.id).semiflatMap(id =>
    TodoSQL.update(todo, id).run.as(todo)
  ).value.transact(xa)

  def list(pageSize: Int, offset: Int): F[List[Todo]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)
}

object DoobieTodoRepositoryInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F]): DoobieTodoRepositoryInterpreter[F] =
    new DoobieTodoRepositoryInterpreter(xa)
}
