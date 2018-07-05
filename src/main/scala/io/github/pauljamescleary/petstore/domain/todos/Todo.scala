package io.github.pauljamescleary.petstore.domain.todos

case class Todo(description: String,
                completed: Boolean,
                id: Option[Long] = None)
