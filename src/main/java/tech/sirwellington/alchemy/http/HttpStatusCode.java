/*
 * Copyright © 2019. Sir Wellington.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.sirwellington.alchemy.http;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.positiveInteger;

public enum HttpStatusCode
{
    INFORMATIONAL(100),
    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NON_AUTHORIZED_INFORMATION(203),
    NO_CONTENT(204),
    RESET_CONTENT(205),
    PARTIAL_CONTENT(206),
    REDIRECTED(300),
    MOVED_PERMANENTLY(301),
    FOUND(302),
    NOT_MODIFIED(304),
    USE_PROXY(305),
    TEMPORARY_REDIRECT(307),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    PAYMENT_REQUIRED(402),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    PROXY_AUTHENTICATION_REQUIRED(407),
    REQUEST_TIMEOUT(408),
    CONFLICT(409),
    GONE(410),
    LENGTH_REQUIRED(411),
    PRECONDITION_FAILED(412),
    REQUEST_ENTITY_TOO_LARGE(413),
    REQUEST_URI_TOO_LONG(414),
    UNSUPPORTED_MEDIA_TYPE(415),
    INTERNAL_SERVICE_ERROR(500),
    NOT_IMPLEMENTED(501),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504),
    HTTP_VERSION_NOT_SUPPORTED(505),
    NETWORK_AUTHENTICATION_REQUIRED(511),
    NETWORK_READ_TIMEOUT_ERROR(598),
    NETWORK_CONNECT_TIMEOUT_ERROR(599);

    private final int code;
    public final String statusName;

    public static final List<HttpStatusCode> all = List.of(values());

    private static final Map<Integer, HttpStatusCode> REVERSE_MAP = all.stream()
        .collect(Collectors.toMap(HttpStatusCode::getCode, status -> status));

    HttpStatusCode(int code)
    {
        checkThat(code).isA(positiveInteger());
        this.code = code;
        this.statusName = this.toString();
    }

    public boolean matchesCode(int code)
    {
        return this.code == code;
    }

    public int getCode()
    {
        return code;
    }

    public static HttpStatusCode any()
    {
        return all.get(ThreadLocalRandom.current().nextInt(all.size()));
    }

    public static HttpStatusCode forCode(int code)
    {
        return REVERSE_MAP.get(code);
    }

    public static HttpStatusCode anyExcept(HttpStatusCode code)
    {
        List<HttpStatusCode> filtered = all.stream()
            .filter(status -> status != code)
            .collect(Collectors.toList());

        return filtered.get(ThreadLocalRandom.current().nextInt(filtered.size()));
    }
}
