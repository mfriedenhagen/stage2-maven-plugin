/*
 * Copyright 2012 The Apache Software Foundation.
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
package org.apache.maven.plugins.stage;

import java.io.IOException;
import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.Authentication;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

/**
 *
 * @author mirko
 * @plexus.component
 */
public class DefaultRepositoryDownloader implements RepositoryDownloader, LogEnabled {

    /** @plexus.requirement */
    private WagonManager wagonManager;

    private Logger logger;


    public void download(ArtifactRepository sourceRepository, String[] gavStrings) throws WagonException, IOException {
        for (String gavString : gavStrings) {
            final Gav gav = Gav.valueOf(gavString);
            download(sourceRepository, gav);
        }
    }

    private void download(ArtifactRepository sourceRepository, Gav gav) throws WagonException {
        final Repository repository = new Repository(sourceRepository.getId(), sourceRepository.getUrl());
        final Authentication authentication = sourceRepository.getAuthentication();
        final Wagon wagon = wagonManager.getWagon(repository);
        wagon.connect(repository);
        logger.info("Gathering artifacts from" + repository + ", gav=" + gav);
        
    }
    
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

}
