package com.tcc.url_cutter_api.dto;

import com.tcc.url_cutter_api.model.Url;
import com.tcc.url_cutter_api.model.auth.User;

public record UrlWithUser(
        Url url,
        User user
) {}