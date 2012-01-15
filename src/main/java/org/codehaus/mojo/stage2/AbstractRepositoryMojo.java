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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * @author Mirko Friedenhagen
 */
public abstract class AbstractRepositoryMojo extends AbstractMojo {
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
     * @component ArtifactRepositoryCreator 
     */
    private ArtifactRepositoryCreator artifactRepositoryCreator;

    /**
     * @return the gavs given on the command line.
     */
    String[] getGavs()
    {
        return gavs;
    }

    ArtifactRepository getRepository(final String artifactRepository, final String role) throws MojoFailureException, MojoExecutionException {
        return artifactRepositoryCreator.getRepository(artifactRepository, role);
    }
}
