package IoT.smartroom.controller;

import IoT.smartroom.entity.Command;
import IoT.smartroom.entity.CommandStatus;
import IoT.smartroom.entity.CommandType;
import IoT.smartroom.service.CommandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commands")
@CrossOrigin(origins = "*")
public class CommandController {

    private final CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    public ResponseEntity<Command> createCommand(@RequestBody Command command) {
        Command savedCommand = commandService.saveCommand(command);
        return new ResponseEntity<>(savedCommand, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Command>> getAllCommands() {
        return ResponseEntity.ok(commandService.getAllCommands());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Command> getCommandById(@PathVariable Long id) {
        return commandService.getCommandById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/latest")
    public ResponseEntity<Command> getLatestCommand() {
        return commandService.getLatestCommand()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<Command>> getCommandsByDeviceId(@PathVariable String deviceId) {
        return ResponseEntity.ok(commandService.getCommandsByDeviceId(deviceId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Command>> getCommandsByStatus(@PathVariable CommandStatus status) {
        return ResponseEntity.ok(commandService.getCommandsByStatus(status));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Command>> getCommandsByType(@PathVariable CommandType type) {
        return ResponseEntity.ok(commandService.getCommandsByType(type));
    }

    @GetMapping("/device/{deviceId}/status/{status}")
    public ResponseEntity<List<Command>> getCommandsByDeviceIdAndStatus(
            @PathVariable String deviceId,
            @PathVariable CommandStatus status
    ) {
        return ResponseEntity.ok(commandService.getCommandsByDeviceIdAndStatus(deviceId, status));
    }

    @GetMapping("/status/{status}/type/{type}")
    public ResponseEntity<List<Command>> getCommandsByStatusAndType(
            @PathVariable CommandStatus status,
            @PathVariable CommandType type
    ) {
        return ResponseEntity.ok(commandService.getCommandsByStatusAndType(status, type));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Command> updateCommandStatus(
            @PathVariable Long id,
            @RequestParam CommandStatus value
    ) {
        return commandService.updateCommandStatus(id, value)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommand(@PathVariable Long id) {
        commandService.deleteCommand(id);
        return ResponseEntity.noContent().build();
    }
}