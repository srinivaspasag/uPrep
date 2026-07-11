package com.lms.components;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.models.Module;
import com.lms.repository.ModuleRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ModuleComponent {
    private static final Logger logger = LoggerFactory.getLogger(ModuleComponent.class);
    @Autowired
    private ModuleRepo moduleRepo;

    public Module getModuleById(String moduleId) throws VedantuException {

        logger.debug("inside getModuleById function" + moduleId);
        Optional<Module> moduleOptional = moduleRepo.findById(moduleId);
        if (!moduleOptional.isPresent()) {
            logger.debug("module is null");
            throw new VedantuException(VedantuErrorCode.MODULE_DOES_NOT_EXISTS,
                    "no module found with id : " + moduleId);
        }

        return moduleOptional.get();
    }
}
