package fr.tunaki.stackoverflow.burnaki.repository;

import org.springframework.data.repository.CrudRepository;

import fr.tunaki.stackoverflow.burnaki.entity.SEApiQuestionCache;

public interface SEApiQuestionCacheRepository extends CrudRepository<SEApiQuestionCache, Integer> {
	
}
