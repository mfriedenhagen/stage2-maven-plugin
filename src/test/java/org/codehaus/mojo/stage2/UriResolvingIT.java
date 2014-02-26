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
 *//*
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

import java.io.File;
import java.net.URI;
import org.junit.Test;

/**
 * @author Mirko Friedenhagen
 *
 */
public class UriResolvingIT {

    @Test
    public void test() {
        final URI targetURI = URI.create("http://localhost:8081/artifactory/libs-qa-local/");
        final File basedir = new File("target");
        final File pom = new File(basedir, "group/id/artifactid/2.0.5/artifactid-2.0.5.pom");
        final File mavenMetadata = new File(pom.getParentFile().getParentFile(), Constants.MAVEN_METADATA);
        System.out.println("UriResolvingTest.test()" + targetURI.resolve(basedir.toURI().relativize(mavenMetadata.toURI())));
        System.out.println("UriResolvingTest.enclosing_method()" + String.valueOf(basedir.toURI().relativize(mavenMetadata.toURI())));
    }
}
