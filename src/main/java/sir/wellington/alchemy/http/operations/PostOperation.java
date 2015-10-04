/*
 * Copyright 2015 Wellington.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sir.wellington.alchemy.http.operations;

import com.google.gson.JsonElement;

/**
 *
 * @author SirWellington
 * @param <ResponseType>
 */
public interface PostOperation<ResponseType> extends HttpOperation<PostOperation<ResponseType>, ResponseType>
{

    PostOperation<ResponseType> body(String jsonString);

    PostOperation<ResponseType> body(JsonElement jsonElement);

    PostOperation<ResponseType> body(Object genericObject);
}
