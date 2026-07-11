package com.vedantu.eventbus.processors.doubts;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.board.daos.BoardDAO;
import com.vedantu.board.models.Board;
import com.vedantu.content.daos.DiscussionDAO;
import com.vedantu.content.daos.DoubtTransactionDAO;
import com.vedantu.content.enums.DoubtState;
import com.vedantu.content.event.details.DoubtsProcessingDetails;
import com.vedantu.content.managers.DiscussionManager;
import com.vedantu.content.models.Discussion;
import com.vedantu.content.models.DoubtTransaction;
import com.vedantu.content.pojos.responses.discussions.AssignDoubtToTeacherRes;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class DoubtsProcessor extends ChainedProcessors implements IProcessor {

    private static final ALogger LOGGER = Logger.of(DoubtsProcessingDetails.class);
    @Override
    public Status process(IConsumable consumable) {
        Event event = (Event) consumable;
        DoubtsProcessingDetails eventDetails = (DoubtsProcessingDetails) event.fetchEventDetails();
        DoubtTransaction doubtTransaction = DoubtTransactionDAO.INSTANCE
                .getByDiscussionId(eventDetails.discussionId);
        Discussion discussion = DiscussionDAO.INSTANCE.getById(eventDetails.discussionId);
        AssignDoubtToTeacherRes response = new AssignDoubtToTeacherRes();
        String subjectId = null;
        for (String boardId : discussion.boardIds) {
            Board board = BoardDAO.INSTANCE.getById(boardId);
            if (board.parentBrdIds != null && board.parentBrdIds.isEmpty()) {
                subjectId = boardId;
                break;
            }
        }
        if (subjectId == null) {
            Logger.error("Subject id is null for doubt : " + eventDetails.discussionId);
            return Status.NOT_CONSUMABLE;
        }
        if (doubtTransaction.completed == false) {
            response = DiscussionManager.assignDoubtToTeacher(subjectId, eventDetails.discussionId);
        } else {
            doubtTransaction.state = DoubtState.COMPLETED;
        }
        if (response.success == false) {
            return Status.FAILURE;
        }
        return Status.SUCCESS;
    }

}
