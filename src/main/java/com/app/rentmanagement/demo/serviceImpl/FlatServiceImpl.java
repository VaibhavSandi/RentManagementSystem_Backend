package com.app.rentmanagement.demo.serviceImpl;

import com.app.rentmanagement.demo.dto.FlatDto;
import com.app.rentmanagement.demo.entity.Flat;
import com.app.rentmanagement.demo.exception.ResourceNotFoundException;
import com.app.rentmanagement.demo.mapper.FlatMapper;
import com.app.rentmanagement.demo.repository.FlatRepository;
import com.app.rentmanagement.demo.repository.RenterRepository;
import com.app.rentmanagement.demo.service.FlatService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FlatServiceImpl implements FlatService {

    private final FlatRepository flatRepository;

    private final RenterRepository renterRepository;

    @Override
    public FlatDto createFlat(FlatDto flatdto) {
        Flat flat = FlatMapper.toEntity(flatdto);
        Flat savedFlat = flatRepository.save(flat);
        return FlatMapper.toDto(savedFlat);
    }

    @Override
    public FlatDto getFlatById(Long flatId) {
        Flat flat = flatRepository.findById(flatId).orElseThrow(
                () -> new ResourceNotFoundException("Flat", "flatId", flatId)
        );
        return FlatMapper.toDto(flat);
    }

    @Override
    public List<FlatDto> getAllFlats() {
        return flatRepository.findAll()
                .stream()
                .map(FlatMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public FlatDto updateFlat(Long flatId, FlatDto flatDto) {
        Flat flat = flatRepository.findById(flatId).orElseThrow(
                () -> new ResourceNotFoundException("Flat", "flatId", flatId)
        );

        flat.setFlatNo(flatDto.getFlatNo());
        flat.setBuildingName(flatDto.getBuildingName());
        flat.setMeterNo(flatDto.getMeterNo());
        flat.setMonthlyRent(flatDto.getMonthlyRent());
        flat.setDepositAmount(flatDto.getDepositAmount());
        flat.setStatus(flatDto.getStatus());

        Flat updatedFlat = flatRepository.save(flat);
        return FlatMapper.toDto(updatedFlat);
    }

    @Override
    public void deleteFlat(Long flatId) {
        Flat flat = flatRepository.findById(flatId).orElseThrow(
                () -> new ResourceNotFoundException("Flat", "flatId", flatId)
        );

        if (renterRepository.existsByFlat_FlatId(flatId)) {

            System.out.println("Can not delete flat");
            throw new RuntimeException("Cannot delete flat because renter is assigned to this flat");
        }


        flatRepository.delete(flat);
    }
}