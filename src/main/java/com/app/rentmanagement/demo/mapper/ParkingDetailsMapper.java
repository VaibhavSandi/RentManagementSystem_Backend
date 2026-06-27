package com.app.rentmanagement.demo.mapper;

import com.app.rentmanagement.demo.dto.ParkingDetailsDto;
import com.app.rentmanagement.demo.entity.ParkingDetails;

public class ParkingDetailsMapper {


    public static ParkingDetailsDto toDto(ParkingDetails parkingDetails) {
        if (parkingDetails == null) return null;

        ParkingDetailsDto dto = new ParkingDetailsDto();
        dto.setParkingId(parkingDetails.getParkingId());
        dto.setParkingNumber(parkingDetails.getParkingNumber());
        dto.setOccupied(parkingDetails.isOccupied());

        if (parkingDetails.getRenter() != null) {
            dto.setRenterId(parkingDetails.getRenter().getRenterId());
            dto.setRenterName(parkingDetails.getRenter().getRenterName());
        }

        if (parkingDetails.getFlat() != null) {
            dto.setFlatId(parkingDetails.getFlat().getFlatId());
            dto.setFlatNo(parkingDetails.getFlat().getFlatNo());
        }

        return dto;
    }

    public static ParkingDetails toEntity(ParkingDetailsDto dto) {
        if (dto == null) return null;

        ParkingDetails parkingDetails = new ParkingDetails();
        parkingDetails.setParkingId(dto.getParkingId());
        parkingDetails.setParkingNumber(dto.getParkingNumber());
        parkingDetails.setOccupied(dto.isOccupied());

        return parkingDetails;
    }
}
