package app.model;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

// This will be AUTO IMPLEMENTED by Spring into a Bean called eegDataRepository
// CRUD refers Create, Read, Update, Delete

public interface EEGDataRepository extends CrudRepository<EEGData, Integer> {
    @Query("SELECT DISTINCT userId, state FROM EEGData WHERE state != '?' AND (deleted IS NULL OR deleted = 0)")
    ArrayList<Object> findDistinctUserIdsAndStates();

    @Modifying
    @Query("UPDATE EEGData SET deleted = true WHERE userId = ?1 AND state IN ?2 AND (deleted IS NULL OR deleted = 0)")
    @Transactional
    Integer deleteStatesIn(Integer userId, Iterable<String> states);
}