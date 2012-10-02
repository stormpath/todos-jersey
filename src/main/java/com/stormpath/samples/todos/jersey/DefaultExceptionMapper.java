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
package com.stormpath.samples.todos.jersey;

import com.stormpath.samples.todos.error.RestError;
import com.stormpath.samples.todos.lang.ClassUtils;
import com.stormpath.samples.todos.lang.OrderPreservingProperties;
import com.stormpath.samples.todos.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

    /**
     * The default name of the exception attribute: "exception".
     */
    public static final String DEFAULT_EXCEPTION_MESSAGE_VALUE = "_exmsg";
    public static final String EXCEPTION_CONFIG_DELIMITER = "|";

    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    private Map<String, RestError> exceptionMappings = Collections.emptyMap();

    public DefaultExceptionMapper() {
        //should be cleaner, but this is fine for a demo:
        InputStream is = ClassUtils.getResourceAsStream("restErrors.properties");
        OrderPreservingProperties props = new OrderPreservingProperties();
        props.load(is);
        this.exceptionMappings = toRestErrors(props);
    }

    @Override
    public Response toResponse(Throwable t) {
        //System.out.println("Throwable: " + t);
        RestError error = getRestError(t);
        return Response.status(Response.Status.fromStatusCode(error.getStatus().value()))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(error.toMap()).build();
    }

    private RestError getRestError(Throwable t) {

        RestError template = getRestErrorTemplate(t);
        if (template == null) {
            return null;
        }

        RestError.Builder builder = new RestError.Builder();
        builder.setStatus(template.getStatus());
        builder.setCode(template.getCode());
        builder.setMoreInfoUrl(template.getMoreInfoUrl());
        builder.setThrowable(t);

        String msg = getMessage(template.getMessage(), t);
        if (msg != null) {
            builder.setMessage(msg);
        }
        msg = getMessage(template.getDeveloperMessage(), t);
        if (msg != null) {
            builder.setDeveloperMessage(msg);
        }

        return builder.build();
    }

    /**
     * Returns the response status message to return to the client, or {@code null} if no
     * status message should be returned.
     *
     * @return the response status message to return to the client, or {@code null} if no
     *         status message should be returned.
     */
    protected String getMessage(String msg, Throwable t) {

        if (msg != null) {
            if (msg.equalsIgnoreCase("null") || msg.equalsIgnoreCase("off")) {
                return null;
            }
            if (msg.equalsIgnoreCase(DEFAULT_EXCEPTION_MESSAGE_VALUE)) {
                msg = t.getMessage();
            }
            //TODO: I18N translation here:
            /*if (messageSource != null) {
                Locale locale = null;
                if (localeResolver != null) {
                    locale = localeResolver.resolveLocale(webRequest.getRequest());
                }
                msg = messageSource.getMessage(msg, null, msg, locale);
            }*/
        }

        return msg;
    }

    private RestError getRestErrorTemplate(Throwable t) {
        Map<String,RestError> mappings = this.exceptionMappings;
        if (mappings == null || mappings.isEmpty()) {
            return null;
        }
        RestError template = null;
        String dominantMapping = null;
        int deepest = Integer.MAX_VALUE;
        for (Map.Entry<String, RestError> entry : mappings.entrySet()) {
            String key = entry.getKey();
            int depth = getDepth(key, t);
            if (depth >= 0 && depth < deepest) {
                deepest = depth;
                dominantMapping = key;
                template = entry.getValue();
            }
        }
        if (template != null && log.isDebugEnabled()) {
            log.debug("Resolving to RestError template '" + template + "' for exception of type [" + t.getClass().getName() +
                    "], based on exception mapping [" + dominantMapping + "]");
        }
        return template;
    }

    /**
     * Return the depth to the superclass matching.
     * <p>0 means ex matches exactly. Returns -1 if there's no match.
     * Otherwise, returns depth. Lowest depth wins.
     */
    protected int getDepth(String exceptionMapping, Throwable t) {
        return getDepth(exceptionMapping, t.getClass(), 0);
    }

    private int getDepth(String exceptionMapping, Class exceptionClass, int depth) {
        if (exceptionClass.getName().contains(exceptionMapping)) {
            // Found it!
            return depth;
        }
        // If we've gone as far as we can go and haven't found it...
        if (exceptionClass.equals(Throwable.class)) {
            return -1;
        }
        return getDepth(exceptionMapping, exceptionClass.getSuperclass(), depth + 1);
    }

    private static int getRequiredInt(String key, String value) {
        try {
            int anInt = Integer.valueOf(value);
            return Math.max(-1, anInt);
        } catch (NumberFormatException e) {
            String msg = "Configuration element '" + key + "' requires an integer value.  The value " +
                    "specified: " + value;
            throw new IllegalArgumentException(msg, e);
        }
    }

    private static int getInt(String key, String value) {
        try {
            return getRequiredInt(key, value);
        } catch ( IllegalArgumentException iae) {
            return 0;
        }
    }

    private static Map<String, RestError> toRestErrors(Map<String, String> smap) {
        if (smap == null || smap.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, RestError> map = new LinkedHashMap<String, RestError>(smap.size());

        for (Map.Entry<String, String> entry : smap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            RestError template = toRestError(value);
            map.put(key, template);
        }

        return map;
    }

    private static RestError toRestError(String exceptionConfig) {
        String[] values = StringUtils.delimitedListToStringArray(exceptionConfig, EXCEPTION_CONFIG_DELIMITER);
        if (values == null || values.length == 0) {
            throw new IllegalStateException("Invalid config mapping.  Exception names must map to a string configuration.");
        }
        if (values.length > 5) {
            throw new IllegalStateException("Invalid config mapping.  Mapped values must not contain more than 2 " +
                    "values (code=y, msg=z, devMsg=x)");
        }

        RestError.Builder builder = new RestError.Builder();

        boolean statusSet = false;
        boolean codeSet = false;
        boolean msgSet = false;
        boolean devMsgSet = false;
        boolean moreInfoSet = false;

        for (String value : values) {

            String trimmedVal = StringUtils.trimWhitespace(value);

            //check to see if the value is an explicitly named key/value pair:
            String[] pair = StringUtils.split(trimmedVal, "=");
            if (pair != null) {
                //explicit attribute set:
                String pairKey = StringUtils.trimWhitespace(pair[0]);
                if (!StringUtils.hasText(pairKey)) {
                    pairKey = null;
                }
                String pairValue = StringUtils.trimWhitespace(pair[1]);
                if (!StringUtils.hasText(pairValue)) {
                    pairValue = null;
                }
                if ("status".equalsIgnoreCase(pairKey)) {
                    int statusCode = getRequiredInt(pairKey, pairValue);
                    builder.setStatus(statusCode);
                    statusSet = true;
                } else if ("code".equalsIgnoreCase(pairKey)) {
                    int code = getRequiredInt(pairKey, pairValue);
                    builder.setCode(code);
                    codeSet = true;
                } else if ("msg".equalsIgnoreCase(pairKey)) {
                    builder.setMessage(pairValue);
                    msgSet = true;
                } else if ("devMsg".equalsIgnoreCase(pairKey)) {
                    builder.setDeveloperMessage(pairValue);
                    devMsgSet = true;
                } else if ("infoUrl".equalsIgnoreCase(pairKey)) {
                    builder.setMoreInfoUrl(pairValue);
                    moreInfoSet = true;
                }
            } else {
                //not a key/value pair - use heuristics to determine what value is being set:
                int val;
                if (!statusSet) {
                    val = getInt("status", trimmedVal);
                    if (val > 0) {
                        builder.setStatus(val);
                        statusSet = true;
                        continue;
                    }
                }
                if (!codeSet) {
                    val = getInt("code", trimmedVal);
                    if (val > 0) {
                        builder.setCode(val);
                        codeSet = true;
                        continue;
                    }
                }
                if (!msgSet) {
                    builder.setMessage(trimmedVal);
                    msgSet = true;
                    continue;
                }
                if (!devMsgSet) {
                    builder.setDeveloperMessage(trimmedVal);
                    devMsgSet = true;
                    continue;
                }
                if (!moreInfoSet) {
                    builder.setMoreInfoUrl(trimmedVal);
                    moreInfoSet = true;
                    //noinspection UnnecessaryContinue
                    continue;
                }
            }
        }

        return builder.build();
    }
}
