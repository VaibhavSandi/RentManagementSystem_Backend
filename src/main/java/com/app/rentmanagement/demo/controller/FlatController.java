package com.app.rentmanagement.demo.controller;

import com.app.rentmanagement.demo.dto.FlatDto;
import com.app.rentmanagement.demo.service.FlatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/flat",produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Tag(

        name = "CRUD Rest API's for Flat Details",
        description = "API for Flat Related Operation i.e create,delete,update,fetch"

)


public class FlatController {


    FlatService flatService;

@Operation(
        summary = "For Saving Flat info in db",
        description = "REST API for Saving flat Details"

)
@ApiResponse(
        responseCode = "201",
        description = "Http Status Created"
)
@PostMapping("/FillFlat")
    private ResponseEntity addFlatDetails(@RequestBody FlatDto flat)
    {
        FlatDto savedflat=flatService.createFlat(flat);

        return new ResponseEntity<>(savedflat, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get Flat Details by Flat Id",
            description = "Getting flat Details By Id"

    )

            @ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
            )
            @ApiResponse(
                    responseCode = "404",
                    description = "HTTP Status NOT_FOUND"


    )
    @ApiResponse(
            responseCode = "500",
            description = "HTTP Status INTERNAL_SERVER_ERROR"
    )


    @GetMapping("/getFlat/{flatId}")
    public ResponseEntity<FlatDto> getFlatById(@PathVariable  long flatId)
    {
        return ResponseEntity.ok(flatService.getFlatById(flatId));
    }


    @Operation(
            summary = "Get All Flat Details",
            description = "Getting flat Details"

    )

    @ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @ApiResponse(
            responseCode = "404",
            description = "HTTP Status NOT_FOUND"


    )
    @ApiResponse(
            responseCode = "500",
            description = "HTTP Status INTERNAL_SERVER_ERROR"
    )
@GetMapping("/getFlatsDetails")
public ResponseEntity<List<FlatDto>> getAllFlatDetails() {
    return ResponseEntity.ok(
            flatService.getAllFlats());
}


    @Operation(
            summary = "Update  Flat Details by Id",
            description = "update flat Details"

    )

    @ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @ApiResponse(
            responseCode = "404",
            description = "HTTP Status NOT_FOUND"


    )
    @ApiResponse(
            responseCode = "500",
            description = "HTTP Status INTERNAL_SERVER_ERROR"
    )
    @PutMapping("update/{id}")
    public ResponseEntity<FlatDto> updateFlat(
            @PathVariable("id") Long flatId,
            @RequestBody FlatDto flatDto) {

        return ResponseEntity.ok(
                flatService.updateFlat(
                        flatId,
                        flatDto));
    }

    @Operation(
            summary = "Delete Flat Details Id",
            description = "Delete flat Details by Id"

    )

    @ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @ApiResponse(
            responseCode = "404",
            description = "HTTP Status NOT_FOUND"


    )
    @ApiResponse(
            responseCode = "500",
            description = "HTTP Status INTERNAL_SERVER_ERROR"
    )
    @DeleteMapping("delete/{id}")
    public ResponseEntity<String> deleteFlat(
            @PathVariable("id") Long flatId) {

        flatService.deleteFlat(flatId);

        return ResponseEntity.ok(
                "Flat deleted successfully");
    }


}
