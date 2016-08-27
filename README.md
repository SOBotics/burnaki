The purpose of this project is to monitor burnination efforts on the Stack Exchange network. It tracks the progress by watching for close votes / reopen votes / delete votes / edits made to questions on the burninated tag.

It also involves a chat bot that posts notification in dedicated chat rooms.

##Chat bot

###`@burnaki start tag [tag] [roomId] [link to Meta]`

This command starts the burnination process of the given tag. All questions with that tag will be queried from SE API and stored in memory. The bot will send notification to the configured chat rooms about actions made to those questions.

Optional: post automatically a community-wiki answer with a predefined-template?

###`@burnaki get progress [tag]`

This command outputs the current progress of the burnination: number of opened questions / number of closed questions / number of retagged questions / number of deleted questions (either manually or by the roomba). An example message is:

> Here's a recap of the efforts so far for [godaddy]: Total questions (1563), Retagged (261), Closed (1213), Roombad (287), Manually deleted (101).

It also prints a graph of the progress:

![Burnination progress](http://i.stack.imgur.com/O3TPr.png)

###`@burnaki update progress [tag]`

The progress is updated automatically every hour. This command forces an update of the progress immediatly.

###`@burnaki delete candidates [tag]`

This command queries all posts that are eligible for deletion. It queries posts that have a score <= -2, are closed since more than 2 days and have at least one answer. It also returns posts with pending delete votes.

![Sample list](http://i.stack.imgur.com/dUlb8.png)

###`@burnaki stop tag [tag]`

This command ends the burnination process of the given tag. No new notifications will be sent.

##Notifications

During a burnination process of a tag, notifications will be sent to the dedicated chat room:

 - When a question has been closed. The goal is to ensure the question gets attraction for a potential inappropriate close.
 - When a reopen vote is cast on a closed question. This means that the question was potentially wrongly closed during the effort and needs to be re-reviewed.
 - When an edit is made to a question removing the tag being burninated. This helps to track which questions were edited and potentially act on wrong edits or users on an edit-spree.
 - When a question is deleted. In the same way as closed questions, the goal is to make sure the question gets another review.
 - When a question is undeleted. This means that the questions was wrongly deleted and needs another review. _NOTE: the Stack Exchange API does not return deleted posts, so it is not possible to raise a notification when an undelete vote is cast_.
 - When a new question is posted in the tags currently in burnination.

Notifications are sent by batches every 5 minutes. An example is:

> Closed: [Codeigniter on Godaddy results in 404 removing index.php](http://stackoverflow.com/q/20163488), [GoDaddy issues with directory (differences with "webroot" and "root")](http://stackoverflow.com/q/20528283), [godaddy shared hosting NICE command path and PHP path](http://stackoverflow.com/q/19869437). 

##Database schema

 - Burnination(Id, Tag, StartDate, EndDate, MetaLink, RoomId) - This table holds meta-data for the tag burnination itself. The roomId corresponds to the chat room being the head-quarters of the burnination effort.
 - BurninationQuestion(#Burnination, QuestionId, Title, Link, ShareLink, Score, ViewCount, AnswerCount, CommentCount, Locked, Migrated, Answered, AcceptedAnswerId, Tags, CreatedDate, ClosedAsDuplicate, DeletedDate, LastEditDate, CloseVoteCount, ReopenVoteCount, DeleteVoteCount, UndeleteVoteCount, Closed, Roombad, ManuallyDeleted, Retagged) - This table is a snapshot of the state of all the questions at the current date and serves as base for notifications.
 - BurninationQuestionHistory(#BurninationQuestion, EventDate, EventType) - This table holds the history for a given question in a tag under burnination. The events corresponds to closed / retagged / deleted and so on.
 - BurninationProgress(#Burnination, ProgressDate, TotalQuestions, Closed, Roombad, ManuallyDeleted, Retagged, OpenedWithTag) - This table holds the progress made at a given date. It will be updated automatically based on the current state of BurninationQuestion.
