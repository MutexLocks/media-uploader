package com.g.media.uploader.model;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum PlatformEnum {
    TOU_TIAO_ARTICLE("TOU_TIAO_ARTICLE", "头条号"),
    SOU_HU_ARTICLE("SOU_HU_ARTICLE", "搜狐号"),
    DA_YU_ARTICLE("DA_YU_ARTICLE", "大鱼号"),
    ZHI_HU_ARTICLE("ZHI_HU_ARTICLE", "知乎文章"),


    DouYin("DouYin", "抖音"),
    XiaoHongShu("XiaoHongShu", "小红书"),

    Bilibili("Bilibili", "B站短视频"),
    Kuaishou("Kuaishou", "快手短视频"),
    Wechat("Wechat", "微信视频号"),
    WeiShi("WeiShi", "腾讯微视"),
    XiGua("XiGua", "西瓜短视频"),
    BAI_JIA_VIDEO("BAI_JIA_VIDEO", "百家号视频");


    private final String code;
    private final String name;

    PlatformEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static PlatformEnum get(String code) {
        Optional<PlatformEnum> first = Arrays.stream(values())
                .filter(item -> StringUtils.equalsIgnoreCase(item.getCode(), code))
                .findFirst();
        return first.orElse(null);
    }

}
