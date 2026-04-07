package com.smartroom.smartroomapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloController {


    @FXML
    private Label statusLabel;

    @FXML
    private Label connectionLabel;
    @FXML
    private Label HumidityLabel;

    @FXML
    private Label TemeperatureLabel;


    @FXML
    protected void getData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            while (true) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://192.168.124.90:8080/api/readings/latest"))
                            .GET()
                            .build();

                    HttpResponse<String> response =
                            client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        SensorRead data = mapper.readValue(response.body(), SensorRead.class);

                        Platform.runLater(() -> {
                            TemeperatureLabel.setText("Température : " + data.getTemperature() + " °C");
                            HumidityLabel.setText("Humidité : " + data.getHumidity() + " %");
                            connectionLabel.setText("Données mises à jour à "+LocalDateTime.now());
                        });
                    } else {
                        Platform.runLater(() -> {
                            connectionLabel.setText("Erreur serveur : " + response.statusCode());
                        });
                    }

                    Thread.sleep(3000);

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        connectionLabel.setText("Erreur connexion backend");
                    });
                    e.printStackTrace();

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
    @FXML
    protected void handleLightOn() {
        statusLabel.setText("État lumière : ALLUMÉE");
        connectionLabel.setText("Envoi de la commande...");

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();

                String json = """
                    {
                      "deviceId": "light-1",
                      "command": "LIGHT_ON",
                      "type": "ACTION"
                    }
                    """;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://192.168.124.90:8080/api/commands")) //
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();




                HttpResponse<String> response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());


                Platform.runLater(() -> {
                    if (response.statusCode() == 201) {
                        connectionLabel.setText("Commande ON envoyée avec succès");
                    } else {
                        connectionLabel.setText("Erreur serveur : " + response.statusCode());
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    connectionLabel.setText("Erreur connexion backend");
                });
                e.printStackTrace();
            }
        }).start();
    }
    @FXML
    protected void handleLightOff() {
        statusLabel.setText("État lumière : ÉTEINTE");
        connectionLabel.setText("Envoi de la commande...");

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();

                String json = """
                        {
                          "deviceId": "light-1",
                          "command": "LIGHT_OFF",
                          "type": "ACTION"
                        }
                        """;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/commands"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() -> {
                    if (response.statusCode() == 201) {
                        connectionLabel.setText("Commande OFF envoyée avec succès");
                    } else {
                        connectionLabel.setText("Erreur serveur : " + response.statusCode());
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    connectionLabel.setText("Erreur de connexion au backend");
                });
                e.printStackTrace();
            }
        }).start();
    }


    public void initialize(){
        getData();
    }

}