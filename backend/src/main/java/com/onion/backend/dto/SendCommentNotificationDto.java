package com.onion.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SendCommentNotificationDto {
    private String type = "send_comment_notification";
    private Long commentId;
    private Long userId;
}
