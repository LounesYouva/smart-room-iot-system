package IoT.smartroom.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_readings")
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;

    private Double temperature;

    private Double humidity;

    private Double lightLevel;

    private Boolean rfidAuthorized;

    private LocalDateTime createdAt;

    public SensorReading() {
        this.createdAt = LocalDateTime.now();
    }

    // getters
    public Long getId() { return id; }

    public String getDeviceId() { return deviceId; }

    public Double getTemperature() { return temperature; }

    public Double getHumidity() { return humidity; }

    public Double getLightLevel() { return lightLevel; }

    public Boolean getRfidAuthorized() { return rfidAuthorized; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    // setters
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public void setHumidity(Double humidity) { this.humidity = humidity; }

    public void setLightLevel(Double lightLevel) { this.lightLevel = lightLevel; }

    public void setRfidAuthorized(Boolean rfidAuthorized) { this.rfidAuthorized = rfidAuthorized; }
}

