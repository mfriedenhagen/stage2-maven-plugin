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
import java.io.IOException;
import java.util.ArrayList;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.FileUtils;

/**
 *
 * @author mirko
 * @plexus.component
 */
class DefaultRepositoryDownloader extends ReadOnlyRepository implements RepositoryDownloader {

    private File basedir;

    /**
     * @Override
     */
    public void download(ArtifactRepository sourceRepository, Gav gav) throws WagonException, IOException {
        basedir = new File(new File(System.getProperty("java.io.tmpdir"), "staging-plugin"), gav.getEncodedPath());
        deleteAndCreateTempDir();
        getLogger().info("Gathering artifacts from " + sourceRepository.getUrl() + ", gav=" + gav + " to " + basedir);
        ArrayList<String> files = collectFiles(sourceRepository, gav);
        getLogger().info("Downloading " + files.size() + " files from the source repository to " + basedir);
        final Wagon sourceWagon = createWagon(sourceRepository);
        for (final String source : files) {
            final File target = new File(basedir, source);
            FileUtils.mkdir(target.getParentFile().getAbsolutePath());
            getLogger().debug("Downloading file from the source repository: " + source);
            sourceWagon.get(source, target);
        }
        getLogger().info("Downloaded " + files.size() + "  files from the source repository to " + basedir);
    }

    /**
     * @param basedir
     *
     s IOException
     */
    private void deleteAndCreateTempDir() throws IOException {
        FileUtils.deleteDirectory(basedir);
        FileUtils.forceMkdir(basedir);
    }
}
