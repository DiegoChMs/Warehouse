package com.diegomorales.warehouse.service;

import com.diegomorales.warehouse.domain.*;
import com.diegomorales.warehouse.dto.LeaseDTO;
import com.diegomorales.warehouse.exception.BadRequestException;
import com.diegomorales.warehouse.exception.GenericException;
import com.diegomorales.warehouse.repository.LeaseRepository;
import com.diegomorales.warehouse.repository.UserRepository;
import com.diegomorales.warehouse.repository.WarehouseRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class LeaseService {
    private final LeaseRepository repository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    private final ServiceLeaseService serviceLeaseService;
    private final ServiceDomainService serviceDomainService;

    public Lease save(LeaseDTO dto) throws GenericException, BadRequestException {
        try {

            Warehouse warehouse = warehouseValid(dto.getId_warehouse());

            userValid(dto.getId_user());

            Optional<Lease> validRecord = this.repository.findFirstByIdUserAndIdWarehouse(dto.getId_user(), dto.getId_warehouse());

            if (validRecord.isPresent()) {
                throw new BadRequestException("The user already occupies this warehouse.");
            }

            Lease entity = new Lease();
            BeanUtils.copyProperties(dto, entity, "id", "total");
            entity.setTotal(warehouse.getPrice());

            Lease saved = this.repository.save(entity);

            if (!dto.getExtraServices().isEmpty()) {

                List<Integer> extraServices = this.serviceDomainService.findServicesByName(dto.getExtraServices());

                this.serviceLeaseService.saveAll(extraServices, saved.getId());
            }

            return saved;

        }catch (BadRequestException e){
            throw e;
        } catch (Exception e) {
            log.error("Processing error", e);
            throw new GenericException("Error processing request");
        }
    }

    public LeaseDTO findOne(Integer id) throws GenericException, BadRequestException{
        try {

            Optional<Lease> valid = this.repository.findById(id);
            if ( valid.isEmpty() ) {
                throw new BadRequestException("The lease with this id does not exist");
            }

            LeaseDTO dto = new LeaseDTO();
            BeanUtils.copyProperties(valid.get(), dto);

            List<ServiceDomain> extraServices = this.serviceLeaseService.findServicesByIdLease(id);

            List<String> extraServicesNames = extraServices.stream().map(
                    ServiceDomain::getName
            ).toList();

            dto.setExtraServices(extraServicesNames);

            return dto;

        }catch (BadRequestException e){
            throw e;
        } catch (Exception e) {
            log.error("Processing error", e);
            throw new GenericException("Error processing request");
        }
    }

    public void delete(Integer id) throws GenericException, BadRequestException{
        try {

            Optional<Lease> valid = this.repository.findById(id);
            if(valid.isEmpty()){
                throw new BadRequestException("The lease with the id " + id + " does not exist");
            }

            List<ServiceDomain> serviceDomains = this.serviceLeaseService.findServicesByIdLease(id);
            if ( !serviceDomains.isEmpty() ) {

                for ( ServiceDomain serviceDomain : serviceDomains ){

                        ServiceLeaseId serviceLeaseId = new ServiceLeaseId();
                        serviceLeaseId.setId_service(serviceDomain.getId());
                        serviceLeaseId.setId_lease(id);

                        this.serviceLeaseService.delete(serviceLeaseId);

                }

            }

            this.repository.delete(valid.get());

        }catch (BadRequestException e){
            throw e;
        } catch (Exception e) {
            log.error("Processing error", e);
            throw new GenericException("Error processing request");
        }
    }

    //Check warehouse
    private Warehouse warehouseValid(Integer id) throws GenericException, BadRequestException{
        try {

            Optional<Warehouse> availableWarehouse = this.warehouseRepository.findById(id);

            if(availableWarehouse.isEmpty()){
                throw new BadRequestException("The warehouse does not exist");
            }

            if (!availableWarehouse.get().getAvailable()) {
                throw new BadRequestException("The warehouse is not available.");
            }

            return availableWarehouse.get();

        }catch (BadRequestException e){
            throw e;
        } catch (Exception e) {
            log.error("Processing error", e);
            throw new GenericException("Error processing request");
        }
    }

    private void userValid(Integer id) throws GenericException, BadRequestException{
        try {

            Optional<UserDomain> valid = this.userRepository.findById(id);
            if (valid.isEmpty()) {
                throw new BadRequestException("The user does not exist");
            }
        }catch (BadRequestException e){
            throw e;
        } catch (Exception e) {
            log.error("Processing error", e);
            throw new GenericException("Error processing request");
        }
    }

}