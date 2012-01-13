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
package org.codehaus.mojo.stage2;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.Authentication;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

/**
 *
 * @author mirko
 */
public abstract class ReadOnlyRepository implements LogEnabled {

    /** @plexus.requirement */
    private WagonManager wagonManager;

    private Logger logger;
    
    Wagon createWagon(ArtifactRepository artifactRepository) throws WagonException {
        final Repository repository = new Repository(artifactRepository.getId(), artifactRepository.getUrl());
        final Authentication authentication = artifactRepository.getAuthentication();
        final Wagon wagon = wagonManager.getWagon(repository);
        wagon.connect(repository);
        return wagon;
    }

    void scan(Wagon wagon, String basePath, List<String> collected) {
        getLogger().debug("Searching in " + basePath);
        try {
            if (basePath.indexOf(".svn") >= 0 || basePath.startsWith(".index") || basePath.startsWith("/.index")) {
            } else {
                @SuppressWarnings(value = "unchecked")
                List<String> files = wagon.getFileList(basePath);
                getLogger().debug("Found files in the source repository: " + files);
                if (files.isEmpty()) {
                    collected.add(basePath);
                } else {
                    for (String file : files) {
                        getLogger().debug("Found file in the source repository: " + file);
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

    ArrayList<String> collectFiles(ArtifactRepository sourceRepository, Gav gav) throws WagonException {
        Wagon wagon = createWagon(sourceRepository);
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
        return files;
    }
    
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    /**
     * @return the logger
     */
    Logger getLogger() {
        return logger;
    }
    
}
