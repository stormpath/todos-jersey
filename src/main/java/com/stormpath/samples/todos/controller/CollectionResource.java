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

import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("unchecked")
public class CollectionResource extends Link {

    public static final int DEFAULT_LIMIT = 25;

    public CollectionResource(UriInfo info, String subPath, Collection c) {
        this(info, subPath, c, 0, getLimit(c));
    }

    public CollectionResource(UriInfo info, String subPath, Collection c, int offset, int limit) {
        super(info, subPath);
        put("offset", offset);
        put("limit", getLimit(limit));
        put("items", c != null ? c : Collections.emptyList());
    }

    private static int getLimit(Collection c) {
        return getLimit(c != null ? c.size() : 0);
    }

    private static int getLimit(int limit) {
        return Math.max(DEFAULT_LIMIT, limit);
    }

}
