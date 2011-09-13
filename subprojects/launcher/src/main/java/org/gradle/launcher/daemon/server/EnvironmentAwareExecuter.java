/*
 * Copyright 2011 the original author or authors.
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

package org.gradle.launcher.daemon.server;

import org.gradle.launcher.exec.DefaultGradleLauncherActionExecuter;
import org.gradle.launcher.daemon.protocol.Build;
import org.gradle.launcher.env.LenientEnvHacker;

import java.util.Map;
import java.util.Properties;

/**
 * Executes the gradle build and makes sure that the system properties and env variables
 * are updated and restored after the build.
 *
 * TODO SF decouple from gradle build so that we can have more explicit coverage in the functional test
 * and add some coverage for bringing the old environment back after build execution
 *
 * @author: Szczepan Faber, created at: 9/6/11
 */
public class EnvironmentAwareExecuter {

    private final DefaultGradleLauncherActionExecuter executer;

    public EnvironmentAwareExecuter(DefaultGradleLauncherActionExecuter executer) {
        this.executer = executer;
    }

    public Object executeBuild(Build build) {
        Properties originalSystemProperties = new Properties();
        originalSystemProperties.putAll(System.getProperties());
        Properties clientSystemProperties = new Properties();
        clientSystemProperties.putAll(build.getParameters().getSystemProperties());
        System.setProperties(clientSystemProperties);

        LenientEnvHacker envHacker = new LenientEnvHacker();
        Map<String, String> originalEnv = System.getenv();
        envHacker.setenv(build.getParameters().getEnvVariables());

        //TODO SF I want explicit coverage for this feature
        envHacker.setProcessDir(build.getParameters().getCurrentDir().getAbsolutePath());

        try {
            return executer.execute(build.getAction(), build.getParameters());
        } finally {
            System.setProperties(originalSystemProperties);
            //TODO SF I'm not sure we should set the original env / work dir
            // in theory if character encoding the native code emits doesn't match Java's modified UTF-16
            // we're going to set some rubbish because we used native way to read the env
            envHacker.setenv(originalEnv);
        }
    }
}
