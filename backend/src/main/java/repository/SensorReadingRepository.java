package IoT.smartroom.repository;

import IoT.smartroom.entity.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    
    List <SensorReading> findByDeviceId(String deviceId);
    Optional <SensorReading> findTopByOrderByCreatedAtDesc();
    Optional < SensorReading> findTopByDeviceIdOrderByCreatedAtDesc(String deviceId);

}



