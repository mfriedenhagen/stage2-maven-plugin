/*
 * Copyright 2012 Codehaus.
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
package org.codehaus.mojo.stage2;

import java.io.IOException;
import java.util.Arrays;
import org.apache.maven.artifact.repository.ArtifactRepository;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.wagon.WagonException;

/**
 * Downloads artifacts from a repository to a temporary directory.
 *
 * @author Mirko Friedenhagen
 *
 * @requiresProject false
 * @goal download
 */
public class DownloadRepositoryMojo extends ReadOnlyRepositoryMojo {

    /**
     * @component
     */
    private RepositoryDownloader repositoryDownloader;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (getGavs().length == 0) {
            throw new MojoExecutionException("Need to have gavs");
        }
        getLog().info("gavs=" + Arrays.toString(getGavs()));
        final ArtifactRepository repository = getSourceRepository();
        for (String gavString : getGavs()) {
            final Gav gav = Gav.valueOf(gavString);
            try {
                repositoryDownloader.download(repository, gav);
            } catch (WagonException e) {
                throw new MojoExecutionException("Error downloading " + gav, e);
            } catch (IOException e) {
                throw new MojoExecutionException("Error downloading " + gav, e);
            }
        }

    }
}
