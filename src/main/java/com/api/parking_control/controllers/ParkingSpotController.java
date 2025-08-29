package com.api.parking_control.controllers;

import com.api.parking_control.dtos.ParkingSpotdtos;
import com.api.parking_control.models.ParkingSpotModel;
import com.api.parking_control.services.ParkingSpotService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }
    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotdtos parkingSpotdtos){
        if(parkingSpotService.existsByLicensePlateCar(parkingSpotdtos.getLicensePlateCar())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Esta placa já está em nosso sistema.");
        }if (parkingSpotService.existsByParkingSpotNumber(parkingSpotdtos.getParkingSpotNumber())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Essa vaga já está sendo usada.");
        }if (parkingSpotService.existsByApartamentAndBlock(parkingSpotdtos.getApartament(), parkingSpotdtos.getBlock())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Uma vaga já está registrada neste apartamento/bloco.");
        }

        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotdtos, parkingSpotModel);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
    }
    @GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> gettAllParkingSpots(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneParkingSpot(@PathVariable (value = "id")UUID id) {
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if (!parkingSpotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vaga não encontrada.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if (!parkingSpotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vaga não encontrada.");
        }
        parkingSpotService.delete(parkingSpotModelOptional.get());
        return  ResponseEntity.status(HttpStatus.OK).body("A vaga foi deletada");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id,
                                                    @RequestBody @Valid ParkingSpotdtos parkingSpotdtos){
    Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
    if (!parkingSpotModelOptional.isPresent()){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("VAGA não encontrada.");
    }
//    ParkingSpotModel parkingSpotModel = parkingSpotModelOptional.get();
//    parkingSpotModel.setParkingSpotNumber(parkingSpotdtos.getParkingSpotNumber());
//    parkingSpotModel.setLicensePlateCar(parkingSpotdtos.getLicensePlateCar());
//    parkingSpotModel.setModelCar(parkingSpotModel.getModelCar());
//    parkingSpotModel.setBrandCar(parkingSpotdtos.getBrandCar());
//    parkingSpotModel.setColorCar(parkingSpotModel.getColorCar());
//    parkingSpotModel.setResponsibleName(parkingSpotdtos.getResponsibleName());
//    parkingSpotModel.setApartament(parkingSpotdtos.getApartament());
//    parkingSpotModel.setBlock(parkingSpotModel.getBlock());

    ParkingSpotModel parkingSpotModel = new ParkingSpotModel();
    BeanUtils.copyProperties(parkingSpotdtos, parkingSpotModel);
    parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
    parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());
    return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
    }
}
