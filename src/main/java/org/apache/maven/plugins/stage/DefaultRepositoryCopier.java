package org.apache.maven.plugins.stage;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Jason van Zyl
 * @plexus.component
 */
public class DefaultRepositoryCopier implements LogEnabled, RepositoryCopier {
    static class Gav {

        final String groupId;

        final String artifactId;

        final String version;

        final Pattern patternFiles;

        final Pattern patternMeta;

        Gav(String groupId, String artifactId, String version) {
            this.groupId = toPath(StringUtils.split(groupId, "."));
            this.artifactId = artifactId;
            this.version = version;
            final String escapedVersion = escape(version);
            if (artifactId.equals("*")) {
                patternFiles = compile(this.groupId, ".*", escapedVersion);
                patternMeta = compile(this.groupId, ".*", Constants.MAVEN_METADATA);
            } else {
                patternFiles = compile(this.groupId, this.artifactId, escapedVersion);
                patternMeta = compile(this.groupId, this.artifactId, Constants.MAVEN_METADATA);
            }
        }

        /**
         * Compiles a pattern from the splitted string combined with '/'.
         * 
         * @param split
         * @return
         */
        private Pattern compile(final String... split) {
            return Pattern.compile(toPath(split));
        }

        /**
         * @param split
         * @return
         */
        private String toPath(final String... split) {
            return StringUtils.join(split, "/");
        }

        /**
         * Escape regex patterns in the string.
         * 
         * @param version
         * @return
         */
        private String escape(String version) {
            return Pattern.quote(version);
        }

        public boolean matches(String file) {
            return patternFiles.matcher(file).find() || patternMeta.matcher(file).find();
        }

        public static Gav valueOf(String version) {
            String[] gavComponents = StringUtils.split(version, ":");
            if (gavComponents.length != 3) {
                throw new IllegalArgumentException(
                        "version must have groupId:artifactId:version, where artifactId may be *");
            }
            return new Gav(gavComponents[0], gavComponents[1], gavComponents[2]);
        }
    }

    /** @plexus.requirement */
    private WagonManager wagonManager;

    private Logger logger;

    public void copy(Repository sourceRepository, Repository targetRepository, String[] gavStrings)
            throws WagonException, IOException {
        for (String gavString : gavStrings) {
            final Gav gav = Gav.valueOf(gavString);
            copy(sourceRepository, targetRepository, gav);
        }
    }

    /**
     * @param sourceRepository
     * @param targetRepository
     * @param gav
     * @throws IOException
     * @throws UnsupportedProtocolException
     * @throws WagonConfigurationException
     * @throws ConnectionException
     * @throws AuthenticationException
     * @throws TransferFailedException
     * @throws ResourceDoesNotExistException
     * @throws AuthorizationException
     */
    void copy(Repository sourceRepository, Repository targetRepository, final Gav gav) throws IOException,
            UnsupportedProtocolException, WagonConfigurationException, ConnectionException, AuthenticationException,
            TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        // Work directory
        String prefix = "staging-plugin";

        File basedir = new File(System.getProperty("java.io.tmpdir"), prefix + "-" + gav.version);

        deleteAndCreateTempDir(basedir);

        List<String> files = collectAndDownloadFiles(sourceRepository, basedir, gav);

        Wagon targetWagon = createTargetWagon(targetRepository);

        final String targetRepositoryUrl = targetRepository.getUrl();
        final URI targetRepositoryUri = downloadAndMergeMetadata(files, basedir, targetWagon, targetRepositoryUrl);
        uploadArtifacts(files, basedir, targetWagon, targetRepositoryUri);
    }

    /**
     * @param targetRepository
     * @return
     * @throws UnsupportedProtocolException
     * @throws WagonConfigurationException
     * @throws ConnectionException
     * @throws AuthenticationException
     */
    Wagon createTargetWagon(Repository targetRepository) throws UnsupportedProtocolException,
            WagonConfigurationException, ConnectionException, AuthenticationException {
        Wagon targetWagon = wagonManager.getWagon(targetRepository);

        AuthenticationInfo targetAuth = wagonManager.getAuthenticationInfo(targetRepository.getId());

        targetWagon.connect(targetRepository, targetAuth);
        return targetWagon;
    }

    /**
     * Now all the files are present locally and now we are going to grab the metadata files from the
     * targetRepositoryUrl and pull those down locally so that we can merge the metadata.
     *
     * @param files
     * @param basedir
     * @param targetWagon
     * @param targetRepositoryUrl
     * @return
     * @throws TransferFailedException
     * @throws AuthorizationException
     * @throws IOException
     */
    URI downloadAndMergeMetadata(List<String> files, File basedir, Wagon targetWagon, final String targetRepositoryUrl)
            throws TransferFailedException, AuthorizationException, IOException {
        logger.info("Downloading metadata from the target repository.");

        final URI targetRepositoryUri = URI.create(targetRepositoryUrl.endsWith("/") ? targetRepositoryUrl
                : targetRepositoryUrl + "/");

        for (String file : files) {

            if (file.startsWith("/")) {
                file = file.substring(1);
            }

            if (file.endsWith(Constants.MAVEN_METADATA)) {
                File emf = new File(basedir, file + Constants.IN_PROCESS_MARKER);

                try {
                    targetWagon.get(file, emf);
                } catch (ResourceDoesNotExistException e) {
                    // We don't have an equivalent on the targetRepositoryUrl side because we have something
                    // new on the sourceRepositoryUrl side so just skip the metadata merging.

                    continue;
                }
                final MetadataMerger metadataMerger = new MetadataMerger(emf);
                metadataMerger.mergeMetadata();
            }

        }
        return targetRepositoryUri;
    }

    /**
     * @param files
     * @param basedir
     * @param targetWagon
     * @param targetRepositoryUri
     * @throws TransferFailedException
     * @throws ResourceDoesNotExistException
     * @throws AuthorizationException
     */
    void uploadArtifacts(List<String> files, File basedir, Wagon targetWagon, final URI targetRepositoryUri)
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        for (String file : files) {
            logger.info("Uploaded: " + targetRepositoryUri.resolve(file));
            targetWagon.put(new File(basedir, file), file);
        }
    }

    /**
     * @param sourceRepository
     * @param basedir
     * @param gav
     * @return
     * @throws UnsupportedProtocolException
     * @throws WagonConfigurationException
     * @throws ConnectionException
     * @throws AuthenticationException
     * @throws TransferFailedException
     * @throws ResourceDoesNotExistException
     * @throws AuthorizationException
     */
    List<String> collectAndDownloadFiles(Repository sourceRepository, File basedir, final Gav gav)
            throws UnsupportedProtocolException, WagonConfigurationException, ConnectionException,
            AuthenticationException, TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        List<String> files = new ArrayList<String>();

        Wagon sourceWagon = createTargetWagon(sourceRepository);

        logger.info("Scanning source repository for all files.");

        List<String> rawFiles = new ArrayList<String>();

        scan(sourceWagon, "", rawFiles);

        logger.debug("all files found in staging repository" + rawFiles);

        logger.info("Scanned source repository for all files, found " + rawFiles.size() + " files.");

        for (String file : rawFiles) {
            if (gav.matches(file)) {
                files.add(file);
            }
        }

        // Need to sort the files, otherwise the sha1 or md5 might be uploaded before the concrete files,
        // which will result in an error.
        Collections.sort(files);
        logger.info("Found " + files.size() + " matching files: " + files);

        logger.info("Downloading files from the source repository to " + basedir);

        for (String s : files) {
            File f = new File(basedir, s);

            FileUtils.mkdir(f.getParentFile().getAbsolutePath());

            logger.debug("Downloading file from the source repository: " + s);

            sourceWagon.get(s, f);
        }

        logger.info("Downloaded " + files.size() + "  files from the source repository to " + basedir);
        return files;
    }

    /**
     * @param basedir
     * @throws IOException
     */
    void deleteAndCreateTempDir(File basedir) throws IOException {
        logger.info("Writing all output to " + basedir);

        FileUtils.deleteDirectory(basedir);

        basedir.mkdirs();
    }


    private void scan(Wagon wagon, String basePath, List<String> collected) {
        try {
            if (basePath.indexOf(".svn") >= 0 || basePath.startsWith(".index") || basePath.startsWith("/.index")) {
            } else {
                @SuppressWarnings("unchecked")
                List<String> files = wagon.getFileList(basePath);

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

    public void enableLogging(Logger logger) {
        this.logger = logger;
    }
}
