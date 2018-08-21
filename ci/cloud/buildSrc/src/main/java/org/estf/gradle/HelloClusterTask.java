package org.estf.gradle;

import co.elastic.cloud.api.builder.ApiClientBuilder;
import co.elastic.cloud.api.builder.SaaSAuthenticationRequestBuilder;
import co.elastic.cloud.api.client.SaaSAuthenticationApi;
import co.elastic.cloud.api.model.generated.CreateElasticsearchClusterRequest;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.swagger.client.ApiClient;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import co.elastic.cloud.api.client.ClusterClient;

import java.io.InputStream;
import java.io.InputStreamReader;

public class HelloClusterTask extends DefaultTask {

    private String stackVersion = "5.6.0";
    private String user = "stack.test.team";
    private String pass = "R7m6Nv9wPKpgaP9LiEA6LkIsgkFXhYgeK3Qfc36YrJY=";
    private String url = "https://admin.staging.foundit.no/api/v0.1";

    @TaskAction
    public void run() {
        ApiClient authApiClient = new ApiClientBuilder()
                .setBasePath(url)
                .build();
        SaaSAuthenticationApi saaSAuthenticationApi = new SaaSAuthenticationApi(authApiClient);
        String token = saaSAuthenticationApi.login(new SaaSAuthenticationRequestBuilder()
                .setUsername(user)
                .setPassword(pass)
                .build()).getToken();
        token = "Bearer " + token;
        ApiClient authenticatedApiClient = new ApiClientBuilder()
                .setBasePath(url + "/v1-regions/us-east-1")
                .setApiKey(token).build();
        authenticatedApiClient.setDebugging(true);
        ClusterClient clusterClient = new ClusterClient(authenticatedApiClient);
        String clusterId = clusterClient.createEsCluster(getFromJson()).getElasticsearchClusterId();
        clusterClient.deleteEsCluster(clusterId);
    }

    private CreateElasticsearchClusterRequest getFromJson() {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("legacyPlan.json");
        JsonReader jsonReader = new JsonReader(new InputStreamReader(in));
        CreateElasticsearchClusterRequest jsonRequest =
                new Gson().fromJson(jsonReader, CreateElasticsearchClusterRequest.class);
        jsonRequest.getPlan().getElasticsearch().setVersion(stackVersion);
        return jsonRequest;
    }
}