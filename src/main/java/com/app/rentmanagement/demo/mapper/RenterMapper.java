package com.app.rentmanagement.demo.mapper;


import com.app.rentmanagement.demo.dto.RenterDto;
import com.app.rentmanagement.demo.entity.Flat;
import com.app.rentmanagement.demo.entity.ParkingDetails;
import com.app.rentmanagement.demo.entity.Renter;

public class RenterMapper {

    public static RenterDto toDto(Renter renter) {
        if (renter == null) return null;

        return RenterDto.builder()
                .renterId(renter.getRenterId())
                .renterName(renter.getRenterName())
                .mobileNumber(renter.getMobileNumber())
                .idProofNo(renter.getIdProofNo())
                .flatId(renter.getFlat() != null ? renter.getFlat().getFlatId() : null)
                .flatNo(renter.getFlat() != null ? renter.getFlat().getFlatNo() : null)
                .joiningDate(renter.getJoiningDate())
                .monthlyRent(renter.getMonthlyRent())
                .depositPaid(renter.getDepositPaid())
                .status(renter.getStatus())
                .parkingId(renter.getParkingDetails() != null ? renter.getParkingDetails().getParkingId() : null)
                .parkingNumber(renter.getParkingDetails() != null ? renter.getParkingDetails().getParkingNumber() : null)
                .isOccupied(renter.getParkingDetails() != null ? renter.getParkingDetails().isOccupied() : false)
                .build();
    }

    public static Renter toEntity(RenterDto dto, Flat flat, ParkingDetails parkingDetails) {
        if (dto == null) return null;

        return Renter.builder()
                .renterId(dto.getRenterId())
                .renterName(dto.getRenterName())
                .mobileNumber(dto.getMobileNumber())
                .idProofNo(dto.getIdProofNo())
                .flat(flat)
                .joiningDate(dto.getJoiningDate())
                .monthlyRent(dto.getMonthlyRent())
                .depositPaid(dto.getDepositPaid())
                .status(dto.getStatus())
                .parkingDetails(parkingDetails)
                .build();
    }
}