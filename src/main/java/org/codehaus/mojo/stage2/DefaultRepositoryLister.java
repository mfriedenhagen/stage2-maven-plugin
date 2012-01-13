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

import java.io.IOException;
import java.util.ArrayList;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.wagon.WagonException;

/**
 *
 * @author mifr
 *
 * @plexus.component
 *
 */
public class DefaultRepositoryLister extends ReadOnlyRepository implements RepositoryLister {
    
    /** @Override */
    @Override
    public void list(ArtifactRepository sourceRepository, Gav gav) throws WagonException, IOException {
        final ArrayList<String> files = collectFiles(sourceRepository, gav);
        getLogger().info("Found " + files + " at " + sourceRepository.getUrl() + " matching " + gav);
    }
}
