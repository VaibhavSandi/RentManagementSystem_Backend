package com.app.rentmanagement.demo.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.rentmanagement.demo.dto.ParkingDetailsDto;
import com.app.rentmanagement.demo.mapper.FlatMapper;
import com.app.rentmanagement.demo.mapper.ParkingDetailsMapper;
import com.app.rentmanagement.demo.mapper.RenterMapper;
import com.app.rentmanagement.demo.repository.ParkingRepository;
import com.app.rentmanagement.demo.service.ParkingService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ParkingServiceImpl implements ParkingService {
    
    
    private final ParkingRepository parkingRepository;
    
    
    
    
    @Override
    public List<ParkingDetailsDto> getAllParkingDetails() {
        // TODO Auto-generated method stub
      return parkingRepository.findAll()
                .stream()
                .map(ParkingDetailsMapper::toDto)
                .collect(Collectors.toList());  
 }

 public List<ParkingDetailsDto> getAllParkingDetailsbyStatus()
 {
    
     return parkingRepository.findAll()
             .stream()
             .filter(parkingDetails -> !parkingDetails.isOccupied())
             .map(ParkingDetailsMapper::toDto)
             .collect(Collectors.toList());

}


public List<ParkingDetailsDto> getAllOccupiedParkingDetails()
{
    return parkingRepository.findAll()
            .stream()
            .filter(parkingDetails -> parkingDetails.isOccupied())
            .map(ParkingDetailsMapper::toDto)
            .collect(Collectors.toList());


}

public List<ParkingDetailsDto> getParkingDetailsByFlatId(Long flatId) {
    return parkingRepository.findByFlatFlatId( flatId)
            .stream()
            .map(ParkingDetailsMapper::toDto)
            .collect(Collectors.toList());
}


public List<ParkingDetailsDto> getParkingDetailsByRenterId(Long renterId)
{
    return parkingRepository.findByRenterRenterId( renterId).stream().map(ParkingDetailsMapper::toDto).collect(Collectors.toList());
}

   

}
