package org.example.service;

import jakarta.annotation.Resource;
import okhttp3.*;
import org.example.common.AiConfig;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class AiService {

    @Resource
    private AiConfig aiConfig;

    private final OkHttpClient client = new OkHttpClient();


}