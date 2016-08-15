package fr.tunaki.stackoverflow.burnaki.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	
	public List<BurninationUpdateEvent> update(String tag, int refreshEvery) {
		Burnination burnination = getCurrentBurninationForTag(tag);
		LOGGER.debug("Updating the burnination of tag [{}]", tag);
		List<BurninationUpdateEvent> events = new ArrayList<>();
		Map<Integer, BurninationQuestion> presentMap = burnination.getQuestions().stream().collect(Collectors.toMap(bq -> bq.getId().getQuestionId(), Function.identity()));
		// update questions in the current burn list
		for (Question question : apiService.getQuestionsWithIds(presentMap.keySet())) {
			BurninationQuestion burninationQuestion = presentMap.get(question.getId());
			events.addAll(populateBurninationQuestion(question, burninationQuestion, tag));
		}
		// add to burn list new questions posted in burn tag since last refresh
		for (Question question : apiService.getQuestionsInTag(tag, Instant.now().minus(refreshEvery, ChronoUnit.MINUTES)).stream().filter(q -> !presentMap.containsKey(q.getId())).collect(Collectors.toList())) {
			LOGGER.debug("New question with id {} asked in tag under burnination [{}]", question.getId(), tag);
			BurninationQuestion burninationQuestion = new BurninationQuestion(burnination, question.getId());
			burnination.addQuestion(burninationQuestion);
			events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.NEW, tag, question));
			populateBurninationQuestion(question, burninationQuestion, tag);
		}
		repository.save(burnination);
		return events;
	}
	
	public void updateProgress(String tag) {
		Burnination burnination = getCurrentBurninationForTag(tag);
		BurninationProgress burninationProgress = burnination.getProgress();
		if (burninationProgress == null) {
			burninationProgress = new BurninationProgress(burnination, Instant.now());
			burnination.setProgress(burninationProgress);
		}
		int closed = 0, manuallyDeleted = 0, retagged = 0, roombad = 0;
		for (BurninationQuestion question : burnination.getQuestions()) {
			if (question.isClosed()) closed++;
			if (question.isManuallyDeleted()) manuallyDeleted++;
			if (question.isRetagged()) retagged++;
			if (question.isRoombad()) roombad++;
		}
		burninationProgress.setClosed(closed);
		burninationProgress.setManuallyDeleted(manuallyDeleted);
		burninationProgress.setRetagged(retagged);
		burninationProgress.setRoombad(roombad);
		burninationProgress.setTotalQuestions(burnination.getQuestions().size());
		repository.save(burnination);
	}
	
	public BurninationProgress getProgress(String tag) {
		return getCurrentBurninationForTag(tag).getProgress();
	}

	public void stop(String tag) {
		Burnination burnination = getCurrentBurninationForTag(tag);
		LOGGER.info("Stopping the burnination of tag [{}]", tag);
		burnination.setEndDate(Instant.now());
		repository.save(burnination);
	}
	
	public List<String> getTagsInBurnination() {
		return repository.findByEndDateNull().map(Burnination::getTag).collect(Collectors.toList());
	}
	
	public Map<Integer, String> getBurnRooms() {
		return repository.findByEndDateNull().collect(Collectors.toMap(Burnination::getRoomId, Burnination::getTag));
	}

	private List<BurninationUpdateEvent> populateBurninationQuestion(Question question, BurninationQuestion burninationQuestion, String tag) {
		List<BurninationUpdateEvent> events = new ArrayList<>();
		burninationQuestion.setCreatedDate(question.getCreatedDate());
		
		if (question.getClosedDate() != null) {
			if (!burninationQuestion.isClosed()) {
				burninationQuestion.addHistory(new BurninationQuestionHistory(burninationQuestion, "CLOSED", question.getClosedDate()));
				events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.CLOSED, tag, question));
			}
			burninationQuestion.setClosed(true);
		} else {
			if (burninationQuestion.isClosed()) {
				burninationQuestion.addHistory(new BurninationQuestionHistory(burninationQuestion, "REOPENED", Instant.now()));
			}
			burninationQuestion.setClosed(false);
		}
		
		burninationQuestion.setCloseVoteCount(question.getCloseVoteCount());
		if (question.getReopenVoteCount() == 1 && burninationQuestion.getReopenVoteCount() == 0) {
			events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.REOPEN_VOTE, tag, question));
		}
		
		burninationQuestion.setReopenVoteCount(question.getReopenVoteCount());
		burninationQuestion.setDeleteVoteCount(question.getDeleteVoteCount());
		
		if (question.getUndeleteVoteCount() == 1 && burninationQuestion.getUndeleteVoteCount() == 0) {
			events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.UNDELETE_VOTE, tag, question));
		}
		burninationQuestion.setUndeleteVoteCount(question.getUndeleteVoteCount());
		
		if (question.hasTag(tag)) {
			if (burninationQuestion.isRetagged()) {
				burninationQuestion.addHistory(new BurninationQuestionHistory(burninationQuestion, "RETAGGED WITH", Instant.now()));
			}
			burninationQuestion.setRetagged(false);
		} else {
			if (!burninationQuestion.isRetagged()) {
				burninationQuestion.addHistory(new BurninationQuestionHistory(burninationQuestion, "RETAGGED WITHOUT", Instant.now()));
				events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.RETAGGED_WITHOUT, tag, question));
			}
			burninationQuestion.setRetagged(true);
		}
		
		if (question.getDeletedDate() != null) {
			if (question.isRoombad()) {
				if (!burninationQuestion.isRoombad()) {
					burninationQuestion.addHistory(new BurninationQuestionHistory(burninationQuestion, "ROOMBAD", Instant.now()));
					events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.DELETED, tag, question));
				}
				burninationQuestion.setRoombad(true);
				burninationQuestion.setManuallyDeleted(false);
			} else {
				if (!burninationQuestion.isManuallyDeleted()) {
					burninationQuestion.addHistory(new BurninationQuestionHistory(burninationQuestion, "MANUALLY DELETED", Instant.now()));
					events.add(new BurninationUpdateEvent(BurninationUpdateEvent.Event.DELETED, tag, question));
				}
				burninationQuestion.setRoombad(false);
				burninationQuestion.setManuallyDeleted(true);
			}
		} else {
			if (burninationQuestion.isManuallyDeleted() || burninationQuestion.isRoombad()) {
				burninationQuestion.addHistory(new BurninationQuestionHistory(burninationQuestion, "UNDELETED", Instant.now()));
			}
			burninationQuestion.setManuallyDeleted(false);
			burninationQuestion.setRoombad(false);
		}
		return events;
	}

	private Burnination getCurrentBurninationForTag(String tag) {
		Objects.requireNonNull(tag, "A tag must be given");
		return repository.findByTagAndEndDateNull(tag).orElseThrow(() -> new BurnakiException("No burnination of " + tag + " is on-going."));
	}

}
