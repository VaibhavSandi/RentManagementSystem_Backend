package com.app.rentmanagement.demo.serviceImpl;




import com.app.rentmanagement.demo.dto.RenterDto;
import com.app.rentmanagement.demo.entity.Flat;
import com.app.rentmanagement.demo.entity.Renter;
import com.app.rentmanagement.demo.exception.ResourceNotFoundException;
import com.app.rentmanagement.demo.mapper.RenterMapper;
import com.app.rentmanagement.demo.repository.FlatRepository;
import com.app.rentmanagement.demo.repository.RentPaymentRepository;
import com.app.rentmanagement.demo.repository.RenterRepository;
import com.app.rentmanagement.demo.service.RenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RenterServiceImpl implements RenterService {

    private final RenterRepository renterRepository;
    private final FlatRepository flatRepository;


    private final RentPaymentRepository rentPaymentRepository;

    @Override
    public RenterDto createRenter(RenterDto renterDto) {

        Flat flat = flatRepository.findById(renterDto.getFlatId())
                .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatId", renterDto.getFlatId()));

        Renter renter = RenterMapper.toEntity(renterDto, flat);
        Renter savedRenter = renterRepository.save(renter);

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

    @Override
    public RenterDto updateRenter(Long renterId, RenterDto renterDto) {

        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new ResourceNotFoundException("Renter", "renterId", renterId));

        Flat flat = flatRepository.findById(renterDto.getFlatId())
                .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatId", renterDto.getFlatId()));

        renter.setRenterName(renterDto.getRenterName());
        renter.setMobileNumber(renterDto.getMobileNumber());
        renter.setIdProofNo(renterDto.getIdProofNo());
        renter.setFlat(flat);
        renter.setJoiningDate(renterDto.getJoiningDate());
        renter.setMonthlyRent(renterDto.getMonthlyRent());
        renter.setDepositPaid(renterDto.getDepositPaid());
        renter.setStatus(renterDto.getStatus());

        Renter updatedRenter = renterRepository.save(renter);

        return RenterMapper.toDto(updatedRenter);
    }

    @Override
    public void deleteRenter(Long renterId) {

        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new ResourceNotFoundException("Renter", "renterId", renterId));


                Flat flat = flatRepository.findById(renter.getFlat().getFlatId())
                        .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatId", renter.getFlat().getFlatId()));



                        flat.setStatus("Vacant");
                        flatRepository.save(flat);

                rentPaymentRepository.deleteByRenterId(renterId);




        renterRepository.delete(renter);
    }

    @Override
    public List<RenterDto> getAllActivateRenters() {
            // TODO Auto-generated method stub
            return renterRepository.findActivateRentersByStatus("Active").stream().map(RenterMapper::toDto).toList();
    }
}