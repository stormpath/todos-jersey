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

import javax.ws.rs.core.UriInfo;

@SuppressWarnings("unchecked")
public class TodoResource extends Link {

    public TodoResource(UriInfo info, Todo todo) {
        super(info, todo);
        put("name", todo.getName());
        put("done", todo.isDone());
        put("created", todo.getCreated());
        put("user", new Link(getFullyQualifiedContextPath(info), todo.getUser()));
    }
}
