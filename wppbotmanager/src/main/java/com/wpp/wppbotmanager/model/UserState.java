package com.wpp.wppbotmanager.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class UserState {
    private Map<String, Object> tempValues;

}