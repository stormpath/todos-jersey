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
package com.stormpath.samples.todos.error;

import com.stormpath.samples.todos.http.HttpStatus;
import com.stormpath.samples.todos.lang.ObjectUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class RestError {

    private static final String STATUS_PROP_NAME = "status";
    private static final String CODE_PROP_NAME = "code";
    private static final String MESSAGE_PROP_NAME = "message";
    private static final String DEVELOPER_MESSAGE_PROP_NAME = "developerMessage";
    private static final String MORE_INFO_PROP_NAME = "moreInfo";

    private static final String DEFAULT_MORE_INFO_URL = "mailto:support@stormpath.com";

    private final HttpStatus status;
    private final int code;
    private final String message;
    private final String developerMessage;
    private final String moreInfoUrl;
    private final Throwable throwable;

    public RestError(HttpStatus status, int code, String message, String developerMessage, String moreInfoUrl, Throwable throwable) {
        if (status == null) {
            throw new NullPointerException("HttpStatus argument cannot be null.");
        }
        this.status = status;
        this.code = code;
        this.message = message;
        this.developerMessage = developerMessage;
        this.moreInfoUrl = moreInfoUrl;
        this.throwable = throwable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof RestError) {
            RestError re = (RestError)o;
            return ObjectUtils.nullSafeEquals(getStatus(), re.getStatus()) &&
                    ObjectUtils.nullSafeEquals(getCode(), re.getCode()) &&
                    ObjectUtils.nullSafeEquals(getMessage(), re.getMessage()) &&
                    ObjectUtils.nullSafeEquals(getDeveloperMessage(), re.getDeveloperMessage()) &&
                    ObjectUtils.nullSafeEquals(getMoreInfoUrl(), re.getMoreInfoUrl()) &&
                    ObjectUtils.nullSafeEquals(getThrowable(), re.getThrowable());

        }
        return false;
    }

    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(getStatus(), getCode(), getMessage(), getDeveloperMessage(), getMoreInfoUrl(), getThrowable());
    }

    public String toString() {
        return append(new StringBuilder(), getStatus()).append(", message: ").append(getMessage()).toString();
    }

    private StringBuilder append(StringBuilder buf, HttpStatus status) {
        buf.append(status.value()).append(" (").append(status.getReasonPhrase()).append(")");
        return buf;
    }

    private String toString(HttpStatus status) {
        return append(new StringBuilder(), status).toString();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public String getMoreInfoUrl() {
        return moreInfoUrl;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Map<String,?> toMap() {
        Map<String,Object> m = new LinkedHashMap<String,Object>();
        HttpStatus status = getStatus();
        m.put(STATUS_PROP_NAME, status.value());

        int code = getCode();
        if (code <= 0) {
            code = status.value();
        }
        m.put(CODE_PROP_NAME, code);

        String httpStatusMessage = null;

        String message = getMessage();
        if (message == null) {
            httpStatusMessage = toString(status);
            message = httpStatusMessage;
        }
        m.put(MESSAGE_PROP_NAME, message);

        String devMsg = getDeveloperMessage();
        if (devMsg == null) {
            if (httpStatusMessage == null) {
                httpStatusMessage = toString(status);
            }
            devMsg = httpStatusMessage;

            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            Throwable t = getThrowable();
            if (t != null) {
                devMsg = devMsg + ": " + t.getMessage();
            }
        }
        m.put(DEVELOPER_MESSAGE_PROP_NAME, devMsg);

        String moreInfoUrl = getMoreInfoUrl();
        if (moreInfoUrl == null) {
            moreInfoUrl = DEFAULT_MORE_INFO_URL;
        }
        m.put(MORE_INFO_PROP_NAME, moreInfoUrl);

        return m;
    }

    public static class Builder {

        private HttpStatus status;
        private int code;
        private String message;
        private String developerMessage;
        private String moreInfoUrl;
        private Throwable throwable;

        public Builder() {
        }

        public Builder setStatus(int statusCode) {
            this.status = HttpStatus.valueOf(statusCode);
            return this;
        }

        public Builder setStatus(HttpStatus status) {
            this.status = status;
            return this;
        }

        public Builder setCode(int code) {
            this.code = code;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setDeveloperMessage(String developerMessage) {
            this.developerMessage = developerMessage;
            return this;
        }

        public Builder setMoreInfoUrl(String moreInfoUrl) {
            this.moreInfoUrl = moreInfoUrl;
            return this;
        }

        public Builder setThrowable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        public RestError build() {
            if (this.status == null) {
                this.status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            if (this.moreInfoUrl == null) {
                this.moreInfoUrl = DEFAULT_MORE_INFO_URL;
            }
            return new RestError(this.status, this.code, this.message, this.developerMessage, this.moreInfoUrl, this.throwable);
        }
    }
}
