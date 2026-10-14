package com.app.rentmanagement.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class ParkingDetailsDto {

  
    private Long parkingId;
  
    private Integer parkingNumber;

      private boolean isOccupied;

   
   
    private Long renterId;

    private String renterName;


    private Long flatId;

    private String flatNo;


   
}
