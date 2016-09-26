# Burnaki

The purpose of this project is to monitor burnination efforts on the Stack Exchange network. The goals are threefold:

 - tracking the progress by watching for close votes / reopen votes / delete votes and edits made to questions on the burninated tag.
 - posting notifications on important events (e.g. new question posted, closure or deletion) by a chat bot in dedicated chat rooms.
 - helping the burnination by providing a review for delete candidates.

## Tracking

This project only relies on the Stack Exchange API and no screen-scraping is performed. During a burnination, the list of questions having the burninated tag are stored, and their status are updated every 5 minutes.

The information that is tracked are edits, close votes, reopen votes, deletion by the system (a.k.a., [the roomba](http://stackoverflow.com/help/roomba)) and deletion by users. Since the API [does not return deleted questions](http://stackapps.com/questions/1917/provide-a-way-to-retrieve-questions-and-answers-that-have-been-deleted), the tracking logic considers a question is deleted when it is not returned by the API anymore. To distinguish between a manual and a system deletion, it performs a best guess based on the roomba logic.

Progress is monitored and updated every hour. Each question has its history throughout the burnination stored (date of a retag, date of a closure...).

## Notifications

During a burnination process of a tag, notifications will be sent to the dedicated chat room:

 - When a question has been closed. The goal is to ensure the question gets attraction for a potential inappropriate close.
 - When a reopen vote is cast on a closed question. This means that the question was potentially wrongly closed during the effort and needs to be re-reviewed.
 - When an edit is made to a question removing the tag being burninated. This helps to track which questions were edited and potentially act on wrong edits or users on an edit-spree.
 - When a question is deleted. In the same way as closed questions, the goal is to make sure the question gets another review.
 - When a question is undeleted. This means that the question was wrongly deleted and needs attention. _NOTE: the Stack Exchange API does not return deleted posts, so it is not possible to raise a notification when an undelete vote is cast_.
 - When a new question is posted.

Notifications are sent by batches every 5 minutes. An example is:

> Closed: [Codeigniter on Godaddy results in 404 removing index.php](http://stackoverflow.com/q/20163488), [GoDaddy issues with directory (differences with "webroot" and "root")](http://stackoverflow.com/q/20528283), [godaddy shared hosting NICE command path and PHP path](http://stackoverflow.com/q/19869437). 

## Chat bot

The chat bot head quarters is the [SOCVFinder](http://chat.stackoverflow.com/rooms/111347/socvfinder) chat room. It can handle multiple burnination efforts at the same time but there can only be one chat room for a given burninated tag.

Every command can be run either in the dedicated burn room, or in the HQ room. As convenience, the `[tag]` can be omitted from the commands if it is ran inside the dedicated burn room.

### `@burnaki start tag [tag] [roomId] [link to Meta]`

This command starts the burnination process of the given tag. All questions with that tag will be queried from Stack Exchange API and stored. The bot will then start the notification process for the configured chat room.

### `@burnaki get progress [tag]`

This command outputs the current progress of the burnination: number of opened questions / number of closed questions / number of retagged questions / number of deleted questions (either manually or by the roomba). An example message is:

> Here's a recap of the efforts so far for [godaddy]: Total questions (1563), Retagged (261), Closed (1213), Roombad (287), Manually deleted (101).

It also prints a graph of the progress:

![Burnination progress](http://i.stack.imgur.com/O3TPr.png)

### `@burnaki update progress [tag]`

The progress is updated automatically every hour. This command forces an update of the progress immediatly.

### `@burnaki delete candidates [tag]`

This command queries all posts that are eligible for deletion. It queries posts that have a score <= -2, are closed since more than 2 days and have at least one answer. It also returns posts with pending delete votes.

![Sample list](http://i.stack.imgur.com/dUlb8.png)

### `@burnaki stop tag [tag]`

This command ends the burnination process of the given tag. No new notifications will be sent.

## Database schema

 - `Burnination(Id, Tag, StartDate, EndDate, LastRefreshDate, MetaLink, RoomId)` - This table holds meta-data for the tag burnination itself. The roomId corresponds to the chat room being the head-quarters of the burnination effort.
 - `BurninationQuestion(#Burnination, QuestionId, Title, Link, ShareLink, Score, ViewCount, AnswerCount, CommentCount, Locked, Migrated, Answered, AcceptedAnswerId, Tags, CreatedDate, ClosedAsDuplicate, DeletedDate, LastEditDate, CloseVoteCount, ReopenVoteCount, DeleteVoteCount, UndeleteVoteCount, Closed, Roombad, ManuallyDeleted, Retagged)` - This table is a snapshot of the state of all the questions at the current date and serves as base for notifications. All of the information stored is necessary to determine if a deletion probably occured from the system or from users.
 - `BurninationQuestionHistory(#BurninationQuestion, EventDate, EventType)` - This table holds the history for a given question in a tag under burnination. The events corresponds to closed / retagged / deleted and so on.
 - `BurninationProgress(#Burnination, ProgressDate, TotalQuestions, Closed, Roombad, ManuallyDeleted, Retagged, OpenedWithTag)` - This table holds the progress made at a given date. It will be updated automatically every hour based on the current state of `BurninationQuestion`.

# Accounts

The bot runs under the user [Burnaki](http://stackoverflow.com/users/6738757/burnaki) and sits in the [SOBotics](http://chat.stackoverflow.com/rooms/111347/sobotics) chat room on Stack Overflow.