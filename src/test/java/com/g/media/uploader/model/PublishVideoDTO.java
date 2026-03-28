package com.g.media.uploader.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PublishVideoDTO {
    private String uuid;

    private List<Long> accountIdList;

    private String title;

    private String description;

    private String extraConfig;

    private Date scheduledTime;
}

