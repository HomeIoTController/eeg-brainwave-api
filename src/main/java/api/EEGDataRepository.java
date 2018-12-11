package api;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

// This will be AUTO IMPLEMENTED by Spring into a Bean called eegDataRepository
// CRUD refers Create, Read, Update, Delete

public interface EEGDataRepository extends CrudRepository<EEGData, Integer> {
    @Query("SELECT DISTINCT userId FROM EEGData")
    ArrayList<Integer> findDistinctUserIds();
}