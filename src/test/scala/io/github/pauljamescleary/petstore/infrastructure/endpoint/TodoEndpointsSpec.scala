package io.github.pauljamescleary.petstore.infrastructure.endpoint

import io.github.pauljamescleary.petstore._
import io.github.pauljamescleary.petstore.infrastructure.repository.inmemory._
import io.github.pauljamescleary.petstore.domain.todos._
import org.scalatest._
import org.scalatest.prop.PropertyChecks
import cats.effect._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.dsl._
import org.http4s.circe._


class TodoEndpointsSpec
  extends FunSuite
  with Matchers
  with PropertyChecks
  with PetStoreArbitraries
  with Http4sDsl[IO] {

  test("create todo") {

    val todosRepos = TodoRepositoryInMemoryInterpreter[IO]()
    val todoValidation = TodoValidationInterpreter[IO]()
    val todoService = TodoService[IO](todosRepos, todoValidation)
    val todoHttpService = TodoEndpoints.endpoints(todoService)

    val todo = Todo("Do some groceries", false)

    for {
        request <- Request[IO](Method.POST, Uri.uri("/todos"))
          .withBody(todo.asJson)
        response <- todoHttpService
          .run(request)
          .getOrElse(fail(s"Request was not handled: $request"))
      } yield {
        response.status shouldEqual Ok
      }
  }

  test("update todo") {
    val todosRepo = TodoRepositoryInMemoryInterpreter[IO]()
    val todoValidation = TodoValidationInterpreter[IO]()
    val userService = TodoService[IO](todosRepo, todoValidation)
    val todoHttpService: HttpService[IO] = TodoEndpoints.endpoints(userService)

    implicit val userDecoder: EntityDecoder[IO, Todo] = jsonOf[IO, Todo]

    val todo = Todo("Do some groceries and pay the bills", false, Some(2))

    for {
        createRequest <- Request[IO](Method.POST, Uri.uri("/todos"))
          .withBody(todo.asJson)
        createResponse <- todoHttpService
          .run(createRequest)
          .getOrElse(fail(s"Request was not handled: $createRequest"))
        createdTodo <- createResponse.as[Todo]
        todoToUpdate = createdTodo.copy(description = createdTodo.description, completed = createdTodo.completed)
        updateRequest <- Request[IO](Method.PUT, Uri.unsafeFromString(s"/todos/${ todoToUpdate.id.get}"))
          .withBody(todoToUpdate.asJson)
        updateResponse <- todoHttpService
          .run(updateRequest)
          .getOrElse(fail(s"Request was not handled: $updateRequest"))
        updatedTodo <- updateResponse.as[Todo]
      } yield {
        updateResponse.status shouldEqual Ok
        updatedTodo.description shouldEqual todo.description
        updatedTodo.completed shouldEqual todo.completed
        createdTodo.id shouldEqual updatedTodo.id
      }
    }

  test("get todo by id") {
    val todosRepo = TodoRepositoryInMemoryInterpreter[IO]()
    val todoValidation = TodoValidationInterpreter[IO]()
    val userService = TodoService[IO](todosRepo, todoValidation)
    val todoHttpService: HttpService[IO] = TodoEndpoints.endpoints(userService)

    implicit val userDecoder: EntityDecoder[IO, Todo] = jsonOf[IO, Todo]

    val todo = Todo("Do some groceries and pay the bills", false, Some(2))

    for {
      createRequest <- Request[IO](Method.POST, Uri.uri("/todos"))
        .withBody(todo.asJson)
      createResponse <- todoHttpService
        .run(createRequest)
        .getOrElse(fail(s"Request was not handled: $createRequest"))
      createdTodo <- createResponse.as[Todo]
      getResponse <- todoHttpService
        .run(Request[IO](Method.GET, Uri.unsafeFromString(s"/todos/${ createdTodo.id.get}")))
        .getOrElse(fail(s"Request was not handled"))
      getTodo <- getResponse.as[Todo]
    } yield {
      getResponse.status shouldEqual Ok
      createdTodo.description shouldEqual getTodo.description
    }
  }

  test("delete todo by userName") {
    val todosRepo = TodoRepositoryInMemoryInterpreter[IO]()
    val todoValidation = TodoValidationInterpreter[IO]()
    val userService = TodoService[IO](todosRepo, todoValidation)
    val todoHttpService: HttpService[IO] = TodoEndpoints.endpoints(userService)


    implicit val userDecoder: EntityDecoder[IO, Todo] = jsonOf[IO, Todo]

    val todo = Todo("Do some groceries and pay the bills", false, Some(2))

    for {
      createRequest <- Request[IO](Method.POST, Uri.uri("/todos"))
        .withBody(todo.asJson)
      createResponse <- todoHttpService
        .run(createRequest)
        .getOrElse(fail(s"Request was not handled: $createRequest"))
      createdTodo <- createResponse.as[Todo]
      deleteResponse <- todoHttpService
        .run(Request[IO](Method.DELETE, Uri.unsafeFromString(s"/users/${createdTodo.id.get}")))
        .getOrElse(fail(s"Delete request was not handled"))
      getResponse <- todoHttpService
        .run(Request[IO](Method.GET, Uri.unsafeFromString(s"/users/${createdTodo.id.get}")))
        .getOrElse(fail(s"Get request was not handled"))
    } yield {
      createResponse.status shouldEqual Ok
      deleteResponse.status shouldEqual Ok
      getResponse.status shouldEqual NotFound
    }
  }
}
