package IoT.smartroom.repository;

import IoT.smartroom.entity.Command;
import IoT.smartroom.entity.CommandStatus;
import IoT.smartroom.entity.CommandType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommandRepository extends JpaRepository<Command, Long> {

    List<Command> findByDeviceId(String deviceId);

    List<Command> findByStatus(CommandStatus status);

    List<Command> findByCommand(String command);

    List<Command> findByType(CommandType type);

    List<Command> findByDeviceIdAndStatus(String deviceId, CommandStatus status);

    List<Command> findByStatusAndType(CommandStatus status, CommandType type);

    Optional<Command> findTopByOrderByCreatedAtDesc();

    Optional<Command> findTopByDeviceIdOrderByCreatedAtDesc(String deviceId);
}