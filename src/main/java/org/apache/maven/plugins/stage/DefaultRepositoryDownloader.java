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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.Authentication;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;

/**
 *
 * @author mirko
 * @plexus.component
 */
class DefaultRepositoryDownloader implements RepositoryDownloader, LogEnabled {

    /** @plexus.requirement */
    private WagonManager wagonManager;

    private Logger logger;

    private File basedir;
    
    public void download(ArtifactRepository sourceRepository, String[] gavStrings) throws WagonException, IOException {
        for (String gavString : gavStrings) {
            final Gav gav = Gav.valueOf(gavString);
            download(sourceRepository, gav);
        }
    }

    public void download(ArtifactRepository sourceRepository, Gav gav) throws WagonException, IOException {
        Wagon wagon = createWagon(sourceRepository);
        basedir = new File(new File(System.getProperty("java.io.tmpdir"), "staging-plugin"), gav.getEncodedPath());
        deleteAndCreateTempDir();
        logger.info("Gathering artifacts from " + sourceRepository.getUrl() + ", gav=" + gav + " to " + basedir);
        final ArrayList<String> rawFiles = new ArrayList<String>();
        scan(wagon, gav.groupIdPath + "/", rawFiles);
        logger.info("Found " + rawFiles.size() + " files in " + sourceRepository.getUrl() + gav.groupIdPath);
        final ArrayList<String> files = new ArrayList<String>();
        for (String file : rawFiles) {
            if (gav.matches(file)) {
                files.add(file);
            }
        }
        logger.info("Found " + files.size() + " files in " + sourceRepository.getUrl() + gav.groupIdPath + " matching " + gav);
    }

    Wagon createWagon(ArtifactRepository artifactRepository) throws WagonException {
        final Repository repository = new Repository(artifactRepository.getId(), artifactRepository.getUrl());
        final Authentication authentication = artifactRepository.getAuthentication();
        final Wagon wagon = wagonManager.getWagon(repository);
        wagon.connect(repository);
        return wagon;       
    }
    
    /**
     * @param basedir
     * @throws IOException
     */
    private void deleteAndCreateTempDir() throws IOException {
        FileUtils.deleteDirectory(basedir);
        FileUtils.forceMkdir(basedir);
    }

    public void enableLogging(Logger logger) {
        this.logger = logger;
    }
    private void scan(Wagon wagon, String basePath, List<String> collected) {
        logger.debug("Searching in " + basePath);
        try {
            if (basePath.indexOf(".svn") >= 0 || basePath.startsWith(".index") || basePath.startsWith("/.index")) {
            } else {
                @SuppressWarnings("unchecked")
                List<String> files = wagon.getFileList(basePath);
                logger.debug("Found files in the source repository: " + files);
                if (files.isEmpty()) {
                    collected.add(basePath);
                } else {
                    for (String file : files) {
                        logger.debug("Found file in the source repository: " + file);
                        scan(wagon, basePath + file, collected);
                    }
                }
            }
        } catch (TransferFailedException e) {
            throw new RuntimeException(e);
        } catch (ResourceDoesNotExistException e) {
            // is thrown when calling getFileList on a file
            collected.add(basePath);
        } catch (AuthorizationException e) {
            throw new RuntimeException(e);
        }

    }

}
