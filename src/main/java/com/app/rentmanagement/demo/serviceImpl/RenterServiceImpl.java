package com.app.rentmanagement.demo.serviceImpl;




import com.app.rentmanagement.demo.dto.RenterDto;
import com.app.rentmanagement.demo.entity.Flat;
import com.app.rentmanagement.demo.entity.ParkingDetails;
import com.app.rentmanagement.demo.entity.Renter;
import com.app.rentmanagement.demo.exception.BadRequestException;
import com.app.rentmanagement.demo.exception.ResourceNotFoundException;
import com.app.rentmanagement.demo.mapper.RenterMapper;
import com.app.rentmanagement.demo.repository.FlatRepository;
import com.app.rentmanagement.demo.repository.ParkingRepository;
import com.app.rentmanagement.demo.repository.RentPaymentRepository;
import com.app.rentmanagement.demo.repository.RentRevisionRepository;
import com.app.rentmanagement.demo.repository.RenterRepository;
import com.app.rentmanagement.demo.entity.RentRevision;
import com.app.rentmanagement.demo.service.RenterService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RenterServiceImpl implements RenterService {

    private final RenterRepository renterRepository;
    private final FlatRepository flatRepository;
    private final ParkingRepository parkingRepository;
    private final RentPaymentRepository rentPaymentRepository;
    private final RentRevisionRepository rentRevisionRepository;

    
public RenterDto createRenter(RenterDto renterDto) {

    Flat flat = flatRepository.findById(renterDto.getFlatId())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Flat", "flatId", renterDto.getFlatId()));

    if (renterDto.getMeterNo() != null && !renterDto.getMeterNo().trim().isEmpty()) {
        flat.setMeterNo(renterDto.getMeterNo());
        flatRepository.save(flat);
    }

    ParkingDetails parkingDetails = null;

    System.out.println("parking id"+renterDto.getParkingId());

    // Parking is optional
    if (renterDto.getParkingId() != null && renterDto.getParkingId() != 0) {

        parkingDetails = parkingRepository.findById(renterDto.getParkingId())
                .orElse(null);

        if (parkingDetails != null) {
            parkingDetails.setOccupied(true);
            parkingDetails.setFlat(flat);
            parkingDetails.setRenter(RenterMapper.toEntity(renterDto, flat, parkingDetails));
            parkingRepository.save(parkingDetails);
        }
    }

    Renter renter = RenterMapper.toEntity(renterDto, flat, parkingDetails);

    Renter savedRenter = renterRepository.save(renter);

    // Save initial rent revision
    RentRevision revision = RentRevision.builder()
            .renter(savedRenter)
            .effectiveDate(savedRenter.getJoiningDate())
            .rentAmount(savedRenter.getMonthlyRent())
            .build();
    rentRevisionRepository.save(revision);

    return RenterMapper.toDto(savedRenter);
}

    @Override
    public RenterDto getRenterById(Long renterId) {

        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new ResourceNotFoundException("Renter", "renterId", renterId));

        return RenterMapper.toDto(renter);
    }

    @Override
    public List<RenterDto> getAllRenters() {

        return renterRepository.findAll()
                .stream()
                .map(RenterMapper::toDto)
                .toList();
    }
@Transactional
@Override
public RenterDto updateRenter(Long renterId, RenterDto renterDto) {

    Renter renter = renterRepository.findById(renterId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Renter", "renterId", renterId));

    Flat flat = flatRepository.findById(renterDto.getFlatId())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Flat", "flatId", renterDto.getFlatId()));

    if (renterDto.getMeterNo() != null && !renterDto.getMeterNo().trim().isEmpty()) {
        flat.setMeterNo(renterDto.getMeterNo());
        flatRepository.save(flat);
    }

    // old parking free
    if (renter.getParkingDetails() != null) {
        ParkingDetails oldParking = renter.getParkingDetails();
        oldParking.setOccupied(false);
        oldParking.setRenter(null);
        oldParking.setFlat(null);
        parkingRepository.save(oldParking);
    }

    ParkingDetails newParking = null;

    if (renterDto.getParkingId() != null && renterDto.getParkingId() != 0) {
        newParking = parkingRepository.findById(renterDto.getParkingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ParkingDetails", "parkingId", renterDto.getParkingId()));

        newParking.setOccupied(true);
        newParking.setFlat(flat);
        newParking.setRenter(renter);
     ParkingDetails   savedParking =parkingRepository.save(newParking);

     System.out.println("Parkingupdated"+savedParking.toString());
    }

    java.math.BigDecimal oldRent = renter.getMonthlyRent();
    
    renter.setRenterName(renterDto.getRenterName());
    renter.setMobileNumber(renterDto.getMobileNumber());
    renter.setIdProofNo(renterDto.getIdProofNo());
    renter.setFlat(flat);
    renter.setParkingDetails(newParking);
    renter.setJoiningDate(renterDto.getJoiningDate());
    renter.setMonthlyRent(renterDto.getMonthlyRent());
    renter.setDepositPaid(renterDto.getDepositPaid());
    renter.setStatus(renterDto.getStatus());

    Renter updatedRenter = renterRepository.save(renter);

    // Check if rent changed, and log revision
    if (oldRent != null && renterDto.getMonthlyRent() != null && oldRent.compareTo(renterDto.getMonthlyRent()) != 0) {
        RentRevision revision = RentRevision.builder()
                .renter(updatedRenter)
                .effectiveDate(java.time.LocalDate.now())
                .rentAmount(renterDto.getMonthlyRent())
                .build();
        rentRevisionRepository.save(revision);
    }

    return RenterMapper.toDto(updatedRenter);
}

  @Override
@Transactional
public void deleteRenter(Long renterId) {

    Renter renter = renterRepository.findById(renterId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Renter", "renterId", renterId));

    if (renter.getParkingDetails() != null) {

        ParkingDetails parkingDetails = renter.getParkingDetails();

        parkingDetails.setOccupied(false);
        parkingDetails.setRenter(null);
        parkingDetails.setFlat(null);

        parkingRepository.save(parkingDetails);
    }

    if (renter.getFlat() != null) {

        Flat flat = renter.getFlat();

        flat.setStatus("Vacant");

        flatRepository.save(flat);
    }

    rentPaymentRepository.deleteByRenterId(renterId);

    // Delete revision records first
    rentRevisionRepository.deleteByRenterId(renterId);

    renterRepository.delete(renter);
}
    @Override
    public List<RenterDto> getAllActivateRenters() {
            // TODO Auto-generated method stub
            return renterRepository.findActivateRentersByStatus("Active").stream().map(RenterMapper::toDto).toList();
    }

    @Override
    public List<com.app.rentmanagement.demo.dto.RentRevisionDto> getRentRevisions(Long renterId) {
        return rentRevisionRepository.findByRenterRenterIdOrderByEffectiveDateAsc(renterId)
                .stream()
                .map(com.app.rentmanagement.demo.mapper.RentRevisionMapper::toDto)
                .toList();
    }

    @Override
    public com.app.rentmanagement.demo.dto.RentRevisionDto addRentRevision(Long renterId, com.app.rentmanagement.demo.dto.RentRevisionDto dto) {
        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new ResourceNotFoundException("Renter", "renterId", renterId));
        
        RentRevision revision = RentRevision.builder()
                .renter(renter)
                .effectiveDate(dto.getEffectiveDate())
                .rentAmount(dto.getRentAmount())
                .build();
        
        RentRevision saved = rentRevisionRepository.save(revision);
        return com.app.rentmanagement.demo.mapper.RentRevisionMapper.toDto(saved);
    }
}