package com.diegomorales.warehouse.service;

import com.diegomorales.warehouse.domain.ServiceDomain;
import com.diegomorales.warehouse.domain.ServiceWarehouse;
import com.diegomorales.warehouse.domain.ServiceWarehouseId;
import com.diegomorales.warehouse.exception.BadRequestException;
import com.diegomorales.warehouse.exception.GenericException;
import com.diegomorales.warehouse.repository.ServiceRepository;
import com.diegomorales.warehouse.repository.ServiceWarehouseRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class ServiceWarehouseService {

    private ServiceRepository serviceRepository;

    private ServiceWarehouseRepository repository;

    public void saveAllById(List<Integer> extraServices, Integer idWarehouse) throws GenericException, BadRequestException{
        try {

            for (Integer idService : extraServices) {
                Optional<ServiceDomain> validService = this.serviceRepository.findById(idService);

                if (validService.isEmpty()) {
                    throw new BadRequestException("The service does not exist");
                }

                var serviceWarehouseId = new ServiceWarehouseId();
                serviceWarehouseId.setId_service(idService);
                serviceWarehouseId.setId_warehouse(idWarehouse);

                var entity = new ServiceWarehouse(serviceWarehouseId);

                this.repository.save(entity);

            }

        }catch (BadRequestException e){
            throw e;
        } catch (Exception e) {
            log.error("Processing error", e);
            throw new GenericException("Error processing request");
        }
    }

    public List<Integer> findAllServicesById(List<String> nameServices) throws BadRequestException, GenericException{
        try {

            List<Integer> ids = new ArrayList<>();

            for(String nameService : nameServices){
                Optional<ServiceDomain> valid = this.serviceRepository.findFirstByNameContainsIgnoreCase(nameService);
                if(valid.isEmpty()){
                    throw new BadRequestException("The service " + nameService + " does not exist");
                }

                ids.add(valid.get().getId());
            }

            return ids;

        }catch (BadRequestException e){
            throw e;
        } catch (Exception e) {
            log.error("Processing error", e);
            throw new GenericException("Error processing request");
        }
    }

    public void deleteAllServicesByWarehouse(List<Integer> idsServices, Integer idWarehouse){

        for(Integer idService : idsServices){

            ServiceWarehouse serviceWarehouse = new ServiceWarehouse();
            serviceWarehouse.setServiceWarehouseId(
                    new ServiceWarehouseId(idWarehouse, idService)
            );

            this.repository.delete(serviceWarehouse);

        }

    }

}
