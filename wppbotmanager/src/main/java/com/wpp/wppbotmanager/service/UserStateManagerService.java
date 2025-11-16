package com.wpp.wppbotmanager.service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import com.wpp.wppbotmanager.model.UserState;


import java.util.HashMap;
import java.util.Map;


@Component
public class UserStateManagerService {
    public static final String MENU_PRINCIPAL = "MENU_PRINCIPAL";
    public static final String AGUARDANDO_NUMERO_PERSONALIZADO_RESUMO = "AGUARDANDO_NUMERO_PERSONALIZADO_RESUMO";
    private final Cache userStateCache;
    public static final String INSERINDO_DATA_INICIO = "INSERINDO_DATA_INICIO";
    public static final String INSERINDO_DATA_FIM = "INSERINDO_DATA_FIM";
    public static final String GERANDO_RESUMO_PERSONALIZADO = "GERANDO_RESUMO_PERSONALIZADO";

    private final Map<String, UserState> userStates = new HashMap<>();
    public void setTempValue(String userNumber, String key, Object value) {
        UserState state = userStates.computeIfAbsent(userNumber, k -> new UserState());

        // Se o mapa de valores temporários não existir, cria
        if (state.getTempValues() == null) {
            state.setTempValues(new HashMap<>());
        }

        // Salva o valor temporário
        state.getTempValues().put(key, value);
    }
    public Object getTempValue(String userNumber, String key) {
        UserState state = userStates.get(userNumber);

        if (state == null || state.getTempValues() == null) {
            return null;
        }

        return state.getTempValues().get(key);
    }

    public UserStateManagerService(CacheManager cacheManager) {
        this.userStateCache = cacheManager.getCache("userStateCache");
    }

    public void setState(String numUser, String state) {
        userStateCache.put(numUser, state);
    }

    public String getState(String numUser) {
        String state = userStateCache.get(numUser, String.class);
        if(state == null) {
            return MENU_PRINCIPAL;
        }
        return state;
    }

}
