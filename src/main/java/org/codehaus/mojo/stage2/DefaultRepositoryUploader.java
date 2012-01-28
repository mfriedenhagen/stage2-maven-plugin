/*
 * Copyright 2012 Codehaus.
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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;

/**
 *
 * @author Mirko Friedenhagen
 *
 * @plexus.component
 */
class DefaultRepositoryUploader extends RepositoryHelper implements RepositoryUploader, LogEnabled {

    private Logger logger;

    private File basedir;

    /**
     * @component
     */
    private ArtifactDeployer deployer;

    /**
     * Component used to create an artifact.
     *
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void upload(ArtifactRepository targetRepository, Gav gav) throws WagonException, IOException {
        basedir = new File(new File(System.getProperty("java.io.tmpdir"), "staging-plugin"), gav.getEncodedPath());
        if (!basedir.exists()) {
            throw new IllegalArgumentException("staging path " + basedir + " could not be found, did you download the artifacts?");
        }
        logger.info("Uploading from " + basedir + " to " + targetRepository.getUrl());
        @SuppressWarnings("unchecked")
        final List<String> files = FileUtils.getFileAndDirectoryNames(basedir, "**/*", "", false, true, true, false);
        logger.info("files=" + files);
        downloadAndMergeMetadata(files, createWagon(targetRepository), URI.create(targetRepository.getUrl()));
    }

    /**
     * Now all the files are present locally and now we are going to grab the metadata files from the
     * targetRepositoryUrl and pull those down locally so that we can merge the metadata.
     *
     * @param files
     * @param targetWagon
     * @param targetRepositoryUri
     *
     * @throws WagonException
     * @throws IOException
     * @throws TransferFailedException
     */
    void downloadAndMergeMetadata(List<String> files, Wagon targetWagon, URI targetRepositoryUri) throws WagonException, IOException {
        logger.info("Downloading metadata from the target repository." + targetRepositoryUri);

        for (String file : files) {

            if (file.startsWith("/")) {
                file = file.substring(1);
            }

            if (file.endsWith(Constants.POM)) {
                final File pomFile = new File(basedir, file);
                final File mavenMetadataFile = new File(new File(basedir, file).getParentFile().getParentFile(), Constants.MAVEN_METADATA);
                final String relativeMavenMetadata = String.valueOf(basedir.toURI().relativize(mavenMetadataFile.toURI()));
                final MetadataMerger metadataMerger = new MetadataMerger(mavenMetadataFile);
                try {
                    targetWagon.get(relativeMavenMetadata, mavenMetadataFile);
                    logger.info("Downloaded " + targetRepositoryUri.resolve(relativeMavenMetadata) + " to " + mavenMetadataFile);
                } catch (ResourceDoesNotExistException e) {
                    // We don't have an equivalent on the targetRepositoryUrl side because we have something
                    // new on the sourceRepositoryUrl side so just skip the metadata merging.
                    metadataMerger.writeNewMetadata(pomFile);
                    continue;
                }
                metadataMerger.mergeMetadata(pomFile);
            }

        }
    }

    @Override
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }
}
