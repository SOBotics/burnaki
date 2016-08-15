The purpose of this project is to monitor burnination efforts on the Stack Exchange network. It tracks the progress by watching for close votes / reopen votes / delete votes / edits made to questions on the burninated tag.

It also involves a chat bot that posts notification in dedicated chat rooms.

##Chat bot

###`@burnaki start tag [tag] [roomId] [link to Meta]`

This command starts the burnination process of the given tag. All questions with that tag will be queried from SE API and stored in memory. The bot will send notification to the configured chat rooms about actions made to those questions.

Optional: post automatically a community-wiki answer with a predefined-template?

###`@burnaki get progress [tag]`

This command outputs the current progress of the burnination: number of opened questions / number of closed questions / number of questions with close-votes / number of questions with delete-votes.

###`@burnaki update progress [tag]`

Optional: update automatically the community-wiki answer posted when the burnination started?

###`@burnaki stop tag [tag]`

This command ends the burnination process of the given tag. No new notifications will be sent.

##Notifications

During a burnination process, notifications will be sent to the configured chat rooms:

 - When a question has been closed. The goal is to ensure the question gets attraction for a potential inappropriate close.
 - When a reopen vote is cast on a closed question. This means that the question was potentially wrongly closed during the effort and needs to be re-reviewed.
 - When an edit is made to a question. This helps to track which questions were edited by whom and potentially warn wrong edits or users on an edit-spree. Focus only on tag edits removing the burning tag?
 - When a delete vote is cast on a closed question.
 - When an undelete vote is cast on a deleted question. This means that the questions was potentially wrongly deleted and needs another review.
 - When a new question is posted in the tags currently in burnination.

Notifications will be sent by batches every 5 (?) minutes.

##Web interface

Print nice progress graphs?

##Database schema

 - Burnination(Id, Tag, StartDate, EndDate, MetaLink, RoomId) - This table holds meta-data for the tag burnination itself. The roomId corresponds to the chat room being the head-quarters of the burnination effort.
 - BurninationQuestion(#Burnination, QuestionId, CreatedDate, CloseVoteCount, ReopenVoteCount, DeleteVoteCount, UndeleteVoteCount, Closed, Roombad, ManuallyDeleted, Retagged) - This table is a snapshot of the state of all the questions at the current date and serves as base for notifications.
 - BurninationQuestionHistory(#BurninationQuestion, EventDate, EventType) - This table holds the history for a given question in a tag under burnination. The events corresponds to closed / retagged / deleted and so on.
 - BurninationProgress(#Burnination, ProgressDate, TotalQuestions, Closed, Roombad, ManuallyDeleted, Retagged) - This table holds the progress made at a given date. It will be updated automatically based on the current state of BurninationQuestion.
