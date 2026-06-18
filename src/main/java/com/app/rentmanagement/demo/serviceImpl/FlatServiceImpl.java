package com.app.rentmanagement.demo.serviceImpl;

import com.app.rentmanagement.demo.dto.FlatDto;
import com.app.rentmanagement.demo.entity.Flat;
import com.app.rentmanagement.demo.exception.ResourceNotFoundException;
import com.app.rentmanagement.demo.mapper.FlatMapper;
import com.app.rentmanagement.demo.repository.FlatRepository;
import com.app.rentmanagement.demo.service.FlatService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FlatServiceImpl implements FlatService {

    private final FlatRepository flatRepository;
    private final FlatMapper flatMapper;
    @Override
    public FlatDto createFlat(FlatDto flatdto) {

        Flat  flat =FlatMapper.toEntity(flatdto);

       Flat savedFlat= flatRepository.save(flat);

       return FlatMapper.toDto(savedFlat);

    }

    @Override
    public FlatDto getFlatById(Long flatId) {
       Flat flat= flatRepository.findById(flatId).orElseThrow(
                ()-> new ResourceNotFoundException(
                        "Flat",
                        "flatId",
                        flatId
                ));
       return FlatMapper.toDto(flat);
    }

    @Override
    public List<FlatDto> getAllFlats() {


        return flatRepository.findAll()
                .stream()
                .map(FlatMapper::toDto)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public FlatDto updateFlat(Long flatId, FlatDto flatDto) {

        Flat flat=flatRepository.findById(flatId).orElseThrow(
                ()->new ResourceNotFoundException("Flat","FlatId",flatId));

        flat.setFlatNo(flatDto.getFlatNo());
        flat.setBuildingName(flatDto.getBuildingName());
        flat.setMonthlyRent(flatDto.getMonthlyRent());
        flatDto.setDepositAmount(flatDto.getDepositAmount());
        flatDto.setStatus(flatDto.getStatus());

        Flat updateFlat=flatRepository.save(flat);
        return FlatMapper.toDto(updateFlat);
    }

    @Override
    public void deleteFlat(Long flatId) {


        flatRepository.findById(flatId).orElseThrow(()->new ResourceNotFoundException("Flat","flatId",flatId));

        flatRepository.deleteById(flatId);

    }
}
