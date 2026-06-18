package com.app.rentmanagement.demo.mapper;


import com.app.rentmanagement.demo.dto.RenterDto;
import com.app.rentmanagement.demo.entity.Flat;
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
                .build();
    }

    public static Renter toEntity(RenterDto dto, Flat flat) {
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
                .build();
    }
}