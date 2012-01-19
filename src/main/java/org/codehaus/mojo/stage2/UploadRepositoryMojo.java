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

import java.io.IOException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.deploy.AbstractDeployMojo;
import org.apache.maven.wagon.WagonException;

/**
 * Uploads artifacts from a temporary directory to a repository.
 *
 * @author Mirko Friedenhagen
 *
 * @requiresProject false
 * @goal upload
 */
public class UploadRepositoryMojo extends AbstractDeployMojo {

    /**
     * Specifies an repository to which the project artifacts should be uploaded.
     *
     * <br/>
     *
     * Format: id::layout::url
     *
     * @parameter expression="${stage.targetRepository}"
     * @required
     */
    private String targetRepository;

    /**
     * The GAV coordinates of the artifact that is to be copied. This is a comma separated list of coordinates like
     * <tt>de.friedenhagen.multimodule:*:1.24,de.friedenhagen.multimodule:parent:1.25</tt>
     * <p>
     * <b>Note:</b> You may enter '*' to copy all artifacts with a specific groupId.
     * </p>
     *
     * @parameter expression="${stage.gavs}"
     * @required
     */
    private String[] gavs;

    /**
     * @component
     */
    private RepositoryUploader repositoryUploader;

    /**
     * @component ArtifactRepositoryCreator
     */
    ArtifactRepositoryCreator artifactRepositoryCreator;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final ArtifactRepository repository = artifactRepositoryCreator.getRepository(targetRepository, "stage.targetRepository");
        for (final String gavString : gavs) {
            final Gav gav = Gav.valueOf(gavString);
            try {
                repositoryUploader.upload(repository, gav);
            } catch (WagonException e) {
                throw new MojoExecutionException("Could not upload to " + targetRepository, e);
            } catch (IOException e) {
                throw new MojoExecutionException("Could not upload to " + targetRepository, e);
            }
        }
    }
}
