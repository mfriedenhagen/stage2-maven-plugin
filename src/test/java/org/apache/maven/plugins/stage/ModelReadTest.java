/**
 * Copyright 2012 Mirko Friedenhagen 
 */

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


import static org.junit.Assert.assertEquals;

import java.io.IOException;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.validation.DefaultModelValidator;
import org.apache.maven.project.validation.ModelValidationResult;
import org.apache.maven.project.validation.ModelValidator;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author mirko
 *
 */
@RunWith(JUnitParamsRunner.class)
public class ModelReadTest {

    @Test
    @Parameters({
        "maven-plugin-pom.xml",
        "jar-pom.xml",
        "pom-pom.xml"
        })
    public void test(String resourcename) throws IOException, XmlPullParserException {
        final XmlStreamReader reader = ReaderFactory.newXmlReader(ModelReadTest.class.getResourceAsStream(resourcename));
        final Model model;
        try {
            model = new MavenXpp3Reader().read(reader);
        } finally {
            reader.close();
        }
        final Parent parent = model.getParent();
        String groupId = model.getGroupId() == null ? parent.getGroupId() : model.getGroupId();
        String artifactId = model.getArtifactId();
        String version = model.getVersion() == null ? parent.getVersion() : model.getVersion();
        String packaging = model.getPackaging();
        Model newModel = generateModel(groupId, artifactId, version, packaging);
        ModelValidator validator = new DefaultModelValidator();
        ModelValidationResult validationResult = validator.validate(newModel);
        assertEquals(0, validationResult.getMessageCount());
        System.out.printf("ModelReadTest.test() %s\n", newModel.getId());
    }
    /**
     * Generates a minimal model from the user-supplied artifact information.
     * 
     * @return The generated model, never <code>null</code>.
     */
    private Model generateModel(String groupId, String artifactId, String version, String packaging)
    {
        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( groupId );
        model.setArtifactId( artifactId );
        model.setVersion( version );
        model.setPackaging( packaging );
        return model;
    }

}
