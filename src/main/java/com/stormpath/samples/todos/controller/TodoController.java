/*
 * Copyright (C) 2012 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.samples.todos.controller;

import com.stormpath.samples.todos.entity.Todo;
import com.stormpath.samples.todos.service.DefaultTodoService;
import com.stormpath.samples.todos.service.TodoService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Path(Link.TODOS)
public class TodoController extends BaseController {

    private TodoService todoService = DefaultTodoService.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public CollectionResource list(@Context UriInfo info,
                                   @DefaultValue("false") @QueryParam("expand") boolean expand) {
        Collection<Todo> todos = todoService.getTodos();
        if (todos == null || todos.size() == 0) {
            return new CollectionResource(info, Link.TODOS, Collections.emptyList());
        }
        Collection items = new ArrayList(todos.size());
        for( Todo todo : todos) {
            if (expand) {
                items.add(new TodoResource(info, todo));
            } else {
                items.add(new Link(info, todo));
            }
        }
        return new CollectionResource(info, Link.TODOS, items);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Context UriInfo info, Todo todo) {
        todo = todoService.save(todo);
        TodoResource resource = new TodoResource(info, todo);
        return created(resource);
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TodoResource getTodo(@Context UriInfo info, @PathParam("id") String id) {
        Todo todo = todoService.getById(id);
        if (todo == null) {
            throw new UnknownResourceException();
        }
        return new TodoResource(info, todo);
    }

    @Path("/{id}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTodo(@Context UriInfo info, @PathParam("id") String id, Map map) {
        Todo existing = todoService.getById(id);
        if (existing == null) {
            throw new UnknownResourceException();
        }
        //typically done via a framework call to automate this for any type of entity:
        if (map.containsKey("name")) {
            existing.setName(String.valueOf(map.get("name")));
        }
        if (map.containsKey("done")) {
            existing.setDone(Boolean.valueOf(String.valueOf(map.get("done"))));
        }
        existing = todoService.save(existing);
        return Response.ok(new TodoResource(info, existing), MediaType.APPLICATION_JSON).build();
    }

    @Path("/{id}")
    @DELETE
    public void deleteTodo(@PathParam("id") String id) {
        Todo todo = todoService.deleteById(id);
        if (todo == null) {
            throw new UnknownResourceException();
        }
    }


}
