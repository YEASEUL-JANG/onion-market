package com.onion.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WriteCommentDto {
    private String type = "write_comment";
    private Long commentId;
}
