package IoT.smartroom.controller;
import IoT.smartroom.entity.SensorReading;
import IoT.smartroom.service.SensorReadingService;

import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("api/readings")
public class SensorReadingController {
    private final SensorReadingService sensorReadingService ;

    public SensorReadingController (SensorReadingService sensorReadingService) {
        this.sensorReadingService = sensorReadingService ;
    }

    @GetMapping
    public List <SensorReading> getAllReadings(){
       return sensorReadingService.getAllReadings();
    }

    @GetMapping("/latest")
    public Optional <SensorReading> getLatestReading(){
        return sensorReadingService.getLatestReading();
    }

  @GetMapping("/device/{deviceId}")
  public Optional<SensorReading> getLatestReadingByDeviceId(@PathVariable String deviceId){
    return sensorReadingService.getLatestReadingByDeviceId(deviceId);
}

    @PostMapping
    public void saveReading(@RequestBody SensorReading data){
        sensorReadingService.saveReading(data);
    }


}