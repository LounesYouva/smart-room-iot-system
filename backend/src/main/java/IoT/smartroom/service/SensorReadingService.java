package IoT.smartroom.service;

import IoT.smartroom.entity.SensorReading;
import IoT.smartroom.repository.SensorReadingRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class SensorReadingService{

    private final SensorReadingRepository sensorReadingRepository ;
    public SensorReadingService(SensorReadingRepository sensorReadingRepository){
        this.sensorReadingRepository = sensorReadingRepository ;
    }
    public void saveReading(SensorReading data){
        this.sensorReadingRepository.save(data);
    }
    public List <SensorReading> getAllReadings(){
       return this.sensorReadingRepository.findAll();
    }
    
    public List <SensorReading> getAllReadingsByDeviceId(String deviceId){
       return this.sensorReadingRepository.findByDeviceId(deviceId);
    }
    public Optional< SensorReading> getLatestReadingByDeviceId(String deviceId){
        return this.sensorReadingRepository.findTopByDeviceIdOrderByCreatedAtDesc(deviceId) ;

    }
    public Optional< SensorReading> getLatestReading(){
        return this.sensorReadingRepository.findTopByOrderByCreatedAtDesc() ;

    }

}