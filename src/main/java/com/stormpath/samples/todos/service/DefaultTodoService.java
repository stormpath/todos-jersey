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
package com.stormpath.samples.todos.service;

import com.stormpath.samples.todos.entity.Todo;
import com.stormpath.samples.todos.entity.User;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultTodoService implements TodoService {

    //ordinarily this would be a DAO used to interact with a data store (e.g. database or NoSQL store).
    Map<String,Todo> todos = new ConcurrentHashMap<String, Todo>();

    //ordinarily DependencyInjection would be used.  This simple example just uses a static singleton:
    private static final DefaultTodoService INSTANCE = new DefaultTodoService();

    //simulate a single User for the demo:
    private final User user;

    public DefaultTodoService() {
        //simulate a single User for this demo:
        User user = new User();
        user.setId(UUID.randomUUID().toString().replace("-", ""));
        user.setUsername("jsmith");
        user.setGivenName("John");
        user.setSurname("Smith");
        this.user = user;
    }

    @Override
    public Collection<Todo> getTodos() {
        return todos.values();
    }

    @Override
    public Todo getById(String id) {
        return todos.get(id);
    }

    @Override
    public Todo save(Todo todo) {
        String id = todo.getId();
        if (id == null) {
            //create:
            id = UUID.randomUUID().toString().replace("-","");
            todo.setId(id);

            //set creation date:
            todo.setCreated(new Date());
            todos.put(id, todo);

            todo.setUser(getCurrentUser());
        } else {
            //update:
            todos.put(id, todo);
        }

        return todo;
    }

    protected User getCurrentUser() {
        //return the 'current user' based on a security framework like Shiro.
        //For this demo, we'll just assume a simulated/manually-constructed one:
        return this.user;
    }

    @Override
    public Todo deleteById(String id) {
        return todos.remove(id);
    }

    public static TodoService getInstance() {
        return INSTANCE;
    }
}
