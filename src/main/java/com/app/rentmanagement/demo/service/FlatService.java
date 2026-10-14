package com.app.rentmanagement.demo.service;

import com.app.rentmanagement.demo.dto.FlatDto;
import jakarta.validation.Valid;

import java.util.List;

public interface FlatService {
    FlatDto createFlat(FlatDto flatDto);

    FlatDto getFlatById(Long flatId);

    List<FlatDto> getAllFlats();

    FlatDto updateFlat(Long flatId, FlatDto flatDto);

    void deleteFlat(Long flatId);

}
