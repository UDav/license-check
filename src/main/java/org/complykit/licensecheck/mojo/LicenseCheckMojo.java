/*
The MIT License (MIT)

Copyright (c) 2013 Michael Rice <me@michaelrice.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package org.complykit.licensecheck.mojo;

import java.util.Iterator;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.complykit.licensecheck.model.Result;

import com.google.gson.Gson;

/**
 * This early version of the license check plug in simply checks to see whether there is a license
 * declared for a given dependency. This is important because, technically, if no license is 
 * associated with a given dependency, that could mean that the owner of the dependency retains
 * all rights associated with the library. Future versions of the license-check will go further.
 * 
 * @author michael rice
 *
 */
@Mojo(name="check")
public class LicenseCheckMojo extends AbstractMojo {

    private Gson gson = new Gson();

    @Component
    private MavenProject project = null;

    @Parameter( defaultValue = "${settings.offline}" )
    private boolean offline;

    /**
     * The validation server, to override, include the following configuration:
     * 
     * <configuration>
     *     <host>http://localhost:8081/validate.php?id=</host>
     * </configuration>
     * 
     */
    @Parameter( property = "check.host", defaultValue = "http://complykit.org/api/license-check/" )
    private String host;

    /**
     * Because this is very much preview, the point of this is to give me a way to notify
     * you when your library is available on the server.
     * 
     * <configuration>
     *     <notificationEmail>me@michaelrice.com</notificationEmail>
     * </configuration>
     * 
     */
    @Parameter( property = "check.notificationEmail" )
    private String notificationEmail;

    /**
     * This is the primary entry point into the Maven plugin.
     */
    @SuppressWarnings("rawtypes")
    public void execute() throws MojoExecutionException, MojoFailureException {

        getLog().info("------------------------------------------------------------------------");
        getLog().info("VALIDATING LICENSES                                                     ");
        getLog().info("------------------------------------------------------------------------");
        if (offline) {
            getLog().info("currently offline, skipping this step");
        } else {
            getLog().info("This plugin will validate that the artifacts you're using have a license file. When the plugin recognizes that an artifact is one of the Open Source Initiative (OSI) approved licenses, it will give you the URL for the license. This plugin and its author are not associated with the OSI.");
            Set artifacts = project.getDependencyArtifacts();
            getLog().info("Found "+artifacts.size()+" artifacts");
            Iterator it=artifacts.iterator();
            while (it.hasNext()) {
                Artifact artifact = (Artifact)it.next();
                String artifactKey = artifact.getGroupId()+":"+artifact.getArtifactId()+":"+artifact.getBaseVersion();
                getLog().info(artifactKey + "...");
                if (!runOnlineArtifactCheck(artifactKey)) {
                    throw new MojoFailureException("could not validate license for artifact "+artifactKey);
                }
            }
        }

    }

    /**
     * This method actually runs the artifact's coordinates against a validation server.
     * Right now, all the logic is buried into this big function.
     * 
     * @param dependencyCoordinate
     * @return
     */
    public boolean runOnlineArtifactCheck(String dependencyCoordinate) {

        boolean result = false;
        HttpClient client = new DefaultHttpClient();
        try {

            String url = host+dependencyCoordinate;
            if (notificationEmail!=null) {
                url += "?notify="+notificationEmail;
            }
            HttpGet get = new HttpGet(url);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = client.execute(get, responseHandler);

            Result serverResult = gson.fromJson(responseBody, Result.class);
            if ("ok".equals(serverResult.getLicensedDeclared())) {
                result = true;
                String msg = "..."+serverResult.getLicensedDeclared()+": "+serverResult.getLicense();
                getLog().info(msg);
            } else {
                result = false;
                String msg = "..."+serverResult.getLicensedDeclared()+": ";
                if (serverResult.getLicense()==null||serverResult.getLicense().length()==0)
                    msg+=": NO LICENSE FOUND";
                else
                    msg+=": "+serverResult.getLicense();
                getLog().error(msg);
            }

        } catch (Exception e) {
            getLog().error(e);
        } finally {
            client.getConnectionManager().shutdown();
        }

        return result;
    }

}
