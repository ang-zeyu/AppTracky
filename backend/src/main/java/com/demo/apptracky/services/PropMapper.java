package com.demo.apptracky.services;

import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;

@Service
public class PropMapper extends ModelMapper {
    public PropMapper() {
        super();
        getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }
}
