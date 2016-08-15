package fr.tunaki.stackoverflow.burnaki.db.repository;

import org.springframework.data.repository.CrudRepository;

import fr.tunaki.stackoverflow.burnaki.db.entities.Burnination;

public interface BurninationRepository extends CrudRepository<Burnination, Long> {

}
