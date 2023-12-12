package com.diegomorales.warehouse.service;

import com.diegomorales.warehouse.domain.UserDomain;
import com.diegomorales.warehouse.dto.UserDTO;
import com.diegomorales.warehouse.exception.*;
import com.diegomorales.warehouse.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final RoleService roleService;
    private final UserRoleService userRoleService;

    public UserDomain save(UserDTO dto) throws GenericException, BadRequestException {
        try {
            Optional<UserDomain> validEmail = this.repository.findFirstByEmailContainsIgnoreCase(dto.getEmail());
            if (validEmail.isPresent()) {
                throw new BadRequestException("The user with this email already exists.");
            }

            VerifyUserName_Roles(dto);

            return savingProcess(dto);

        }catch (BadRequestException e){
            throw e;
        }catch (Exception e){
            logger.error("Processing error", e);
            throw new GenericException("Error processing request");
        }
    }

    public UserDomain saveWithDefaultRole(UserDTO dto) throws GenericException, BadRequestException{
        try {
            Optional<UserDomain> valid = this.repository.findFirstByEmailContainsIgnoreCase(dto.getEmail());
            if (valid.isPresent()) {
                throw new BadRequestException("The user with this email already exists");
            }

            Optional<UserDomain> validUserName = this.repository.findFirstByUsernameContainsIgnoreCase(dto.getUsername());
            if (validUserName.isPresent()) {
                throw new BadRequestException("The user with this userName already exists.");
            }

            dto.setRoles(List.of("USER"));

            return savingProcess(dto);


        }catch (BadRequestException e){
            throw e;
        }catch (Exception e){
            logger.error("Processing error", e);
            throw new GenericException("Error processing request");
        }
    }

    public UserDTO findOne(Integer id) throws GenericException, BadRequestException{
        try {

            Optional<UserDomain> valid = this.repository.findById(id);
            if (valid.isEmpty()) {
                throw new BadRequestException("The user does not exists");
            }

            var dto = new UserDTO();
            BeanUtils.copyProperties(valid.get(), dto);

            List<String> roles = this.userRoleService.findAllRolesByUser(id);

            dto.setRoles(roles);

            return dto;
        }catch (BadRequestException e){
            throw e;
        }catch (Exception e){
            logger.error("Processing error", e);
            throw new GenericException("Error processing request");
        }
    }

    public void update(Integer id, UserDTO dto) throws GenericException, BadRequestException{
        try {

            Optional<UserDomain> valid = this.repository.findById(id);
            if (valid.isEmpty()) {
                throw new BadRequestException("The user does not exists");
            }

            VerifyUserName_Roles(dto);

            BeanUtils.copyProperties(dto, valid.get(), "id");
            valid.get().setPassword(encryptPassword(valid.get().getPassword()));

            roleComparison(id, dto.getRoles());
            this.repository.save(valid.get());


        }catch (BadRequestException e){
            throw e;
        }catch (Exception e){
            logger.error("Processing error", e);
            throw new GenericException("Error processing request");
        }
    }

    public UserDomain delete(Integer id) throws GenericException, BadRequestException {
        try {
            Optional<UserDomain> valid = this.repository.findById(id);
            if (valid.isEmpty()) {
                throw new BadRequestException("The user does not exists");
            }

            this.userRoleService.deleteByUser(id);
            this.repository.delete(valid.get());

            return valid.get();
        }catch (BadRequestException e){
            throw e;
        }catch (Exception e){
            logger.error("Processing error", e);
            throw new GenericException("Error processing request");
        }
    }

    public Page<UserDomain> findAll(String search, Pageable page) throws GenericException, NoContentException{
        try {
            Page<UserDomain> response;
            if (search != null && !search.isEmpty()) {
                response = this.repository.findAllByUsernameContainsIgnoreCase(search, page);
            } else {
                response = this.repository.findAll(page);
            }
            if (response.isEmpty()) {
                throw new NoContentException("No records found");
            }

            return response;
        }catch (NoContentException e){
            throw e;
        }catch (Exception e){
            logger.error("Processing error", e);
            throw new GenericException("Error processing request");
        }
    }


    //Repeated process in two functions
    private void VerifyUserName_Roles(UserDTO dto) throws BadRequestException {
        Optional<UserDomain> validUserName = this.repository.findFirstByUsernameContainsIgnoreCase(dto.getUsername());
        if (validUserName.isPresent()) {
            throw new BadRequestException("The user with this userName already exists.");
        }

        List<String> nonExistentRoles = this.roleService.incorrectRoles(dto.getRoles());
        if (!nonExistentRoles.isEmpty()) {
            throw new BadRequestException("The role(s): " + nonExistentRoles + ", do not exist");
        }
    }

    private UserDomain savingProcess(UserDTO dto){
        var entity = new UserDomain();
        BeanUtils.copyProperties(dto, entity);
        entity.setPassword(encryptPassword(entity.getPassword()));
        entity.setId(null);
        var userSaved = this.repository.save(entity);

        dto.getRoles().forEach(
                _role -> {
                    try {
                        this.userRoleService.save(userSaved.getId(), _role);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        return userSaved;
    }

    /**
     * Encrypt the password with BCrypt
     * @param password String to encode
     * @return Coded String
     */
    public String encryptPassword(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Method for Matching Incoming Role Changes
     * @param id id of the user
     * @param newRoles List of the Roles in the DTO
     * @throws Exception Exception
     */
    public void roleComparison(Integer id, List<String> newRoles) throws Exception{
        try{
            //List of saved roles
            List<String> oldRoles = this.userRoleService.findAllRolesByUser(id);

            Iterator<String> iterator = newRoles.iterator();
            while (iterator.hasNext()) {
                String _role = iterator.next();
                var index = oldRoles.indexOf(_role);
                if (index != -1) {
                    iterator.remove();
                    oldRoles.remove(_role);
                }
            }

            if(!oldRoles.isEmpty()){
                this.userRoleService.deleteAllRolesByName(id, oldRoles);
            }
            if(!newRoles.isEmpty()){
                this.userRoleService.addAllRolesByName(id, newRoles);
            }
        }catch (Exception e){
            logger.error("Processing error", e);
            throw new GenericException("Error processing request");
        }

    }

}
