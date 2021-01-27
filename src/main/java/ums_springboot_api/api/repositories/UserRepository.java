package ums_springboot_api.api.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import ums_springboot_api.api.Pojos.UserModel;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<UserModel, String> {

    @Query("{username : ?0}")
    UserModel findByUsername(String username);

    @Query("{email : ?0}")
    UserModel findByEmail(String email);

    @Query("{phoneNumber : ?0}")
    UserModel findByPhoneNumber(String phoneNumber);

    UserModel findByIdNumber(String idNumber);

     Optional<UserModel> findByIdOrUsernameOrIdNumber(String id);

    List<UserModel> findAllByOrderByCreatedAtDesc();
}
