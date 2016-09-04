package fr.tunaki.stackoverflow.burnaki.service;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tunaki.stackoverflow.burnaki.BurnakiException;
import fr.tunaki.stackoverflow.burnaki.api.Question;
import fr.tunaki.stackoverflow.burnaki.api.StackExchangeAPIService;
import fr.tunaki.stackoverflow.burnaki.entity.Burnination;
import fr.tunaki.stackoverflow.burnaki.entity.BurninationProgress;
import fr.tunaki.stackoverflow.burnaki.entity.BurninationQuestion;
import fr.tunaki.stackoverflow.burnaki.entity.BurninationQuestionHistory;
import fr.tunaki.stackoverflow.burnaki.repository.BurninationRepository;

@Service
@Transactional
public class BurninationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BurninationService.class);

	private BurninationRepository repository;
	private StackExchangeAPIService apiService;

	@Autowired
	public BurninationService(BurninationRepository repository, StackExchangeAPIService apiService) {
		this.repository = repository;
		this.apiService = apiService;
	}

	public int start(String tag, int roomId, String metaLink) {
		Objects.requireNonNull(tag, "A tag must be given");
		Objects.requireNonNull(metaLink, "A link to a Meta post must be given");
		LOGGER.info("Starting the burnination of tag [{}]", tag);
		if (repository.findByTagAndEndDateNull(tag).isPresent()) {
			throw new BurnakiException("A burnination of " + tag + " is already on-going.");
		}
		Burnination burnination = new Burnination();
		burnination.setTag(tag);
		burnination.setStartDate(Instant.now());
		burnination.setRoomId(roomId);
		burnination.setMetaLink(metaLink);
		for (Question question : apiService.getQuestionsInTag(tag, null)) {
			BurninationQuestion burninationQuestion = new BurninationQuestion(burnination, question.getId());
			burnination.addQuestion(burninationQuestion);
			populateBurninationQuestion(question, burninationQuestion, tag);
		}
		int size = burnination.getQuestions().size();
		repository.save(burnination);
		return size;
	}

	public List<BurninationUpdateEvent> update(String tag) {
		Burnination burnination = getCurrentBurninationForTag(tag);
		LOGGER.debug("Updating the burnination of tag [{}]", tag);
		List<BurninationUpdateEvent> events = new ArrayList<>();
		Map<Integer, BurninationQuestion> presentMap = burnination.getQuestions().stream().collect(toMap(bq -> bq.getId().getQuestionId(), Function.identity()));
		Set<Integer> keySet = presentMap.keySet();
		List<Question> results = apiService.getQuestionsWithIds(keySet);

		// identify deleted questions not returned by API (those for which the ID was given but nothing was returned)
		Set<Integer> returnedIds = results.stream().map(t -> t.getId()).collect(toSet());
		Map<Integer, BurninationQuestion> deltas = new HashMap<>(presentMap);
		deltas.keySet().removeAll(returnedIds);
		for (BurninationQuestion bq : deltas.values()) {
			if (bq.getDeletedDate() == null) {
				bq.setDeletedDate(Instant.now());
				if (bq.wasProbablyRoombad()) {
					bq.addHistory(new BurninationQuestionHistory(bq, "ROOMBAD", bq.getDeletedDate()));
					events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.DELETED, tag, bq));
					bq.setRoombad(true);
					bq.setManuallyDeleted(false);
				} else {
					bq.addHistory(new BurninationQuestionHistory(bq, "MANUALLY DELETED", bq.getDeletedDate()));
					events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.DELETED, tag, bq));
					bq.setRoombad(false);
					bq.setManuallyDeleted(true);
				}
			}
		}

		// update questions in the current burn list
		for (Question question : results) {
			BurninationQuestion burninationQuestion = presentMap.get(question.getId());
			events.addAll(populateBurninationQuestion(question, burninationQuestion, tag));
		}

		// add to burn list new questions posted in burn tag since last refresh (minus 5 minutes to account for the delay of doing all this work)
		Instant from = burnination.getLastRefreshDate() == null ? null : burnination.getLastRefreshDate().minus(5, ChronoUnit.MINUTES);
		for (Question question : apiService.getQuestionsInTag(tag, from).stream().filter(q -> !presentMap.containsKey(q.getId())).collect(toList())) {
			LOGGER.debug("New question with id {} asked in tag under burnination [{}]", question.getId(), tag);
			BurninationQuestion burninationQuestion = new BurninationQuestion(burnination, question.getId());
			burnination.addQuestion(burninationQuestion);
			events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.NEW, tag, burninationQuestion));
			populateBurninationQuestion(question, burninationQuestion, tag);
		}

		burnination.setLastRefreshDate(Instant.now());
		repository.save(burnination);
		return events;
	}

	public void updateProgress(String tag) {
		Burnination burnination = getCurrentBurninationForTag(tag);
		BurninationProgress burninationProgress = new BurninationProgress(burnination, Instant.now());
		burnination.addProgress(burninationProgress);
		int closed = 0, manuallyDeleted = 0, retagged = 0, roombad = 0, openedWithTag = 0;
		for (BurninationQuestion question : burnination.getQuestions()) {
			if (question.getClosedDate() != null) closed++;
			if (question.isManuallyDeleted()) manuallyDeleted++;
			if (question.isRetagged()) retagged++;
			if (question.isRoombad()) roombad++;
			if (question.getDeletedDate() == null && question.getClosedDate() == null && !question.isRetagged()) openedWithTag++;
		}
		burninationProgress.setClosed(closed);
		burninationProgress.setManuallyDeleted(manuallyDeleted);
		burninationProgress.setRetagged(retagged);
		burninationProgress.setRoombad(roombad);
		burninationProgress.setOpenedWithTag(openedWithTag);
		burninationProgress.setTotalQuestions(burnination.getQuestions().size());
		repository.save(burnination);
	}

	public Burnination getBurninationWithProgress(String tag) {
		Burnination burnination = getCurrentBurninationForTag(tag);
		burnination.getProgresses().sort(comparing(e -> e.getId().getProgressDate()));
		return burnination;
	}

	public void stop(String tag) {
		Burnination burnination = getCurrentBurninationForTag(tag);
		LOGGER.info("Stopping the burnination of tag [{}]", tag);
		burnination.setEndDate(Instant.now());
		repository.save(burnination);
	}

	public List<String> getTagsInBurnination() {
		return repository.findByEndDateNull().map(Burnination::getTag).collect(toList());
	}

	public Map<Integer, List<String>> getBurnRooms() {
		return repository.findByEndDateNull().collect(groupingBy(Burnination::getRoomId, mapping(Burnination::getTag, toList())));
	}

	public List<BurninationQuestion> getDeleteCandidates(String tag) {
		Burnination burnination = getCurrentBurninationForTag(tag);
		return burnination.getQuestions().stream().filter(q -> q.getDeletedDate() == null && (
				(q.getScore() <= -1 &&
				q.getClosedDate() != null &&
				DAYS.between(q.getClosedDate(), Instant.now()) >= 2 &&
				q.getAnswerCount() > 0) ||
				q.getDeleteVoteCount() > 0
		)).collect(toList());
	}

	private List<BurninationUpdateEvent> populateBurninationQuestion(Question question, BurninationQuestion burninationQuestion, String tag) {
		List<BurninationUpdateEvent> events = new ArrayList<>();

		if (question.getClosedDate() != null) {
			if (burninationQuestion.getClosedDate() == null) {
				burninationQuestion.addHistory(new BurninationQuestionHistory(burninationQuestion, "CLOSED", question.getClosedDate()));
				events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.CLOSED, tag, burninationQuestion));
			}
		} else {
			if (burninationQuestion.getClosedDate() != null) {
				burninationQuestion.addHistory(new BurninationQuestionHistory(burninationQuestion, "REOPENED", Instant.now()));
			}
		}
		burninationQuestion.setClosedDate(question.getClosedDate());

		burninationQuestion.setCloseVoteCount(question.getCloseVoteCount());
		if (question.getReopenVoteCount() > 0 && burninationQuestion.getReopenVoteCount() == 0) {
			events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.REOPEN_VOTE, tag, burninationQuestion));
		}

		burninationQuestion.setReopenVoteCount(question.getReopenVoteCount());
		burninationQuestion.setDeleteVoteCount(question.getDeleteVoteCount());
		burninationQuestion.setUndeleteVoteCount(question.getUndeleteVoteCount());

		if (question.getTags().contains(tag)) {
			if (burninationQuestion.isRetagged()) {
				burninationQuestion.addHistory(new BurninationQuestionHistory(burninationQuestion, "RETAGGED WITH", question.getLastEditDate()));
			}
			burninationQuestion.setRetagged(false);
		} else {
			if (!burninationQuestion.isRetagged()) {
				burninationQuestion.addHistory(new BurninationQuestionHistory(burninationQuestion, "RETAGGED WITHOUT", question.getLastEditDate()));
				events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.RETAGGED_WITHOUT, tag, burninationQuestion));
			}
			burninationQuestion.setRetagged(true);
		}

		if (burninationQuestion.getDeletedDate() != null) {
			events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.UNDELETED, tag, burninationQuestion));
			burninationQuestion.addHistory(new BurninationQuestionHistory(burninationQuestion, "UNDELETED", Instant.now()));
			burninationQuestion.setDeletedDate(null);
		}
		burninationQuestion.setManuallyDeleted(false);
		burninationQuestion.setRoombad(false);

		burninationQuestion.setTitle(question.getTitle());
		burninationQuestion.setLink(question.getLink());
		burninationQuestion.setShareLink(question.getShareLink());
		burninationQuestion.setScore(question.getScore());
		burninationQuestion.setViewCount(question.getViewCount());
		burninationQuestion.setAnswerCount(question.getAnswerCount());
		burninationQuestion.setCommentCount(question.getCommentCount());
		burninationQuestion.setLocked(question.getLockedDate() != null);
		burninationQuestion.setMigrated(question.isMigrated());
		burninationQuestion.setAnswered(question.isAnswered());
		burninationQuestion.setAcceptedAnswerId(question.getAcceptedAnswerId());
		burninationQuestion.setTags(question.getTags().stream().collect(joining(";")));
		burninationQuestion.setCreatedDate(question.getCreatedDate());
		burninationQuestion.setClosedAsDuplicate("duplicate".equals(question.getClosedReason()));
		burninationQuestion.setLastEditDate(question.getLastEditDate());

		return events;
	}

	private Burnination getCurrentBurninationForTag(String tag) {
		Objects.requireNonNull(tag, "A tag must be given");
		return repository.findByTagAndEndDateNull(tag).orElseThrow(() -> new BurnakiException("No burnination of " + tag + " is on-going."));
	}

}
