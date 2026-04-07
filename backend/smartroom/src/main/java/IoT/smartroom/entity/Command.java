package IoT.smartroom.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Command {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;

    private String command;

    @Enumerated(EnumType.STRING)
    private CommandType type;

    @Enumerated(EnumType.STRING)
    private CommandStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    public Command() {
        this.createdAt = LocalDateTime.now();
        this.status = CommandStatus.PENDING;
        this.type = CommandType.ACTION;
    }

    public Long getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public CommandStatus getStatus() {
        return status;
    }

    public void setStatus(CommandStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}