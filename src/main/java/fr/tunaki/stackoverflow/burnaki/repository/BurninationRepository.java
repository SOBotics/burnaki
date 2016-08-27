package fr.tunaki.stackoverflow.burnaki.repository;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.repository.CrudRepository;

import fr.tunaki.stackoverflow.burnaki.entity.Burnination;

public interface BurninationRepository extends CrudRepository<Burnination, Long> {

	/**
	 * Returns the on-going burnination for the given tag, as an <code>Optional</code>.
	 * @param tag Tag to search for.
	 * @return The on-going burnination for the given tag, as an <code>Optional</code>.
	 */
	Optional<Burnination> findByTagAndEndDateNull(String tag);

	/**
	 * Returns the on-going burnination for the given room, as an <code>Optional</code>.
	 * @param roomId Room ID.
	 * @return The on-going burnination for the given room, as an <code>Optional</code>.
	 */
	Optional<Burnination> findByRoomIdAndEndDateNull(int roomId);

	/**
	 * Returns the list of on-going burninations.
	 * @return The list of on-going burninations.
	 */
	Stream<Burnination> findByEndDateNull();

}
