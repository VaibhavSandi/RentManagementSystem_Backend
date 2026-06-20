package com.app.rentmanagement.demo.controller;


import com.app.rentmanagement.demo.dto.RenterDto;
import com.app.rentmanagement.demo.service.RenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/renters")
@RequiredArgsConstructor
@Tag(
        name = "CRUD Operation for Renter",
        description = "Create,Update,Delete,Retrieve operation for renter"
)

public class RenterController {

    private final RenterService renterService;


    @Operation(
            summary = "For Saving Renter  info in db",
            description = "REST API for Saving renter Details"
    )
    @ApiResponse(
            responseCode="201",
            description = "HTTP_STATUS_CREATED"
    )
    @ApiResponse(
            responseCode = "404",
            description = "HTTP Status NOT_FOUND"


    )
    @ApiResponse(
            responseCode = "500",
            description = "HTTP Status INTERNAL_SERVER_ERROR"
    )
    @PostMapping("/addrenter")
    public ResponseEntity<RenterDto> createRenter(@RequestBody RenterDto renterDto) {
        return new ResponseEntity<>(renterService.createRenter(renterDto), HttpStatus.CREATED);
    }
    @Operation(
            summary = "Get  Renter Details by id",
            description = "Getting Renter Details by id"

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
    @GetMapping("getrenter/{id}")
    public ResponseEntity<RenterDto> getRenterById(@PathVariable("id") Long renterId) {
        return ResponseEntity.ok(renterService.getRenterById(renterId));
    }
    @Operation(
            summary = "Get All Renter Details",
            description = "Getting Renter Details"

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
    @GetMapping
    public ResponseEntity<?> getAllRenters() {
        return ResponseEntity.ok(renterService.getAllRenters());
    }
    @Operation(
            summary = "update All Renter Details",
            description = "update Renter Details"

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
    @PutMapping("updaterenter/{id}")
    public ResponseEntity<RenterDto> updateRenter(
            @PathVariable("id") Long renterId,
            @RequestBody RenterDto renterDto) {

        return ResponseEntity.ok(renterService.updateRenter(renterId, renterDto));
    }
    @Operation(
            summary = "Delete  Renter Details by renterId",
            description = "delete Renter Details by renterId"

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
    @DeleteMapping("deleterenter/{id}")
    public ResponseEntity<String> deleteRenter(@PathVariable("id") Long renterId) {
        renterService.deleteRenter(renterId);
        return ResponseEntity.ok("Renter deleted successfully");
    }
}