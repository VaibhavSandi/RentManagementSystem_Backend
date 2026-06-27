package com.app.rentmanagement.demo.mapper;

import com.app.rentmanagement.demo.dto.RentRevisionDto;
import com.app.rentmanagement.demo.entity.RentRevision;

public class RentRevisionMapper {

    public static RentRevisionDto toDto(RentRevision entity) {
        if (entity == null) {
            return null;
        }

        return RentRevisionDto.builder()
                .revisionId(entity.getRevisionId())
                .renterId(entity.getRenter() != null ? entity.getRenter().getRenterId() : null)
                .effectiveDate(entity.getEffectiveDate())
                .rentAmount(entity.getRentAmount())
                .build();
    }
}
