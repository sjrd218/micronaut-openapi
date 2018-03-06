/*
 * Copyright 2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.particleframework.configuration.neo4j.bolt;

import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.particleframework.context.exceptions.ConfigurationException;
import org.particleframework.core.util.StringUtils;
import org.particleframework.retry.annotation.Retryable;

import javax.inject.Singleton;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Builds the Neo4j driver and retries the connection via {@link Retryable}
 *
 * @author graemerocher
 * @since 1.0
 */
@Singleton
public class Neo4jDriverBuilder {

    private final Neo4jBoltConfiguration boltConfiguration;

    public Neo4jDriverBuilder(Neo4jBoltConfiguration boltConfiguration) {
        if(boltConfiguration == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        this.boltConfiguration = boltConfiguration;
    }

    @Retryable(ServiceUnavailableException.class)
    public Driver buildDriver() {
        Neo4jBoltConfiguration configuration = this.boltConfiguration;
        List<URI> uris = configuration.getUris();
        Optional<AuthToken> configuredAuthToken = configuration.getAuthToken();
        AuthToken authToken = configuredAuthToken.orElse(null);
        if(uris.size() == 1) {
            URI uri = uris.get(0);
            String userInfo = uri.getUserInfo();
            if(authToken == null && StringUtils.hasText(userInfo)) {
                String[] info = userInfo.split(":");
                if(info.length == 2) {
                    authToken = AuthTokens.basic(info[0], info[1]);
                }
            }
            return GraphDatabase.driver(
                    uri,
                    authToken,
                    configuration.getConfig()
            );
        }
        else if(!uris.isEmpty()) {
            return GraphDatabase.routingDriver(
                    uris,
                    authToken,
                    configuration.getConfig()
            );
        }
        else {
            throw new ConfigurationException("At least one Neo4j URI should be specified eg. neo4j.uri=" + Neo4jBoltConfiguration.DEFAULT_URI);
        }
    }
}