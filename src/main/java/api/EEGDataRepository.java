package api;

import org.springframework.data.repository.CrudRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called eegDataRepository
// CRUD refers Create, Read, Update, Delete

public interface EEGDataRepository extends CrudRepository<EEGData, Integer> {

}