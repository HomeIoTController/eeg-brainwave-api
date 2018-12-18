package app.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;


// This will be AUTO IMPLEMENTED by Spring into a Bean called userStateRepository
// CRUD refers Create, Read, Update, Delete

public interface UserStateRepository extends CrudRepository<UserState, Integer> {
    @Query(value="SELECT * FROM UserState WHERE userId = ?1",nativeQuery=true)
    ArrayList<UserState> findByUserId(Integer userId);

    @Query(value="DELETE FROM UserState WHERE userId = ?1",nativeQuery=true)
    Boolean deleteByUserId(Integer userId);
}
