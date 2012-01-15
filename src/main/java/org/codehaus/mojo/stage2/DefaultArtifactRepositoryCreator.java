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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

/**
 *
 * @author Mirko Friedenhagen
 * 
 * @plexus.component
 *
 */
class DefaultArtifactRepositoryCreator implements LogEnabled, ArtifactRepositoryCreator {
    
    private Logger logger;
    
    private static final Pattern ALT_REPO_SYNTAX_PATTERN = Pattern.compile( "(.+)::(.+)::(.+)" );
    
    /**
     * Component used to create a repository.
     *
     * @plexus.requirement
     */
    private ArtifactRepositoryFactory repositoryFactory;

    /**
     * Map that contains the layouts.
     *
     * @plexus.requirement role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    private Map repositoryLayouts;

    public ArtifactRepository getRepository(final String artifactRepository, final String role) throws MojoFailureException, MojoExecutionException {
        ArtifactRepository repo = null;
        if (artifactRepository != null)
        {
            logger.info("Using " + role + " " + artifactRepository);
            Matcher matcher = ALT_REPO_SYNTAX_PATTERN.matcher(artifactRepository);
            if ( !matcher.matches() )
            {
                throw new MojoFailureException( artifactRepository, "Invalid syntax for " + role, "Invalid syntax for " + role + ". Use \"id::layout::url\"." );
            }
            else
            {
                String id = matcher.group( 1 ).trim();
                String layout = matcher.group( 2 ).trim();
                String url = matcher.group( 3 ).trim();
                ArtifactRepositoryLayout repoLayout = getLayout( layout );
                repo = repositoryFactory.createDeploymentArtifactRepository( id, url, repoLayout, true );
            }
        }
        if ( repo == null )
        {
            String msg = "List or download failed: invalid or missing '-D" + role + "=id::layout::url' parameter";
            throw new MojoExecutionException( msg );
        }
        return repo;
    }

    @Override
    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }
    
    private ArtifactRepositoryLayout getLayout(String id) throws MojoExecutionException
    {
        ArtifactRepositoryLayout layout = (ArtifactRepositoryLayout) repositoryLayouts.get(id);
        if (layout == null)
        {
            throw new MojoExecutionException("Invalid repository layout: " + id);
        }
        return layout;
    }

}
