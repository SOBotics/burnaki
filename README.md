The purpose of this project is to monitor burnination efforts on the Stack Exchange network. It tracks the progress by watching for close votes / reopen votes / delete votes / edits made to questions on the burninated tag.

It also involves a chat bot that posts notification in dedicated chat rooms.

##Chat bot

###`@burnaki start tag [tag] [link to Meta] [rooms]...`

This command starts the burnination process of the given tag. All questions with that tag will be queried from SE API and stored in memory. The bot will send notification to the configured chat rooms about actions made to those questions.

Optional: post automatically a community-wiki answer with a predefined-template?

###`@burnaki get progress [tag]`

This command outputs the current progress of the burnination: number of opened questions / number of closed questions / number of questions with close-votes / number of questions with delete-votes.

###`@burnaki update progress [tag]`

Optional: update automatically the community-wiki answer posted when the burnination started?

###`@burnaki stop tag [tag]`

This command end the burnination process of the tag given tag. No new notifications will be sent.

##Notifications

During a burnination process, notifications will be sent to the configured chat rooms:

 - When a question has been closed. The goal is to ensure the question gets attraction for a potential inappropriate close.
 - When a reopen vote is cast on a closed question. This means that the question was potentially wrongly closed during the effort and needs to be re-reviewed.
 - When an edit is made to a question. This helps to track which questions were edited by whom and potentially warn wrong edits or users on an edit-spree. Focus only on tag edits removing the burning tag?
 - When a delete vote is cast on a closed question.
 - When a new question is posted in the tags currently in burnination.

Notifications will be sent by batches every 5 (?) minutes.

##Web interface

Print nice progress graphs?

##Database schema

 - Burnination(Tag, StartDate, EndDate, MetaLink, Rooms)
 - BurninationQuestions(#Burnination, QuestionId, CloseVoteCount, ReopenVoteCount, DeleteVoteCount) - This table is a snapshot of all the questions at the start of the burnination and serves as base for notifications.
 - BurninationProgress(#Burnination, DateTime, TotalQuestions, Open, Retagged, ClosedNotDeleted, Roombad, ManuallyDeleted)
