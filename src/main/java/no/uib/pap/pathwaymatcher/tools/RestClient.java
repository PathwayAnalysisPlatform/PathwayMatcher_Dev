/*
 * Copyright 2017 Luis Francisco Hernández Sánchez.
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
package no.uib.pap.pathwaymatcher.tools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import org.apache.http.client.utils.URIBuilder;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class RestClient {

    public static void main(String[] args) throws Exception {
        String server = "http://rest.ensembl.org";
        String ext = "/vep/human/id";
        URI url = new URIBuilder(server + ext).addParameter("uniprot", "true").build();
        //URL url = new URL(server + ext);

        URLConnection connection = url.toURL().openConnection();
        HttpURLConnection httpConnection = (HttpURLConnection) connection;

        String postBody = "{ \"ids\" : [\"rs56116432\", \"rs699\", \"rs144678492\" ] }";
        httpConnection.setRequestMethod("POST");
        httpConnection.setRequestProperty("Content-Type", "application/json");
        httpConnection.setRequestProperty("Accept", "application/json");
        httpConnection.setRequestProperty("Content-Length", Integer.toString(postBody.getBytes().length));
        httpConnection.setUseCaches(false);
        httpConnection.setDoInput(true);
        httpConnection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream());
        wr.writeBytes(postBody);
        wr.flush();
        wr.close();

        InputStream response = connection.getInputStream();
        int responseCode = httpConnection.getResponseCode();

        if (responseCode != 200) {
            throw new RuntimeException("Response code was not 200. Detected response was " + responseCode);
        }

        String output;
        Reader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            output = builder.toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException logOrIgnore) {
                    logOrIgnore.printStackTrace();
                }
            }
        }

        System.out.println(output);
    }
}
