package com.example.myapplication.data;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;

import org.junit.Test;

import java.lang.reflect.Field;

public class ArticleDetailDataTest {
    @Test
    public void mapsSummaryVoiceLinkFromApiJson() throws Exception {
        ArticleDetailData data = new Gson().fromJson(
                "{\"id\":101,\"sum_voice_link\":\"https://cdn.techbyte.vn/articles/101/summary.mp3\"}",
                ArticleDetailData.class
        );

        Field field = ArticleDetailData.class.getDeclaredField("sumVoiceLink");
        field.setAccessible(true);

        assertEquals("https://cdn.techbyte.vn/articles/101/summary.mp3", field.get(data));
    }
}
