/**
 * Copyright 2012 Mirko Friedenhagen 
 */

package org.codehaus.mojo.stage2;

import org.codehaus.mojo.stage2.Constants;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;

import org.junit.Test;

/**
 * @author mirko
 *
 */
public class UriResolvingTest {

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
