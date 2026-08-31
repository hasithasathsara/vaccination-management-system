package com.moh.vaxtrack.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SriLankaDistricts {

    // LinkedHashMap keeps province order stable in the dropdown 
    public static final Map<String, List<String>> BY_PROVINCE = new LinkedHashMap<>();

    static {
        BY_PROVINCE.put("Western", List.of("Colombo", "Gampaha", "Kalutara"));
        BY_PROVINCE.put("Central", List.of("Kandy", "Matale", "Nuwara Eliya"));
        BY_PROVINCE.put("Southern", List.of("Galle", "Matara", "Hambantota"));
        BY_PROVINCE.put("Northern", List.of("Jaffna", "Kilinochchi", "Mannar", "Mullaitivu", "Vavuniya"));
        BY_PROVINCE.put("Eastern", List.of("Trincomalee", "Batticaloa", "Ampara"));
        BY_PROVINCE.put("North Western", List.of("Kurunegala", "Puttalam"));
        BY_PROVINCE.put("North Central", List.of("Anuradhapura", "Polonnaruwa"));
        BY_PROVINCE.put("Uva", List.of("Badulla", "Monaragala"));
        BY_PROVINCE.put("Sabaragamuwa", List.of("Ratnapura", "Kegalle"));
    }


    private SriLankaDistricts() {
    }
}
