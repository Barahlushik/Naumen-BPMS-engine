package ru.naumen.bpms.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.naumen.bpms.model.User;

import java.util.Optional;

@RepositoryRestResource(path = "users", collectionResourceRel = "users")
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameAndActiveTrue(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
