/*
 * Copyright (C) 2013 Moritz Munte <m.munte@preselect.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package PdfResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;


/**
 * HTTP Client
 *
 * @author Moritz Munte <m.munte@preselect.com>
 */
public class HttpClient {

        private Client client;
        private WebResource resource;

        public HttpClient(String path) {
                client = Client.create();
                resource = client.resource(path);
        }

        public ClientResponse send(Object json) {
                ClientResponse response = resource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, json);
                if (response.getStatus() != 200) {
                        throw new RuntimeException("Failed sending object: HTTP error code : " + response.getStatus());
                }

                return response;
        }

        public Object getJson(Class<?> cls) {
                ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
                if (response.getStatus() != 200) {
                        throw new RuntimeException("Failed recieving object: HTTP error code : " + response.getStatus());
                }
                Object output = response.getEntity(cls);

                return output;
        }
}
