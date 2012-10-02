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

import com.stormpath.samples.todos.entity.Entity;

import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;

@SuppressWarnings("unchecked")
public class Link extends LinkedHashMap {

    public static final String PATH_SEPARATOR = "/";

    public static final String TODOS = PATH_SEPARATOR + "todos";
    public static final String USERS = PATH_SEPARATOR + "users";

    public Link(UriInfo info, Entity entity) {
        this(getFullyQualifiedContextPath(info), entity);
    }

    public Link(String fqBasePath, Entity entity) {
        String href = createHref(fqBasePath, entity);
        put("href", href);
    }

    public Link(UriInfo info, String subPath) {
        this(getFullyQualifiedContextPath(info), subPath);
    }

    public Link(String fqBasePath, String subPath) {
        String href = fqBasePath + subPath;
        put("href", href);
    }

    protected static String getFullyQualifiedContextPath(UriInfo info) {
        String fq = info.getBaseUri().toString();
        if (fq.endsWith("/")) {
            return fq.substring(0, fq.length()-1);
        }
        return fq;
    }

    protected String createHref(String fqBasePath, Entity entity) {
        StringBuilder sb = new StringBuilder(fqBasePath);
        ResourcePath path = ResourcePath.forClass(entity.getClass());
        sb.append(path.getPath()).append(PATH_SEPARATOR).append(entity.getId());
        return sb.toString();
    }

    public String getHref() {
        return (String)get("href");
    }

}
