package IoT.smartroom.service;

import IoT.smartroom.entity.Command;
import IoT.smartroom.entity.CommandStatus;
import IoT.smartroom.entity.CommandType;
import IoT.smartroom.repository.CommandRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommandService {

    private final CommandRepository commandRepository;

    public CommandService(CommandRepository commandRepository) {
        this.commandRepository = commandRepository;
    }

    public Command saveCommand(Command command) {
        if (command.getStatus() == null) {
            command.setStatus(CommandStatus.PENDING);
        }

        if (command.getType() == null) {
            command.setType(resolveType(command.getCommand()));
        }

        if (command.getType() == CommandType.STATE && command.getDeviceId() != null) {
            List<Command> pendingCommands =
                    commandRepository.findByDeviceIdAndStatus(command.getDeviceId(), CommandStatus.PENDING);

            for (Command oldCommand : pendingCommands) {
                if (oldCommand.getType() == CommandType.STATE) {
                    oldCommand.setStatus(CommandStatus.SUPERSEDED);
                    commandRepository.save(oldCommand);
                }
            }
        }

        return commandRepository.save(command);
    }

    public List<Command> getAllCommands() {
        return commandRepository.findAll();
    }

    public Optional<Command> getCommandById(Long id) {
        return commandRepository.findById(id);
    }

    public List<Command> getCommandsByDeviceId(String deviceId) {
        return commandRepository.findByDeviceId(deviceId);
    }

    public List<Command> getCommandsByStatus(CommandStatus status) {
        return commandRepository.findByStatus(status);
    }

    public List<Command> getCommandsByType(CommandType type) {
        return commandRepository.findByType(type);
    }

    public List<Command> getCommandsByDeviceIdAndStatus(String deviceId, CommandStatus status) {
        return commandRepository.findByDeviceIdAndStatus(deviceId, status);
    }

    public List<Command> getCommandsByStatusAndType(CommandStatus status, CommandType type) {
        return commandRepository.findByStatusAndType(status, type);
    }

    public Optional<Command> getLatestCommand() {
        return commandRepository.findTopByOrderByCreatedAtDesc();
    }

    public Optional<Command> getLatestCommandByDeviceId(String deviceId) {
        return commandRepository.findTopByDeviceIdOrderByCreatedAtDesc(deviceId);
    }

    public Optional<Command> updateCommandStatus(Long id, CommandStatus status) {
        Optional<Command> existingCommand = commandRepository.findById(id);

        if (existingCommand.isPresent()) {
            Command command = existingCommand.get();
            command.setStatus(status);
            return Optional.of(commandRepository.save(command));
        }

        return Optional.empty();
    }

    public void deleteCommand(Long id) {
        commandRepository.deleteById(id);
    }

    private CommandType resolveType(String command) {
        if (command == null || command.isBlank()) {
            return CommandType.ACTION;
        }

        String normalized = command.toUpperCase();

        if (normalized.contains("ON") || normalized.contains("OFF")) {
            return CommandType.STATE;
        }

        return CommandType.ACTION;
    }
}